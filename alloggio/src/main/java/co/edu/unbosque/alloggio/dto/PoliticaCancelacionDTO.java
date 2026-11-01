package co.edu.unbosque.alloggio.dto;

public class PoliticaCancelacionDTO {

    private Long idPolitica;
    private String nombre;
    private Double porcentajeDevolucion;
    private String condiciones;
    private Integer plazoCancelacion;

    public PoliticaCancelacionDTO() {
    }

    /**
     * Constructor para la clase PoliticaCancelacionDTO.
     * 
     * @param idPolitica
     * @param nombre
     * @param porcentajeDevolucion
     * @param condiciones
     * @param plazoCancelacion
     */
    public PoliticaCancelacionDTO(Long idPolitica, String nombre, Double porcentajeDevolucion, String condiciones,
            Integer plazoCancelacion) {
        this.idPolitica = idPolitica;
        this.nombre = nombre;
        this.porcentajeDevolucion = porcentajeDevolucion;
        this.condiciones = condiciones;
        this.plazoCancelacion = plazoCancelacion;
    }

    // Getters y Setters

    public Long getIdPolitica() {
        return idPolitica;
    }

    public void setIdPolitica(Long idPolitica) {
        this.idPolitica = idPolitica;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPorcentajeDevolucion() {
        return porcentajeDevolucion;
    }

    public void setPorcentajeDevolucion(Double porcentajeDevolucion) {
        this.porcentajeDevolucion = porcentajeDevolucion;
    }

    public String getCondiciones() {
        return condiciones;
    }

    public void setCondiciones(String condiciones) {
        this.condiciones = condiciones;
    }

    public Integer getPlazoCancelacion() {
        return plazoCancelacion;
    }

    public void setPlazoCancelacion(Integer plazoCancelacion) {
        this.plazoCancelacion = plazoCancelacion;
    }

}
