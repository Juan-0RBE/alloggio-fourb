package co.edu.unbosque.alloggio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.alloggio.service.ClienteHTTP;
import co.edu.unbosque.alloggio.dto.ResultadoPasarelaDTO;
import co.edu.unbosque.alloggio.dto.StripeRespuestaDTO;
import co.edu.unbosque.alloggio.util.exception.PasarelaException;

/**
 * Implementación de la pasarela con Stripe en modo de prueba.
 */
@Service
public class StripePasarelaService implements PasarelaPagoService {

    @Autowired
    private ClienteHTTP cliente;

    public StripePasarelaService() {
    }

    @Override
    public ResultadoPasarelaDTO cobrar(BigDecimal monto, String moneda, String metodoPago, String referencia,
            String claveIdempotencia) {
        StripeRespuestaDTO respuesta = cliente.crearCobro(aUnidadMenor(monto), moneda, metodoPago, referencia,
                claveIdempotencia);

        ResultadoPasarelaDTO resultado = new ResultadoPasarelaDTO();
        resultado.setReferencia(respuesta.getId());
        resultado.setMarcaTarjeta(respuesta.getMarcaTarjeta());
        resultado.setUltimosCuatro(respuesta.getUltimosCuatro());

        if (respuesta.esExitoso()) {
            resultado.setAprobado(true);
            return resultado;
        }

        // Cualquier estado distinto de "succeeded" se trata como rechazado
        String codigo = respuesta.getCodigoRechazo() != null ? respuesta.getCodigoRechazo()
                : (respuesta.getCodigoError() != null ? respuesta.getCodigoError() : respuesta.getEstado());
        resultado.setAprobado(false);
        resultado.setCodigoRechazo(codigo);
        resultado.setMotivoRechazo(traducirMotivo(codigo));
        return resultado;
    }

    @Override
    public ResultadoPasarelaDTO reembolsar(String referenciaPasarela, BigDecimal monto, String claveIdempotencia) {
        StripeRespuestaDTO respuesta = cliente.reembolsar(referenciaPasarela, aUnidadMenor(monto),
                claveIdempotencia);

        ResultadoPasarelaDTO resultado = new ResultadoPasarelaDTO();
        resultado.setReferencia(respuesta.getId());
        boolean aceptado = "succeeded".equals(respuesta.getEstado()) || "pending".equals(respuesta.getEstado());
        resultado.setAprobado(aceptado);
        if (!aceptado) {
            resultado.setCodigoRechazo(respuesta.getEstado());
            resultado.setMotivoRechazo("La pasarela no pudo procesar la devolución");
        }
        return resultado;
    }

    /**
     * Convierte un monto a la unidad menor de la moneda.
     */
    private long aUnidadMenor(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new PasarelaException("El monto a cobrar debe ser positivo");
        }
        return monto.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
    }

    /**
     * Traduce el código de rechazo de Stripe a un mensaje para el huésped (CA-32).
     */
    private String traducirMotivo(String codigo) {
        if (codigo == null) {
            return "El pago fue rechazado por la pasarela";
        }
        return switch (codigo) {
            case "insufficient_funds" -> "Fondos insuficientes";
            case "expired_card" -> "La tarjeta está vencida";
            case "incorrect_cvc", "invalid_cvc" -> "El código de seguridad es incorrecto";
            case "incorrect_number", "invalid_number" -> "El número de la tarjeta no es válido";
            case "processing_error" -> "Error al procesar la tarjeta; intenta de nuevo";
            case "requires_action", "requires_payment_method" ->
                "El pago requiere una autenticación adicional que no está disponible";
            case "card_declined", "generic_decline", "lost_card", "stolen_card" -> "La tarjeta fue rechazada";
            default -> "El pago fue rechazado por la pasarela";
        };
    }
}