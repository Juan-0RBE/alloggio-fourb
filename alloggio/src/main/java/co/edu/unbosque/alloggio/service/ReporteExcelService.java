package co.edu.unbosque.alloggio.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import co.edu.unbosque.alloggio.dto.SeccionReporteDTO;

/**
 * Genera el reporte de indicadores en Excel (.xlsx) con Apache POI.
 */
@Service
public class ReporteExcelService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Genera el archivo Excel con una hoja "Indicadores".
     * 
     * @param titulo      título del reporte
     * @param solicitante nombre de usuario de quien exporta
     * @param fecha       fecha y hora de generación
     * @param secciones   tablas a escribir
     * @return bytes del archivo .xlsx
     * @throws Exception si falla la escritura del archivo
     */
    public byte[] generar(String titulo, String solicitante, LocalDateTime fecha, List<SeccionReporteDTO> secciones)
            throws Exception {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Indicadores");
            DataFormat formato = libro.createDataFormat();

            Font fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            CellStyle estiloTitulo = libro.createCellStyle();
            estiloTitulo.setFont(fuenteTitulo);

            Font fuenteNegrita = libro.createFont();
            fuenteNegrita.setBold(true);
            CellStyle estiloSeccion = libro.createCellStyle();
            estiloSeccion.setFont(fuenteNegrita);

            CellStyle estiloEncabezado = libro.createCellStyle();
            estiloEncabezado.setFont(fuenteNegrita);
            estiloEncabezado.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle estiloMoneda = libro.createCellStyle();
            estiloMoneda.setDataFormat(formato.getFormat("\"$\"#,##0.00"));

            int fila = 0;
            escribirTexto(hoja.createRow(fila++), 0, titulo, estiloTitulo);
            Row filaFecha = hoja.createRow(fila++);
            escribirTexto(filaFecha, 0, "Generado:", null);
            escribirTexto(filaFecha, 1, fecha.format(FORMATO_FECHA), null);
            Row filaSolicitante = hoja.createRow(fila++);
            escribirTexto(filaSolicitante, 0, "Solicitado por:", null);
            escribirTexto(filaSolicitante, 1, solicitante, null);
            fila++; // línea en blanco

            for (SeccionReporteDTO seccion : secciones) {
                escribirTexto(hoja.createRow(fila++), 0, seccion.getTitulo(), estiloSeccion);

                Row encabezado = hoja.createRow(fila++);
                for (int i = 0; i < seccion.getEncabezados().length; i++) {
                    escribirTexto(encabezado, i, seccion.getEncabezados()[i], estiloEncabezado);
                }

                for (Object[] datos : seccion.getFilas()) {
                    Row row = hoja.createRow(fila++);
                    for (int i = 0; i < datos.length; i++) {
                        Object valor = datos[i];
                        if (valor instanceof BigDecimal dinero) {
                            Cell celda = row.createCell(i);
                            celda.setCellValue(dinero.doubleValue());
                            celda.setCellStyle(estiloMoneda);
                        } else if (valor instanceof Number numero) {
                            row.createCell(i).setCellValue(numero.doubleValue());
                        } else {
                            escribirTexto(row, i, String.valueOf(valor), null);
                        }
                    }
                }
                fila++; // línea en blanco entre secciones
            }

            // Anchos fijos (autoSizeColumn depende de fuentes del sistema)
            hoja.setColumnWidth(0, 60 * 256);
            hoja.setColumnWidth(1, 20 * 256);
            hoja.setColumnWidth(2, 24 * 256);

            libro.write(salida);
            return salida.toByteArray();
        }
    }

    private void escribirTexto(Row fila, int columna, String texto, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(texto);
        if (estilo != null) {
            celda.setCellStyle(estilo);
        }
    }
}