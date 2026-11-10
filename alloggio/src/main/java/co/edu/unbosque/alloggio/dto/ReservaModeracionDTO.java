package co.edu.unbosque.alloggio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Reserva futura de una publicación, para la revisión manual del administrador.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservaModeracionDTO {
    private Long idReserva;
    private LocalDate fechaLlegada;
    private LocalDate fechaSalida;
    private BigDecimal total;
    private String nombreUsuarioHuesped;
    private String nombreHuesped;

    public ReservaModeracionDTO() {
    }

    public ReservaModeracionDTO(Long idReserva, LocalDate fechaLlegada, LocalDate fechaSalida, BigDecimal total,
            String nombreUsuarioHuesped, String nombreHuesped) {
        this.idReserva = idReserva;
        this.fechaLlegada = fechaLlegada;
        this.fechaSalida = fechaSalida;
        this.total = total;
        this.nombreUsuarioHuesped = nombreUsuarioHuesped;
        this.nombreHuesped = nombreHuesped;
    }

    // Getters y Setters

    public Long getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Long idReserva) {
        this.idReserva = idReserva;
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

    public String getNombreUsuarioHuesped() {
        return nombreUsuarioHuesped;
    }

    public void setNombreUsuarioHuesped(String nombreUsuarioHuesped) {
        this.nombreUsuarioHuesped = nombreUsuarioHuesped;
    }

    public String getNombreHuesped() {
        return nombreHuesped;
    }

    public void setNombreHuesped(String nombreHuesped) {
        this.nombreHuesped = nombreHuesped;
    }

}