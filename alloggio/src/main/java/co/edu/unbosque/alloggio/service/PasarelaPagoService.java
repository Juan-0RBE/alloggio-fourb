package co.edu.unbosque.alloggio.service;

import java.math.BigDecimal;

import co.edu.unbosque.alloggio.dto.ResultadoPasarelaDTO;
import co.edu.unbosque.alloggio.util.exception.PasarelaException;

/**
 * Contrato de la pasarela de pagos.
 */
public interface PasarelaPagoService {

    /**
     * Cobra un monto con el método de pago indicado.
     * 
     * @param monto
     * @param moneda
     * @param metodoPago
     * @param referencia
     * @param claveIdempotencia
     * @return
     * @throws PasarelaException
     */
    ResultadoPasarelaDTO cobrar(BigDecimal monto, String moneda, String metodoPago, String referencia,
            String claveIdempotencia);

    /**
     * Devuelve total o parcialmente un cobro aprobado.
     * 
     * @param referenciaPasarela
     * @param monto
     * @param claveIdempotencia
     * @return
     * @throws PasarelaException
     */
    ResultadoPasarelaDTO reembolsar(String referenciaPasarela, BigDecimal monto, String claveIdempotencia);
}