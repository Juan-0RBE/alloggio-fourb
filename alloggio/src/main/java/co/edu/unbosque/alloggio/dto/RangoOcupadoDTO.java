package co.edu.unbosque.alloggio.dto;

import java.time.LocalDate;

/**
 * DTO público de un rango de fechas no disponible de un alojamiento. No
 * distingue si lo ocupa un bloqueo del anfitrión o una reserva y no expone la
 * reserva asociada. La fecha de fin es exclusiva (fecha de salida).
 */
public class RangoOcupadoDTO {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public RangoOcupadoDTO() {
    }

    /**
     * Constructor para la clase RangoOcupadoDTO.
     * 
     * @param fechaInicio
     * @param fechaFin
     */
    public RangoOcupadoDTO(LocalDate fechaInicio, LocalDate fechaFin) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    // Getters y Setters

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

}