package co.edu.unbosque.alloggio.dto;

import java.util.List;

/**
 * DTO genérico para devolver una página de resultados junto con los datos de
 * control que necesita el front para construir el paginador.
 * 
 * @param <T> tipo de los elementos de la página
 */
public class PaginaDTO<T> {

    private List<T> contenido;
    private int paginaActual;
    private int tamano;
    private long totalElementos;
    private int totalPaginas;

    public PaginaDTO() {
    }

    /**
     * Constructor para la clase PaginaDTO.
     * 
     * @param contenido      elementos de la página actual
     * @param paginaActual   número de la página (empieza en 0)
     * @param tamano         cantidad máxima de elementos por página
     * @param totalElementos total de elementos que cumplen los criterios
     * @param totalPaginas   total de páginas disponibles
     */
    public PaginaDTO(List<T> contenido, int paginaActual, int tamano, long totalElementos, int totalPaginas) {
        this.contenido = contenido;
        this.paginaActual = paginaActual;
        this.tamano = tamano;
        this.totalElementos = totalElementos;
        this.totalPaginas = totalPaginas;
    }

    // Getters y Setters

    public List<T> getContenido() {
        return contenido;
    }

    public void setContenido(List<T> contenido) {
        this.contenido = contenido;
    }

    public int getPaginaActual() {
        return paginaActual;
    }

    public void setPaginaActual(int paginaActual) {
        this.paginaActual = paginaActual;
    }

    public int getTamano() {
        return tamano;
    }

    public void setTamano(int tamano) {
        this.tamano = tamano;
    }

    public long getTotalElementos() {
        return totalElementos;
    }

    public void setTotalElementos(long totalElementos) {
        this.totalElementos = totalElementos;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }

}