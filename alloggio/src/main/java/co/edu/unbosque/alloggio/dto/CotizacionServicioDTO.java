package co.edu.unbosque.alloggio.dto;

/**
 * Representa un servicio adicional incluido en una cotización.
 */
public class CotizacionServicioDTO {

    private Long idServicio;
    private String nombre;
    private Double costoAplicado;

    public CotizacionServicioDTO() {
    }

    /**
     * Constructor para crear un nuevo DTO de servicio de cotización con todos los
     * atributos
     * 
     * @param idServicio
     * @param nombre
     * @param costoAplicado
     */
    public CotizacionServicioDTO(Long idServicio, String nombre, Double costoAplicado) {
        this.idServicio = idServicio;
        this.nombre = nombre;
        this.costoAplicado = costoAplicado;
    }

    // Getters y Setters
    public Long getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Long idServicio) {
        this.idServicio = idServicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getCostoAplicado() {
        return costoAplicado;
    }

    public void setCostoAplicado(Double costoAplicado) {
        this.costoAplicado = costoAplicado;
    }
}