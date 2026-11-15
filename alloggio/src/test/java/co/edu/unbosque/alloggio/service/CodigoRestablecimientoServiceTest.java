package co.edu.unbosque.alloggio.service;

import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.CORREO;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.usuarioActivo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.alloggio.dto.CodigoRestablecimientoDTO;
import co.edu.unbosque.alloggio.dto.UsuarioDTO;
import co.edu.unbosque.alloggio.entity.CodigoRestablecimiento;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.CodigoRestablecimientoRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;

/**
 * Pruebas unitarias del flujo de recuperación de contraseña.
 *
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CodigoRestablecimientoService - recuperación de contraseña")
class CodigoRestablecimientoServiceTest {

    private static final int MINUTOS_VIGENCIA = 15;
    private static final String CODIGO = "123456";
    private static final String NUEVA_CLAVE = "NuevaClave1!";

    @Mock
    private CodigoRestablecimientoRepository codigoRepo;

    @Mock
    private UsuarioRepository usuarioRepo;

    @Mock
    private UsuarioService usuarioServ;

    @Mock
    private EmailService emailServ;

    @InjectMocks
    private CodigoRestablecimientoService servicio;

    @BeforeEach
    void configurarVigencia() {
        ReflectionTestUtils.setField(servicio, "minutosVigencia", MINUTOS_VIGENCIA);
    }

    private static CodigoRestablecimiento codigoPendiente(Usuario usuario, String codigo, LocalDateTime expira) {
        CodigoRestablecimiento c = new CodigoRestablecimiento();
        c.setCodigo(codigo);
        c.setFechaCreacion(LocalDateTime.now().minusMinutes(1));
        c.setFechaExpiracion(expira);
        c.setUsado(false);
        c.setUsuario(usuario);
        return c;
    }

    private static CodigoRestablecimientoDTO solicitudDeCambio(String codigo, String nuevaClave) {
        CodigoRestablecimientoDTO dto = new CodigoRestablecimientoDTO();
        dto.setCorreoUsuario(CORREO);
        dto.setCodigo(codigo);
        dto.setNuevaClave(nuevaClave);
        return dto;
    }

    // CU-003 - Solicitar el código

    @Nested
    @DisplayName("CU-003 - Solicitar código de recuperación")
    class SolicitarCodigo {

        @Test
        @DisplayName("Complementaria (CA-10) - Genera un código de 6 dígitos con vigencia, lo registra y lo envía")
        void solicitarCodigo_correoRegistrado_registraCodigoVigenteYLoEnvia() {
            Usuario usuario = usuarioActivo();
            when(usuarioRepo.findByCorreoUsuario(CORREO)).thenReturn(Optional.of(usuario));
            when(emailServ.enviarEmailDeRecuperacion(eq(CORREO), anyString(), eq(MINUTOS_VIGENCIA)))
                    .thenReturn(true);

            int resultado = servicio.solicitarCodigo(CORREO);

            assertEquals(0, resultado);
            verify(codigoRepo).invalidarPendientes(usuario);
            ArgumentCaptor<CodigoRestablecimiento> captor = ArgumentCaptor.forClass(CodigoRestablecimiento.class);
            verify(codigoRepo).save(captor.capture());
            CodigoRestablecimiento guardado = captor.getValue();
            assertAll("código registrado",
                    () -> assertTrue(guardado.getCodigo().matches("\\d{6}")),
                    () -> assertFalse(guardado.getUsado()),
                    () -> assertEquals(usuario, guardado.getUsuario()),
                    () -> assertEquals(Duration.ofMinutes(MINUTOS_VIGENCIA),
                            Duration.between(guardado.getFechaCreacion(), guardado.getFechaExpiracion())));
            verify(emailServ).enviarEmailDeRecuperacion(CORREO, guardado.getCodigo(), MINUTOS_VIGENCIA);
        }

        @Test
        @DisplayName("CP-009 - Recuperación con correo no registrado (solicitud)")
        void solicitarCodigo_correoNoRegistrado_retorna1SinGenerarNiEnviarCodigo() {
            when(usuarioRepo.findByCorreoUsuario("nadie@correo.com")).thenReturn(Optional.empty());

            int resultado = servicio.solicitarCodigo("nadie@correo.com");

            assertEquals(1, resultado);
            verifyNoInteractions(codigoRepo, emailServ);
        }
    }

    // CU-003 - Confirmar el cambio

    @Nested
    @DisplayName("CU-003 - Confirmar el código y definir la nueva contraseña")
    class ConfirmarCambio {

        @Test
        @DisplayName("CP-008 - Recuperación de contraseña exitosa")
        void confirmarCambio_codigoVigente_actualizaClaveYMarcaCodigoComoUsado() {
            Usuario usuario = usuarioActivo();
            CodigoRestablecimiento pendiente = codigoPendiente(usuario, CODIGO,
                    LocalDateTime.now().plusMinutes(10));
            when(usuarioRepo.findByCorreoUsuario(CORREO)).thenReturn(Optional.of(usuario));
            when(codigoRepo.findTopByUsuarioAndUsadoFalseOrderByFechaCreacionDesc(usuario))
                    .thenReturn(Optional.of(pendiente));
            when(usuarioServ.updatePasswordById(eq(usuario.getIdUsuario()), any(UsuarioDTO.class))).thenReturn(0);

            int resultado = servicio.confirmarCambio(solicitudDeCambio(CODIGO, NUEVA_CLAVE));

            assertEquals(0, resultado);
            ArgumentCaptor<UsuarioDTO> captor = ArgumentCaptor.forClass(UsuarioDTO.class);
            verify(usuarioServ).updatePasswordById(eq(usuario.getIdUsuario()), captor.capture());
            assertEquals(NUEVA_CLAVE, captor.getValue().getClaveUsuario());
            assertTrue(pendiente.getUsado(), "El código no puede reutilizarse");
            verify(codigoRepo).save(pendiente);
        }

        @Test
        @DisplayName("CP-009 - Recuperación con correo no registrado (confirmación)")
        void confirmarCambio_correoNoRegistrado_retorna1() {
            when(usuarioRepo.findByCorreoUsuario(CORREO)).thenReturn(Optional.empty());

            int resultado = servicio.confirmarCambio(solicitudDeCambio(CODIGO, NUEVA_CLAVE));

            assertEquals(1, resultado);
            verifyNoInteractions(codigoRepo, usuarioServ);
        }

        @Test
        @DisplayName("CP-010 - Recuperación con código incorrecto")
        void confirmarCambio_codigoIncorrecto_retorna2YNoCambiaLaClave() {
            Usuario usuario = usuarioActivo();
            CodigoRestablecimiento pendiente = codigoPendiente(usuario, CODIGO,
                    LocalDateTime.now().plusMinutes(10));
            when(usuarioRepo.findByCorreoUsuario(CORREO)).thenReturn(Optional.of(usuario));
            when(codigoRepo.findTopByUsuarioAndUsadoFalseOrderByFechaCreacionDesc(usuario))
                    .thenReturn(Optional.of(pendiente));

            int resultado = servicio.confirmarCambio(solicitudDeCambio("999999", NUEVA_CLAVE));

            assertEquals(2, resultado);
            verify(usuarioServ, never()).updatePasswordById(any(), any(UsuarioDTO.class));
            assertFalse(pendiente.getUsado());
        }

        @Test
        @DisplayName("CP-010 - Recuperación con código expirado")
        void confirmarCambio_codigoExpirado_retorna2YNoCambiaLaClave() {
            Usuario usuario = usuarioActivo();
            CodigoRestablecimiento vencido = codigoPendiente(usuario, CODIGO, LocalDateTime.now().minusMinutes(1));
            when(usuarioRepo.findByCorreoUsuario(CORREO)).thenReturn(Optional.of(usuario));
            when(codigoRepo.findTopByUsuarioAndUsadoFalseOrderByFechaCreacionDesc(usuario))
                    .thenReturn(Optional.of(vencido));

            int resultado = servicio.confirmarCambio(solicitudDeCambio(CODIGO, NUEVA_CLAVE));

            assertEquals(2, resultado);
            verify(usuarioServ, never()).updatePasswordById(any(), any(UsuarioDTO.class));
            assertFalse(vencido.getUsado());
        }

        @Test
        @DisplayName("CP-010 - Recuperación sin código pendiente (ya usado o nunca solicitado)")
        void confirmarCambio_sinCodigoPendiente_retorna2() {
            Usuario usuario = usuarioActivo();
            when(usuarioRepo.findByCorreoUsuario(CORREO)).thenReturn(Optional.of(usuario));
            when(codigoRepo.findTopByUsuarioAndUsadoFalseOrderByFechaCreacionDesc(usuario))
                    .thenReturn(Optional.empty());

            int resultado = servicio.confirmarCambio(solicitudDeCambio(CODIGO, NUEVA_CLAVE));

            assertEquals(2, resultado);
            verify(usuarioServ, never()).updatePasswordById(any(), any(UsuarioDTO.class));
        }

    }

    // CU-002 - Usuario que olvidó su contraseña

    @Nested
    @DisplayName("CU-002 A1 - Olvidé mi contraseña")
    class OlvideMiContrasena {

        @Test
        @DisplayName("CP-005 - Flujo completo de recuperación de contraseña olvidada")
        void flujoCompleto_solicitarYConfirmar_recuperaLaContrasena() {
            Usuario usuario = usuarioActivo();
            when(usuarioRepo.findByCorreoUsuario(CORREO)).thenReturn(Optional.of(usuario));
            when(emailServ.enviarEmailDeRecuperacion(eq(CORREO), anyString(), eq(MINUTOS_VIGENCIA)))
                    .thenReturn(true);

            assertEquals(0, servicio.solicitarCodigo(CORREO));
            ArgumentCaptor<CodigoRestablecimiento> captor = ArgumentCaptor.forClass(CodigoRestablecimiento.class);
            verify(codigoRepo, times(1)).save(captor.capture());
            CodigoRestablecimiento generado = captor.getValue();

            when(codigoRepo.findTopByUsuarioAndUsadoFalseOrderByFechaCreacionDesc(usuario))
                    .thenReturn(Optional.of(generado));
            when(usuarioServ.updatePasswordById(eq(usuario.getIdUsuario()), any(UsuarioDTO.class))).thenReturn(0);

            assertEquals(0, servicio.confirmarCambio(solicitudDeCambio(generado.getCodigo(), NUEVA_CLAVE)));
            assertTrue(generado.getUsado());
            verify(usuarioServ).updatePasswordById(eq(usuario.getIdUsuario()), any(UsuarioDTO.class));
        }
    }
}