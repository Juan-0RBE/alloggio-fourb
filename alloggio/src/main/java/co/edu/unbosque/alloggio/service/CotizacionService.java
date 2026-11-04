package co.edu.unbosque.alloggio.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.dto.CotizacionDTO;
import co.edu.unbosque.alloggio.dto.CotizacionServicioDTO;
import co.edu.unbosque.alloggio.entity.Cotizacion;
import co.edu.unbosque.alloggio.entity.CotizacionServicio;
import co.edu.unbosque.alloggio.entity.ParametroTarifa;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.ServicioAlojamiento;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.CotizacionRepository;
import co.edu.unbosque.alloggio.repository.ParametroTarifaRepository;
import co.edu.unbosque.alloggio.repository.PublicacionRepository;
import co.edu.unbosque.alloggio.repository.ServicioAlojamientoRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;

/**
 * Servicio para cotizar un alojamiento (CU-016) y agregar servicios adicionales
 * a la cotización (CU-017). La cotización no crea reservas ni bloquea fechas.
 */
@Service
public class CotizacionService {

    /** Minutos durante los cuales una cotización sigue vigente. */
    private static final long VIGENCIA_MINUTOS = 30;

    @Autowired
    private CotizacionRepository cotizacionRepo;

    @Autowired
    private PublicacionRepository publicacionRepo;

    @Autowired
    private ServicioAlojamientoRepository servicioRepo;

    @Autowired
    private ParametroTarifaRepository parametroRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private AlojamientoService alojamientoService;

    public CotizacionService() {
    }

    /**
     * Calcula y guarda la cotización de un alojamiento para un rango de fechas. El
     * huésped se toma de la sesión. Si es exitoso, el id de la cotización creada
     * queda en dto.idCotizacion.
     * 
     * @param nombreUsuario nombre del usuario autenticado
     * @param dto           debe traer idPublicacion, fechaLlegada y fechaSalida
     * @return 0 si es exitoso, 1 si el usuario no existe, 2 si la publicación no
     *         existe o no está ACTIVA, 3 si faltan fechas o la salida no es
     *         posterior a la llegada (RN-07), 4 si la llegada es anterior a hoy, 5
     *         si el alojamiento no está disponible en el rango (CU-016 E2), 6 si
     *         las tarifas de la plataforma no están configuradas
     */
    @Transactional
    public int cotizar(String nombreUsuario, CotizacionDTO dto) {
        Optional<Usuario> huespedOpt = usuarioRepo.findByNombreUsuario(nombreUsuario);
        if (!huespedOpt.isPresent()) {
            return 1; // El huésped no existe
        }

        if (dto.getIdPublicacion() == null) {
            return 2;
        }
        Optional<Publicacion> pubOpt = publicacionRepo.findById(dto.getIdPublicacion());
        if (!pubOpt.isPresent() || pubOpt.get().getEstado() != EstadoPublicacion.ACTIVA) {
            return 2; // Solo se cotizan publicaciones ACTIVAS (RN-04)
        }
        Publicacion pub = pubOpt.get();

        LocalDate llegada = dto.getFechaLlegada();
        LocalDate salida = dto.getFechaSalida();
        if (llegada == null || salida == null) {
            return 3;
        }
        int criterio = alojamientoService.validarCriterios(llegada, salida, null, null, null);
        if (criterio == 2) {
            return 3; // La salida debe ser posterior a la llegada (RN-07)
        }
        if (criterio == 3) {
            return 4; // La llegada está en el pasado
        }

        if (!alojamientoService.estaDisponible(pub.getIdPublicacion(), llegada, salida)) {
            return 5; // El rango cruza un bloqueo o una reserva (RN-08)
        }

        Optional<ParametroTarifa> parametroOpt = parametroRepo.findFirstByOrderByIdParametroTarifaAsc();
        if (!parametroOpt.isPresent()) {
            return 6; // No hay tarifas configuradas
        }
        ParametroTarifa parametro = parametroOpt.get();

        int noches = (int) ChronoUnit.DAYS.between(llegada, salida);
        double precioBase = redondear(pub.getPrecioNoche() * noches);
        double limpieza = redondear(pub.getTarifaLimpieza());
        double servicio = redondear(precioBase * parametro.getPorcentajeServicio() / 100.0);
        double descuento = 0.0;
        if (noches >= parametro.getNochesMinimasDescuento()) {
            descuento = redondear(precioBase * parametro.getPorcentajeDescuento() / 100.0);
        }

        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setHuesped(huespedOpt.get());
        cotizacion.setPublicacion(pub);
        cotizacion.setFechaLlegada(llegada);
        cotizacion.setFechaSalida(salida);
        cotizacion.setNoches(noches);
        cotizacion.setPrecioBase(precioBase);
        cotizacion.setLimpieza(limpieza);
        cotizacion.setServicio(servicio);
        cotizacion.setDescuento(descuento);
        cotizacion.setVigencia(LocalDateTime.now().plusMinutes(VIGENCIA_MINUTOS));
        recalcularTotal(cotizacion);

        Cotizacion guardada = cotizacionRepo.save(cotizacion);
        dto.setIdCotizacion(guardada.getIdCotizacion());

        return 0; // Cotización creada exitosamente
    }

