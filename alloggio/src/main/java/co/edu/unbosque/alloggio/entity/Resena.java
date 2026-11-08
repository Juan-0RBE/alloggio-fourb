package co.edu.unbosque.alloggio.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Reseña y calificación que un huésped deja sobre una estadía completada.
 */
@Entity
@Table(name = "RESENA")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idResena;

    @Column(name = "Calificacion", nullable = false)
    private Integer calificacion;

    @Column(name = "Comentario", length = 1000, nullable = false)
    private String comentario;

    @Column(name = "Fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "Visible", nullable = false)
    private Boolean visible;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idReserva", nullable = false, unique = true)
    private Reserva reserva;

    public Resena() {
        this.fecha = LocalDateTime.now();
        this.visible = true;
    }

    public Resena(Long idResena) {
        this();
        this.idResena = idResena;
    }

    /**
     * Constructor para crear una nueva reseña con todos los atributos.
     * 
     * @param idResena
     * @param calificacion entero de 1 a 5
     * @param comentario
     * @param fecha
     * @param visible
     * @param reserva
     */
    public Resena(Long idResena, Integer calificacion, String comentario, LocalDateTime fecha, Boolean visible,
            Reserva reserva) {
        this.idResena = idResena;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fecha = fecha;
        this.visible = visible;
        this.reserva = reserva;
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

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }
}