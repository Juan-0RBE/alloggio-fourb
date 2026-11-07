package co.edu.unbosque.alloggio.util.exception;

/**
 * Falla de comunicación o de configuración con la pasarela de pagos (sin
 * conexión, tiempo agotado, clave inválida, respuesta ilegible).
 */
public class PasarelaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PasarelaException(String mensaje) {
        super(mensaje);
    }

    public PasarelaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
