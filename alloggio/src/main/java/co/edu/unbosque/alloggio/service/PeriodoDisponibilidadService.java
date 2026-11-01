package co.edu.unbosque.alloggio.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.dto.PeriodoDisponibilidadDTO;
import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad;
import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad.TipoPeriodo;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.repository.PeriodoDisponibilidadRepository;
import co.edu.unbosque.alloggio.repository.PublicacionRepository;

/**
 * Clase de servicio para manejar la lógica de negocio relacionada con los
 * períodos de disponibilidad.
 * 
 * PeriodoDisponibilidadService
 */
@Service
public class PeriodoDisponibilidadService {

    @Autowired
    private PeriodoDisponibilidadRepository periodoRepo;

    @Autowired
    private PublicacionRepository publicacionRepo;

    public PeriodoDisponibilidadService() {
    }

    /**
     * Verifica que la publicación exista y pertenezca al usuario autenticado.
     * 
     * @param nombreUsuario nombre del usuario autenticado
     * @param idPublicacion ID de la publicación
     * @return 0 si el usuario es el propietario, 1 si la publicación no existe, 2
     *         si la publicación pertenece a otro anfitrión
     */
    @Transactional(readOnly = true)
    public int validarPropietario(String nombreUsuario, Long idPublicacion) {
        Optional<Publicacion> pubOpt = publicacionRepo.findById(idPublicacion);
        if (!pubOpt.isPresent()) {
            return 1; // Publicación no encontrada
        }
        if (!esPropietario(pubOpt.get(), nombreUsuario)) {
            return 2; // La publicación no pertenece al usuario
        }
        return 0;
    }

    /**
     * Bloquea un rango de fechas de una publicación del anfitrión autenticado.
     * 
     * @param nombreUsuario nombre del usuario autenticado
     * @param dto           idPublicacion, fechaInicio y fechaFin (día de salida)
     * @return 0 si es exitoso, 1 si la publicación no existe, 2 si no pertenece al
     *         usuario, 3 si las fechas son nulas o la fecha de fin no es posterior
     *         a la de inicio, 4 si el rango inicia en el pasado, 5 si se cruza con
     *         una reserva confirmada, 6 si se cruza con otro bloqueo existente
     */
    @Transactional
    public int bloquearRango(String nombreUsuario, PeriodoDisponibilidadDTO dto) {
        if (dto.getIdPublicacion() == null) {
            return 1; // Publicación no encontrada
        }
        Optional<Publicacion> pubOpt = publicacionRepo.findById(dto.getIdPublicacion());
        if (!pubOpt.isPresent()) {
            return 1; // Publicación no encontrada
        }

        Publicacion pub = pubOpt.get();
        if (!esPropietario(pub, nombreUsuario)) {
            return 2; // La publicación no pertenece al usuario
        }
        if (!rangoOrdenado(dto.getFechaInicio(), dto.getFechaFin())) {
            return 3; // Fechas inválidas (RN-07)
        }
        if (dto.getFechaInicio().isBefore(LocalDate.now())) {
            return 4; // El rango inicia en el pasado
        }

        List<PeriodoDisponibilidad> intersecciones = periodoRepo.findIntersectingPeriods(pub.getIdPublicacion(),
                dto.getFechaInicio(), dto.getFechaFin());
        for (PeriodoDisponibilidad interseccion : intersecciones) {
            if (interseccion.getTipo() == TipoPeriodo.RESERVADO) {
                return 5; // Se cruza con una reserva confirmada
            }
        }
        if (!intersecciones.isEmpty()) {
            return 6; // Se cruza con otro bloqueo
        }

        PeriodoDisponibilidad periodo = new PeriodoDisponibilidad();
        periodo.setFechaInicio(dto.getFechaInicio());
        periodo.setFechaFin(dto.getFechaFin());
        periodo.setTipo(TipoPeriodo.BLOQUEADO);
        periodo.setPublicacion(pub);
        periodoRepo.save(periodo);
        return 0; // Rango bloqueado exitosamente
    }

