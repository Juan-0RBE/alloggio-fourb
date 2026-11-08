package co.edu.unbosque.alloggio.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Representa el DTO de una cancelación. Al cancelar, el cliente solo envía el
 * motivo, los demás campos los calcula el sistema.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CancelacionDTO {

    // Dato de entrada
    private String motivo;

    // Datos de salida
    private Long idCancelacion;
    private Long idReserva;
    private LocalDateTime fecha;
    private BigDecimal valorDevolucion;
    /** Solo si hubo devolución en la pasarela. */
    private String referenciaReembolso;
    /** Estado final de la reserva (CANCELADA). */
    private String estadoReserva;

    public CancelacionDTO() {
    }

    /**
     * Constructor para crear un nuevo DTO de cancelación con todos los atributos
     * 
     * @param motivo
     * @param idCancelacion
     * @param idReserva
     * @param fecha
     * @param valorDevolucion
     * @param estadoReserva
     * @param referenciaReembolso
     */
    public CancelacionDTO(String motivo, Long idCancelacion, Long idReserva, LocalDateTime fecha,
            BigDecimal valorDevolucion, String estadoReserva, String referenciaReembolso) {
        this.motivo = motivo;
        this.idCancelacion = idCancelacion;
        this.idReserva = idReserva;
        this.fecha = fecha;
        this.valorDevolucion = valorDevolucion;
        this.estadoReserva = estadoReserva;
        this.referenciaReembolso = referenciaReembolso;
    }

    // Getters y Setters
    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Long getIdCancelacion() {
        return idCancelacion;
    }

    public void setIdCancelacion(Long idCancelacion) {
        this.idCancelacion = idCancelacion;
    }

    public Long getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Long idReserva) {
        this.idReserva = idReserva;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getValorDevolucion() {
        return valorDevolucion;
    }

    public void setValorDevolucion(BigDecimal valorDevolucion) {
        this.valorDevolucion = valorDevolucion;
    }

    public String getEstadoReserva() {
        return estadoReserva;
    }

    public void setEstadoReserva(String estadoReserva) {
        this.estadoReserva = estadoReserva;
    }

    public String getReferenciaReembolso() {
        return referenciaReembolso;
    }

    public void setReferenciaReembolso(String referenciaReembolso) {
        this.referenciaReembolso = referenciaReembolso;
    }
}