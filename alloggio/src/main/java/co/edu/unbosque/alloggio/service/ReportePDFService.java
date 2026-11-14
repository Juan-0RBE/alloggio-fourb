package co.edu.unbosque.alloggio.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import co.edu.unbosque.alloggio.dto.SeccionReporteDTO;

/**
 * Genera el reporte de indicadores en PDF con OpenPDF.
 */
@Service
public class ReportePDFService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Genera el archivo PDF: encabezado y una tabla por sección.
     * 
     * @param titulo      título del reporte
     * @param solicitante nombre de usuario de quien exporta
     * @param fecha       fecha y hora de generación
     * @param secciones   tablas a escribir
     * @return bytes del archivo PDF
     * @throws Exception si falla la generación del documento
     */
    public byte[] generar(String titulo, String solicitante, LocalDateTime fecha, List<SeccionReporteDTO> secciones)
            throws Exception {
        NumberFormat moneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));

        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font fuenteSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
        Font fuenteEncabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Font fuenteTexto = FontFactory.getFont(FontFactory.HELVETICA, 10);

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(documento, salida);
        documento.open();

        documento.add(new Paragraph(titulo, fuenteTitulo));
        documento.add(new Paragraph("Generado: " + fecha.format(FORMATO_FECHA), fuenteTexto));
        documento.add(new Paragraph("Solicitado por: " + solicitante, fuenteTexto));

        for (SeccionReporteDTO seccion : secciones) {
            Paragraph encabezadoSeccion = new Paragraph(seccion.getTitulo(), fuenteSeccion);
            encabezadoSeccion.setSpacingBefore(14);
            encabezadoSeccion.setSpacingAfter(6);
            documento.add(encabezadoSeccion);

            int columnas = seccion.getEncabezados().length;
            PdfPTable tabla = new PdfPTable(columnas);
            tabla.setWidthPercentage(100);
            if (columnas == 3) {
                tabla.setWidths(new float[] { 3f, 1f, 1.6f });
            } else if (columnas == 2) {
                tabla.setWidths(new float[] { 3f, 1f });
            }

            for (String encabezado : seccion.getEncabezados()) {
                PdfPCell celda = new PdfPCell(new Phrase(encabezado, fuenteEncabezado));
                celda.setBackgroundColor(new Color(52, 73, 94));
                celda.setPadding(5);
                tabla.addCell(celda);
            }

            for (Object[] datos : seccion.getFilas()) {
                for (Object valor : datos) {
                    String texto = valor instanceof BigDecimal dinero ? moneda.format(dinero) : String.valueOf(valor);
                    PdfPCell celda = new PdfPCell(new Phrase(texto, fuenteTexto));
                    celda.setPadding(4);
                    if (valor instanceof Number) {
                        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    }
                    tabla.addCell(celda);
                }
            }
            documento.add(tabla);
        }

        documento.close();
        return salida.toByteArray();
    }
}