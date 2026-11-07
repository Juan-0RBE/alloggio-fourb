package co.edu.unbosque.alloggio.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Cotización de un alojamiento para un rango de fechas. Guarda una copia de los
 * valores calculados (precio base, limpieza, servicio, descuento y total) para
 * que un cambio posterior en las tarifas no altere cotizaciones ya emitidas.
 */
@Entity
@Table(name = "COTIZACION")
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCotizacion;

    @Column(name = "Fecha_Llegada", nullable = false)
    private LocalDate fechaLlegada;

    /** Fecha de salida (fin exclusivo). */
    @Column(name = "Fecha_Salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(name = "Noches", nullable = false)
    private Integer noches;

    @Column(name = "Precio_Base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "Limpieza", nullable = false, precision = 12, scale = 2)
    private BigDecimal limpieza;

    @Column(name = "Servicio", nullable = false, precision = 12, scale = 2)
    private BigDecimal servicio;

    @Column(name = "Descuento", nullable = false, precision = 12, scale = 2)
    private BigDecimal descuento;

    /** Incluye los servicios adicionales agregados a la cotización. */
    @Column(name = "Total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    /** Fecha y hora hasta la cual la cotización puede usarse. */
    @Column(name = "Vigencia", nullable = false)
    private LocalDateTime vigencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario huesped;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idPublicacion", nullable = false)
    private Publicacion publicacion;

    // Servicios adicionales elegidos (tabla intermedia)
    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CotizacionServicio> servicios = new ArrayList<>();

    public Cotizacion() {
        this.noches = 0;
        this.precioBase = BigDecimal.ZERO;
        this.limpieza = BigDecimal.ZERO;
        this.servicio = BigDecimal.ZERO;
        this.descuento = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }

    /**
     * Constructor para crear una nueva cotización con todos los atributos
     * 
     * @param idCotizacion
     * @param fechaLlegada
     * @param fechaSalida
     * @param noches
     * @param precioBase
     * @param limpieza
     * @param servicio
     * @param descuento
     * @param total
     * @param vigencia
     * @param huesped
     * @param publicacion
     */
    public Cotizacion(Long idCotizacion, LocalDate fechaLlegada, LocalDate fechaSalida, Integer noches,
            BigDecimal precioBase, BigDecimal limpieza, BigDecimal servicio, BigDecimal descuento,
            BigDecimal total, LocalDateTime vigencia, Usuario huesped, Publicacion publicacion) {
        this.idCotizacion = idCotizacion;
        this.fechaLlegada = fechaLlegada;
        this.fechaSalida = fechaSalida;
        this.noches = noches;
        this.precioBase = precioBase;
        this.limpieza = limpieza;
        this.servicio = servicio;
        this.descuento = descuento;
        this.total = total;
        this.vigencia = vigencia;
        this.huesped = huesped;
        this.publicacion = publicacion;
    }

    // Getters y Setters
    public Long getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(Long idCotizacion) {
        this.idCotizacion = idCotizacion;
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

    public Integer getNoches() {
        return noches;
    }

    public void setNoches(Integer noches) {
        this.noches = noches;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public BigDecimal getLimpieza() {
        return limpieza;
    }

    public void setLimpieza(BigDecimal limpieza) {
        this.limpieza = limpieza;
    }

    public BigDecimal getServicio() {
        return servicio;
    }

    public void setServicio(BigDecimal servicio) {
        this.servicio = servicio;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getVigencia() {
        return vigencia;
    }

    public void setVigencia(LocalDateTime vigencia) {
        this.vigencia = vigencia;
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

    public List<CotizacionServicio> getServicios() {
        return servicios;
    }

    public void setServicios(List<CotizacionServicio> servicios) {
        this.servicios = servicios;
    }
}