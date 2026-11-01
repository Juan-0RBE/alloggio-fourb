package co.edu.unbosque.alloggio.dto;

import java.time.LocalDateTime;

public class CodigoRestablecimientoDTO {

    private Long idCodigo;
    private String codigo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
    private Boolean usado;
    private String correoUsuario;
    private String nuevaClave;

    public CodigoRestablecimientoDTO() {
    }

    /**
     * Constructor para crear un nuevo DTO de CodigoRestablecimiento con todos los
     * atributos.
     * 
     * @param idCodigo
     * @param codigo
     * @param fechaCreacion
     * @param fechaExpiracion
     * @param usado
     * @param correoUsuario
     * @param nuevaClave
     */
    public CodigoRestablecimientoDTO(Long idCodigo, String codigo, LocalDateTime fechaCreacion,
            LocalDateTime fechaExpiracion, Boolean usado, String correoUsuario, String nuevaClave) {
        this.idCodigo = idCodigo;
        this.codigo = codigo;
        this.fechaCreacion = fechaCreacion;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = usado;
        this.correoUsuario = correoUsuario;
        this.nuevaClave = nuevaClave;
    }

    public Long getIdCodigo() {
        return idCodigo;
    }

    public void setIdCodigo(Long idCodigo) {
        this.idCodigo = idCodigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public Boolean getUsado() {
        return usado;
    }

    public void setUsado(Boolean usado) {
        this.usado = usado;
    }

    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public void setCorreoUsuario(String correoUsuario) {
        this.correoUsuario = correoUsuario;
    }

    public String getNuevaClave() {
        return nuevaClave;
    }

    public void setNuevaClave(String nuevaClave) {
        this.nuevaClave = nuevaClave;
    }

    @Override
    public String toString() {
        return "CodigoRestablecimientoDTO [idCodigo=" + idCodigo + ", codigo=" + codigo + ", fechaCreacion="
                + fechaCreacion + ", fechaExpiracion=" + fechaExpiracion + ", usado=" + usado + ", correoUsuario="
                + correoUsuario + ", nuevaClave=" + nuevaClave + "]";
    }

}
