package co.edu.unbosque.alloggio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "PUBLICACION")
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPublicacion;

    @Column(name = "Titulo", length = 100, nullable = false)
    private String titulo;

    @Column(name = "Descripcion", length = 500, nullable = false)
    private String descripcion;

    @Column(name = "Ubicacion", length = 100, nullable = false)
    private String ubicacion;

    @Column(name = "Ciudad", length = 50, nullable = false)
    private String ciudad;

    @Enumerated(EnumType.STRING)
    @Column(name = "Tipo", length = 50, nullable = false)
    private TipoAlojamiento tipo;

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Column(name = "Precio_Noche", nullable = false)
    private Double precioNoche;

    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false)
    private EstadoPublicacion estado;

    @Column(name = "Motivo_Bloqueo", length = 500)
    private String motivoBloqueo;

    // Relación con Usuario (Anfitrión)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario anfitrion;

    // Relación con PoliticaCancelacion
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idPoliticaCancelacion", nullable = false)
    private PoliticaCancelacion politicaCancelacion;

    // Definición de los Enums (pueden ir aquí mismo o en archivos separados)
    public enum TipoAlojamiento {
        CASA, APARTAMENTO, HABITACION, CABAÑA
    }

    public enum EstadoPublicacion {
        BORRADOR, ACTIVA, PAUSADA, BLOQUEADA
    }

    public Publicacion() {
        this.estado = EstadoPublicacion.BORRADOR; // Valor por defecto común
    }

    /**
     * Constructor para crear una nueva publicación con todos los atributos
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
     * @param anfitrion
     * @param politicaCancelacion
     */
    public Publicacion(Long idPublicacion, String titulo, String descripcion, String ubicacion, String ciudad,
            TipoAlojamiento tipo, Integer capacidad, Double precioNoche, EstadoPublicacion estado, String motivoBloqueo,
            Usuario anfitrion, PoliticaCancelacion politicaCancelacion) {
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
        this.anfitrion = anfitrion;
        this.politicaCancelacion = politicaCancelacion;
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

    public Usuario getAnfitrion() {
        return anfitrion;
    }

    public void setAnfitrion(Usuario anfitrion) {
        this.anfitrion = anfitrion;
    }

    public PoliticaCancelacion getPoliticaCancelacion() {
        return politicaCancelacion;
    }

    public void setPoliticaCancelacion(PoliticaCancelacion politicaCancelacion) {
        this.politicaCancelacion = politicaCancelacion;
    }
}