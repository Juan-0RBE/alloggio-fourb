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
 * Entidad que representa un evento de auditoría.
 * 
 */
@Entity
@Table(name = "EVENTO_AUDITORIA")
public class EventoAuditoria {

    /**
     * Acciones auditables. Se agregan nuevas a medida que se construyen los
     * módulos.
     */
    public enum Accion {
        REGISTRO_USUARIO, INICIO_SESION, SOLICITUD_RECUPERACION, RESTABLECER_CONTRASENA,
        ACTUALIZAR_PERFIL, DESACTIVAR_CUENTA, ACTIVAR_CUENTA,
        CREAR_PUBLICACION, ACTUALIZAR_PUBLICACION, ELIMINAR_PUBLICACION,
        ACTIVAR_PUBLICACION, PAUSAR_PUBLICACION, BLOQUEAR_PUBLICACION,
        BLOQUEAR_RANGO, DESBLOQUEAR_RANGO,
        PROCESAR_PAGO, CREAR_RESERVA, CANCELAR_RESERVA,
        CREAR_RESENA, OCULTAR_RESENA,
        RESTRINGIR_USUARIO, EXPORTAR_INDICADORES
    }

    /** Tipos de entidad sobre los que puede recaer una acción. */
    public enum TipoEntidad {
        USUARIO, PUBLICACION, PERIODO_DISPONIBILIDAD, RESERVA, PAGO, RESENA, REPORTE
    }

    /** Resultado de la acción registrada. */
    public enum Resultado {
        EXITO, FALLIDO, DENEGADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_Auditoria")
    private Long idAuditoria;

    // Nullable: un login fallido o un registro rechazado no tienen usuario
    // autenticado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Id_Usuario")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "Accion", length = 50, nullable = false)
    private Accion accion;

    @Enumerated(EnumType.STRING)
    @Column(name = "Entidad_Tipo", length = 50, nullable = false)
    private TipoEntidad entidadTipo;

    @Column(name = "Entidad_Id")
    private Long entidadId;

    @Column(name = "Fecha_Hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "Descripcion", length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "Resultado", length = 20, nullable = false)
    private Resultado resultado;

    protected EventoAuditoria() {
    }

    /**
     * Constructor para crear un evento de auditoría. La fecha se asigna en el
     * servidor.
     * 
     * @param usuario
     * @param accion
     * @param entidadTipo
     * @param entidadId
     * @param resultado
     * @param descripcion
     */
    public EventoAuditoria(Usuario usuario, Accion accion, TipoEntidad entidadTipo, Long entidadId,
            Resultado resultado, String descripcion) {
        this.usuario = usuario;
        this.accion = accion;
        this.entidadTipo = entidadTipo;
        this.entidadId = entidadId;
        this.resultado = resultado;
        this.descripcion = descripcion;
        this.fechaHora = LocalDateTime.now();
    }

    // Getters

    public Long getIdAuditoria() {
        return idAuditoria;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Accion getAccion() {
        return accion;
    }

    public TipoEntidad getEntidadTipo() {
        return entidadTipo;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Resultado getResultado() {
        return resultado;
    }

}