package co.edu.unbosque.alloggio.service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.dto.ArchivoExportadoDTO;
import co.edu.unbosque.alloggio.dto.CiudadDemandaDTO;
import co.edu.unbosque.alloggio.dto.DashboardDTO;
import co.edu.unbosque.alloggio.dto.SeccionReporteDTO;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Accion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.TipoEntidad;

/**
 * Exportación de los indicadores del dashboard a PDF o Excel (CU-030, RN-20).
 * Exporta exactamente lo que muestra el dashboard: calcula los indicadores con
 * DashboardService y los entrega a los generadores. Al terminar con éxito
 * registra en auditoría quién exportó y cuándo (la fecha la pone el evento).
 */
@Service
public class ExportacionService {

    public static final String FORMATO_PDF = "PDF";
    public static final String FORMATO_EXCEL = "EXCEL";

    private static final String TITULO_REPORTE = "Alloggio - Indicadores de hospedajes";
    private static final String TIPO_PDF = "application/pdf";
    private static final String TIPO_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final DateTimeFormatter FORMATO_NOMBRE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Autowired
    private DashboardService dashboardServ;

    @Autowired
    private ReporteExcelService excelServ;

    @Autowired
    private ReportePDFService pdfServ;

    @Autowired
    private EventoAuditoriaService auditoriaServ;

    /**
     * Genera el archivo de indicadores en el formato pedido y registra la
     * exportación en auditoría. No usa readOnly porque escribe el evento.
     * 
     * @param nombreUsuario administrador que exporta
     * @param formato       PDF o EXCEL (no distingue mayúsculas)
     * @param salida        objeto que se llena con el archivo cuando el estado es 0
     * @return 0 éxito, 1 formato inválido, 2 error al generar el archivo
     */
    @Transactional
    public int exportar(String nombreUsuario, String formato, ArchivoExportadoDTO salida) {
        if (formato == null) {
            return 1;
        }
        String formatoNormalizado = formato.trim().toUpperCase();
        if (!formatoNormalizado.equals(FORMATO_PDF) && !formatoNormalizado.equals(FORMATO_EXCEL)) {
            return 1;
        }

        DashboardDTO indicadores = dashboardServ.obtenerIndicadores();
        List<SeccionReporteDTO> secciones = construirSecciones(indicadores);
        String marca = indicadores.getFechaGeneracion().format(FORMATO_NOMBRE);

        try {
            if (formatoNormalizado.equals(FORMATO_PDF)) {
                salida.setContenido(
                        pdfServ.generar(TITULO_REPORTE, nombreUsuario, indicadores.getFechaGeneracion(), secciones));
                salida.setNombreArchivo("indicadores_alloggio_" + marca + ".pdf");
                salida.setTipoContenido(TIPO_PDF);
            } else {
                salida.setContenido(
                        excelServ.generar(TITULO_REPORTE, nombreUsuario, indicadores.getFechaGeneracion(), secciones));
                salida.setNombreArchivo("indicadores_alloggio_" + marca + ".xlsx");
                salida.setTipoContenido(TIPO_EXCEL);
            }
        } catch (Exception e) {
            return 2;
        }

        auditoriaServ.registrarExito(nombreUsuario, Accion.EXPORTAR_INDICADORES, TipoEntidad.REPORTE, null,
                "Exportación de indicadores en formato " + formatoNormalizado);
        return 0;
    }

    /**
     * Convierte los indicadores en tablas simples que dibujan los generadores.
     */
    private List<SeccionReporteDTO> construirSecciones(DashboardDTO d) {
        String[] encabezadosConteo = { "Indicador", "Cantidad" };
        List<SeccionReporteDTO> secciones = new ArrayList<>();

        List<Object[]> usuarios = new ArrayList<>();
        usuarios.add(new Object[] { "Total de usuarios", d.getTotalUsuarios() });
        agregarMapa(usuarios, "Rol: ", d.getUsuariosPorRol());
        agregarMapa(usuarios, "Estado de cuenta: ", d.getUsuariosPorEstadoCuenta());
        secciones.add(new SeccionReporteDTO("Usuarios", encabezadosConteo, usuarios));

        List<Object[]> publicaciones = new ArrayList<>();
        publicaciones.add(new Object[] { "Total de publicaciones", d.getTotalPublicaciones() });
        publicaciones.add(new Object[] { "Publicaciones activas", d.getPublicacionesActivas() });
        agregarMapa(publicaciones, "Estado: ", d.getPublicacionesPorEstado());
        secciones.add(new SeccionReporteDTO("Publicaciones", encabezadosConteo, publicaciones));

        List<Object[]> reservas = new ArrayList<>();
        reservas.add(new Object[] { "Total de reservas", d.getTotalReservas() });
        agregarMapa(reservas, "Estado: ", d.getReservasPorEstado());
        secciones.add(new SeccionReporteDTO("Reservas por estado", encabezadosConteo, reservas));

        List<Object[]> ingresos = new ArrayList<>();
        ingresos.add(new Object[] { "Reservas CONFIRMADA y COMPLETADA (pagos simulados)",
                d.getIngresosEstimados() });
        secciones.add(new SeccionReporteDTO("Ingresos estimados", new String[] { "Indicador", "Valor" }, ingresos));

        List<Object[]> ciudades = new ArrayList<>();
        for (CiudadDemandaDTO c : d.getCiudadesDemanda()) {
            ciudades.add(new Object[] { c.getCiudad(), c.getReservas(), c.getIngresos() });
        }
        if (ciudades.isEmpty()) {
            ciudades.add(new Object[] { "Sin reservas registradas", 0L, BigDecimal.ZERO });
        }
        secciones.add(new SeccionReporteDTO("Ciudades con mayor demanda",
                new String[] { "Ciudad", "Reservas", "Ingresos" }, ciudades));

        return secciones;
    }

    private void agregarMapa(List<Object[]> filas, String prefijo, Map<String, Long> mapa) {
        for (Map.Entry<String, Long> entrada : mapa.entrySet()) {
            filas.add(new Object[] { prefijo + entrada.getKey(), entrada.getValue() });
        }
    }
}