package co.edu.unbosque.alloggio.dto;

/**
 * Representa, de forma resumida, la respuesta JSON de la API de Stripe (un
 * PaymentIntent, un Refund o un error de tarjeta). Solo conserva los campos que
 * el sistema usa; el resto del JSON se ignora.
 */
public class StripeRespuestaDTO {

    private int codigoHttp;
    private String id;
    /** Campo "status" de Stripe (succeeded, requires_action, ...). */
    private String estado;
    private Long monto;
    private String moneda;
    private String marcaTarjeta;
    private String ultimosCuatro;
    /** Campo "code" del error (card_declined, expired_card, ...). */
    private String codigoError;
    /** Campo "decline_code" del error (insufficient_funds, ...). */
    private String codigoRechazo;
    private String mensajeError;

    public StripeRespuestaDTO() {
    }

    /**
     * Constructor para crear un nuevo DTO de respuesta de Stripe con todos los
     * atributos
     * 
     * @param codigoHttp
     * @param id
     * @param estado
     * @param monto
     * @param moneda
     * @param marcaTarjeta
     * @param ultimosCuatro
     * @param codigoError
     * @param codigoRechazo
     * @param mensajeError
     */
    public StripeRespuestaDTO(int codigoHttp, String id, String estado, Long monto, String moneda,
            String marcaTarjeta, String ultimosCuatro, String codigoError, String codigoRechazo,
            String mensajeError) {
        this.codigoHttp = codigoHttp;
        this.id = id;
        this.estado = estado;
        this.monto = monto;
        this.moneda = moneda;
        this.marcaTarjeta = marcaTarjeta;
        this.ultimosCuatro = ultimosCuatro;
        this.codigoError = codigoError;
        this.codigoRechazo = codigoRechazo;
        this.mensajeError = mensajeError;
    }

    /**
     * Indica si el cobro quedó realmente pagado: HTTP 2xx y estado "succeeded".
     * 
     * @return true si el pago fue exitoso
     */
    public boolean esExitoso() {
        return codigoHttp >= 200 && codigoHttp < 300 && "succeeded".equals(estado);
    }

    // Getters y Setters
    public int getCodigoHttp() {
        return codigoHttp;
    }

    public void setCodigoHttp(int codigoHttp) {
        this.codigoHttp = codigoHttp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getMarcaTarjeta() {
        return marcaTarjeta;
    }

    public void setMarcaTarjeta(String marcaTarjeta) {
        this.marcaTarjeta = marcaTarjeta;
    }

    public String getUltimosCuatro() {
        return ultimosCuatro;
    }

    public void setUltimosCuatro(String ultimosCuatro) {
        this.ultimosCuatro = ultimosCuatro;
    }

    public String getCodigoError() {
        return codigoError;
    }

    public void setCodigoError(String codigoError) {
        this.codigoError = codigoError;
    }

    public String getCodigoRechazo() {
        return codigoRechazo;
    }

    public void setCodigoRechazo(String codigoRechazo) {
        this.codigoRechazo = codigoRechazo;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }
}