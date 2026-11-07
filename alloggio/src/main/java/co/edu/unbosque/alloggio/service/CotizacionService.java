package co.edu.unbosque.alloggio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * Servicio para cotizar un alojamiento y agregar servicios adicionales
 * a la cotización.
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
     * @param nombreUsuario
     * @param dto
     * @return
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
            return 2; // Solo se cotizan publicaciones ACTIVAS
        }
        Publicacion pub = pubOpt.get();

        LocalDate llegada = dto.getFechaLlegada();
        LocalDate salida = dto.getFechaSalida();
        if (llegada == null || salida == null) {
            return 3;
        }
        int criterio = alojamientoService.validarCriterios(llegada, salida, null, null, null);
        if (criterio == 2) {
            return 3; // La salida debe ser posterior a la llegada
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
        BigDecimal precioBase = redondear(aDecimal(pub.getPrecioNoche()).multiply(BigDecimal.valueOf(noches)));
        BigDecimal limpieza = redondear(aDecimal(pub.getTarifaLimpieza()));
        BigDecimal servicio = porcentaje(precioBase, parametro.getPorcentajeServicio());
        BigDecimal descuento = BigDecimal.ZERO;
        if (noches >= parametro.getNochesMinimasDescuento()) {
            descuento = porcentaje(precioBase, parametro.getPorcentajeDescuento());
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
     * 
     * @param nombreUsuario nombre del usuario autenticado
     * @param idCotizacion
     * @param idsServicios
     * @return
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
            // El costo del servicio es por noche
            BigDecimal costoPorNoche = aDecimal(servicio.getCosto());
            BigDecimal costo = redondear(costoPorNoche.multiply(BigDecimal.valueOf(cotizacion.getNoches())));
            cotizacion.getServicios().add(new CotizacionServicio(null, costo, cotizacion, servicio));
        }

        recalcularTotal(cotizacion);
        cotizacionRepo.save(cotizacion);

        return 0; // Servicios agregados y total recalculado
    }

    /**
     * Quita un servicio adicional de una cotización vigente y recalcula el total.
     * 
     * @param nombreUsuario nombre del usuario autenticado
     * @param idCotizacion
     * @param idServicio
     * @return
     */
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
     * @return
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
        for (CotizacionServicio cs : cotizacion.getServicios()) {
            servicios.add(new CotizacionServicioDTO(cs.getServicio().getIdServicio(), cs.getServicio().getNombre(),
                    cs.getCostoAplicado()));
        }

        return new CotizacionDTO(cotizacion.getIdCotizacion(), cotizacion.getPublicacion().getIdPublicacion(),
                cotizacion.getFechaLlegada(), cotizacion.getFechaSalida(), cotizacion.getNoches(),
                cotizacion.getPrecioBase(), cotizacion.getLimpieza(), cotizacion.getServicio(),
                cotizacion.getDescuento(), sumarExtras(cotizacion), cotizacion.getTotal(),
                cotizacion.getVigencia(), servicios);
    }

    /**
     * Recalcula el total: precio base + limpieza + servicio + servicios adicionales
     * - descuento.
     * 
     * @param cotizacion
     */
    private void recalcularTotal(Cotizacion cotizacion) {
        cotizacion.setTotal(redondear(cotizacion.getPrecioBase().add(cotizacion.getLimpieza())
                .add(cotizacion.getServicio()).add(sumarExtras(cotizacion)).subtract(cotizacion.getDescuento())));
    }

    /**
     * Suma el costo aplicado de los servicios adicionales de la cotización.
     * 
     * @param cotizacion
     * @return
     */
    private BigDecimal sumarExtras(Cotizacion cotizacion) {
        BigDecimal extras = BigDecimal.ZERO;
        for (CotizacionServicio cs : cotizacion.getServicios()) {
            extras = extras.add(cs.getCostoAplicado());
        }
        return redondear(extras);
    }

    /**
     * Calcula base × porcentaje / 100, redondeado a dos decimales.
     * 
     * @param base
     * @param porcentaje
     * @return el valor del porcentaje sobre la base
     */
    private BigDecimal porcentaje(BigDecimal base, Number porcentaje) {
        return base.multiply(aDecimal(porcentaje)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Convierte un número a BigDecimal sin arrastrar error binario; nulo vale cero.
     * 
     * @param valor
     * @return el valor como BigDecimal
     */
    private BigDecimal aDecimal(Number valor) {
        return valor == null ? BigDecimal.ZERO : new BigDecimal(valor.toString());
    }

    /**
     * Redondea un valor monetario a dos decimales (mitad hacia arriba).
     * 
     * @param valor
     * @return el valor redondeado
     */
    private BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}