package co.edu.unbosque.alloggio.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.dto.PagoDTO;
import co.edu.unbosque.alloggio.dto.ReservaDTO;
import co.edu.unbosque.alloggio.entity.Cotizacion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Accion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Resultado;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.TipoEntidad;
import co.edu.unbosque.alloggio.entity.Notificacion.TipoNotificacion;
import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad;
import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad.TipoPeriodo;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.Reserva;
import co.edu.unbosque.alloggio.entity.Reserva.EstadoReserva;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.CotizacionRepository;
import co.edu.unbosque.alloggio.repository.PeriodoDisponibilidadRepository;
import co.edu.unbosque.alloggio.repository.PublicacionRepository;
import co.edu.unbosque.alloggio.repository.ReservaRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;
import co.edu.unbosque.alloggio.util.exception.PasarelaException;

/**
 * Servicio para crear reservas.
 */
@Service
public class ReservaService {

    private static final Logger log = LoggerFactory.getLogger(ReservaService.class);

    private static final int LONGITUD_MAX_CLAVE = 100;
    private static final String PATRON_METODO_PAGO = "^pm_[A-Za-z0-9_]{3,100}$";

    @Autowired
    private ReservaRepository reservaRepo;

    @Autowired
    private CotizacionRepository cotizacionRepo;

    @Autowired
    private PublicacionRepository publicacionRepo;

    @Autowired
    private PeriodoDisponibilidadRepository periodoRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private PasarelaPagoService pasarela;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private EventoAuditoriaService auditoriaService;

    public ReservaService() {
    }

