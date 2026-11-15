package co.edu.unbosque.alloggio.testutil;

import co.edu.unbosque.alloggio.dto.ReservaDTO;
import co.edu.unbosque.alloggio.entity.Reserva;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Usuario;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ReservaFixtures {

    public static final Long ID_RESERVA = 1L;

    public static ReservaDTO reservaDTOValida(Long idPublicacion) {
        ReservaDTO dto = new ReservaDTO();
        dto.setIdReserva(ID_RESERVA);
        dto.setIdPublicacion(idPublicacion);
        dto.setFechaLlegada(LocalDate.now().plusDays(10));
        dto.setFechaSalida(LocalDate.now().plusDays(13));
        dto.setTotal(new BigDecimal("865000.00"));
        dto.setEstado("CONFIRMADA");
        return dto;
    }

    public static Reserva reservaConfirmada(Usuario huesped, Publicacion pub) {
        Reserva reserva = new Reserva();
        reserva.setIdReserva(ID_RESERVA);
        reserva.setHuesped(huesped);
        reserva.setPublicacion(pub);
        reserva.setFechaLlegada(LocalDate.now().plusDays(10));
        reserva.setFechaSalida(LocalDate.now().plusDays(13));
        reserva.setEstado(Reserva.EstadoReserva.CONFIRMADA); // Usando el Enum real de la entidad
        return reserva;
    }
}