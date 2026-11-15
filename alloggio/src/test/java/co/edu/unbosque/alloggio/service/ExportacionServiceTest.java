package co.edu.unbosque.alloggio.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.alloggio.dto.ArchivoExportadoDTO;
import co.edu.unbosque.alloggio.dto.DashboardDTO;

/**
 * Pruebas unitarias de ExportacionService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExportacionService - Módulo de Exportación de Indicadores")
class ExportacionServiceTest {

    @Mock
    private DashboardService dashboardServ;

    @Mock
    private ReporteExcelService excelServ;

    @Mock
    private ReportePDFService pdfServ;

    @Mock
    private EventoAuditoriaService auditoriaServ;

    @InjectMocks
    private ExportacionService exportacionService;

    private static final String ADMIN = "adminTest";

    @Nested
    @DisplayName("CU-030 - Exportación de indicadores del dashboard")
    class ExportarIndicadores {

        @Test
        @DisplayName("CP-078 - Exportación exitosa a formato PDF")
        void exportar_formatoPdfValido_retornaCeroYLlenaArchivo() throws Exception {
            // Arrange
            ArchivoExportadoDTO salida = new ArchivoExportadoDTO();
            DashboardDTO dashboardMock = new DashboardDTO();
            dashboardMock.setFechaGeneracion(LocalDateTime.now());

            when(dashboardServ.obtenerIndicadores()).thenReturn(dashboardMock);
            when(pdfServ.generar(any(), any(), any(), any())).thenReturn(new byte[] { 1, 2, 3 });

            // Act
            int estado = exportacionService.exportar(ADMIN, "PDF", salida);

            // Assert
            assertAll(
                    () -> assertEquals(0, estado),
                    () -> assertNotNull(salida.getContenido()),
                    () -> assertEquals("application/pdf", salida.getTipoContenido()),
                    () -> assertTrue(salida.getNombreArchivo().endsWith(".pdf")),
                    () -> verify(auditoriaServ).registrarExito(eq(ADMIN), any(), any(), eq(null), anyString()));
        }

        @Test
        @DisplayName("CP-078 - Exportación exitosa a formato Excel")
        void exportar_formatoExcelValido_retornaCeroYLlenaArchivo() throws Exception {
            // Arrange
            ArchivoExportadoDTO salida = new ArchivoExportadoDTO();
            DashboardDTO dashboardMock = new DashboardDTO();
            dashboardMock.setFechaGeneracion(LocalDateTime.now());

            when(dashboardServ.obtenerIndicadores()).thenReturn(dashboardMock);
            when(excelServ.generar(any(), any(), any(), any())).thenReturn(new byte[] { 4, 5, 6 });

            // Act
            int estado = exportacionService.exportar(ADMIN, "EXCEL", salida);

            // Assert
            assertAll(
                    () -> assertEquals(0, estado),
                    () -> assertNotNull(salida.getContenido()),
                    () -> assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            salida.getTipoContenido()),
                    () -> assertTrue(salida.getNombreArchivo().endsWith(".xlsx")),
                    () -> verify(auditoriaServ).registrarExito(eq(ADMIN), any(), any(), eq(null), anyString()));
        }

        @Test
        @DisplayName("CP-079 - Exportación de indicadores cancelada o con formato nulo")
        void exportar_formatoNulo_retornaUnoYNoGeneraArchivo() {
            // Arrange
            ArchivoExportadoDTO salida = new ArchivoExportadoDTO();

            // Act
            int estado = exportacionService.exportar(ADMIN, null, salida);

            // Assert
            assertAll(
                    () -> assertEquals(1, estado),
                    () -> verifyNoInteractions(pdfServ, excelServ, auditoriaServ));
        }

    }
}