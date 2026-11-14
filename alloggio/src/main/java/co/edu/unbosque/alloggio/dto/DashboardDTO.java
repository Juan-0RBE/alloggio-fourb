package co.edu.unbosque.alloggio.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Indicadores básicos del dashboard del administrador.
 */
public class DashboardDTO {

    private LocalDateTime fechaGeneracion;

    private long totalUsuarios;
    private Map<String, Long> usuariosPorRol;
    private Map<String, Long> usuariosPorEstadoCuenta;

    private long totalPublicaciones;
    private long publicacionesActivas;
    private Map<String, Long> publicacionesPorEstado;

    private long totalReservas;
    private Map<String, Long> reservasPorEstado;

    private BigDecimal ingresosEstimados;

    private List<CiudadDemandaDTO> ciudadesDemanda;

    private boolean sinDatos;

    public DashboardDTO() {
        this.usuariosPorRol = new LinkedHashMap<>();
        this.usuariosPorEstadoCuenta = new LinkedHashMap<>();
        this.publicacionesPorEstado = new LinkedHashMap<>();
        this.reservasPorEstado = new LinkedHashMap<>();
        this.ingresosEstimados = BigDecimal.ZERO;
        this.ciudadesDemanda = new ArrayList<>();
        this.sinDatos = true;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(long totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public Map<String, Long> getUsuariosPorRol() {
        return usuariosPorRol;
    }

    public void setUsuariosPorRol(Map<String, Long> usuariosPorRol) {
        this.usuariosPorRol = usuariosPorRol;
    }

    public Map<String, Long> getUsuariosPorEstadoCuenta() {
        return usuariosPorEstadoCuenta;
    }

    public void setUsuariosPorEstadoCuenta(Map<String, Long> usuariosPorEstadoCuenta) {
        this.usuariosPorEstadoCuenta = usuariosPorEstadoCuenta;
    }

    public long getTotalPublicaciones() {
        return totalPublicaciones;
    }

    public void setTotalPublicaciones(long totalPublicaciones) {
        this.totalPublicaciones = totalPublicaciones;
    }

    public long getPublicacionesActivas() {
        return publicacionesActivas;
    }

    public void setPublicacionesActivas(long publicacionesActivas) {
        this.publicacionesActivas = publicacionesActivas;
    }

    public Map<String, Long> getPublicacionesPorEstado() {
        return publicacionesPorEstado;
    }

    public void setPublicacionesPorEstado(Map<String, Long> publicacionesPorEstado) {
        this.publicacionesPorEstado = publicacionesPorEstado;
    }

    public long getTotalReservas() {
        return totalReservas;
    }

    public void setTotalReservas(long totalReservas) {
        this.totalReservas = totalReservas;
    }

    public Map<String, Long> getReservasPorEstado() {
        return reservasPorEstado;
    }

    public void setReservasPorEstado(Map<String, Long> reservasPorEstado) {
        this.reservasPorEstado = reservasPorEstado;
    }

    public BigDecimal getIngresosEstimados() {
        return ingresosEstimados;
    }

    public void setIngresosEstimados(BigDecimal ingresosEstimados) {
        this.ingresosEstimados = ingresosEstimados;
    }

    public List<CiudadDemandaDTO> getCiudadesDemanda() {
        return ciudadesDemanda;
    }

    public void setCiudadesDemanda(List<CiudadDemandaDTO> ciudadesDemanda) {
        this.ciudadesDemanda = ciudadesDemanda;
    }

    public boolean isSinDatos() {
        return sinDatos;
    }

    public void setSinDatos(boolean sinDatos) {
        this.sinDatos = sinDatos;
    }
}