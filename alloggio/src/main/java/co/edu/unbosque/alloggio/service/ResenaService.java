package co.edu.unbosque.alloggio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.dto.ResenaDTO;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Accion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.TipoEntidad;
import co.edu.unbosque.alloggio.entity.Reserva;
import co.edu.unbosque.alloggio.entity.Reserva.EstadoReserva;
import co.edu.unbosque.alloggio.entity.Resena;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.ReservaRepository;
import co.edu.unbosque.alloggio.repository.ResenaRepository;

/**
 * Servicio de reseñas y calificaciones.
 */
@Service
public class ResenaService {

    private static final int CALIFICACION_MIN = 1;
    private static final int CALIFICACION_MAX = 5;
    private static final int COMENTARIO_MAX = 1000;

    @Autowired
    private ResenaRepository resenaRepo;

    @Autowired
    private ReservaRepository reservaRepo;

    @Autowired
    private EventoAuditoriaService auditoriaService;

    public ResenaService() {
    }

    /**
     * Crea la reseña de una reserva completada del huésped autenticado. Bloquea la
     * fila de la reserva para que dos envíos simultáneos no creen dos reseñas; la
     * restricción única de la tabla queda como respaldo.
     * 
     * @param nombreUsuario
     * @param dto
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int crear(String nombreUsuario, ResenaDTO dto) {
        if (dto.getIdReserva() == null) {
            return 1; // Reserva no encontrada
        }
        Optional<Reserva> reservaOpt = reservaRepo.findByIdParaActualizar(dto.getIdReserva());
        if (!reservaOpt.isPresent()) {
            return 1; // Reserva no encontrada
        }

        Reserva reserva = reservaOpt.get();
        if (!reserva.getHuesped().getNombreUsuario().equals(nombreUsuario)) {
            auditoriaService.registrarDenegado(nombreUsuario, Accion.CREAR_RESENA, TipoEntidad.RESERVA,
                    reserva.getIdReserva(), "Intento de reseñar una reserva ajena");
            return 2; // La reserva no pertenece al usuario
        }
        if (reserva.getEstado() != EstadoReserva.COMPLETADA) {
            return 3; // Solo se reseña una estadía completada
        }
        if (dto.getCalificacion() == null || dto.getCalificacion() < CALIFICACION_MIN
                || dto.getCalificacion() > CALIFICACION_MAX) {
            return 4; // Calificación fuera de rango
        }
        String comentario = dto.getComentario() == null ? "" : dto.getComentario().trim();
        if (comentario.isEmpty() || comentario.length() > COMENTARIO_MAX) {
            return 5; // Comentario inválido
        }
        if (resenaRepo.existsByReservaIdReserva(reserva.getIdReserva())) {
            return 6; // La reserva ya tiene reseña
        }

        Resena resena = new Resena();
        resena.setCalificacion(dto.getCalificacion());
        resena.setComentario(comentario);
        resena.setReserva(reserva);
        Resena guardada = resenaRepo.save(resena);

        auditoriaService.registrarExito(nombreUsuario, Accion.CREAR_RESENA, TipoEntidad.RESENA,
                guardada.getIdResena(), "Reseña de la reserva " + reserva.getIdReserva() + " sobre la publicación "
                        + reserva.getPublicacion().getIdPublicacion() + " con calificación "
                        + guardada.getCalificacion());
        dto.setIdResena(guardada.getIdResena());
        return 0;
    }

    /**
     * Oculta una reseña que incumple las normas. No se borra: deja de mostrarse en
     * el detalle del alojamiento y de contarse en el promedio.
     * 
     * @param nombreUsuario
     * @param idResena
     * @param confirmar
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int ocultar(String nombreUsuario, Long idResena, Boolean confirmar) {
        Optional<Resena> resenaOpt = resenaRepo.findByIdParaActualizar(idResena);
        if (!resenaOpt.isPresent()) {
            return 1; // Reseña no encontrada
        }
        if (!Boolean.TRUE.equals(confirmar)) {
            return 2; // Sin confirmación no se modifica nada
        }

        Resena resena = resenaOpt.get();
        if (!resena.getVisible()) {
            return 3; // Ya estaba oculta
        }

        resena.setVisible(false);
        resenaRepo.save(resena);

        auditoriaService.registrarExito(nombreUsuario, Accion.OCULTAR_RESENA, TipoEntidad.RESENA, idResena,
                "Reseña ocultada en la publicación " + resena.getReserva().getPublicacion().getIdPublicacion());
        return 0;
    }

    /**
     * Obtiene una reseña por su id.
     * 
     * @param idResena
     * @return
     */
    @Transactional(readOnly = true)
    public ResenaDTO obtenerPorId(Long idResena) {
        return resenaRepo.findById(idResena).map(this::convertir).orElse(null);
    }

