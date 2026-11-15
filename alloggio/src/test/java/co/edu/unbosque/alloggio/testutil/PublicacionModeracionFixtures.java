package co.edu.unbosque.alloggio.testutil;

import java.math.BigDecimal;
import java.time.LocalDate;

import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.Reserva;
import co.edu.unbosque.alloggio.entity.Usuario;

public class PublicacionModeracionFixtures {

    public static final Long ID_PUBLICACION = 1L;

    public static Publicacion publicacionActiva(Usuario anfitrion) {
        Publicacion p = new Publicacion();
        p.setIdPublicacion(ID_PUBLICACION);
        p.setTitulo("Apto en la playa");
        p.setEstado(EstadoPublicacion.ACTIVA);
        p.setAnfitrion(anfitrion);
        return p;
    }

    public static Publicacion publicacionBorrador(Long id) {
        Publicacion p = new Publicacion();
        p.setIdPublicacion(id);
        p.setEstado(EstadoPublicacion.BORRADOR);
        return p;
    }

    public static Reserva reservaFuturaConfirmada(Long idReserva, Publicacion pub, Usuario huesped) {
        Reserva r = new Reserva();
        r.setIdReserva(idReserva);
        r.setPublicacion(pub);
        r.setHuesped(huesped);
        r.setEstado(Reserva.EstadoReserva.CONFIRMADA);
        r.setFechaLlegada(LocalDate.now().plusDays(10));
        r.setFechaSalida(LocalDate.now().plusDays(13));
        r.setTotal(new BigDecimal("450000.00"));
        return r;
    }
}