    /**
     * Crea una reserva a partir de una cotización vigente.
     * 
     * @param nombreUsuario
     * @param dto
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int crear(String nombreUsuario, ReservaDTO dto) {
        Optional<Usuario> huespedOpt = usuarioRepo.findByNombreUsuario(nombreUsuario);
        if (!huespedOpt.isPresent()) {
            return 1; // El huésped no existe
        }
        Usuario huesped = huespedOpt.get();

        // Validaciones que no requieren consultar la base de datos
        if (dto.getIdCotizacion() == null) {
            return 2;
        }
        String clave = dto.getClaveIdempotencia();
        if (clave == null || clave.isBlank() || clave.length() > LONGITUD_MAX_CLAVE) {
            return 5;
        }
        String metodoPago = dto.getMetodoPago();
        if (metodoPago == null || !metodoPago.matches(PATRON_METODO_PAGO)) {
            return 6;
        }

        Optional<Cotizacion> cotOpt = cotizacionRepo.findById(dto.getIdCotizacion());
        if (!cotOpt.isPresent()) {
            return 2; // Cotización no encontrada
        }
        Cotizacion cotizacion = cotOpt.get();
        if (!cotizacion.getHuesped().getNombreUsuario().equals(nombreUsuario)) {
            return 3; // La cotización no pertenece al usuario
        }
        Long idCotizacion = cotizacion.getIdCotizacion();
        Long idPublicacion = cotizacion.getPublicacion().getIdPublicacion();

        // Bloquea la publicación: desde aquí nadie más modifica su calendario
        Optional<Publicacion> pubOpt = publicacionRepo.findByIdParaActualizar(idPublicacion);
        if (!pubOpt.isPresent()) {
            return 7;
        }
        Publicacion pub = pubOpt.get();

        Optional<Reserva> porClave = reservaRepo.findByHuespedIdUsuarioAndClaveIdempotencia(huesped.getIdUsuario(),
                clave);
        if (porClave.isPresent()) {
            if (!porClave.get().getCotizacion().getIdCotizacion().equals(idCotizacion)) {
                return 14; // La misma clave no puede servir para otra cotización
            }
            dto.setIdReserva(porClave.get().getIdReserva());
            return 12;
        }
        Optional<Reserva> porCotizacion = reservaRepo.findByCotizacionIdCotizacion(idCotizacion);
        if (porCotizacion.isPresent()) {
            dto.setIdReserva(porCotizacion.get().getIdReserva());
            return 12;
        }

        if (LocalDateTime.now().isAfter(cotizacion.getVigencia())) {
            return 4; // La cotización venció
        }
        if (pub.getEstado() != EstadoPublicacion.ACTIVA) {
            return 7; // Solo las publicaciones ACTIVAS reciben reservas
        }

        // La verificación se repite al confirmar
        if (!periodoRepo.findIntersectingPeriods(idPublicacion, cotizacion.getFechaLlegada(),
                cotizacion.getFechaSalida()).isEmpty()) {
            return 8;
        }

        // Cobro
        String claveStripe = "alloggio-" + idCotizacion + "-" + clave;
        PagoDTO pagoDto = new PagoDTO();
        pagoDto.setMetodoPago(metodoPago);
        pagoDto.setClaveIdempotencia(claveStripe);
        int estadoPago = pagoService.cobrar(nombreUsuario, idCotizacion, pagoDto);

        if (estadoPago == 7) {
            return 9; // Pasarela no disponible: no se registró nada
        }
        if (estadoPago == 8) {
            // Pago rechazado: queda registrado como RECHAZADO y no se crea la reserva
            PagoDTO rechazado = pagoService.obtenerPorId(pagoDto.getIdPago());
            dto.setMotivoRechazo(rechazado.getMotivoRechazo());
            auditoriaService.registrar(nombreUsuario, Accion.PROCESAR_PAGO, TipoEntidad.PAGO, pagoDto.getIdPago(),
                    Resultado.FALLIDO, "Pago rechazado de la cotización " + idCotizacion);
            return 10;
        }
        if (estadoPago == 4) {
            return 11;
        }
        if (estadoPago != 0) {
            return 13;
        }

        // Pago aprobado: desde aquí cualquier falla debe devolver el dinero
        PagoDTO pagoAprobado = pagoService.obtenerPorId(pagoDto.getIdPago());
        try {
            Reserva reserva = new Reserva();
            reserva.setHuesped(huesped);
            reserva.setPublicacion(pub);
            reserva.setCotizacion(cotizacion);
            reserva.setFechaLlegada(cotizacion.getFechaLlegada());
            reserva.setFechaSalida(cotizacion.getFechaSalida());
            reserva.setTotal(cotizacion.getTotal());
            reserva.setEstado(EstadoReserva.CONFIRMADA);
            reserva.setClaveIdempotencia(clave);
            reserva.setFechaCreacion(LocalDateTime.now());
            reserva = reservaRepo.save(reserva);

            // Las fechas se bloquean automáticamente, sin intervención manual (CA-23)
            periodoRepo.save(new PeriodoDisponibilidad(null, cotizacion.getFechaLlegada(),
                    cotizacion.getFechaSalida(), TipoPeriodo.RESERVADO, pub, reserva));

            pagoService.vincularReserva(pagoDto.getIdPago(), reserva);

            String fechas = cotizacion.getFechaLlegada() + " al " + cotizacion.getFechaSalida();
            notificacionService.notificarUsuario(nombreUsuario, TipoNotificacion.RESERVA,
                    "Tu reserva en «" + pub.getTitulo() + "» del " + fechas + " fue confirmada.");
            notificacionService.notificarAOtroUsuario(pub.getAnfitrion(), TipoNotificacion.RESERVA,
                    "Nueva reserva en «" + pub.getTitulo() + "» del " + fechas + ".");

            auditoriaService.registrarExito(nombreUsuario, Accion.PROCESAR_PAGO, TipoEntidad.PAGO,
                    pagoDto.getIdPago(), "Pago aprobado de la cotización " + idCotizacion);
            auditoriaService.registrarExito(nombreUsuario, Accion.CREAR_RESERVA, TipoEntidad.RESERVA,
                    reserva.getIdReserva(), "Reserva creada en la publicación " + idPublicacion);

            dto.setIdReserva(reserva.getIdReserva());
        } catch (RuntimeException e) {
            reembolsarPorFalla(pagoAprobado, claveStripe, idCotizacion);
            throw e; // La transacción se revierte: no queda reserva ni fechas bloqueadas
        }
        return 0; // Reserva creada exitosamente
    }

    /**
     * Obtiene una reserva por su ID.
     * 
     * @param idReserva
     * @return
     */
    @Transactional(readOnly = true)
    public ReservaDTO obtenerPorId(Long idReserva) {
        Optional<Reserva> found = reservaRepo.findById(idReserva);
        if (!found.isPresent()) {
            return null;
        }
        Reserva reserva = found.get();

        // Las relaciones son LAZY: el DTO se arma a mano dentro de la transacción
        ReservaDTO dto = new ReservaDTO();
        dto.setIdReserva(reserva.getIdReserva());
        dto.setIdCotizacion(reserva.getCotizacion().getIdCotizacion());
        dto.setIdPublicacion(reserva.getPublicacion().getIdPublicacion());
        dto.setFechaLlegada(reserva.getFechaLlegada());
        dto.setFechaSalida(reserva.getFechaSalida());
        dto.setTotal(reserva.getTotal());
        dto.setEstado(reserva.getEstado().name());
        dto.setFechaCreacion(reserva.getFechaCreacion());
        return dto;
    }

