package co.edu.unbosque.alloggio.entity;

import java.time.LocalDate;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa una entidad de recuperación de contraseña.
 * CodigoRecuperacion
 */
@Entity
@Table(name = "codigo_recuperacion")
public class CodigoRecuperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCodigoRecuperacion;

    @Column(name = "Usado")
    private boolean estaUsado;

    @Column(name = "Codigo", nullable = false)
    private String codigo;

    @Column(name = "FechaExpiracion", nullable = false)
    private LocalDate fechaExpiracion;

    public CodigoRecuperacion() {
    }

    /**
     * Constructor para crear una nueva entidad de recuperacion de código con todos
     * los atributos.
     * 
     * @param idCodigoRecuperacion
     * @param estaUsado
     * @param codigo
     * @param fechaExpiracion
     */
    public CodigoRecuperacion(Long idCodigoRecuperacion, boolean estaUsado, String codigo, LocalDate fechaExpiracion) {
        this.idCodigoRecuperacion = idCodigoRecuperacion;
        this.estaUsado = estaUsado;
        this.codigo = codigo;
        this.fechaExpiracion = fechaExpiracion;
    }

    public Long getIdCodigoRecuperacion() {
        return idCodigoRecuperacion;
    }

    public void setIdCodigoRecuperacion(Long idCodigoRecuperacion) {
        this.idCodigoRecuperacion = idCodigoRecuperacion;
    }

    public boolean isEstaUsado() {
        return estaUsado;
    }

    public void setEstaUsado(boolean estaUsado) {
        this.estaUsado = estaUsado;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDate getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDate fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

}
