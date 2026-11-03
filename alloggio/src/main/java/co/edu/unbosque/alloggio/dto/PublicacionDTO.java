package co.edu.unbosque.alloggio.dto;

import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.TipoAlojamiento;

/**
 * Representa el DTO para la entidad Publicacion.
 */
public class PublicacionDTO {

    private Long idPublicacion;
    private String titulo;
    private String descripcion;
    private String ubicacion;
    private String ciudad;
    private TipoAlojamiento tipo;
    private Integer capacidad;
    private Double precioNoche;
    private EstadoPublicacion estado;
    private String motivoBloqueo;

    // IDs para las relaciones con Usuario (anfitrión) y PoliticaCancelacion
    private Long idAnfitrion;
    private Long idPoliticaCancelacion;

    public PublicacionDTO() {
    }

    /**
     * Constructor para crear un nuevo DTO de publicación con todos los atributos
     * 
     * @param idPublicacion
     * @param titulo
     * @param descripcion
     * @param ubicacion
     * @param ciudad
     * @param tipo
     * @param capacidad
     * @param precioNoche
     * @param estado
     * @param motivoBloqueo
     * @param idAnfitrion
     * @param idPoliticaCancelacion
     */
    public PublicacionDTO(Long idPublicacion, String titulo, String descripcion, String ubicacion, String ciudad,
            TipoAlojamiento tipo, Integer capacidad, Double precioNoche, EstadoPublicacion estado, String motivoBloqueo,
            Long idAnfitrion, Long idPoliticaCancelacion) {
        this.idPublicacion = idPublicacion;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.ciudad = ciudad;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.precioNoche = precioNoche;
        this.estado = estado;
        this.motivoBloqueo = motivoBloqueo;
        this.idAnfitrion = idAnfitrion;
        this.idPoliticaCancelacion = idPoliticaCancelacion;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
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

    public EstadoPublicacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoPublicacion estado) {
        this.estado = estado;
    }

    public String getMotivoBloqueo() {
        return motivoBloqueo;
    }

    public void setMotivoBloqueo(String motivoBloqueo) {
        this.motivoBloqueo = motivoBloqueo;
    }

    public Long getIdAnfitrion() {
        return idAnfitrion;
    }

    public void setIdAnfitrion(Long idAnfitrion) {
        this.idAnfitrion = idAnfitrion;
    }

    public Long getIdPoliticaCancelacion() {
        return idPoliticaCancelacion;
    }

    public void setIdPoliticaCancelacion(Long idPoliticaCancelacion) {
        this.idPoliticaCancelacion = idPoliticaCancelacion;
    }

    @Override
    public String toString() {
        return "PublicacionDTO [idPublicacion=" + idPublicacion + ", titulo=" + titulo + ", ciudad=" + ciudad
                + ", tipo=" + tipo + ", precioNoche=" + precioNoche + ", estado=" + estado + "]";
    }
}