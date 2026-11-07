package co.edu.unbosque.alloggio.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Reserva de un alojamiento.
 */
@Entity
@Table(name = "RESERVA", uniqueConstraints = @UniqueConstraint(name = "UK_RESERVA_HUESPED_CLAVE", columnNames = {
        "idUsuario", "Clave_Idempotencia" }))
public class Reserva {

    public enum EstadoReserva {
        CONFIRMADA, CANCELADA, COMPLETADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReserva;

    @Column(name = "Fecha_Llegada", nullable = false)
    private LocalDate fechaLlegada;

    @Column(name = "Fecha_Salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(name = "Total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false, length = 20)
    private EstadoReserva estado;

    @Column(name = "Clave_Idempotencia", nullable = false, length = 100)
    private String claveIdempotencia;

    @Column(name = "Fecha_Creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario huesped;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idPublicacion", nullable = false)
    private Publicacion publicacion;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idCotizacion", nullable = false, unique = true)
    private Cotizacion cotizacion;

    public Reserva() {
        this.total = BigDecimal.ZERO;
        this.estado = EstadoReserva.CONFIRMADA;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Reserva(Long idReserva) {
        this();
        this.idReserva = idReserva;
    }

    /**
     * Constructor para crear una nueva reserva con todos los atributos
     * 
     * @param idReserva
     * @param fechaLlegada
     * @param fechaSalida
     * @param total
     * @param estado
     * @param claveIdempotencia
     * @param fechaCreacion
     * @param huesped
     * @param publicacion
     * @param cotizacion
     */
    public Reserva(Long idReserva, LocalDate fechaLlegada, LocalDate fechaSalida, BigDecimal total,
            EstadoReserva estado, String claveIdempotencia, LocalDateTime fechaCreacion, Usuario huesped,
            Publicacion publicacion, Cotizacion cotizacion) {
        this.idReserva = idReserva;
        this.fechaLlegada = fechaLlegada;
        this.fechaSalida = fechaSalida;
        this.total = total;
        this.estado = estado;
        this.claveIdempotencia = claveIdempotencia;
        this.fechaCreacion = fechaCreacion;
        this.huesped = huesped;
        this.publicacion = publicacion;
        this.cotizacion = cotizacion;
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

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public String getClaveIdempotencia() {
        return claveIdempotencia;
    }

    public void setClaveIdempotencia(String claveIdempotencia) {
        this.claveIdempotencia = claveIdempotencia;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Usuario getHuesped() {
        return huesped;
    }

    public void setHuesped(Usuario huesped) {
        this.huesped = huesped;
    }

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public void setPublicacion(Publicacion publicacion) {
        this.publicacion = publicacion;
    }

    public Cotizacion getCotizacion() {
        return cotizacion;
    }

    public void setCotizacion(Cotizacion cotizacion) {
        this.cotizacion = cotizacion;
    }
}