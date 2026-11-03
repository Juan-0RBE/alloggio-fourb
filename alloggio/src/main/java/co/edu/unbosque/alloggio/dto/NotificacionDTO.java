package co.edu.unbosque.alloggio.dto;

import java.time.LocalDateTime;

import co.edu.unbosque.alloggio.entity.Notificacion.TipoNotificacion;

/**
 * DTO de salida para la entidad Notificacion. Usa los mismos nombres que la
 * entidad.
 */
public class NotificacionDTO {

    private Long idNotificacion;
    private TipoNotificacion tipoNotificacion;
    private String mensaje;
    private LocalDateTime fecha;
    private Boolean estaLeida;
    private Long idUsuario;

    public NotificacionDTO() {
    }

    /**
     * Constructor de la clase NotificacionDTO
     * 
     * @param idNotificacion
     * @param tipoNotificacion
     * @param mensaje
     * @param fecha
     * @param estaLeida
     * @param idUsuario
     */
    public NotificacionDTO(Long idNotificacion, TipoNotificacion tipoNotificacion, String mensaje,
            LocalDateTime fecha, Boolean estaLeida, Long idUsuario) {
        this.idNotificacion = idNotificacion;
        this.tipoNotificacion = tipoNotificacion;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.estaLeida = estaLeida;
        this.idUsuario = idUsuario;
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

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
}