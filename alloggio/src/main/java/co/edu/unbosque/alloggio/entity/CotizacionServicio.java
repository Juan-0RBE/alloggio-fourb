package co.edu.unbosque.alloggio.entity;

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

    @Column(name = "Costo_Aplicado", nullable = false)
    private Double costoAplicado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idCotizacion", nullable = false)
    private Cotizacion cotizacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idServicio", nullable = false)
    private ServicioAlojamiento servicio;

    public CotizacionServicio() {
        this.costoAplicado = 0.0;
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
    public CotizacionServicio(Long idCotizacionServicio, Double costoAplicado, Cotizacion cotizacion,
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

    public Double getCostoAplicado() {
        return costoAplicado;
    }

    public void setCostoAplicado(Double costoAplicado) {
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