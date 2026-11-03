package co.edu.unbosque.alloggio.entity;

import java.time.LocalDateTime;

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

/**
 * Entidad que representa una notificación dirigida a un usuario.
 */
@Entity
@Table(name = "NOTIFICACION")
public class Notificacion {

    public enum TipoNotificacion {
        RESERVA, PAGO, SISTEMA, ALERTA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_Notificacion")
    private Long idNotificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "Tipo_Notificacion", length = 20, nullable = false)
    private TipoNotificacion tipoNotificacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Id_Usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "Mensaje", length = 500, nullable = false)
    private String mensaje;

    @Column(name = "Fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "Esta_Leida", nullable = false)
    private Boolean estaLeida;

    public Notificacion() {
    }

    /**
     * Crea una notificación nueva: la fecha se asigna en el servidor y nace sin
     * leer.
     * 
     * @param usuario
     * @param tipoNotificacion
     * @param mensaje
     */
    public Notificacion(Usuario usuario, TipoNotificacion tipoNotificacion, String mensaje) {
        this.usuario = usuario;
        this.tipoNotificacion = tipoNotificacion;
        this.mensaje = mensaje;
        this.fecha = LocalDateTime.now();
        this.estaLeida = false;
    }

    // Getters y Setters

    public Long getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(Long idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public TipoNotificacion getTipoNotificacion() {
        return tipoNotificacion;
    }

    public void setTipoNotificacion(TipoNotificacion tipoNotificacion) {
        this.tipoNotificacion = tipoNotificacion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Boolean getEstaLeida() {
        return estaLeida;
    }

    public void setEstaLeida(Boolean estaLeida) {
        this.estaLeida = estaLeida;
    }

}