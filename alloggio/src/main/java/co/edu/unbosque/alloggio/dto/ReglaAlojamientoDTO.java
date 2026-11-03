package co.edu.unbosque.alloggio.dto;

public class ReglaAlojamientoDTO {

    private Long idRegla;
    private String descripcion;
    private Long idPublicacion;

    public ReglaAlojamientoDTO() {
    }

    /**
     * Constructor para crear una nueva regla de alojamiento con todos los atributos
     * 
     * @param idRegla
     * @param descripcion
     * @param idPublicacion
     */
    public ReglaAlojamientoDTO(Long idRegla, String descripcion, Long idPublicacion) {
        this.idRegla = idRegla;
        this.descripcion = descripcion;
        this.idPublicacion = idPublicacion;
    }

    // Getters y Setters

    public Long getIdRegla() {
        return idRegla;
    }

    public void setIdRegla(Long idRegla) {
        this.idRegla = idRegla;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getIdPublicacion() {
        return idPublicacion;
    }

    public void setIdPublicacion(Long idPublicacion) {
        this.idPublicacion = idPublicacion;
    }

}
