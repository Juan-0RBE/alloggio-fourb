package co.edu.unbosque.alloggio.testutil;

import java.time.LocalDateTime;

import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Reserva;
import co.edu.unbosque.alloggio.entity.Reserva.EstadoReserva;
import co.edu.unbosque.alloggio.entity.Resena;
import co.edu.unbosque.alloggio.entity.Usuario;

/**
 * Datos de prueba reutilizables para reseñas.
 */
public final class ResenaFixtures {

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
}