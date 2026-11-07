package co.edu.unbosque.alloggio.dto;

/**
 * Resultado de una operación con la pasarela de pagos.
 */
public class ResultadoPasarelaDTO {

    private boolean aprobado;
    /** Identificador de la operación en la pasarela (pi_..., re_...). */
    private String referencia;
    private String marcaTarjeta;
    private String ultimosCuatro;
    private String codigoRechazo;
    /** Motivo en español, listo para mostrar al huésped. */
    private String motivoRechazo;

    public ResultadoPasarelaDTO() {
    }

    /**
     * Constructor para crear un nuevo resultado de pasarela con todos los
     * atributos
     * 
     * @param aprobado
     * @param referencia
     * @param marcaTarjeta
     * @param ultimosCuatro
     * @param codigoRechazo
     * @param motivoRechazo
     */
    public ResultadoPasarelaDTO(boolean aprobado, String referencia, String marcaTarjeta, String ultimosCuatro,
            String codigoRechazo, String motivoRechazo) {
        this.aprobado = aprobado;
        this.referencia = referencia;
        this.marcaTarjeta = marcaTarjeta;
        this.ultimosCuatro = ultimosCuatro;
        this.codigoRechazo = codigoRechazo;
        this.motivoRechazo = motivoRechazo;
    }

    // Getters y Setters
    public boolean isAprobado() {
        return aprobado;
    }

    public void setAprobado(boolean aprobado) {
        this.aprobado = aprobado;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
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

    public String getCodigoRechazo() {
        return codigoRechazo;
    }

    public void setCodigoRechazo(String codigoRechazo) {
        this.codigoRechazo = codigoRechazo;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }
}