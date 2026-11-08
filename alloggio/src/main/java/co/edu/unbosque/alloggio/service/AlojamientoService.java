package co.edu.unbosque.alloggio.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.dto.AlojamientoDetalleDTO;
import co.edu.unbosque.alloggio.dto.AlojamientoResumenDTO;
import co.edu.unbosque.alloggio.dto.ImagenAlojamientoDTO;
import co.edu.unbosque.alloggio.dto.PaginaDTO;
import co.edu.unbosque.alloggio.dto.PoliticaCancelacionDTO;
import co.edu.unbosque.alloggio.dto.RangoOcupadoDTO;
import co.edu.unbosque.alloggio.dto.ReglaAlojamientoDTO;
import co.edu.unbosque.alloggio.dto.ServicioAlojamientoDTO;
import co.edu.unbosque.alloggio.entity.ImagenAlojamiento;
import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad;
import co.edu.unbosque.alloggio.entity.PoliticaCancelacion;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.TipoAlojamiento;
import co.edu.unbosque.alloggio.entity.ReglaAlojamiento;
import co.edu.unbosque.alloggio.entity.ServicioAlojamiento;
import co.edu.unbosque.alloggio.repository.ImagenAlojamientoRepository;
import co.edu.unbosque.alloggio.repository.PeriodoDisponibilidadRepository;
import co.edu.unbosque.alloggio.repository.PublicacionRepository;
import co.edu.unbosque.alloggio.repository.ReglaAlojamientoRepository;
import co.edu.unbosque.alloggio.repository.ServicioAlojamientoRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

/**
 * Servicio para la consulta pública de alojamientos: búsqueda con filtros
 * y detalle. Solo lee datos; no modifica ninguna entidad.
 */
@Service
public class AlojamientoService {

    /** Tamaño máximo de página permitido en la búsqueda. */
    private static final int TAMANO_MAX_PAGINA = 50;

    @Autowired
    private PublicacionRepository publicacionRepo;

    @Autowired
    private ServicioAlojamientoRepository servicioRepo;

    @Autowired
    private ReglaAlojamientoRepository reglaRepo;

    @Autowired
    private ImagenAlojamientoRepository imagenRepo;

    @Autowired
    private PeriodoDisponibilidadRepository periodoRepo;

    @Autowired
    private ResenaService resenaService;

    @Autowired
    private ModelMapper modelMapper;

    public AlojamientoService() {
    }

    /**
     * Valida los criterios de búsqueda. Ningún filtro es obligatorio.
     * 
     * @param fechaLlegada
     * @param fechaSalida
     * @param precioMin
     * @param precioMax
     * @param capacidad
     * @return
     */
    public int validarCriterios(LocalDate fechaLlegada, LocalDate fechaSalida, Double precioMin, Double precioMax,
            Integer capacidad) {
        if ((fechaLlegada == null) != (fechaSalida == null)) {
            return 1; // Las fechas van juntas o ninguna
        }
        if (fechaLlegada != null) {
            if (!fechaSalida.isAfter(fechaLlegada)) {
                return 2; // La salida debe ser posterior a la llegada (RN-07)
            }
            if (fechaLlegada.isBefore(LocalDate.now())) {
                return 3; // La llegada está en el pasado
            }
        }
        if ((precioMin != null && precioMin < 0) || (precioMax != null && precioMax < 0)
                || (capacidad != null && capacidad < 1)) {
            return 5; // Valores fuera de rango
        }
        if (precioMin != null && precioMax != null && precioMin > precioMax) {
            return 4; // Rango de precio incoherente
        }
        return 0;
    }

