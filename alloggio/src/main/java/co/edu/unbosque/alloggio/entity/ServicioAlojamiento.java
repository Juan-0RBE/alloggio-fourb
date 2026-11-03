package co.edu.unbosque.alloggio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "SERVICIO_ALOJAMIENTO")
public class ServicioAlojamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idServicio;

    @Column(name = "Nombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "Costo")
    private Double costo; // nulo si es incluido

    @Column(name = "Adicional", nullable = false)
    private Boolean adicional; // sí/no

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idPublicacion", nullable = false)
    private Publicacion publicacion;

    public ServicioAlojamiento() {
    }

    /**
     * Constructor para crear un nuevo servicio de alojamiento con todos los
     * atributos
     * 
     * @param idServicio
     * @param nombre
     * @param costo
     * @param adicional
     * @param publicacion
     */
    public ServicioAlojamiento(Long idServicio, String nombre, Double costo, Boolean adicional,
            Publicacion publicacion) {
        this.idServicio = idServicio;
        this.nombre = nombre;
        this.costo = costo;
        this.adicional = adicional;
        this.publicacion = publicacion;
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

    public Boolean getAdicional() {
        return adicional;
    }

    public void setAdicional(Boolean adicional) {
        this.adicional = adicional;
    }

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public void setPublicacion(Publicacion publicacion) {
        this.publicacion = publicacion;
    }
}