    /**
     * Lista todas las reseñas de un alojamiento, visibles y ocultas, para que el
     * administrador elija cuál ocultar.
     * 
     * @param idPublicacion
     * @return reseñas de la más reciente a la más antigua (vacía si no hay)
     */
    @Transactional(readOnly = true)
    public List<ResenaDTO> obtenerPorAlojamiento(Long idPublicacion) {
        List<ResenaDTO> dtoList = new ArrayList<>();
        for (Resena r : resenaRepo.findByReservaPublicacionIdPublicacionOrderByFechaDescIdResenaDesc(idPublicacion)) {
            dtoList.add(convertir(r));
        }
        return dtoList;
    }

    /**
     * Las 10 reseñas visibles más recientes de un alojamiento, para su detalle.
     * 
     * @param idPublicacion
     * @return reseñas visibles (vacía si no hay)
     */
    @Transactional(readOnly = true)
    public List<ResenaDTO> obtenerVisiblesRecientes(Long idPublicacion) {
        List<ResenaDTO> dtoList = new ArrayList<>();
        for (Resena r : resenaRepo
                .findTop10ByReservaPublicacionIdPublicacionAndVisibleTrueOrderByFechaDescIdResenaDesc(idPublicacion)) {
            dtoList.add(convertir(r));
        }
        return dtoList;
    }

    /**
     * Calificación promedio de las reseñas visibles, redondeada a un decimal.
     * 
     * @param idPublicacion
     * @return el promedio, o null si el alojamiento no tiene reseñas visibles
     */
    @Transactional(readOnly = true)
    public Double calcularPromedio(Long idPublicacion) {
        Double promedio = resenaRepo.promedioVisible(idPublicacion);
        return promedio == null ? null : Math.round(promedio * 10.0) / 10.0;
    }

    /**
     * Cantidad de reseñas visibles de un alojamiento.
     * 
     * @param idPublicacion
     * @return total de reseñas visibles
     */
    @Transactional(readOnly = true)
    public long contarVisibles(Long idPublicacion) {
        return resenaRepo.countByReservaPublicacionIdPublicacionAndVisibleTrue(idPublicacion);
    }

    /**
     * Convierte una reseña en su DTO. Se arma a mano porque el autor vive dentro de
     * la relación con la reserva (ModelMapper no lo llena). Debe llamarse dentro de
     * una transacción por las relaciones LAZY.
     * 
     * @param resena
     * @return
     */
    private ResenaDTO convertir(Resena resena) {
        Usuario huesped = resena.getReserva().getHuesped();
        ResenaDTO dto = new ResenaDTO();
        dto.setIdResena(resena.getIdResena());
        dto.setCalificacion(resena.getCalificacion());
        dto.setComentario(resena.getComentario());
        dto.setFecha(resena.getFecha());
        dto.setVisible(resena.getVisible());
        dto.setAutor(huesped.getPrimerNombre() + " " + huesped.getPrimerApellido());
        return dto;
    }
}