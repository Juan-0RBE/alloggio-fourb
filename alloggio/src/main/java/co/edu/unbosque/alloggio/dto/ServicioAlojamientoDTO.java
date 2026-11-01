package co.edu.unbosque.alloggio.dto;

public class ServicioAlojamientoDTO {

    private Long idServicio;
    private String nombre;
    private Double costo;
    private Boolean esAdicional;
    private Long idPublicacion;

    public ServicioAlojamientoDTO() {
    }

    /**
     * Constructor para la clase ServicioAlojamientoDTO.
     * 
     * @param idServicio
     * @param nombre
     * @param costo
     * @param esAdicional
     * @param idPublicacion
     */
    public ServicioAlojamientoDTO(Long idServicio, String nombre, Double costo, Boolean esAdicional,
            Long idPublicacion) {
        this.idServicio = idServicio;
        this.nombre = nombre;
        this.costo = costo;
        this.esAdicional = esAdicional;
        this.idPublicacion = idPublicacion;
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

    public Double getCosto() {
        return costo;
    }

    public void setCosto(Double costo) {
        this.costo = costo;
    }

    public Boolean getEsAdicional() {
        return esAdicional;
    }

    public void setEsAdicional(Boolean esAdicional) {
        this.esAdicional = esAdicional;
    }

    public Long getIdPublicacion() {
        return idPublicacion;
    }

    public void setIdPublicacion(Long idPublicacion) {
        this.idPublicacion = idPublicacion;
    }

}
