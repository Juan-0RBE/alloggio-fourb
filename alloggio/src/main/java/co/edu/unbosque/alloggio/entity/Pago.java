package co.edu.unbosque.alloggio.entity;

import java.math.BigDecimal;
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
 * Intento de pago de una cotización a través de la pasarela. Una cotización
 * puede tener intentos rechazados.
 */
@Entity
@Table(name = "PAGO")
public class Pago {

    /** Resultado del intento de cobro. */
    public enum EstadoPago {
        APROBADO, RECHAZADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPago;

    @Column(name = "Monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "Moneda", nullable = false, length = 3)
    private String moneda;

    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false, length = 20)
    private EstadoPago estado;

    /** Identificador del cobro en la pasarela (por ejemplo, pi_...). */
    @Column(name = "Referencia_Pasarela", length = 100)
    private String referenciaPasarela;

    @Column(name = "Fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "Marca_Tarjeta", length = 30)
    private String marcaTarjeta;

    @Column(name = "Ultimos_Cuatro", length = 4)
    private String ultimosCuatro;

    /** Código de rechazo de la pasarela (solo si el pago fue rechazado). */
    @Column(name = "Codigo_Rechazo", length = 80)
    private String codigoRechazo;

    /** Motivo de rechazo en español, para mostrar al huésped. */
    @Column(name = "Motivo_Rechazo", length = 255)
    private String motivoRechazo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idCotizacion", nullable = false)
    private Cotizacion cotizacion;

    /** Se asigna cuando el pago aprobado queda ligado a una reserva. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idReserva")
    private Reserva reserva;

    public Pago() {
        this.monto = BigDecimal.ZERO;
        this.moneda = "COP";
        this.estado = EstadoPago.RECHAZADO;
        this.fecha = LocalDateTime.now();
    }

    /**
     * Constructor para crear un nuevo pago con todos los atributos
     * 
     * @param idPago
     * @param monto
     * @param moneda
     * @param estado
     * @param referenciaPasarela
     * @param fecha
     * @param marcaTarjeta
     * @param ultimosCuatro
     * @param codigoRechazo
     * @param motivoRechazo
     * @param cotizacion
     * @param reserva
     */
    public Pago(Long idPago, BigDecimal monto, String moneda, EstadoPago estado, String referenciaPasarela,
            LocalDateTime fecha, String marcaTarjeta, String ultimosCuatro, String codigoRechazo,
            String motivoRechazo, Cotizacion cotizacion, Reserva reserva) {
        this.idPago = idPago;
        this.monto = monto;
        this.moneda = moneda;
        this.estado = estado;
        this.referenciaPasarela = referenciaPasarela;
        this.fecha = fecha;
        this.marcaTarjeta = marcaTarjeta;
        this.ultimosCuatro = ultimosCuatro;
        this.codigoRechazo = codigoRechazo;
        this.motivoRechazo = motivoRechazo;
        this.cotizacion = cotizacion;
        this.reserva = reserva;
    }

    // Getters y Setters
    public Long getIdPago() {
        return idPago;
    }

    public void setIdPago(Long idPago) {
        this.idPago = idPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public String getReferenciaPasarela() {
        return referenciaPasarela;
    }

    public void setReferenciaPasarela(String referenciaPasarela) {
        this.referenciaPasarela = referenciaPasarela;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getMarcaTarjeta() {
        return marcaTarjeta;
    }

    public void setMarcaTarjeta(String marcaTarjeta) {
        this.marcaTarjeta = marcaTarjeta;
    }

    public String getUltimosCuatro() {
        return ultimosCuatro;
    }

    public void setUltimosCuatro(String ultimosCuatro) {
        this.ultimosCuatro = ultimosCuatro;
    }

    public String getCodigoRechazo() {
        return codigoRechazo;
    }

    public void setCodigoRechazo(String codigoRechazo) {
        this.codigoRechazo = codigoRechazo;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public Cotizacion getCotizacion() {
        return cotizacion;
    }

    public void setCotizacion(Cotizacion cotizacion) {
        this.cotizacion = cotizacion;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }
}