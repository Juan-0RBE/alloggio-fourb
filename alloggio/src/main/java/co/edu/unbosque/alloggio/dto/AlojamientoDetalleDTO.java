package co.edu.unbosque.alloggio.dto;

import java.util.List;

import co.edu.unbosque.alloggio.entity.Publicacion.TipoAlojamiento;

/**
 * DTO público con el detalle completo de un alojamiento: descripción,
 * servicios, reglas, imágenes, ubicación textual, calendario y precio, además
 * de
 * la política de cancelación. No expone datos internos como el anfitrión, el
 * estado o el motivo de bloqueo.
 */
public class AlojamientoDetalleDTO {

    private Long idPublicacion;
    private String titulo;
    private String descripcion;
    private String ubicacion;
    private String ciudad;
    private TipoAlojamiento tipo;
    private Integer capacidad;
    private Double precioNoche;
    private PoliticaCancelacionDTO politicaCancelacion;
    private List<ServicioAlojamientoDTO> servicios;
    private List<ReglaAlojamientoDTO> reglas;
    private List<ImagenAlojamientoDTO> imagenes;
    private List<RangoOcupadoDTO> calendarioOcupado;

    public AlojamientoDetalleDTO() {
    }

    /**
     * Constructor para la clase AlojamientoDetalleDTO.
     * 
     * @param idPublicacion
     * @param titulo
     * @param descripcion
     * @param ubicacion
     * @param ciudad
     * @param tipo
     * @param capacidad
     * @param precioNoche
     * @param politicaCancelacion
     * @param servicios
     * @param reglas
     * @param imagenes
     * @param calendarioOcupado   rangos no disponibles desde hoy en adelante
     */
    public AlojamientoDetalleDTO(Long idPublicacion, String titulo, String descripcion, String ubicacion,
            String ciudad, TipoAlojamiento tipo, Integer capacidad, Double precioNoche,
            PoliticaCancelacionDTO politicaCancelacion, List<ServicioAlojamientoDTO> servicios,
            List<ReglaAlojamientoDTO> reglas, List<ImagenAlojamientoDTO> imagenes,
            List<RangoOcupadoDTO> calendarioOcupado) {
        this.idPublicacion = idPublicacion;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.ciudad = ciudad;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.precioNoche = precioNoche;
        this.politicaCancelacion = politicaCancelacion;
        this.servicios = servicios;
        this.reglas = reglas;
        this.imagenes = imagenes;
        this.calendarioOcupado = calendarioOcupado;
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

    public PoliticaCancelacionDTO getPoliticaCancelacion() {
        return politicaCancelacion;
    }

    public void setPoliticaCancelacion(PoliticaCancelacionDTO politicaCancelacion) {
        this.politicaCancelacion = politicaCancelacion;
    }

    public List<ServicioAlojamientoDTO> getServicios() {
        return servicios;
    }

    public void setServicios(List<ServicioAlojamientoDTO> servicios) {
        this.servicios = servicios;
    }

    public List<ReglaAlojamientoDTO> getReglas() {
        return reglas;
    }

    public void setReglas(List<ReglaAlojamientoDTO> reglas) {
        this.reglas = reglas;
    }

    public List<ImagenAlojamientoDTO> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<ImagenAlojamientoDTO> imagenes) {
        this.imagenes = imagenes;
    }

    public List<RangoOcupadoDTO> getCalendarioOcupado() {
        return calendarioOcupado;
    }

    public void setCalendarioOcupado(List<RangoOcupadoDTO> calendarioOcupado) {
        this.calendarioOcupado = calendarioOcupado;
    }

}