    /**
     * Agrega servicios adicionales a una cotización vigente y recalcula el total.
     * El costo de cada servicio es por noche, así que se multiplica por las noches
     * de la estadía. Los servicios ya agregados se ignoran; una lista vacía no
     * cambia nada (CU-017 A1).
     * 
     * @param nombreUsuario nombre del usuario autenticado
     * @param idCotizacion
     * @param idsServicios  ids de los servicios adicionales a agregar
     * @return 0 si es exitoso, 1 si la cotización no existe, 2 si no pertenece al
     *         huésped autenticado, 3 si la cotización ya no está vigente, 4 si el
     *         alojamiento no ofrece servicios adicionales (CU-017 E1), 5 si algún
     *         servicio no existe, no es adicional o no pertenece al alojamiento
     */
    @Transactional
    public int agregarServicios(String nombreUsuario, Long idCotizacion, List<Long> idsServicios) {
        Optional<Cotizacion> cotOpt = cotizacionRepo.findById(idCotizacion);
        if (!cotOpt.isPresent()) {
            return 1; // Cotización no encontrada
        }

        Cotizacion cotizacion = cotOpt.get();
        if (!cotizacion.getHuesped().getNombreUsuario().equals(nombreUsuario)) {
            return 2; // La cotización no pertenece al usuario
        }
        if (LocalDateTime.now().isAfter(cotizacion.getVigencia())) {
            return 3; // La cotización venció
        }

        // Servicios adicionales que ofrece el alojamiento, por id
        Map<Long, ServicioAlojamiento> adicionales = new HashMap<>();
        for (ServicioAlojamiento s : servicioRepo
                .findByPublicacionIdPublicacion(cotizacion.getPublicacion().getIdPublicacion())) {
            if (Boolean.TRUE.equals(s.getAdicional())) {
                adicionales.put(s.getIdServicio(), s);
            }
        }
        if (adicionales.isEmpty()) {
            return 4; // El alojamiento no ofrece servicios adicionales
        }
        if (idsServicios == null || idsServicios.isEmpty()) {
            return 0; // No se selecciona ningún servicio: la cotización no cambia
        }

        Set<Long> solicitados = new LinkedHashSet<>(idsServicios);
        for (Long idServicio : solicitados) {
            if (idServicio == null || !adicionales.containsKey(idServicio)) {
                return 5; // Se valida todo antes de modificar la cotización
            }
        }

        Set<Long> yaAgregados = new HashSet<>();
        for (CotizacionServicio cs : cotizacion.getServicios()) {
            yaAgregados.add(cs.getServicio().getIdServicio());
        }
        for (Long idServicio : solicitados) {
            if (yaAgregados.contains(idServicio)) {
                continue;
            }
            ServicioAlojamiento servicio = adicionales.get(idServicio);
            // El costo del servicio es por noche: se multiplica por las noches de la
            // estadía
            double costoPorNoche = servicio.getCosto() == null ? 0.0 : servicio.getCosto();
            double costo = redondear(costoPorNoche * cotizacion.getNoches());
            cotizacion.getServicios().add(new CotizacionServicio(null, costo, cotizacion, servicio));
        }

        recalcularTotal(cotizacion);
        cotizacionRepo.save(cotizacion);

        return 0; // Servicios agregados y total recalculado
    }

