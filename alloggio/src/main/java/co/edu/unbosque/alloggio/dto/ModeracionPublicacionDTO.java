package co.edu.unbosque.alloggio.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.TipoAlojamiento;

/**
 * DTO de moderación de publicaciones. Mezcla entrada (motivoBloqueo) y salida;
 * el listado llena solo el resumen y el detalle agrega descripción y reservas.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModeracionPublicacionDTO {
    private Long idPublicacion;
    private String titulo;
    private String ciudad;
    private String ubicacion;
    private String descripcion;
    private TipoAlojamiento tipo;
    private Integer capacidad;
    private Double precioNoche;
    private EstadoPublicacion estado;
    private String motivoBloqueo;
    private String nombreUsuarioAnfitrion;
    private String nombreAnfitrion;
    private Integer totalReservasFuturas;
    private List<ReservaModeracionDTO> reservasFuturas;

    public ModeracionPublicacionDTO() {
    }

    public ModeracionPublicacionDTO(Long idPublicacion, String titulo, String ciudad, String ubicacion,
            String descripcion, TipoAlojamiento tipo, Integer capacidad, Double precioNoche, EstadoPublicacion estado,
            String motivoBloqueo, String nombreUsuarioAnfitrion, String nombreAnfitrion, Integer totalReservasFuturas,
            List<ReservaModeracionDTO> reservasFuturas) {
        this.idPublicacion = idPublicacion;
        this.titulo = titulo;
        this.ciudad = ciudad;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.precioNoche = precioNoche;
        this.estado = estado;
        this.motivoBloqueo = motivoBloqueo;
        this.nombreUsuarioAnfitrion = nombreUsuarioAnfitrion;
        this.nombreAnfitrion = nombreAnfitrion;
        this.totalReservasFuturas = totalReservasFuturas;
        this.reservasFuturas = reservasFuturas;
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

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    public String getNombreUsuarioAnfitrion() {
        return nombreUsuarioAnfitrion;
    }

    public void setNombreUsuarioAnfitrion(String nombreUsuarioAnfitrion) {
        this.nombreUsuarioAnfitrion = nombreUsuarioAnfitrion;
    }

    public String getNombreAnfitrion() {
        return nombreAnfitrion;
    }

    public void setNombreAnfitrion(String nombreAnfitrion) {
        this.nombreAnfitrion = nombreAnfitrion;
    }

    public Integer getTotalReservasFuturas() {
        return totalReservasFuturas;
    }

    public void setTotalReservasFuturas(Integer totalReservasFuturas) {
        this.totalReservasFuturas = totalReservasFuturas;
    }

    public List<ReservaModeracionDTO> getReservasFuturas() {
        return reservasFuturas;
    }

    public void setReservasFuturas(List<ReservaModeracionDTO> reservasFuturas) {
        this.reservasFuturas = reservasFuturas;
    }

}