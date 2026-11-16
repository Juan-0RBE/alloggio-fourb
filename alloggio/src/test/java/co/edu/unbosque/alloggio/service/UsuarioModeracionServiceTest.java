package co.edu.unbosque.alloggio.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.alloggio.dto.UsuarioDTO;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;
import co.edu.unbosque.alloggio.testutil.UsuarioModeracionFixtures;

/**
 * Pruebas unitarias de UsuarioService enfocadas en la moderación y restricción
 * de cuentas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Módulo de Moderación y Restricción de Usuarios")
class UsuarioModeracionServiceTest {

        @Mock
        private UsuarioRepository usuarioRepo;

        @Mock
        private EventoAuditoriaService auditoriaService;

        @InjectMocks
        private UsuarioService usuarioService;

        @Nested
        @DisplayName("CU-027 - Restricción de cuentas de usuario")
        class RestringirUsuario {

                @Test
                @DisplayName("CP-Admin-05 - Restricción de usuario exitosa con confirmación")
                void restringirUsuario_usuarioValidoConConfirmacion_restringeYRetornaCero() {
                        // Arrange
                        UsuarioDTO datos = new UsuarioDTO();
                        datos.setConfirmar(true);

                        Usuario objetivo = UsuarioModeracionFixtures.usuarioHuesped("huespedProblema");

                        when(usuarioRepo.findById(UsuarioModeracionFixtures.ID_USUARIO))
                                        .thenReturn(Optional.of(objetivo));

                        // Act
                        int resultado = usuarioService.restringirUsuario(UsuarioModeracionFixtures.ADMIN_USER,
                                        UsuarioModeracionFixtures.ID_USUARIO, datos);

                        // Assert
                        assertAll(
                                        () -> assertEquals(0, resultado),
                                        () -> assertEquals(Usuario.ESTADO_RESTRINGIDA, objetivo.getEstadoCuenta()),
                                        () -> verify(usuarioRepo).save(objetivo),
                                        () -> verify(auditoriaService).registrarExito(
                                                        eq(UsuarioModeracionFixtures.ADMIN_USER), any(),
                                                        any(), eq(UsuarioModeracionFixtures.ID_USUARIO), anyString()));
                }

                @Test
                @DisplayName("CP-Admin-06 - Intento del administrador de restringir su propia cuenta retorna dos")
                void restringirUsuario_intentoAutoRestriccion_retornaDos() {
                        // Arrange
                        UsuarioDTO datos = new UsuarioDTO();
                        datos.setConfirmar(true);

                        Usuario objetivo = UsuarioModeracionFixtures
                                        .usuarioAdministrador(UsuarioModeracionFixtures.ADMIN_USER);

                        when(usuarioRepo.findById(UsuarioModeracionFixtures.ID_USUARIO))
                                        .thenReturn(Optional.of(objetivo));

                        // Act
                        int resultado = usuarioService.restringirUsuario(UsuarioModeracionFixtures.ADMIN_USER,
                                        UsuarioModeracionFixtures.ID_USUARIO, datos);

                        // Assert
                        assertAll(
                                        () -> assertEquals(2, resultado),
                                        () -> verify(usuarioRepo, never()).save(any()),
                                        () -> verify(auditoriaService).registrarDenegado(
                                                        eq(UsuarioModeracionFixtures.ADMIN_USER), any(),
                                                        any(), eq(UsuarioModeracionFixtures.ID_USUARIO), anyString()));
                }

                @Test
                @DisplayName("CP-Admin-07 - Intento de restringir a otro administrador retorna tres")
                void restringirUsuario_objetivoEsAdministrador_retornaTres() {
                        // Arrange
                        UsuarioDTO datos = new UsuarioDTO();
                        datos.setConfirmar(true);

                        Usuario objetivo = UsuarioModeracionFixtures.usuarioAdministrador("otroAdmin");

                        when(usuarioRepo.findById(UsuarioModeracionFixtures.ID_USUARIO))
                                        .thenReturn(Optional.of(objetivo));

                        // Act
                        int resultado = usuarioService.restringirUsuario(UsuarioModeracionFixtures.ADMIN_USER,
                                        UsuarioModeracionFixtures.ID_USUARIO, datos);

                        // Assert
                        assertAll(
                                        () -> assertEquals(3, resultado),
                                        () -> verify(usuarioRepo, never()).save(any()),
                                        () -> verify(auditoriaService).registrarDenegado(
                                                        eq(UsuarioModeracionFixtures.ADMIN_USER), any(),
                                                        any(), eq(UsuarioModeracionFixtures.ID_USUARIO), anyString()));
                }

                @Test
                @DisplayName("CP-Admin-08 - Intento de restringir sin marcar confirmación retorna cinco")
                void restringirUsuario_sinConfirmacion_retornaCinco() {
                        // Arrange
                        UsuarioDTO datos = new UsuarioDTO();
                        datos.setConfirmar(false);

                        Usuario objetivo = UsuarioModeracionFixtures.usuarioHuesped("huespedProblema");

                        when(usuarioRepo.findById(UsuarioModeracionFixtures.ID_USUARIO))
                                        .thenReturn(Optional.of(objetivo));

                        // Act
                        int resultado = usuarioService.restringirUsuario(UsuarioModeracionFixtures.ADMIN_USER,
                                        UsuarioModeracionFixtures.ID_USUARIO, datos);

                        // Assert
                        assertAll(
                                        () -> assertEquals(5, resultado),
                                        () -> verify(usuarioRepo, never()).save(any()));
                }
        }
}