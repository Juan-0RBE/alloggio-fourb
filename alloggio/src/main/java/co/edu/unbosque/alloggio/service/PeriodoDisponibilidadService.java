package co.edu.unbosque.alloggio.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.dto.PeriodoDisponibilidadDTO;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Accion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.TipoEntidad;
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
 */
@Service
public class PeriodoDisponibilidadService {

    @Autowired
    private PeriodoDisponibilidadRepository periodoRepo;

    @Autowired
    private PublicacionRepository publicacionRepo;

    @Autowired
    private EventoAuditoriaService auditoriaService;

    public PeriodoDisponibilidadService() {
    }

    /**
     * Verifica que la publicación exista y pertenezca al usuario autenticado.
     * 
     * @param nombreUsuario
     * @param idPublicacion
     * @return
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
     * @param nombreUsuario
     * @param dto
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int bloquearRango(String nombreUsuario, PeriodoDisponibilidadDTO dto) {
        if (dto.getIdPublicacion() == null) {
            return 1; // Publicación no encontrada
        }
        Optional<Publicacion> pubOpt = publicacionRepo.findByIdParaActualizar(dto.getIdPublicacion());
        if (!pubOpt.isPresent()) {
            return 1; // Publicación no encontrada
        }

        Publicacion pub = pubOpt.get();
        if (!esPropietario(pub, nombreUsuario)) {
            auditoriaService.registrarDenegado(nombreUsuario, Accion.BLOQUEAR_RANGO, TipoEntidad.PUBLICACION,
                    pub.getIdPublicacion(), "Intento de bloquear fechas de una publicación ajena");
            return 2; // La publicación no pertenece al usuario
        }
        if (!rangoOrdenado(dto.getFechaInicio(), dto.getFechaFin())) {
            return 3; // Fechas inválidas
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
        // periodoRepo.save(periodo);

        // Guarda en la base de datos y recupera la entidad persistida con su ID
        // generado
        PeriodoDisponibilidad guardado = periodoRepo.save(periodo);

        // Registro en auditoria
        auditoriaService.registrarExito(nombreUsuario, Accion.BLOQUEAR_RANGO, TipoEntidad.PERIODO_DISPONIBILIDAD,
                guardado.getIdPeriodoDisponibilidad(), "Rango bloqueado del " + dto.getFechaInicio() + " al "
                        + dto.getFechaFin() + " en la publicación " + pub.getIdPublicacion());
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
            auditoriaService.registrarDenegado(nombreUsuario, Accion.DESBLOQUEAR_RANGO,
                    TipoEntidad.PERIODO_DISPONIBILIDAD, idPeriodo,
                    "Intento de desbloquear fechas de una publicación ajena");
            return 2; // La publicación no pertenece al usuario
        }
        if (periodo.getTipo() != TipoPeriodo.BLOQUEADO) {
            return 3; // Solo se desbloquean los bloqueos del anfitrión
        }

        String detalle = "Bloqueo eliminado del " + periodo.getFechaInicio() + " al " + periodo.getFechaFin()
                + " en la publicación " + periodo.getPublicacion().getIdPublicacion();
        periodoRepo.delete(periodo);

        // Registro en auditoria
        auditoriaService.registrarExito(nombreUsuario, Accion.DESBLOQUEAR_RANGO, TipoEntidad.PERIODO_DISPONIBILIDAD,
                idPeriodo, detalle);
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
     * @param periodo
     * @return
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
