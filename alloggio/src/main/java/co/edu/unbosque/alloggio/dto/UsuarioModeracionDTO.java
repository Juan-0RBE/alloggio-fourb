package co.edu.unbosque.alloggio.dto;

import co.edu.unbosque.alloggio.entity.Usuario.Role;

import com.fasterxml.jackson.annotation.JsonInclude;

/** DTO de moderación de usuarios: sin clave, correo, documento ni teléfono. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioModeracionDTO {
    private Long idUsuario;
    private String nombreUsuario;
    private String primerNombre;
    private String primerApellido;
    private Role role;
    private String estadoCuenta;
    private Boolean estaActivo;

    public UsuarioModeracionDTO() {
    }

    public UsuarioModeracionDTO(Long idUsuario, String nombreUsuario, String primerNombre, String primerApellido,
            Role role, String estadoCuenta, Boolean estaActivo) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.primerNombre = primerNombre;
        this.primerApellido = primerApellido;
        this.role = role;
        this.estadoCuenta = estadoCuenta;
        this.estaActivo = estaActivo;
    }

    // Getters y Setters

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getEstadoCuenta() {
        return estadoCuenta;
    }

    public void setEstadoCuenta(String estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }

    public Boolean getEstaActivo() {
        return estaActivo;
    }

    public void setEstaActivo(Boolean estaActivo) {
        this.estaActivo = estaActivo;
    }

}