    /**
     * Elimina un bloqueo del calendario, con lo que el rango vuelve a estar
     * disponible. Las reservas no se pueden desbloquear desde aquí.
     * 
     * @param nombreUsuario
     * @param idPeriodo
     * @return
     */
    @Transactional
    public int desbloquearRango(String nombreUsuario, Long idPeriodo) {
        Optional<PeriodoDisponibilidad> periodoOpt = periodoRepo.findById(idPeriodo);
        if (!periodoOpt.isPresent()) {
            return 1; // Periodo no encontrado
        }

        PeriodoDisponibilidad periodo = periodoOpt.get();
        if (!esPropietario(periodo.getPublicacion(), nombreUsuario)) {
            return 2; // La publicación no pertenece al usuario
        }
        if (periodo.getTipo() != TipoPeriodo.BLOQUEADO) {
            return 3; // Solo se desbloquean los bloqueos del anfitrión
        }

        periodoRepo.delete(periodo);
        return 0; // Rango desbloqueado exitosamente
    }

    /**
     * Indica si la publicación pertenece al usuario.
     * 
     * @param pub
     * @param nombreUsuario
     * @return
     */
    private boolean esPropietario(Publicacion pub, String nombreUsuario) {
        return pub.getAnfitrion().getNombreUsuario().equals(nombreUsuario);
    }

    /**
     * Obtiene los periodos bloqueados y reservados de una publicación,
     * ordenados por fecha de inicio. Las fechas que no aparecen están disponibles.
     * La validación del propietario se hace antes con validarPropietario.
     * 
     * @param idPublicacion
     * @return
     */
    @Transactional(readOnly = true)
    public List<PeriodoDisponibilidadDTO> consultarCalendario(Long idPublicacion) {
        List<PeriodoDisponibilidad> periodos = periodoRepo
                .findByPublicacionIdPublicacionOrderByFechaInicioAsc(idPublicacion);

        List<PeriodoDisponibilidadDTO> dtos = new ArrayList<>();
        for (PeriodoDisponibilidad periodo : periodos) {
            dtos.add(aDTO(periodo));
        }
        return dtos;
    }

    /**
     * Indica si una publicación se puede reservar en el rango [fechaInicio,
     * fechaFin). La usarán la búsqueda, la cotización y la creación de la reserva.
     * 
     * @param idPublicacion
     * @param fechaInicio
     * @param fechaFin
     * @return
     */
    @Transactional(readOnly = true)
    public boolean estaDisponible(Long idPublicacion, LocalDate fechaInicio, LocalDate fechaFin) {
        if (idPublicacion == null || !rangoOrdenado(fechaInicio, fechaFin) || fechaInicio.isBefore(LocalDate.now())) {
            return false;
        }
        Optional<Publicacion> pubOpt = publicacionRepo.findById(idPublicacion);
        if (!pubOpt.isPresent() || pubOpt.get().getEstado() != EstadoPublicacion.ACTIVA) {
            return false;
        }
        return periodoRepo.findIntersectingPeriods(idPublicacion, fechaInicio, fechaFin).isEmpty();
    }

    /**
     * Verifica que ambas fechas existan y que la de fin sea posterior a la de
     * inicio.
     * 
     * @param fechaInicio
     * @param fechaFin
     * @return true si el rango es válido
     */
    private boolean rangoOrdenado(LocalDate fechaInicio, LocalDate fechaFin) {
        return fechaInicio != null && fechaFin != null && fechaFin.isAfter(fechaInicio);
    }

    /**
     * Convierte un periodo en su DTO.
     * 
     * @param periodo entidad a convertir
     * @return el DTO del periodo
     */
    private PeriodoDisponibilidadDTO aDTO(PeriodoDisponibilidad periodo) {
        PeriodoDisponibilidadDTO dto = new PeriodoDisponibilidadDTO();
        dto.setIdPeriodoDisponibilidad(periodo.getIdPeriodoDisponibilidad());
        dto.setFechaInicio(periodo.getFechaInicio());
        dto.setFechaFin(periodo.getFechaFin());
        dto.setTipo(periodo.getTipo());
        dto.setIdPublicacion(periodo.getPublicacion().getIdPublicacion());
        if (periodo.getReserva() != null) {
            dto.setIdReserva(periodo.getReserva().getIdReserva());
        }
        return dto;
    }

}
