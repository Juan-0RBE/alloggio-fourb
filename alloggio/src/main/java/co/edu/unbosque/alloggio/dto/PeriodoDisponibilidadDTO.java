package co.edu.unbosque.alloggio.dto;

import java.time.LocalDate;

import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad.TipoPeriodo;

public class PeriodoDisponibilidadDTO {

    private Long idPeriodoDisponibilidad;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private TipoPeriodo tipo;
    private Long idPublicacion;
    private Long idReserva;

    public PeriodoDisponibilidadDTO() {
    }

    /**
     * Constructor para la clase PeriodoDisponibilidadDTO.
     * 
     * @param idPeriodoDisponibilidad
     * @param fechaInicio
     * @param fechaFin
     * @param tipo
     * @param idPublicacion
     * @param idReserva
     */
    public PeriodoDisponibilidadDTO(Long idPeriodoDisponibilidad, LocalDate fechaInicio, LocalDate fechaFin,
            TipoPeriodo tipo, Long idPublicacion, Long idReserva) {
        this.idPeriodoDisponibilidad = idPeriodoDisponibilidad;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipo = tipo;
        this.idPublicacion = idPublicacion;
        this.idReserva = idReserva;
    }

    public Long getIdPeriodoDisponibilidad() {
        return idPeriodoDisponibilidad;
    }

    // Getters y Setters
    public void setIdPeriodoDisponibilidad(Long idPeriodoDisponibilidad) {
        this.idPeriodoDisponibilidad = idPeriodoDisponibilidad;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public TipoPeriodo getTipo() {
        return tipo;
    }

    public void setTipo(TipoPeriodo tipo) {
        this.tipo = tipo;
    }

    public Long getIdPublicacion() {
        return idPublicacion;
    }

    public void setIdPublicacion(Long idPublicacion) {
        this.idPublicacion = idPublicacion;
    }

    public Long getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Long idReserva) {
        this.idReserva = idReserva;
    }

}