    /**
     * Busca alojamientos ACTIVOS aplicando de forma simultánea los filtros que
     * lleguen. La respuesta es paginada y ordenada por precio por noche
     * ascendente (con el id como desempate para que las páginas sean estables).
     * Una búsqueda sin resultados devuelve una página vacía.
     * 
     * @param ciudad
     * @param fechaLlegada
     * @param fechaSalida
     * @param precioMin
     * @param precioMax
     * @param capacidad
     * @param tipo
     * @param servicios
     * @param pagina
     * @param tamano
     * @return
     */
    @Transactional(readOnly = true)
    public PaginaDTO<AlojamientoResumenDTO> buscar(String ciudad, LocalDate fechaLlegada, LocalDate fechaSalida,
            Double precioMin, Double precioMax, Integer capacidad, TipoAlojamiento tipo, List<String> servicios,
            int pagina, int tamano) {

        Specification<Publicacion> spec = (root, query, cb) -> {
            List<Predicate> filtros = new ArrayList<>();

            // Solo las publicaciones ACTIVAS
            filtros.add(cb.equal(root.get("estado"), EstadoPublicacion.ACTIVA));

            if (ciudad != null && !ciudad.isBlank()) {
                filtros.add(cb.equal(cb.lower(root.<String>get("ciudad")), ciudad.trim().toLowerCase()));
            }
            if (precioMin != null) {
                filtros.add(cb.greaterThanOrEqualTo(root.<Double>get("precioNoche"), precioMin));
            }
            if (precioMax != null) {
                filtros.add(cb.lessThanOrEqualTo(root.<Double>get("precioNoche"), precioMax));
            }
            if (capacidad != null) {
                filtros.add(cb.greaterThanOrEqualTo(root.<Integer>get("capacidad"), capacidad));
            }
            if (tipo != null) {
                filtros.add(cb.equal(root.get("tipo"), tipo));
            }

            // Disponibilidad: no debe existir ningún periodo (bloqueo o reserva) que cruce
            // el rango.
            if (fechaLlegada != null && fechaSalida != null) {
                Subquery<Long> ocupado = query.subquery(Long.class);
                Root<PeriodoDisponibilidad> periodo = ocupado.from(PeriodoDisponibilidad.class);
                ocupado.select(periodo.<Long>get("idPeriodoDisponibilidad"))
                        .where(cb.equal(periodo.get("publicacion").get("idPublicacion"), root.get("idPublicacion")),
                                cb.lessThan(periodo.<LocalDate>get("fechaInicio"), fechaSalida),
                                cb.greaterThan(periodo.<LocalDate>get("fechaFin"), fechaLlegada));
                filtros.add(cb.not(cb.exists(ocupado)));
            }

            // FILTRO DE SERVICIOS (se prueba al final): la publicación debe tener todos
            // los servicios indicados. Una subconsulta de existencia por cada nombre.
            if (servicios != null) {
                for (String nombreServicio : servicios) {
                    if (nombreServicio == null || nombreServicio.isBlank()) {
                        continue;
                    }
                    Subquery<Long> tiene = query.subquery(Long.class);
                    Root<ServicioAlojamiento> servicio = tiene.from(ServicioAlojamiento.class);
                    tiene.select(cb.literal(1L)).where(
                            cb.equal(servicio.get("publicacion").get("idPublicacion"), root.get("idPublicacion")),
                            cb.equal(cb.lower(servicio.<String>get("nombre")), nombreServicio.trim().toLowerCase()));
                    filtros.add(cb.exists(tiene));
                }
            }

            return cb.and(filtros.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 1), TAMANO_MAX_PAGINA),
                Sort.by(Sort.Direction.ASC, "precioNoche").and(Sort.by(Sort.Direction.ASC, "idPublicacion")));

        Page<Publicacion> resultado = publicacionRepo.findAll(spec, pageable);

        List<AlojamientoResumenDTO> contenido = new ArrayList<>();
        for (Publicacion pub : resultado.getContent()) {
            contenido.add(convertirResumen(pub));
        }

