package co.edu.unbosque.alloggio.dto;

/**
 * Archivo generado por la exportación de indicadores. El servicio lo
 * llena cuando la exportación es exitosa; el controller lo entrega como
 * descarga.
 */
public class ArchivoExportadoDTO {

    private byte[] contenido;
    private String nombreArchivo;
    private String tipoContenido;

    public ArchivoExportadoDTO() {
    }

    /**
     * Constructor con todos los atributos.
     * 
     * @param contenido     bytes del archivo
     * @param nombreArchivo nombre sugerido para la descarga
     * @param tipoContenido tipo MIME del archivo
     */
    public ArchivoExportadoDTO(byte[] contenido, String nombreArchivo, String tipoContenido) {
        this.contenido = contenido;
        this.nombreArchivo = nombreArchivo;
        this.tipoContenido = tipoContenido;
    }

    public byte[] getContenido() {
        return contenido;
    }

    public void setContenido(byte[] contenido) {
        this.contenido = contenido;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public void setTipoContenido(String tipoContenido) {
        this.tipoContenido = tipoContenido;
    }
}