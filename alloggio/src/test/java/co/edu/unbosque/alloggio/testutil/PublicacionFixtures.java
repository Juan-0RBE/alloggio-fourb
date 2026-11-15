package co.edu.unbosque.alloggio.testutil;

import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.usuarioActivo;

import co.edu.unbosque.alloggio.dto.PublicacionDTO;
import co.edu.unbosque.alloggio.entity.PoliticaCancelacion;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.TipoAlojamiento;
import co.edu.unbosque.alloggio.entity.Usuario;

/**
 * Datos de prueba reutilizables para publicaciones y disponibilidad.
 */
public final class PublicacionFixtures {

    public static final Long ID_PUBLICACION = 10L;
    public static final String OTRO_USUARIO = "otroanfitrion";

    private PublicacionFixtures() {
    }

    /**
     * Política de cancelación con el ID indicado.
     */
    public static PoliticaCancelacion politica(Long id) {
        PoliticaCancelacion p = new PoliticaCancelacion();
        p.setIdPolitica(id);
        p.setNombre("Flexible");
        return p;
    }

    /**
     * Otro usuario distinto del anfitrión de los fixtures (para casos de
     * publicación pertenecientes a otro usuario).
     */
    public static Usuario otroUsuario() {
        Usuario u = usuarioActivo();
        u.setIdUsuario(2L);
        u.setNombreUsuario(OTRO_USUARIO);
        u.setCorreoUsuario("otro.anfitrion@correo.com");
        u.setDocumentoIdentidad("2222222222");
        return u;
    }

    /**
     * DTO de publicación con todos los datos obligatorios válidos.
     */
    public static PublicacionDTO dtoPublicacionValido() {
        PublicacionDTO dto = new PublicacionDTO();
        dto.setTitulo("Casa campestre en Villa de Leyva");
        dto.setDescripcion("Casa con jardín, chimenea y vista a las montañas");
        dto.setUbicacion("Vereda Sabana, km 3");
        dto.setCiudad("Villa de Leyva");
        dto.setTipo(TipoAlojamiento.CASA);
        dto.setCapacidad(6);
        dto.setPrecioNoche(250000.0);
        dto.setTarifaLimpieza(40000.0);
        dto.setIdPoliticaCancelacion(1L);
        return dto;
    }

    /**
     * Copia manual de DTO a entidad. Se usa para simular a ModelMapper.
     */
    public static Publicacion publicacionDesde(PublicacionDTO dto) {
        Publicacion p = new Publicacion();
        p.setIdPublicacion(dto.getIdPublicacion());
        p.setTitulo(dto.getTitulo());
        p.setDescripcion(dto.getDescripcion());
        p.setUbicacion(dto.getUbicacion());
        p.setCiudad(dto.getCiudad());
        p.setTipo(dto.getTipo());
        p.setCapacidad(dto.getCapacidad());
        p.setPrecioNoche(dto.getPrecioNoche());
        p.setTarifaLimpieza(dto.getTarifaLimpieza());
        if (dto.getEstado() != null) {
            p.setEstado(dto.getEstado());
        }
        return p;
    }

    /**
     * Publicación completa (ID 10) del anfitrión indicado, en el estado indicado.
     */
    public static Publicacion publicacion(Usuario anfitrion, EstadoPublicacion estado) {
        Publicacion p = publicacionDesde(dtoPublicacionValido());
        p.setIdPublicacion(ID_PUBLICACION);
        p.setEstado(estado);
        p.setAnfitrion(anfitrion);
        p.setPoliticaCancelacion(politica(1L));
        return p;
    }

    /** DTO mínimo que devolvería ModelMapper al convertir la publicación. */
    public static PublicacionDTO dtoDe(Publicacion p) {
        PublicacionDTO dto = new PublicacionDTO();
        dto.setIdPublicacion(p.getIdPublicacion());
        dto.setTitulo(p.getTitulo());
        dto.setEstado(p.getEstado());
        return dto;
    }
}