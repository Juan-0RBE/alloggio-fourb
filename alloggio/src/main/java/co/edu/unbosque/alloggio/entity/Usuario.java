package co.edu.unbosque.alloggio.entity;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa una entidad de usuario.
 * Esta clase se asigna a la tabla "usuario" de la base de datos.
 */
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {

    private static final long serialVersionUID = 1L;

    /** Valor de estadoCuenta para una cuenta operativa. */
    public static final String ESTADO_ACTIVA = "ACTIVA";

    /**
     * Valor de estadoCuenta para una cuenta restringida por un administrador
     * (CU-027).
     */
    public static final String ESTADO_RESTRINGIDA = "RESTRINGIDA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(name = "CorreoUsuario", length = 50, nullable = false, unique = true)
    private String correoUsuario;

    @Column(name = "NombreUsuario", length = 60, nullable = false, unique = true)
    private String nombreUsuario;

    @Column(name = "Clave_Usuario", length = 255, nullable = false)
    private String claveUsuario;

    @Column(name = "Documento_Identidad", length = 20, nullable = false, unique = true)
    private String documentoIdentidad;

    @Column(name = "PrimerNombre", length = 50, nullable = false)
    private String primerNombre;

    @Column(name = "SegundoNombre", length = 50)
    private String segundoNombre;

    @Column(name = "PrimerApellido", length = 50, nullable = false)
    private String primerApellido;

    @Column(name = "SegundoApellido", length = 50)
    private String segundoApellido;

    @Column(name = "FechaNacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "Edad")
    private Integer edad;

    @Column(name = "NumeroTelefono", length = 20, nullable = false)
    private String numeroTelefono;

    @Column(name = "estadoCuenta", nullable = false)
    private String estadoCuenta;

    @Column(name = "estaActivo", nullable = false)
    private Boolean estaActivo;

    @Column(name = "IsVerified", nullable = false)
    private Boolean isVerified;

    @Column(name = "AccountNonExpired", nullable = false)
    private Boolean accountNonExpired;

    @Column(name = "AccountNonLocked", nullable = false)
    private Boolean accountNonLocked;

    @Column(name = "CredentialsNonExpired", nullable = false)
    private Boolean credentialsNonExpired;

    @Column(name = "Enabled", nullable = false)
    private Boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false)
    private Role role;

    public enum Role {
        USUARIO, ADMINISTRADOR
    }

    public Usuario() {
        this.isVerified = false;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = true;
        this.role = Role.USUARIO;
        this.estaActivo = true;
        this.estadoCuenta = ESTADO_ACTIVA;
    }

    /**
     * Constructor para crear un nuevo usuario con todos los atributos.
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
     * @param isVerified
     * @param accountNonExpired
     * @param accountNonLocked
     * @param credentialsNonExpired
     * @param enabled
     * @param role
     */
    public Usuario(Long idUsuario, String correoUsuario, String nombreUsuario, String claveUsuario,
            String documentoIdentidad, String primerNombre, String segundoNombre, String primerApellido,
            String segundoApellido, LocalDate fechaNacimiento, Integer edad, String numeroTelefono, String estadoCuenta,
            Boolean estaActivo, Boolean isVerified, Boolean accountNonExpired, Boolean accountNonLocked,
            Boolean credentialsNonExpired, Boolean enabled, Role role) {
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
        this.isVerified = isVerified;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
        this.role = role;
    }


	/**
	 * Constructor con nombre de usuario, contraseña y rol específico.
	 * 
	 * @param username Nombre de usuario
	 * @param password Contraseña del usuario
	 * @param rol      Rol asignado al usuario
	 */
	public Usuario(String nombreUsuario, String password, Role role, String documentoIdentidad, String correoUsuario) {
		super();
		this.nombreUsuario = nombreUsuario;
		this.claveUsuario = password;
		this.role = role;
		this.accountNonExpired = true;
		this.accountNonLocked = true;
		this.credentialsNonExpired = true;
		this.enabled = true;
		this.correoUsuario = correoUsuario; 
		this.documentoIdentidad= documentoIdentidad; 
		this.numeroTelefono=""; 
		this.estaActivo = true; 
		this.isVerified = true;
		this.estadoCuenta = "";
		this.primerApellido = "";
		this.primerNombre ="";
		this.fechaNacimiento = LocalDate.now();
	}
    
    public static long getSerialversionuid() {
        return serialVersionUID;
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

    public Boolean getAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(Boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public Boolean getAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public Boolean getCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Métodos relacionados con UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return claveUsuario;
    }

    @Override
    public String getUsername() {
        return nombreUsuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
