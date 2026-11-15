package co.edu.unbosque.alloggio.testutil;

import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.ID_PUBLICACION;

import java.time.LocalDate;

import co.edu.unbosque.alloggio.dto.ImagenAlojamientoDTO;
import co.edu.unbosque.alloggio.dto.ReglaAlojamientoDTO;
import co.edu.unbosque.alloggio.dto.ServicioAlojamientoDTO;
import co.edu.unbosque.alloggio.entity.ImagenAlojamiento;
import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad;
import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad.TipoPeriodo;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.ReglaAlojamiento;
import co.edu.unbosque.alloggio.entity.ServicioAlojamiento;

/**
 * Datos de prueba reutilizables para la búsqueda y el detalle de alojamientos.
 */
public final class AlojamientoFixtures {

    private AlojamientoFixtures() {
    }

    public static ServicioAlojamiento servicio(Long id, String nombre, Double costo, Boolean adicional,
            Publicacion publicacion) {
        ServicioAlojamiento s = new ServicioAlojamiento();
        s.setIdServicio(id);
        s.setNombre(nombre);
        s.setCosto(costo);
        s.setAdicional(adicional);
        s.setPublicacion(publicacion);
        return s;
    }

    /**
     * DTO que devolvería ModelMapper al convertir el servicio.
     */
    public static ServicioAlojamientoDTO servicioDto(Long id, String nombre, Double costo, Boolean adicional) {
        return new ServicioAlojamientoDTO(id, nombre, costo, adicional, ID_PUBLICACION);
    }

    public static ReglaAlojamiento regla(Long id, String descripcion, Publicacion publicacion) {
        ReglaAlojamiento r = new ReglaAlojamiento();
        r.setIdRegla(id);
        r.setDescripcion(descripcion);
        r.setPublicacion(publicacion);
        return r;
    }

    public static ReglaAlojamientoDTO reglaDto(Long id, String descripcion) {
        return new ReglaAlojamientoDTO(id, descripcion, ID_PUBLICACION);
    }

    public static ImagenAlojamiento imagen(Long id, String url, int orden, Publicacion publicacion) {
        ImagenAlojamiento i = new ImagenAlojamiento();
        i.setIdImagen(id);
        i.setUrlImagen(url);
        i.setOrden(orden);
        i.setPublicacion(publicacion);
        return i;
    }

    public static ImagenAlojamientoDTO imagenDto(Long id, String url, int orden) {
        return new ImagenAlojamientoDTO(id, url, orden, ID_PUBLICACION);
    }

    public static PeriodoDisponibilidad periodo(Long id, LocalDate inicio, LocalDate fin, TipoPeriodo tipo,
            Publicacion publicacion) {
        PeriodoDisponibilidad p = new PeriodoDisponibilidad();
        p.setIdPeriodoDisponibilidad(id);
        p.setFechaInicio(inicio);
        p.setFechaFin(fin);
        p.setTipo(tipo);
        p.setPublicacion(publicacion);
        return p;
    }
}