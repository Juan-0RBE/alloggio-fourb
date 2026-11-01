package co.edu.unbosque.alloggio.dto;

import java.time.LocalDate;

import co.edu.unbosque.alloggio.entity.Usuario.Role;

/**
 * Representa un objeto de transferencia de datos (DTO) para la entidad Usuario.
 */
public class UsuarioDTO {

    private Long idUsuario;
    private String correoUsuario;
    private String nombreUsuario;
    private String claveUsuario;
    private String documentoIdentidad;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private LocalDate fechaNacimiento;
    private Integer edad;
    private String numeroTelefono;
    private String estadoCuenta;
    private Boolean estaActivo;
    private Boolean isVerified;
    private Role role;

    /**
     * Campo solo de entrada: confirma la acción solicitada (desactivar o
     * reactivar cuenta). No pertenece a la entidad.
     */
    private Boolean confirmar;

    public UsuarioDTO() {
    }

    /**
     * Constructor para crear un nuevo UsuarioDTO con todos los atributos.
     * 
     * @param idUsuario
     * @param correoUsuario
     * @param nombreUsuario
     * @param claveUsuario
     * @param documentoIdentidad
     * @param primerNombre
     * @param segundoNombre
     * @param primerApellido
     * @param segundoApellido
     * @param fechaNacimiento
     * @param edad
     * @param numeroTelefono
     * @param estadoCuenta
     * @param estaActivo
     */
    public UsuarioDTO(Long idUsuario, String correoUsuario, String nombreUsuario, String claveUsuario,
            String documentoIdentidad, String primerNombre, String segundoNombre, String primerApellido,
            String segundoApellido, LocalDate fechaNacimiento, Integer edad, String numeroTelefono, String estadoCuenta,
            Boolean estaActivo) {
        this.idUsuario = idUsuario;
        this.correoUsuario = correoUsuario;
        this.nombreUsuario = nombreUsuario;
        this.claveUsuario = claveUsuario;
        this.documentoIdentidad = documentoIdentidad;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.fechaNacimiento = fechaNacimiento;
        this.edad = edad;
        this.numeroTelefono = numeroTelefono;
        this.estadoCuenta = estadoCuenta;
        this.estaActivo = estaActivo;
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

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getClaveUsuario() {
        return claveUsuario;
    }

    public void setClaveUsuario(String claveUsuario) {
        this.claveUsuario = claveUsuario;
    }

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getSegundoNombre() {
        return segundoNombre;
    }

    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
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

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getConfirmar() {
        return confirmar;
    }

    public void setConfirmar(Boolean confirmar) {
        this.confirmar = confirmar;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((correoUsuario == null) ? 0 : correoUsuario.hashCode());
        result = prime * result + ((nombreUsuario == null) ? 0 : nombreUsuario.hashCode());
        result = prime * result + ((claveUsuario == null) ? 0 : claveUsuario.hashCode());
        result = prime * result + ((documentoIdentidad == null) ? 0 : documentoIdentidad.hashCode());
        result = prime * result + ((primerNombre == null) ? 0 : primerNombre.hashCode());
        result = prime * result + ((segundoNombre == null) ? 0 : segundoNombre.hashCode());
        result = prime * result + ((primerApellido == null) ? 0 : primerApellido.hashCode());
        result = prime * result + ((segundoApellido == null) ? 0 : segundoApellido.hashCode());
        result = prime * result + ((fechaNacimiento == null) ? 0 : fechaNacimiento.hashCode());
        result = prime * result + ((edad == null) ? 0 : edad.hashCode());
        result = prime * result + ((numeroTelefono == null) ? 0 : numeroTelefono.hashCode());
        result = prime * result + ((estadoCuenta == null) ? 0 : estadoCuenta.hashCode());
        result = prime * result + ((estaActivo == null) ? 0 : estaActivo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UsuarioDTO other = (UsuarioDTO) obj;
        if (correoUsuario == null) {
            if (other.correoUsuario != null)
                return false;
        } else if (!correoUsuario.equals(other.correoUsuario))
            return false;
        if (nombreUsuario == null) {
            if (other.nombreUsuario != null)
                return false;
        } else if (!nombreUsuario.equals(other.nombreUsuario))
            return false;
        if (claveUsuario == null) {
            if (other.claveUsuario != null)
                return false;
        } else if (!claveUsuario.equals(other.claveUsuario))
            return false;
        if (documentoIdentidad == null) {
            if (other.documentoIdentidad != null)
                return false;
        } else if (!documentoIdentidad.equals(other.documentoIdentidad))
            return false;
        if (primerNombre == null) {
            if (other.primerNombre != null)
                return false;
        } else if (!primerNombre.equals(other.primerNombre))
            return false;
        if (segundoNombre == null) {
            if (other.segundoNombre != null)
                return false;
        } else if (!segundoNombre.equals(other.segundoNombre))
            return false;
        if (primerApellido == null) {
            if (other.primerApellido != null)
                return false;
        } else if (!primerApellido.equals(other.primerApellido))
            return false;
        if (segundoApellido == null) {
            if (other.segundoApellido != null)
                return false;
        } else if (!segundoApellido.equals(other.segundoApellido))
            return false;
        if (fechaNacimiento == null) {
            if (other.fechaNacimiento != null)
                return false;
        } else if (!fechaNacimiento.equals(other.fechaNacimiento))
            return false;
        if (edad == null) {
            if (other.edad != null)
                return false;
        } else if (!edad.equals(other.edad))
            return false;
        if (numeroTelefono == null) {
            if (other.numeroTelefono != null)
                return false;
        } else if (!numeroTelefono.equals(other.numeroTelefono))
            return false;
        if (estadoCuenta == null) {
            if (other.estadoCuenta != null)
                return false;
        } else if (!estadoCuenta.equals(other.estadoCuenta))
            return false;
        if (estaActivo == null) {
            if (other.estaActivo != null)
                return false;
        } else if (!estaActivo.equals(other.estaActivo))
            return false;
        return true;
    }

    /**
     * Retorna una representación en forma de string del objeto UsuarioDTO.
     *
     * @return Una cadena que representa el objeto UsuarioDTO.
     */
    @Override
    public String toString() {
        return "UsuarioDTO [correoUsuario=" + correoUsuario + ", nombreUsuario=" + nombreUsuario
                + ", documentoIdentidad=" + documentoIdentidad + ", primerNombre=" + primerNombre
                + ", segundoNombre=" + segundoNombre + ", primerApellido=" + primerApellido + ", segundoApellido="
                + segundoApellido + ", fechaNacimiento=" + fechaNacimiento + ", edad=" + edad + ", numeroTelefono="
                + numeroTelefono + ", estadoCuenta=" + estadoCuenta + ", estaActivo=" + estaActivo + ", isVerified="
                + isVerified + ", role=" + role + "]";
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

}