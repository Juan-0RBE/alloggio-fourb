package co.edu.unbosque.alloggio.dto;

import co.edu.unbosque.alloggio.entity.Publicacion.TipoAlojamiento;

/**
 * DTO público con los datos resumidos de un alojamiento para la lista de
 * resultados de búsqueda. No expone datos internos como el anfitrión, el estado
 * o el motivo de bloqueo.
 */
public class AlojamientoResumenDTO {

    private Long idPublicacion;
    private String titulo;
    private String ciudad;
    private TipoAlojamiento tipo;
    private Integer capacidad;
    private Double precioNoche;
    private ImagenAlojamientoDTO imagenPrincipal;

    public AlojamientoResumenDTO() {
    }

    /**
     * Constructor para la clase AlojamientoResumenDTO.
     * 
     * @param idPublicacion
     * @param titulo
     * @param ciudad
     * @param tipo
     * @param capacidad
     * @param precioNoche
     * @param imagenPrincipal primera imagen según el orden (puede ser nula)
     */
    public AlojamientoResumenDTO(Long idPublicacion, String titulo, String ciudad, TipoAlojamiento tipo,
            Integer capacidad, Double precioNoche, ImagenAlojamientoDTO imagenPrincipal) {
        this.idPublicacion = idPublicacion;
        this.titulo = titulo;
        this.ciudad = ciudad;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.precioNoche = precioNoche;
        this.imagenPrincipal = imagenPrincipal;
    }

    // Getters y Setters

    public Long getIdPublicacion() {
        return idPublicacion;
    }

    public void setIdPublicacion(Long idPublicacion) {
        this.idPublicacion = idPublicacion;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public TipoAlojamiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoAlojamiento tipo) {
        this.tipo = tipo;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public Double getPrecioNoche() {
        return precioNoche;
    }

    public void setPrecioNoche(Double precioNoche) {
        this.precioNoche = precioNoche;
    }

    public ImagenAlojamientoDTO getImagenPrincipal() {
        return imagenPrincipal;
    }

    public void setImagenPrincipal(ImagenAlojamientoDTO imagenPrincipal) {
        this.imagenPrincipal = imagenPrincipal;
    }

}