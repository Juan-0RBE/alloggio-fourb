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
@Table(name = "IMAGEN_ALOJAMIENTO")
public class ImagenAlojamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImagen;

    @Column(name = "URL_Imagen", length = 500, nullable = false)
    private String urlImagen; // URL de la imagen

    @Column(name = "Orden", nullable = false)
    private Integer orden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idPublicacion", nullable = false)
    private Publicacion publicacion;

    public ImagenAlojamiento() {
    }

    /**
     * Constructor para la clase ImagenAlojamiento.
     * 
     * @param idImagen
     * @param urlImagen
     * @param orden
     * @param publicacion
     */
    public ImagenAlojamiento(Long idImagen, String urlImagen, Integer orden, Publicacion publicacion) {
        this.idImagen = idImagen;
        this.urlImagen = urlImagen;
        this.orden = orden;
        this.publicacion = publicacion;
    }

    // Getters y Setters

    public Long getIdImagen() {
        return idImagen;
    }

    public void setIdImagen(Long idImagen) {
        this.idImagen = idImagen;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public void setPublicacion(Publicacion publicacion) {
        this.publicacion = publicacion;
    }

}