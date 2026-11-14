package co.edu.unbosque.alloggio.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Sección de un reporte exportado: un título, los encabezados de la tabla y sus
 * filas.
 */
public class SeccionReporteDTO {

    private String titulo;
    private String[] encabezados;
    private List<Object[]> filas;

    public SeccionReporteDTO() {
        this.encabezados = new String[0];
        this.filas = new ArrayList<>();
    }

    /**
     * Constructor con todos los atributos.
     * 
     * @param titulo
     * @param encabezados
     * @param filas
     */
    public SeccionReporteDTO(String titulo, String[] encabezados, List<Object[]> filas) {
        this.titulo = titulo;
        this.encabezados = encabezados;
        this.filas = filas;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String[] getEncabezados() {
        return encabezados;
    }

    public void setEncabezados(String[] encabezados) {
        this.encabezados = encabezados;
    }

    public List<Object[]> getFilas() {
        return filas;
    }

    public void setFilas(List<Object[]> filas) {
        this.filas = filas;
    }
}