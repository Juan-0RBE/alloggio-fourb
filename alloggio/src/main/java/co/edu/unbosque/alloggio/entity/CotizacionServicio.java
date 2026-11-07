package co.edu.unbosque.alloggio.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Servicio adicional elegido en una cotización. Guarda el costo aplicado en el
 * momento de agregarlo, aunque el anfitrión cambie después el costo del
 * servicio.
 */
@Entity
@Table(name = "COTIZACION_SERVICIO")
public class CotizacionServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCotizacionServicio;

    @Column(name = "Costo_Aplicado", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoAplicado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idCotizacion", nullable = false)
    private Cotizacion cotizacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idServicio", nullable = false)
    private ServicioAlojamiento servicio;

    public CotizacionServicio() {
        this.costoAplicado = BigDecimal.ZERO;
    }

    /**
     * Constructor para crear un nuevo servicio de cotización con todos los
     * atributos
     * 
     * @param idCotizacionServicio
     * @param costoAplicado
     * @param cotizacion
     * @param servicio
     */
    public CotizacionServicio(Long idCotizacionServicio, BigDecimal costoAplicado, Cotizacion cotizacion,
            ServicioAlojamiento servicio) {
        this.idCotizacionServicio = idCotizacionServicio;
        this.costoAplicado = costoAplicado;
        this.cotizacion = cotizacion;
        this.servicio = servicio;
    }

    // Getters y Setters
    public Long getIdCotizacionServicio() {
        return idCotizacionServicio;
    }

    public void setIdCotizacionServicio(Long idCotizacionServicio) {
        this.idCotizacionServicio = idCotizacionServicio;
    }

    public BigDecimal getCostoAplicado() {
        return costoAplicado;
    }

    public void setCostoAplicado(BigDecimal costoAplicado) {
        this.costoAplicado = costoAplicado;
    }

    public Cotizacion getCotizacion() {
        return cotizacion;
    }

    public void setCotizacion(Cotizacion cotizacion) {
        this.cotizacion = cotizacion;
    }

    public ServicioAlojamiento getServicio() {
        return servicio;
    }

    public void setServicio(ServicioAlojamiento servicio) {
        this.servicio = servicio;
    }
}