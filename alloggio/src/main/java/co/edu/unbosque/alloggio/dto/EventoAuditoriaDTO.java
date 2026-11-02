package co.edu.unbosque.alloggio.dto;

import java.time.LocalDateTime;

import co.edu.unbosque.alloggio.entity.EventoAuditoria.Accion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Resultado;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.TipoEntidad;

/**
 * DTO de salida para representar un evento de auditoría.
 */
public class EventoAuditoriaDTO {

    private Long idAuditoria;
    private Long idUsuario;
    private String correoUsuario;
    private Accion accion;
    private TipoEntidad entidadTipo;
    private Long entidadId;
    private LocalDateTime fechaHora;
    private String descripcion;
    private Resultado resultado;

    public EventoAuditoriaDTO() {
    }

    /**
     * Constructor con parámetros para crear un evento de auditoría DTO.
     * 
     * @param idAuditoria
     * @param idUsuario
     * @param correoUsuario
     * @param accion
     * @param entidadTipo
     * @param entidadId
     * @param fechaHora
     * @param descripcion
     * @param resultado
     */
    public EventoAuditoriaDTO(Long idAuditoria, Long idUsuario, String correoUsuario, Accion accion,
            TipoEntidad entidadTipo, Long entidadId, LocalDateTime fechaHora, String descripcion,
            Resultado resultado) {
        this.idAuditoria = idAuditoria;
        this.idUsuario = idUsuario;
        this.correoUsuario = correoUsuario;
        this.accion = accion;
        this.entidadTipo = entidadTipo;
        this.entidadId = entidadId;
        this.fechaHora = fechaHora;
        this.descripcion = descripcion;
        this.resultado = resultado;
    }

    // Getters y Setters

    public Long getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Long idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public void setCorreoUsuario(String correoUsuario) {
        this.correoUsuario = correoUsuario;
    }

    public Accion getAccion() {
        return accion;
    }

    public void setAccion(Accion accion) {
        this.accion = accion;
    }

    public TipoEntidad getEntidadTipo() {
        return entidadTipo;
    }

    public void setEntidadTipo(TipoEntidad entidadTipo) {
        this.entidadTipo = entidadTipo;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(Long entidadId) {
        this.entidadId = entidadId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Resultado getResultado() {
        return resultado;
    }

    public void setResultado(Resultado resultado) {
        this.resultado = resultado;
    }

}