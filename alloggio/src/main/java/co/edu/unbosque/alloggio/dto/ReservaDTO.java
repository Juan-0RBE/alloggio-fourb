package co.edu.unbosque.alloggio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Representa el DTO de una reserva.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservaDTO {

    private Long idCotizacion;
    /**
     * Identificador del método de pago de la pasarela (pm_...), nunca un número de
     * tarjeta.
     */
    private String metodoPago;

    private String claveIdempotencia;

    // Datos de salida
    private Long idReserva;
    private Long idPublicacion;
    private LocalDate fechaLlegada;
    private LocalDate fechaSalida;
    private BigDecimal total;
    private String estado;
    private LocalDateTime fechaCreacion;

    private String motivoRechazo;

    public ReservaDTO() {
    }

    /**
     * Constructor para crear un nuevo DTO de reserva con todos los atributos
     * 
     * @param idCotizacion
     * @param metodoPago
     * @param claveIdempotencia
     * @param idReserva
     * @param idPublicacion
     * @param fechaLlegada
     * @param fechaSalida
     * @param total
     * @param estado
     * @param fechaCreacion
     * @param motivoRechazo
     */
    public ReservaDTO(Long idCotizacion, String metodoPago, String claveIdempotencia, Long idReserva,
            Long idPublicacion, LocalDate fechaLlegada, LocalDate fechaSalida, BigDecimal total, String estado,
            LocalDateTime fechaCreacion, String motivoRechazo) {
        this.idCotizacion = idCotizacion;
        this.metodoPago = metodoPago;
        this.claveIdempotencia = claveIdempotencia;
        this.idReserva = idReserva;
        this.idPublicacion = idPublicacion;
        this.fechaLlegada = fechaLlegada;
        this.fechaSalida = fechaSalida;
        this.total = total;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.motivoRechazo = motivoRechazo;
    }

    // Getters y Setters

    public Long getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(Long idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

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

    public Long getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Long idReserva) {
        this.idReserva = idReserva;
    }

    public Long getIdPublicacion() {
        return idPublicacion;
    }

    public void setIdPublicacion(Long idPublicacion) {
        this.idPublicacion = idPublicacion;
    }

    public LocalDate getFechaLlegada() {
        return fechaLlegada;
    }

    public void setFechaLlegada(LocalDate fechaLlegada) {
        this.fechaLlegada = fechaLlegada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
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
        return "ReservaDTO [idReserva=" + idReserva + ", idCotizacion=" + idCotizacion + ", idPublicacion="
                + idPublicacion + ", total=" + total + ", estado=" + estado + "]";
    }
}