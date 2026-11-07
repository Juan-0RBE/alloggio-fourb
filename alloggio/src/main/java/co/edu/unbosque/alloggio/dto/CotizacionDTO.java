package co.edu.unbosque.alloggio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa el DTO para la cotización. Al cotizar, el cliente solo envía
 * idPublicacion, fechaLlegada y fechaSalida
 * Los demás campos los calcula el sistema.
 */
public class CotizacionDTO {

    private Long idCotizacion;
    private Long idPublicacion;
    private LocalDate fechaLlegada;
    private LocalDate fechaSalida;
    private Integer noches;
    private BigDecimal precioBase;
    private BigDecimal limpieza;
    private BigDecimal servicio;
    private BigDecimal descuento;
    private BigDecimal serviciosAdicionales;
    private BigDecimal total;
    private LocalDateTime vigencia;
    private List<CotizacionServicioDTO> servicios = new ArrayList<>();

    public CotizacionDTO() {
    }

    /**
     * Constructor para crear un nuevo DTO de cotización con todos los atributos
     * 
     * @param idCotizacion
     * @param idPublicacion
     * @param fechaLlegada
     * @param fechaSalida
     * @param noches
     * @param precioBase
     * @param limpieza
     * @param servicio
     * @param descuento
     * @param serviciosAdicionales
     * @param total
     * @param vigencia
     * @param servicios
     */
    public CotizacionDTO(Long idCotizacion, Long idPublicacion, LocalDate fechaLlegada, LocalDate fechaSalida,
            Integer noches, BigDecimal precioBase, BigDecimal limpieza, BigDecimal servicio, BigDecimal descuento,
            BigDecimal serviciosAdicionales, BigDecimal total, LocalDateTime vigencia,
            List<CotizacionServicioDTO> servicios) {
        this.idCotizacion = idCotizacion;
        this.idPublicacion = idPublicacion;
        this.fechaLlegada = fechaLlegada;
        this.fechaSalida = fechaSalida;
        this.noches = noches;
        this.precioBase = precioBase;
        this.limpieza = limpieza;
        this.servicio = servicio;
        this.descuento = descuento;
        this.serviciosAdicionales = serviciosAdicionales;
        this.total = total;
        this.vigencia = vigencia;
        this.servicios = servicios;
    }

    // Getters y Setters
    public Long getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(Long idCotizacion) {
        this.idCotizacion = idCotizacion;
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

    public BigDecimal getServiciosAdicionales() {
        return serviciosAdicionales;
    }

    public void setServiciosAdicionales(BigDecimal serviciosAdicionales) {
        this.serviciosAdicionales = serviciosAdicionales;
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

    public List<CotizacionServicioDTO> getServicios() {
        return servicios;
    }

    public void setServicios(List<CotizacionServicioDTO> servicios) {
        this.servicios = servicios;
    }
}