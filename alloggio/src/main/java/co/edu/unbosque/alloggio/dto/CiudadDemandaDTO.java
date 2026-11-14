package co.edu.unbosque.alloggio.dto;

import java.math.BigDecimal;

/**
 * Demanda de una ciudad en el dashboard: cuántas reservas (no canceladas) tiene
 * y cuánto suman.
 */
public class CiudadDemandaDTO {

    private String ciudad;
    private long reservas;
    private BigDecimal ingresos;

    public CiudadDemandaDTO() {
        this.reservas = 0;
        this.ingresos = BigDecimal.ZERO;
    }

    /**
     * Constructor con todos los atributos.
     * 
     * @param ciudad
     * @param reservas
     * @param ingresos
     */
    public CiudadDemandaDTO(String ciudad, long reservas, BigDecimal ingresos) {
        this.ciudad = ciudad;
        this.reservas = reservas;
        this.ingresos = ingresos;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public long getReservas() {
        return reservas;
    }

    public void setReservas(long reservas) {
        this.reservas = reservas;
    }

    public BigDecimal getIngresos() {
        return ingresos;
    }

    public void setIngresos(BigDecimal ingresos) {
        this.ingresos = ingresos;
    }
}