package co.edu.unbosque.alloggio.testutil;

import co.edu.unbosque.alloggio.entity.Usuario;

public class UsuarioModeracionFixtures {

    public static final Long ID_USUARIO = 10L;
    public static final String ADMIN_USER = "adminTest";

    public static Usuario usuarioHuesped(String nombreUsuario) {
        Usuario u = new Usuario();
        u.setIdUsuario(ID_USUARIO);
        u.setNombreUsuario(nombreUsuario);
        u.setRole(Usuario.Role.USUARIO);
        u.setEstadoCuenta("ACTIVA");
        return u;
    }

    public static Usuario usuarioAdministrador(String nombreUsuario) {
        Usuario u = new Usuario();
        u.setIdUsuario(ID_USUARIO);
        u.setNombreUsuario(nombreUsuario);
        u.setRole(Usuario.Role.ADMINISTRADOR);
        u.setEstadoCuenta("ACTIVA");
        return u;
    }
}