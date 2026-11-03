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
@Table(name = "REGLA_ALOJAMIENTO")
public class ReglaAlojamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRegla;

    @Column(name = "descripcion", length = 255, nullable = false)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idPublicacion", nullable = false)
    private Publicacion publicacion;

    public ReglaAlojamiento() {
    }

    /**
     * Constructor para crear una nueva regla de alojamiento con todos los atributos
     * 
     * @param idRegla
     * @param descripcion
     * @param publicacion
     */
    public ReglaAlojamiento(Long idRegla, String descripcion, Publicacion publicacion) {
        this.idRegla = idRegla;
        this.descripcion = descripcion;
        this.publicacion = publicacion;
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

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public void setPublicacion(Publicacion publicacion) {
        this.publicacion = publicacion;
    }
}