package co.edu.unbosque.alloggio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.dto.CancelacionDTO;
import co.edu.unbosque.alloggio.dto.ResultadoPasarelaDTO;
import co.edu.unbosque.alloggio.entity.Cancelacion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Accion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.TipoEntidad;
import co.edu.unbosque.alloggio.entity.Notificacion.TipoNotificacion;
import co.edu.unbosque.alloggio.entity.Pago;
import co.edu.unbosque.alloggio.entity.Pago.EstadoPago;
import co.edu.unbosque.alloggio.entity.PoliticaCancelacion;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Reserva;
import co.edu.unbosque.alloggio.entity.Reserva.EstadoReserva;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.CancelacionRepository;
import co.edu.unbosque.alloggio.repository.PagoRepository;
import co.edu.unbosque.alloggio.repository.PeriodoDisponibilidadRepository;
import co.edu.unbosque.alloggio.repository.ReservaRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;
import co.edu.unbosque.alloggio.util.exception.PasarelaException;

/**
 * Servicio para cancelar una reserva confirmada. Aplica la política de
 * cancelación de la publicación, devuelve el dinero por la pasarela (simulada),
 * libera las fechas y deja el registro de la cancelación.
 */
@Service
public class CancelacionService {

    private static final Logger log = LoggerFactory.getLogger(CancelacionService.class);

    private static final int LONGITUD_MAX_MOTIVO = 500;

    @Autowired
    private ReservaRepository reservaRepo;

    @Autowired
    private PagoRepository pagoRepo;

    @Autowired
    private PeriodoDisponibilidadRepository periodoRepo;

    @Autowired
    private CancelacionRepository cancelacionRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasarelaPagoService pasarela;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private EventoAuditoriaService auditoriaService;

    public CancelacionService() {
    }

