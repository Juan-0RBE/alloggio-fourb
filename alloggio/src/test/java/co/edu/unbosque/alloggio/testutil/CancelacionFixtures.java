package co.edu.unbosque.alloggio.testutil;

import co.edu.unbosque.alloggio.dto.CancelacionDTO;
import java.time.LocalDateTime;

public class CancelacionFixtures {

    public static final Long ID_CANCELACION = 1L;

    public static CancelacionDTO cancelacionDTOValida(Long idReserva) {
        CancelacionDTO dto = new CancelacionDTO();
        dto.setIdCancelacion(ID_CANCELACION);
        dto.setIdReserva(idReserva);
        dto.setMotivo("Cambio de planes de viaje");
        return dto;
    }
}