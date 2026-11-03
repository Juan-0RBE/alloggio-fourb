package co.edu.unbosque.alloggio.dto;

public class ImagenAlojamientoDTO {

    /*
     * @Id
     * 
     * @GeneratedValue(strategy = GenerationType.IDENTITY)
     * private Long idImagen;
     * 
     * @Column(name = "Referencia", length = 500, nullable = false)
     * private String referencia; // URL de la imagen
     * 
     * @Column(name = "Orden", nullable = false)
     * private Integer orden;
     * 
     * @ManyToOne(fetch = FetchType.LAZY, optional = false)
     * 
     * @JoinColumn(name = "idPublicacion", nullable = false)
     * private Publicacion publicacion;
     * 
     * 
     */

    private Long idImagen;
    private String urlImagen;
    private Integer orden;
    private Long idPublicacion;

    public ImagenAlojamientoDTO() {
    }

    /**
     * Constructor para la clase ImagenAlojamientoDTO.
     * 
     * @param idImagen
     * @param urlImagen
     * @param orden
     * @param idPublicacion
     */
    public ImagenAlojamientoDTO(Long idImagen, String urlImagen, Integer orden, Long idPublicacion) {
        this.idImagen = idImagen;
        this.urlImagen = urlImagen;
        this.orden = orden;
        this.idPublicacion = idPublicacion;
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

    public Long getIdPublicacion() {
        return idPublicacion;
    }

    public void setIdPublicacion(Long idPublicacion) {
        this.idPublicacion = idPublicacion;
    }

}