    /**
     * Intenta devolver un cobro aprobado cuando la reserva no pudo registrarse
     * 
     * @param pago
     * @param claveStripe
     * @param idCotizacion
     */
    private void reembolsarPorFalla(PagoDTO pago, String claveStripe, Long idCotizacion) {
        try {
            pasarela.reembolsar(pago.getReferenciaPasarela(), pago.getMonto(), "reembolso-" + claveStripe);
            log.error("La reserva de la cotización {} falló después del cobro; se reembolsó {}", idCotizacion,
                    pago.getReferenciaPasarela());
        } catch (PasarelaException ex) {
            log.error("REEMBOLSO PENDIENTE: la reserva de la cotización {} falló y el cobro {} no se pudo devolver",
                    idCotizacion, pago.getReferenciaPasarela());
        }
    }

    // Para el historial de reservas

    /**
     * 
     * Lista todas las reservas del huésped, de la llegada más reciente a la más
     * antigua, con su estado.
     * 
     * 
     * @param nombreUsuario
     * @return
     */
    @Transactional(readOnly = true)
    public List<ReservaDTO> obtenerHistorial(String nombreUsuario) {
        List<ReservaDTO> historial = new ArrayList<>();
        for (Reserva r : reservaRepo.findByHuespedNombreUsuarioOrderByFechaLlegadaDescIdReservaDesc(nombreUsuario)) {
            historial.add(convertir(r));
        }
        return historial;
    }

    /**
     * Valida que la reserva exista y pertenezca al huésped.
     * 
     * @return
     */
    @Transactional(readOnly = true)
    public int consultarEstado(String nombreUsuario, Long idReserva) {
        Optional<Reserva> found = reservaRepo.findById(idReserva);
        if (!found.isPresent()) {
            return 1;
        }
        if (!found.get().getHuesped().getNombreUsuario().equals(nombreUsuario)) {
            return 2;
        }
        return 0;
    }

    private ReservaDTO convertir(Reserva reserva) {
        ReservaDTO dto = new ReservaDTO();
        dto.setIdReserva(reserva.getIdReserva());
        dto.setIdCotizacion(reserva.getCotizacion().getIdCotizacion());
        dto.setIdPublicacion(reserva.getPublicacion().getIdPublicacion());
        dto.setFechaLlegada(reserva.getFechaLlegada());
        dto.setFechaSalida(reserva.getFechaSalida());
        dto.setTotal(reserva.getTotal());
        dto.setEstado(reserva.getEstado().name());
        dto.setFechaCreacion(reserva.getFechaCreacion());
        return dto;
    }
}