package co.edu.unbosque.alloggio.controller;

import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.CLAVE_VALIDA;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.CORREO;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.NOMBRE_USUARIO;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.dtoRegistroValido;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.usuarioActivo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.alloggio.dto.UsuarioDTO;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.security.JwtUtil;
import co.edu.unbosque.alloggio.service.EmailService;
import co.edu.unbosque.alloggio.service.UsuarioService;

/**
 * Pruebas unitarias de AuthController.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - registro, login y reactivación")
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UsuarioService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthController authController;

    private static String cuerpo(ResponseEntity<?> respuesta) {
        return String.valueOf(respuesta.getBody());
    }

    // CU-001 - Registrar usuario

    @Nested
    @DisplayName("CU-001 - Registrar usuario")
    class RegistrarUsuario {

        @Test
        @DisplayName("CP-001 - Registro de usuario exitoso")
        void register_datosValidos_retorna201YEnviaCorreoDeVerificacion() throws Exception {
            UsuarioDTO dto = dtoRegistroValido();
            when(userService.create(dto)).thenReturn(0);
            when(userService.getByCorreo(CORREO)).thenReturn(dto);

            ResponseEntity<?> respuesta = authController.register(dto);

            assertEquals(201, respuesta.getStatusCode().value());
            assertTrue(cuerpo(respuesta).contains("registrado exitosamente"));
            verify(emailService).enviarEmailDeVerificacion(CORREO);
        }

        @Test
        @DisplayName("CP-002 - Registro con correo ya existente")
        void register_correoExistente_retorna409YNoCreaCuenta() {
            UsuarioDTO dto = dtoRegistroValido();
            when(userService.findEmailAlreadyTaken(CORREO)).thenReturn(true);

            ResponseEntity<?> respuesta = authController.register(dto);

            assertEquals(409, respuesta.getStatusCode().value());
            assertTrue(cuerpo(respuesta).contains("correo"));
            verify(userService, never()).create(any(UsuarioDTO.class));
            verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("CP-002 (variante) - Registro con nombre de usuario ya existente")
        void register_nombreUsuarioExistente_retorna409YNoCreaCuenta() {
            UsuarioDTO dto = dtoRegistroValido();
            when(userService.findUsernameAlreadyTaken(NOMBRE_USUARIO)).thenReturn(true);

            ResponseEntity<?> respuesta = authController.register(dto);

            assertEquals(409, respuesta.getStatusCode().value());
            verify(userService, never()).create(any(UsuarioDTO.class));
        }

        @Test
        @DisplayName("CP-003 - Registro con datos faltantes: retorna 400 e indica los campos")
        void register_datosFaltantes_retorna400IndicandoLosCampos() {
            UsuarioDTO dto = dtoRegistroValido();
            when(userService.camposInvalidosRegistro(dto)).thenReturn(List.of("fechaNacimiento", "numeroTelefono"));

            ResponseEntity<?> respuesta = authController.register(dto);

            assertEquals(400, respuesta.getStatusCode().value());
            assertTrue(cuerpo(respuesta).contains("fechaNacimiento"));
            assertTrue(cuerpo(respuesta).contains("numeroTelefono"));
            verify(userService, never()).create(any(UsuarioDTO.class));
        }

        @Test
        @DisplayName("CP-003 - Registro con contraseña débil: retorna 400 y lo indica")
        void register_claveDebil_retorna400IndicandoLaContrasena() {
            UsuarioDTO dto = dtoRegistroValido();
            when(userService.create(dto)).thenReturn(2);

            ResponseEntity<?> respuesta = authController.register(dto);

            assertEquals(400, respuesta.getStatusCode().value());
            assertTrue(cuerpo(respuesta).toLowerCase().contains("contraseña"));
            verifyNoInteractions(emailService);
        }
    }

    // CU-002 - Iniciar sesión

    @Nested
    @DisplayName("CU-002 - Iniciar sesión")
    class IniciarSesion {

        private UsuarioDTO solicitud(String nombre, String clave) {
            UsuarioDTO dto = new UsuarioDTO();
            dto.setNombreUsuario(nombre);
            dto.setClaveUsuario(clave);
            return dto;
        }

        /**
         * Cuenta tal como la devuelve el service al consultar por nombre de usuario.
         */
        private UsuarioDTO cuentaEnBd(boolean verificada, boolean activa, String estadoCuenta) {
            UsuarioDTO dto = new UsuarioDTO();
            dto.setNombreUsuario(NOMBRE_USUARIO);
            dto.setIsVerified(verificada);
            dto.setEstaActivo(activa);
            dto.setEstadoCuenta(estadoCuenta);
            return dto;
        }

        @Test
        @DisplayName("CP-004 - Inicio de sesión exitoso")
        void login_credencialesCorrectasYCuentaActiva_retorna200ConTokenYRol() {
            Usuario usuario = usuarioActivo();
            Authentication autenticacion = mock(Authentication.class);
            when(userService.getByNombreUsuario(NOMBRE_USUARIO))
                    .thenReturn(cuentaEnBd(true, true, Usuario.ESTADO_ACTIVA));
            when(authenticationManager.authenticate(any())).thenReturn(autenticacion);
            when(autenticacion.getPrincipal()).thenReturn(usuario);
            when(jwtUtil.generateToken(usuario)).thenReturn("jwt-token");

            ResponseEntity<?> respuesta = authController.login(solicitud(NOMBRE_USUARIO, CLAVE_VALIDA));

            assertEquals(200, respuesta.getStatusCode().value());
            Object cuerpoRespuesta = respuesta.getBody();
            assertEquals("jwt-token", ReflectionTestUtils.getField(cuerpoRespuesta, "token"));
            assertEquals("USUARIO", ReflectionTestUtils.getField(cuerpoRespuesta, "role"));
        }

        @Test
        @DisplayName("CP-006 - Inicio de sesión con contraseña incorrecta")
        void login_claveIncorrecta_retorna401SinGenerarToken() {
            when(userService.getByNombreUsuario(NOMBRE_USUARIO))
                    .thenReturn(cuentaEnBd(true, true, Usuario.ESTADO_ACTIVA));
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

            ResponseEntity<?> respuesta = authController.login(solicitud(NOMBRE_USUARIO, "ClaveEquivocada1!"));

            assertEquals(401, respuesta.getStatusCode().value());
            verifyNoInteractions(jwtUtil);
        }

        @Test
        @DisplayName("CP-006 - Inicio de sesión con usuario inexistente")
        void login_usuarioInexistente_retorna401SinIntentarAutenticar() {
            when(userService.getByNombreUsuario("fantasma")).thenReturn(null);

            ResponseEntity<?> respuesta = authController.login(solicitud("fantasma", CLAVE_VALIDA));

            assertEquals(401, respuesta.getStatusCode().value());
            verifyNoInteractions(authenticationManager, jwtUtil);
        }

        @Test
        @DisplayName("CP-006 (CA-04) - El mensaje no revela cuál dato es incorrecto")
        void login_usuarioInexistenteOClaveIncorrecta_devuelvenElMismoMensaje() {
            when(userService.getByNombreUsuario("fantasma")).thenReturn(null);
            when(userService.getByNombreUsuario(NOMBRE_USUARIO))
                    .thenReturn(cuentaEnBd(true, true, Usuario.ESTADO_ACTIVA));
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

            ResponseEntity<?> sinUsuario = authController.login(solicitud("fantasma", CLAVE_VALIDA));
            ResponseEntity<?> sinClave = authController.login(solicitud(NOMBRE_USUARIO, "ClaveEquivocada1!"));

            assertEquals(cuerpo(sinUsuario), cuerpo(sinClave));
        }

        @Test
        @DisplayName("CP-007 - Inicio de sesión con cuenta desactivada")
        void login_cuentaDesactivada_retorna403IndicandoQueEstaInactiva() {
            Authentication autenticacion = mock(Authentication.class);
            when(userService.getByNombreUsuario(NOMBRE_USUARIO))
                    .thenReturn(cuentaEnBd(true, false, Usuario.ESTADO_ACTIVA));
            when(authenticationManager.authenticate(any())).thenReturn(autenticacion);

            ResponseEntity<?> respuesta = authController.login(solicitud(NOMBRE_USUARIO, CLAVE_VALIDA));

            assertEquals(403, respuesta.getStatusCode().value());
            assertTrue(cuerpo(respuesta).contains("inactiva"));
            verifyNoInteractions(jwtUtil);
        }

        @Test
        @DisplayName("CP-004 (variante) - Inicio de sesión exitoso usando correo electrónico")
        void login_conCorreoValido_retorna200ConToken() {
            Usuario usuario = usuarioActivo();
            Authentication autenticacion = mock(Authentication.class);
            UsuarioDTO req = new UsuarioDTO();
            req.setCorreoUsuario(CORREO);
            req.setClaveUsuario(CLAVE_VALIDA);

            when(userService.getByCorreo(CORREO))
                    .thenReturn(cuentaEnBd(true, true, Usuario.ESTADO_ACTIVA));
            when(authenticationManager.authenticate(any())).thenReturn(autenticacion);
            when(autenticacion.getPrincipal()).thenReturn(usuario);
            when(jwtUtil.generateToken(usuario)).thenReturn("jwt-token");

            ResponseEntity<?> respuesta = authController.login(req);

            assertEquals(200, respuesta.getStatusCode().value());
            verify(userService).getByCorreo(CORREO);
            verify(userService, never()).getByNombreUsuario(any());
        }

        @Test
        @DisplayName("CP-006 (variante) - Inicio de sesión con correo vacío y sin usuario")
        void login_correoVacioYSinUsuario_retorna401() {
            UsuarioDTO req = new UsuarioDTO();
            req.setCorreoUsuario("");
            req.setNombreUsuario("");
            req.setClaveUsuario(CLAVE_VALIDA);

            ResponseEntity<?> respuesta = authController.login(req);

            assertEquals(401, respuesta.getStatusCode().value());
            verifyNoInteractions(userService, authenticationManager, jwtUtil);
        }

        @Test
        @DisplayName("CP-008 - Inicio de sesión con cuenta restringida retorna 403")
        void login_cuentaRestringida_retorna403() {
            Authentication autenticacion = mock(Authentication.class);
            when(userService.getByNombreUsuario(NOMBRE_USUARIO))
                    .thenReturn(cuentaEnBd(true, true, Usuario.ESTADO_RESTRINGIDA));
            when(authenticationManager.authenticate(any())).thenReturn(autenticacion);

            ResponseEntity<?> respuesta = authController.login(solicitud(NOMBRE_USUARIO, CLAVE_VALIDA));

            assertEquals(403, respuesta.getStatusCode().value());
            assertTrue(cuerpo(respuesta).contains("restringida"));
            verifyNoInteractions(jwtUtil);
        }
    }

    // CU-006 - Activar cuenta

    @Nested
    @DisplayName("CU-006 - Activar cuenta")
    class ActivarCuenta {

        @Test
        @DisplayName("CP-016 - Reactivación exitosa responde 200")
        void reactivar_exitoso_retorna200() {
            UsuarioDTO datos = new UsuarioDTO();
            when(userService.reactivarCuenta(datos)).thenReturn(0);

            assertEquals(200, authController.reactivar(datos).getStatusCode().value());
        }

        @Test
        @DisplayName("CP-017 - Reactivación sin confirmar responde 200 y aclara que la cuenta sigue inactiva")
        void reactivar_sinConfirmar_retorna200ConMensajeDeCancelacion() {
            UsuarioDTO datos = new UsuarioDTO();
            when(userService.reactivarCuenta(datos)).thenReturn(4);

            ResponseEntity<String> respuesta = authController.reactivar(datos);

            assertEquals(200, respuesta.getStatusCode().value());
            assertTrue(respuesta.getBody().contains("inactiva"));
        }

    }

}