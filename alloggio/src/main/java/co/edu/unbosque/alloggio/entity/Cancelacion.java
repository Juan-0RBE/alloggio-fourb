package co.edu.unbosque.alloggio.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Cancelación de una reserva. Solo existe para las reservas
 * canceladas: registra quién canceló, cuándo, por qué y cuánto se devuelve
 * según la política de la publicación.
 */
@Entity
@Table(name = "CANCELACION")
public class Cancelacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCancelacion;

    @Column(name = "Fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "Motivo", length = 500, nullable = false)
    private String motivo;

    /** Valor que corresponde devolver según la política; puede ser cero. */
    @Column(name = "Valor_Devolucion", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorDevolucion;

    @Column(name = "Referencia_Reembolso", length = 100)
    private String referenciaReembolso;

    // Una reserva solo puede cancelarse una vez
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idReserva", nullable = false, unique = true)
    private Reserva reserva;

    // Usuario que realizó la cancelación
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    public Cancelacion() {
        this.fecha = LocalDateTime.now();
        this.valorDevolucion = BigDecimal.ZERO;
    }

    /**
     * Constructor para crear una nueva cancelación con todos los atributos
     * 
     * @param idCancelacion
     * @param fecha
     * @param motivo
     * @param valorDevolucion
     * @param referenciaReembolso
     * @param reserva
     * @param usuario
     */
    public Cancelacion(Long idCancelacion, LocalDateTime fecha, String motivo, BigDecimal valorDevolucion,
            String referenciaReembolso, Reserva reserva, Usuario usuario) {
        this.idCancelacion = idCancelacion;
        this.fecha = fecha;
        this.motivo = motivo;
        this.valorDevolucion = valorDevolucion;
        this.referenciaReembolso = referenciaReembolso;
        this.reserva = reserva;
        this.usuario = usuario;
    }

    // Getters y Setters
    public Long getIdCancelacion() {
        return idCancelacion;
    }

    public void setIdCancelacion(Long idCancelacion) {
        this.idCancelacion = idCancelacion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public BigDecimal getValorDevolucion() {
        return valorDevolucion;
    }

    public void setValorDevolucion(BigDecimal valorDevolucion) {
        this.valorDevolucion = valorDevolucion;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getReferenciaReembolso() {
        return referenciaReembolso;
    }

    public void setReferenciaReembolso(String referenciaReembolso) {
        this.referenciaReembolso = referenciaReembolso;
    }

}