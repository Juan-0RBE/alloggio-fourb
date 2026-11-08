package co.edu.unbosque.alloggio.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO de reseña.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResenaDTO {

    private Long idResena;
    private Integer calificacion;
    private String comentario;
    private LocalDateTime fecha;
    private Boolean visible;
    private String autor;

    // Solo de entrada
    private Long idReserva;
    private Boolean confirmar;

    public ResenaDTO() {
    }

    /**
     * Constructor para la clase ResenaDTO.
     * 
     * @param idResena
     * @param calificacion
     * @param comentario
     * @param fecha
     * @param visible
     * @param autor
     * @param idReserva
     * @param confirma
     */
    public ResenaDTO(Long idResena, Integer calificacion, String comentario, LocalDateTime fecha, Boolean visible,
            String autor, Long idReserva, Boolean confirmar) {
        this.idResena = idResena;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fecha = fecha;
        this.visible = visible;
        this.autor = autor;
        this.idReserva = idReserva;
        this.confirmar = confirmar;
    }

    // Getters y Setters

    public Long getIdResena() {
        return idResena;
    }

    public void setIdResena(Long idResena) {
        this.idResena = idResena;
    }

    public Integer getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Integer calificacion) {
        this.calificacion = calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public Long getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Long idReserva) {
        this.idReserva = idReserva;
    }

    public Boolean getConfirmar() {
        return confirmar;
    }

    public void setConfirmar(Boolean confirmar) {
        this.confirmar = confirmar;
    }

    @Override
    public String toString() {
        return "ResenaDTO [idResena=" + idResena + ", calificacion=" + calificacion + ", fecha=" + fecha
                + ", visible=" + visible + ", autor=" + autor + ", idReserva=" + idReserva + "]";
    }
}