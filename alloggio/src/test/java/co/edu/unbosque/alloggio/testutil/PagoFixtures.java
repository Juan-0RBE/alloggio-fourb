package co.edu.unbosque.alloggio.testutil;

import co.edu.unbosque.alloggio.dto.PagoDTO;
import co.edu.unbosque.alloggio.entity.Pago;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagoFixtures {

    public static final Long ID_PAGO = 1L;

    public static PagoDTO pagoDTOValido() {
        PagoDTO dto = new PagoDTO();
        dto.setMetodoPago("pm_card_visa");
        dto.setClaveIdempotencia("idempotency_key_123");
        dto.setMonto(new BigDecimal("865000.00"));
        dto.setMoneda("COP");
        return dto;
    }

    public static Pago pagoAprobado() {
        Pago pago = new Pago();
        pago.setIdPago(ID_PAGO);
        pago.setMonto(new BigDecimal("865000.00"));
        pago.setEstado(Pago.EstadoPago.APROBADO);
        pago.setFecha(LocalDateTime.now());
        return pago;
    }
}