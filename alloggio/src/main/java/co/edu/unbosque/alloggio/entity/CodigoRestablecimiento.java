package co.edu.unbosque.alloggio.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "CODIGO_RESTABLECIMIENTO")
public class CodigoRestablecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCodigo;

    @Column(name = "Codigo", length = 6, nullable = false)
    private String codigo;

    @Column(name = "FechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FechaExpiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "Usado", nullable = false)
    private Boolean usado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    public CodigoRestablecimiento() {
    }

    /**
     * Constructor para crear un nuevo código de restablecimiento con todos los
     * atributos
     * 
     * @param idCodigo
     * @param codigo
     * @param fechaCreacion
     * @param fechaExpiracion
     * @param usado
     * @param usuario
     */
    public CodigoRestablecimiento(Long idCodigo, String codigo, LocalDateTime fechaCreacion,
            LocalDateTime fechaExpiracion, Boolean usado, Usuario usuario) {
        this.idCodigo = idCodigo;
        this.codigo = codigo;
        this.fechaCreacion = fechaCreacion;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = usado;
        this.usuario = usuario;
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

}