    /**
     * Cancela una reserva confirmada del huésped. La fila de la reserva se bloquea
     * durante la transacción, así que dos cancelaciones simultáneas no pueden
     * devolver el dinero dos veces.
     * 
     * @param nombreUsuario
     * @param idReserva
     * @param dto
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int cancelar(String nombreUsuario, Long idReserva, CancelacionDTO dto) {
        Optional<Usuario> usuarioOpt = usuarioRepo.findByNombreUsuario(nombreUsuario);
        if (!usuarioOpt.isPresent()) {
            return 1; // El usuario no existe
        }
        Usuario usuario = usuarioOpt.get();

        String motivo = dto.getMotivo();
        if (motivo == null || motivo.isBlank() || motivo.length() > LONGITUD_MAX_MOTIVO) {
            return 5;
        }
        if (idReserva == null) {
            return 2;
        }

        // Bloquea la fila de la reserva hasta el final de la transacción
        Optional<Reserva> reservaOpt = reservaRepo.findByIdParaActualizar(idReserva);
        if (!reservaOpt.isPresent()) {
            return 2; // Reserva no encontrada
        }
        Reserva reserva = reservaOpt.get();

        if (!reserva.getHuesped().getNombreUsuario().equals(nombreUsuario)) {
            auditoriaService.registrarDenegado(nombreUsuario, Accion.CANCELAR_RESERVA, TipoEntidad.RESERVA,
                    idReserva, "Intento de cancelar una reserva ajena");
            return 3;
        }
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            return 4; // Ya cancelada, completada o no confirmada
        }
        LocalDate hoy = LocalDate.now();
        if (!hoy.isBefore(reserva.getFechaLlegada())) {
            return 6; // La estadía ya comenzó o terminó
        }

        Publicacion pub = reserva.getPublicacion();
        BigDecimal valor = calcularDevolucion(reserva.getTotal(), pub.getPoliticaCancelacion(), hoy,
                reserva.getFechaLlegada());

        // Primero se devuelve el dinero; si la pasarela falla, la reserva no cambia
        String referenciaReembolso = null;
        if (valor.signum() > 0) {
            Optional<Pago> pagoOpt = pagoRepo.findFirstByReservaIdReservaAndEstado(idReserva, EstadoPago.APROBADO);
            if (!pagoOpt.isPresent()) {
                return 9;
            }
            try {
                ResultadoPasarelaDTO resultado = pasarela.reembolsar(pagoOpt.get().getReferenciaPasarela(), valor,
                        "reembolso-reserva-" + idReserva);
                if (!resultado.isAprobado()) {
                    return 7;
                }
                referenciaReembolso = resultado.getReferencia();
            } catch (PasarelaException e) {
                log.error("Falla al reembolsar la reserva {}: {}", idReserva, e.getMessage());
                return 7;
            }
        }

        try {
            reserva.setEstado(EstadoReserva.CANCELADA);
            reservaRepo.save(reserva);

            // Libera el rango de fechas (CA-36)
            periodoRepo.findByReservaIdReserva(idReserva).ifPresent(periodoRepo::delete);

            Cancelacion cancelacion = cancelacionRepo
                    .save(new Cancelacion(null, LocalDateTime.now(), motivo, valor, referenciaReembolso, reserva,
                            usuario));

            String fechas = reserva.getFechaLlegada() + " al " + reserva.getFechaSalida();
            notificacionService.notificarUsuario(nombreUsuario, TipoNotificacion.RESERVA, "Cancelaste tu reserva en «"
                    + pub.getTitulo() + "» del " + fechas + ". Devolución: " + valor.toPlainString() + " COP.");
            notificacionService.notificarAOtroUsuario(pub.getAnfitrion(), TipoNotificacion.RESERVA,
                    "Se canceló la reserva en «" + pub.getTitulo() + "» del " + fechas + ".");
            auditoriaService.registrarExito(nombreUsuario, Accion.CANCELAR_RESERVA, TipoEntidad.RESERVA, idReserva,
                    "Reserva cancelada; devolución " + valor.toPlainString() + " COP");

            dto.setIdCancelacion(cancelacion.getIdCancelacion());
            dto.setIdReserva(idReserva);
            dto.setFecha(cancelacion.getFecha());
            dto.setValorDevolucion(valor);
            dto.setEstadoReserva(EstadoReserva.CANCELADA.name());
            dto.setReferenciaReembolso(referenciaReembolso);
        } catch (RuntimeException e) {
            if (referenciaReembolso != null) {
                // El dinero ya se devolvió; al reintentar, la clave de idempotencia evita
                // devolverlo otra vez
                log.error("La reserva {} no pudo registrarse como cancelada, pero ya se reembolsó ({})", idReserva,
                        referenciaReembolso);
            }
            throw e;
        }
        return 0; // Reserva cancelada exitosamente
    }

    /**
     * Calcula cuánto se devuelve según la política: si faltan al menos
     * plazoCancelacion días para la llegada, se devuelve porcentajeDevolucion % del
     * total; si no, no se devuelve nada. Si cambia la regla de la política, solo
     * cambia este método.
     * 
     * @param total    total pagado por la reserva
     * @param politica política de cancelación de la publicación
     * @param hoy      fecha de la cancelación
     * @param llegada  fecha de llegada de la reserva
     * @return valor a devolver, con dos decimales
     */
    private BigDecimal calcularDevolucion(BigDecimal total, PoliticaCancelacion politica, LocalDate hoy,
            LocalDate llegada) {
        // Sin política (o incompleta) no corresponde devolución
        if (politica == null || politica.getPlazoCancelacion() == null
                || politica.getPorcentajeDevolucion() == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        long diasAnticipacion = ChronoUnit.DAYS.between(hoy, llegada);
        if (diasAnticipacion < politica.getPlazoCancelacion()) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal porcentaje = new BigDecimal(politica.getPorcentajeDevolucion().toString());
        BigDecimal valor = total.multiply(porcentaje).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return valor.min(total);
    }
}