package co.edu.unbosque.alloggio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Parámetros de tarifa configurables de la plataforma. Se mantiene una
 * única fila con la tarifa de servicio (comisión de la plataforma) y el
 * descuento por estadía larga.
 */
@Entity
@Table(name = "PARAMETRO_TARIFA")
public class ParametroTarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idParametroTarifa;

    @Column(name = "Porcentaje_Servicio", nullable = false)
    private Double porcentajeServicio;

    @Column(name = "Porcentaje_Descuento", nullable = false)
    private Double porcentajeDescuento;

    @Column(name = "Noches_Minimas_Descuento", nullable = false)
    private Integer nochesMinimasDescuento;

    public ParametroTarifa() {
        this.porcentajeServicio = 0.0;
        this.porcentajeDescuento = 0.0;
        this.nochesMinimasDescuento = 1;
    }

    /**
     * Constructor para crear un nuevo parámetro de tarifa con todos los atributos
     * 
     * @param idParametroTarifa
     * @param porcentajeServicio
     * @param porcentajeDescuento
     * @param nochesMinimasDescuento
     */
    public ParametroTarifa(Long idParametroTarifa, Double porcentajeServicio, Double porcentajeDescuento,
            Integer nochesMinimasDescuento) {
        this.idParametroTarifa = idParametroTarifa;
        this.porcentajeServicio = porcentajeServicio;
        this.porcentajeDescuento = porcentajeDescuento;
        this.nochesMinimasDescuento = nochesMinimasDescuento;
    }

    // Getters y Setters
    public Long getIdParametroTarifa() {
        return idParametroTarifa;
    }

    public void setIdParametroTarifa(Long idParametroTarifa) {
        this.idParametroTarifa = idParametroTarifa;
    }

    public Double getPorcentajeServicio() {
        return porcentajeServicio;
    }

    public void setPorcentajeServicio(Double porcentajeServicio) {
        this.porcentajeServicio = porcentajeServicio;
    }

    public Double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(Double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public Integer getNochesMinimasDescuento() {
        return nochesMinimasDescuento;
    }

    public void setNochesMinimasDescuento(Integer nochesMinimasDescuento) {
        this.nochesMinimasDescuento = nochesMinimasDescuento;
    }
}