package co.edu.unbosque.alloggio.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.alloggio.dto.NotificacionDTO;
import co.edu.unbosque.alloggio.entity.Notificacion;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.NotificacionRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;
import co.edu.unbosque.alloggio.testutil.NotificacionFixtures;

/**
 * Pruebas unitarias de NotificacionService.
 * Casos de prueba que cubre: CP-059 a CP-060.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacionService - Módulo de Notificaciones")
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepo;

    @Mock
    private UsuarioRepository usuarioRepo;

    @InjectMocks
    private NotificacionService notificacionService;

    private static final String NOMBRE_USUARIO = "huespedTest";

    @Nested
    @DisplayName("CU-023 - Consulta y gestión de notificaciones")
    class ConsultaNotificaciones {

        @Test
        @DisplayName("CP-059 - Consulta de notificaciones exitosa con elementos en bandeja")
        void obtenerMisNotificaciones_conNotificaciones_retornaListaDTO() {
            // Arrange
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(1L);
            usuario.setNombreUsuario(NOMBRE_USUARIO);

            Notificacion notif = NotificacionFixtures.notificacionValida(usuario, "Tu reserva fue confirmada.");

            when(notificacionRepo.findByUsuarioNombreUsuarioOrderByFechaDesc(NOMBRE_USUARIO))
                    .thenReturn(List.of(notif));

            // Act
            List<NotificacionDTO> resultado = notificacionService.obtenerMisNotificaciones(NOMBRE_USUARIO);

            // Assert
            assertAll(
                    () -> assertTrue(!resultado.isEmpty()),
                    () -> assertEquals(1, resultado.size()),
                    () -> assertEquals(NotificacionFixtures.ID_NOTIFICACION, resultado.get(0).getIdNotificacion()),
                    () -> assertEquals("Tu reserva fue confirmada.", resultado.get(0).getMensaje()),
                    () -> verify(notificacionRepo).findByUsuarioNombreUsuarioOrderByFechaDesc(NOMBRE_USUARIO));
        }

        @Test
        @DisplayName("CP-060 - Consulta de notificaciones sin elementos (bandeja vacía)")
        void obtenerMisNotificaciones_sinNotificaciones_retornaListaVacia() {
            // Arrange
            when(notificacionRepo.findByUsuarioNombreUsuarioOrderByFechaDesc(NOMBRE_USUARIO))
                    .thenReturn(List.of());

            // Act
            List<NotificacionDTO> resultado = notificacionService.obtenerMisNotificaciones(NOMBRE_USUARIO);

            // Assert
            assertAll(
                    () -> assertTrue(resultado.isEmpty()),
                    () -> verify(notificacionRepo).findByUsuarioNombreUsuarioOrderByFechaDesc(NOMBRE_USUARIO));
        }

        @Test
        @DisplayName("Complementaria - Marcar notificación propia como leída exitosamente")
        void marcarLeida_notificacionPropia_retornaCeroYActualizaEstado() {
            // Arrange
            Usuario usuario = new Usuario();
            usuario.setNombreUsuario(NOMBRE_USUARIO);

            Notificacion notif = NotificacionFixtures.notificacionValida(usuario, "Mensaje test");

            when(notificacionRepo.findById(NotificacionFixtures.ID_NOTIFICACION)).thenReturn(Optional.of(notif));

            // Act
            int estado = notificacionService.marcarLeida(NOMBRE_USUARIO, NotificacionFixtures.ID_NOTIFICACION);

            // Assert
            assertAll(
                    () -> assertEquals(0, estado),
                    () -> assertTrue(notif.getEstaLeida()),
                    () -> verify(notificacionRepo).save(notif));
        }

        @Test
        @DisplayName("Complementaria (Seguridad) - Marcar notificación de otro usuario retorna dos (no autorizada)")
        void marcarLeida_notificacionAjena_retornaDosYNoGuarda() {
            // Arrange
            Usuario otroUsuario = new Usuario();
            otroUsuario.setNombreUsuario("otroUser");

            Notificacion notif = NotificacionFixtures.notificacionValida(otroUsuario, "Mensaje ajeno");

            when(notificacionRepo.findById(NotificacionFixtures.ID_NOTIFICACION)).thenReturn(Optional.of(notif));

            // Act
            int estado = notificacionService.marcarLeida(NOMBRE_USUARIO, NotificacionFixtures.ID_NOTIFICACION);

            // Assert
            assertAll(
                    () -> assertEquals(2, estado),
                    () -> assertTrue(!notif.getEstaLeida()),
                    () -> verify(notificacionRepo, never()).save(any()));
        }
    }
}