    @Transactional
    public int quitarServicio(String nombreUsuario, Long idCotizacion, Long idServicio) {
        Optional<Cotizacion> cotOpt = cotizacionRepo.findById(idCotizacion);
        if (!cotOpt.isPresent()) {
            return 1; // Cotización no encontrada
        }

        Cotizacion cotizacion = cotOpt.get();
        if (!cotizacion.getHuesped().getNombreUsuario().equals(nombreUsuario)) {
            return 2; // La cotización no pertenece al usuario
        }
        if (LocalDateTime.now().isAfter(cotizacion.getVigencia())) {
            return 3; // La cotización venció
        }

        // orphanRemoval elimina la fila de COTIZACION_SERVICIO al sacarla de la lista
        boolean quitado = cotizacion.getServicios()
                .removeIf(cs -> cs.getServicio().getIdServicio().equals(idServicio));
        if (!quitado) {
            return 4; // Ese servicio no estaba en la cotización
        }

        recalcularTotal(cotizacion);
        cotizacionRepo.save(cotizacion);

        return 0; // Servicio quitado y total recalculado
    }

    /**
     * Obtiene una cotización por su ID, con el detalle de sus servicios
     * adicionales.
     * 
     * @param idCotizacion
     * @return CotizacionDTO, o null si no existe
     */
    @Transactional(readOnly = true)
    public CotizacionDTO obtenerPorId(Long idCotizacion) {
        Optional<Cotizacion> found = cotizacionRepo.findById(idCotizacion);
        if (!found.isPresent()) {
            return null;
        }
        Cotizacion cotizacion = found.get();

        // Las relaciones son LAZY: el DTO se arma a mano dentro de la transacción
        List<CotizacionServicioDTO> servicios = new ArrayList<>();
        double extras = 0.0;
        for (CotizacionServicio cs : cotizacion.getServicios()) {
            servicios.add(new CotizacionServicioDTO(cs.getServicio().getIdServicio(), cs.getServicio().getNombre(),
                    cs.getCostoAplicado()));
            extras += cs.getCostoAplicado();
        }

        return new CotizacionDTO(cotizacion.getIdCotizacion(), cotizacion.getPublicacion().getIdPublicacion(),
                cotizacion.getFechaLlegada(), cotizacion.getFechaSalida(), cotizacion.getNoches(),
                cotizacion.getPrecioBase(), cotizacion.getLimpieza(), cotizacion.getServicio(),
                cotizacion.getDescuento(), redondear(extras), cotizacion.getTotal(), cotizacion.getVigencia(),
                servicios);
    }

    /**
     * Recalcula el total: precio base + limpieza + servicio + servicios adicionales
     * - descuento. Usa los valores guardados en la cotización, no las tarifas
     * actuales.
     * 
     * @param cotizacion
     */
    private void recalcularTotal(Cotizacion cotizacion) {
        double extras = 0.0;
        for (CotizacionServicio cs : cotizacion.getServicios()) {
            extras += cs.getCostoAplicado();
        }
        cotizacion.setTotal(redondear(cotizacion.getPrecioBase() + cotizacion.getLimpieza()
                + cotizacion.getServicio() + extras - cotizacion.getDescuento()));
    }

    /**
     * Redondea un valor monetario a dos decimales.
     * 
     * @param valor
     * @return
     */
    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}