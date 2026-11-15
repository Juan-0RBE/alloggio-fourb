package co.edu.unbosque.alloggio.testutil;

import java.time.LocalDate;

import co.edu.unbosque.alloggio.dto.UsuarioDTO;
import co.edu.unbosque.alloggio.entity.Usuario;

/**
 * Datos de prueba reutilizables para el módulo de cuentas. Cada método devuelve
 * un objeto nuevo para que las pruebas no compartan estado.
 */
public final class UsuarioFixtures {

    public static final String CLAVE_VALIDA = "Clave123!";
    public static final String CLAVE_CIFRADA = "HASH_CLAVE";
    public static final String NOMBRE_USUARIO = "juanperez";
    public static final String CORREO = "juan.perez@correo.com";

    private UsuarioFixtures() {
    }

    /**
     * DTO de registro con todos los datos obligatorios válidos.
     */
    public static UsuarioDTO dtoRegistroValido() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombreUsuario(NOMBRE_USUARIO);
        dto.setCorreoUsuario(CORREO);
        dto.setClaveUsuario(CLAVE_VALIDA);
        dto.setDocumentoIdentidad("1012345678");
        dto.setPrimerNombre("Juan");
        dto.setSegundoNombre("Carlos");
        dto.setPrimerApellido("Pérez");
        dto.setSegundoApellido("Gómez");
        dto.setFechaNacimiento(LocalDate.now().minusYears(25));
        dto.setNumeroTelefono("3001234567");
        return dto;
    }

    /**
     * Copia manual de DTO a entidad. Se usa para simular a ModelMapper, que en las
     * pruebas se reemplaza por un mock.
     */
    public static Usuario entidadDesde(UsuarioDTO dto) {
        Usuario u = new Usuario();
        u.setIdUsuario(dto.getIdUsuario());
        u.setNombreUsuario(dto.getNombreUsuario());
        u.setCorreoUsuario(dto.getCorreoUsuario());
        u.setClaveUsuario(dto.getClaveUsuario());
        u.setDocumentoIdentidad(dto.getDocumentoIdentidad());
        u.setPrimerNombre(dto.getPrimerNombre());
        u.setSegundoNombre(dto.getSegundoNombre());
        u.setPrimerApellido(dto.getPrimerApellido());
        u.setSegundoApellido(dto.getSegundoApellido());
        u.setFechaNacimiento(dto.getFechaNacimiento());
        u.setNumeroTelefono(dto.getNumeroTelefono());
        return u;
    }

    /**
     * Usuario ya registrado, verificado, activo y sin restricciones.
     */
    public static Usuario usuarioActivo() {
        Usuario u = new Usuario();
        u.setIdUsuario(1L);
        u.setNombreUsuario(NOMBRE_USUARIO);
        u.setCorreoUsuario(CORREO);
        u.setClaveUsuario(CLAVE_CIFRADA);
        u.setDocumentoIdentidad("1012345678");
        u.setPrimerNombre("Juan");
        u.setSegundoNombre("Carlos");
        u.setPrimerApellido("Pérez");
        u.setSegundoApellido("Gómez");
        u.setFechaNacimiento(LocalDate.now().minusYears(25));
        u.setEdad(25);
        u.setNumeroTelefono("3001234567");
        u.setIsVerified(true);
        u.setEstaActivo(true);
        u.setEstadoCuenta(Usuario.ESTADO_ACTIVA);
        u.setRole(Usuario.Role.USUARIO);
        return u;
    }

    /**
     * Usuario que desactivó voluntariamente su cuenta.
     */
    public static Usuario usuarioInactivo() {
        Usuario u = usuarioActivo();
        u.setEstaActivo(false);
        return u;
    }
}