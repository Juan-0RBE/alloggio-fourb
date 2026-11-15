package co.edu.unbosque.alloggio.testutil;

import java.time.LocalDateTime;

import co.edu.unbosque.alloggio.dto.ResenaDTO;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Reserva;
import co.edu.unbosque.alloggio.entity.Reserva.EstadoReserva;
import co.edu.unbosque.alloggio.entity.Resena;
import co.edu.unbosque.alloggio.entity.Usuario;

/**
 * Datos de prueba reutilizables para reseñas. Cada método devuelve un objeto
 * nuevo.
 */
public final class ResenaFixtures {

    public static final Long ID_RESERVA = 20L;
    public static final Long ID_RESENA = 5L;
    public static final String NOMBRE_ADMIN = "admin1";

    private ResenaFixtures() {
    }

    /**
     * Reseña visible de una reserva completada del huésped indicado sobre la
     * publicación indicada.
     */
    public static Resena resenaVisible(Long id, int calificacion, String comentario, Usuario huesped,
            Publicacion publicacion) {
        Reserva reserva = new Reserva();
        reserva.setIdReserva(id);
        reserva.setEstado(EstadoReserva.COMPLETADA);
        reserva.setHuesped(huesped);
        reserva.setPublicacion(publicacion);

        Resena resena = new Resena();
        resena.setIdResena(id);
        resena.setCalificacion(calificacion);
        resena.setComentario(comentario);
        resena.setFecha(LocalDateTime.now().minusDays(1));
        resena.setVisible(true);
        resena.setReserva(reserva);
        return resena;
    }

    /** Reserva en el estado indicado, del huésped y la publicación dados. */
    public static Reserva reserva(EstadoReserva estado, Usuario huesped, Publicacion publicacion) {
        Reserva reserva = new Reserva();
        reserva.setIdReserva(ID_RESERVA);
        reserva.setEstado(estado);
        reserva.setHuesped(huesped);
        reserva.setPublicacion(publicacion);
        return reserva;
    }

    /** Solicitud de reseña como la envía el huésped. */
    public static ResenaDTO solicitudResena(Long idReserva, Integer calificacion, String comentario) {
        ResenaDTO dto = new ResenaDTO();
        dto.setIdReserva(idReserva);
        dto.setCalificacion(calificacion);
        dto.setComentario(comentario);
        return dto;
    }
}