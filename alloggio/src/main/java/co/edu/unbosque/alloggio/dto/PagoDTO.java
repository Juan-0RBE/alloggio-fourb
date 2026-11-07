package co.edu.unbosque.alloggio.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa el DTO de un pago.
 */
public class PagoDTO {

    // Datos de entrada
    /**
     * Identificador del método de pago de la pasarela (pm_...), nunca un número de
     * tarjeta.
     */
    private String metodoPago;
    /** Clave que evita cobros duplicados ante reintentos. */
    private String claveIdempotencia;

    // Datos de salida
    private Long idPago;
    private Long idCotizacion;
    private BigDecimal monto;
    private String moneda;
    private String estado;
    private String referenciaPasarela;
    private LocalDateTime fecha;
    private String marcaTarjeta;
    private String ultimosCuatro;
    private String motivoRechazo;

    public PagoDTO() {
    }

    /**
     * Constructor para crear un nuevo DTO de pago con todos los atributos
     * 
     * @param metodoPago
     * @param claveIdempotencia
     * @param idPago
     * @param idCotizacion
     * @param monto
     * @param moneda
     * @param estado
     * @param referenciaPasarela
     * @param fecha
     * @param marcaTarjeta
     * @param ultimosCuatro
     * @param motivoRechazo
     */
    public PagoDTO(String metodoPago, String claveIdempotencia, Long idPago, Long idCotizacion, BigDecimal monto,
            String moneda, String estado, String referenciaPasarela, LocalDateTime fecha, String marcaTarjeta,
            String ultimosCuatro, String motivoRechazo) {
        this.metodoPago = metodoPago;
        this.claveIdempotencia = claveIdempotencia;
        this.idPago = idPago;
        this.idCotizacion = idCotizacion;
        this.monto = monto;
        this.moneda = moneda;
        this.estado = estado;
        this.referenciaPasarela = referenciaPasarela;
        this.fecha = fecha;
        this.marcaTarjeta = marcaTarjeta;
        this.ultimosCuatro = ultimosCuatro;
        this.motivoRechazo = motivoRechazo;
    }

    // Getters y Setters
    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getClaveIdempotencia() {
        return claveIdempotencia;
    }

    public void setClaveIdempotencia(String claveIdempotencia) {
        this.claveIdempotencia = claveIdempotencia;
    }

    public Long getIdPago() {
        return idPago;
    }

    public void setIdPago(Long idPago) {
        this.idPago = idPago;
    }

    public Long getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(Long idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getReferenciaPasarela() {
        return referenciaPasarela;
    }

    public void setReferenciaPasarela(String referenciaPasarela) {
        this.referenciaPasarela = referenciaPasarela;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
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

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    /** No incluye el método de pago ni la clave de idempotencia. */
    @Override
    public String toString() {
        return "PagoDTO [idPago=" + idPago + ", idCotizacion=" + idCotizacion + ", monto=" + monto + ", moneda="
                + moneda + ", estado=" + estado + ", referenciaPasarela=" + referenciaPasarela + "]";
    }
}