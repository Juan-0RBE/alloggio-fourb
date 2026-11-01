package co.edu.unbosque.alloggio.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Clase que representa un período de disponibilidad en el sistema de
 * alojamiento.
 * 
 * PeriodoDisponibilidad
 */
@Entity
@Table(name = "PERIODO_DISPONIBILIDAD")
public class PeriodoDisponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPeriodoDisponibilidad;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    private TipoPeriodo tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idPublicacion", nullable = false)
    private Publicacion publicacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "idReserva")
    private Reserva reserva;

    public enum TipoPeriodo {
        BLOQUEADO, RESERVADO
    }

    public PeriodoDisponibilidad() {
        this.tipo = TipoPeriodo.BLOQUEADO;
    }

    public PeriodoDisponibilidad(Long idPeriodoDisponibilidad, LocalDate fechaInicio, LocalDate fechaFin,
            TipoPeriodo tipo, Publicacion publicacion, Reserva reserva) {
        this.idPeriodoDisponibilidad = idPeriodoDisponibilidad;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipo = tipo;
        this.publicacion = publicacion;
        this.reserva = reserva;
    }

    public Long getIdPeriodoDisponibilidad() {
        return idPeriodoDisponibilidad;
    }

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

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public void setPublicacion(Publicacion publicacion) {
        this.publicacion = publicacion;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

}