        return new PaginaDTO<>(contenido, resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(),
                resultado.getTotalPages());
    }

    /**
     * Obtiene el detalle público de un alojamiento.
     * 
     * @param idPublicacion
     * @return
     */
    @Transactional(readOnly = true)
    public AlojamientoDetalleDTO obtenerDetalle(Long idPublicacion) {
        Optional<Publicacion> pubOpt = publicacionRepo.findById(idPublicacion);
        if (!pubOpt.isPresent() || pubOpt.get().getEstado() != EstadoPublicacion.ACTIVA) {
            return null;
        }
        Publicacion pub = pubOpt.get();

        // La política es LAZY: se arma el DTO a mano dentro de la transacción
        PoliticaCancelacion politica = pub.getPoliticaCancelacion();
        PoliticaCancelacionDTO politicaDto = new PoliticaCancelacionDTO(politica.getIdPolitica(),
                politica.getNombre(), politica.getPorcentajeDevolucion(), politica.getCondiciones(),
                politica.getPlazoCancelacion());

        List<ServicioAlojamientoDTO> servicios = new ArrayList<>();
        for (ServicioAlojamiento s : servicioRepo.findByPublicacionIdPublicacion(idPublicacion)) {
            servicios.add(modelMapper.map(s, ServicioAlojamientoDTO.class));
        }

        List<ReglaAlojamientoDTO> reglas = new ArrayList<>();
        for (ReglaAlojamiento r : reglaRepo.findByPublicacionIdPublicacion(idPublicacion)) {
            reglas.add(modelMapper.map(r, ReglaAlojamientoDTO.class));
        }

        List<ImagenAlojamientoDTO> imagenes = new ArrayList<>();
        for (ImagenAlojamiento i : imagenRepo.findByPublicacionIdPublicacionOrderByOrdenAsc(idPublicacion)) {
            imagenes.add(modelMapper.map(i, ImagenAlojamientoDTO.class));
        }

        // Calendario: solo rangos que terminan después de hoy, sin distinguir bloqueo
        // de
        // reserva ni exponer la reserva asociada
        List<RangoOcupadoDTO> calendario = new ArrayList<>();
        for (PeriodoDisponibilidad p : periodoRepo
                .findByPublicacionIdPublicacionAndFechaFinAfterOrderByFechaInicioAsc(idPublicacion,
                        LocalDate.now())) {
            calendario.add(new RangoOcupadoDTO(p.getFechaInicio(), p.getFechaFin()));
        }

        return new AlojamientoDetalleDTO(pub.getIdPublicacion(), pub.getTitulo(), pub.getDescripcion(),
                pub.getUbicacion(), pub.getCiudad(), pub.getTipo(), pub.getCapacidad(), pub.getPrecioNoche(),
                politicaDto, servicios, reglas, imagenes, calendario,
                resenaService.calcularPromedio(idPublicacion),
                resenaService.contarVisibles(idPublicacion),
                resenaService.obtenerVisiblesRecientes(idPublicacion));
    }

    /**
     * Convierte una publicación en el resumen público que se muestra en los
     * resultados de búsqueda, con su imagen principal.
     * 
     * @param pub
     * @return
     */
    private AlojamientoResumenDTO convertirResumen(Publicacion pub) {
        ImagenAlojamientoDTO imagenPrincipal = imagenRepo
                .findFirstByPublicacionIdPublicacionOrderByOrdenAsc(pub.getIdPublicacion())
                .map(i -> modelMapper.map(i, ImagenAlojamientoDTO.class)).orElse(null);

        return new AlojamientoResumenDTO(pub.getIdPublicacion(), pub.getTitulo(), pub.getCiudad(), pub.getTipo(),
                pub.getCapacidad(), pub.getPrecioNoche(), imagenPrincipal);
    }

    /**
     * Indica si una publicación está libre en el rango, ningún
     * periodo debe cruzarlo.
     */
    @Transactional(readOnly = true)
    public boolean estaDisponible(Long idPublicacion, LocalDate fechaLlegada, LocalDate fechaSalida) {
        return periodoRepo.findIntersectingPeriods(idPublicacion, fechaLlegada, fechaSalida).isEmpty();
    }
}