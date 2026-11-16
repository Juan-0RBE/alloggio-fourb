package co.edu.unbosque.alloggio.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.alloggio.dto.ModeracionPublicacionDTO;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.Reserva;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.PublicacionRepository;
import co.edu.unbosque.alloggio.repository.ReservaRepository;
import co.edu.unbosque.alloggio.testutil.PublicacionModeracionFixtures;

/**
 * Pruebas unitarias de PublicacionService enfocadas en la moderación y bloqueo.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PublicacionService - Módulo de Moderación de Publicaciones")
class PublicacionModeracionServiceTest {

        @Mock
        private PublicacionRepository publicacionRepo;

        @Mock
        private ReservaRepository reservaRepo;

        @Mock
        private EventoAuditoriaService auditoriaService;

        @InjectMocks
        private PublicacionService publicacionService;

        private static final String ADMIN = "adminTest";

        @Nested
        @DisplayName("CU-026 - Bloqueo de publicaciones por parte del administrador")
        class BloquearPublicacion {

                @Test
                @DisplayName("CP-Admin-01 - Bloqueo de publicación activa exitoso")
                void bloquearPublicacion_activaConMotivoValido_bloqueaYRetornaCero() {
                        // Arrange
                        ModeracionPublicacionDTO dto = new ModeracionPublicacionDTO();
                        dto.setMotivoBloqueo("Incumplimiento de normas de la comunidad");

                        Usuario anfitrion = new Usuario();
                        anfitrion.setNombreUsuario("anfitrion1");

                        Publicacion pub = PublicacionModeracionFixtures.publicacionActiva(anfitrion);

                        when(publicacionRepo.findByIdParaActualizar(PublicacionModeracionFixtures.ID_PUBLICACION))
                                        .thenReturn(Optional.of(pub));
                        when(reservaRepo.findByPublicacionIdPublicacionAndEstadoAndFechaSalidaAfterOrderByFechaLlegadaAsc(
                                        eq(PublicacionModeracionFixtures.ID_PUBLICACION), any(), any()))
                                        .thenReturn(List.of());

                        // Act
                        int resultado = publicacionService.bloquearPublicacion(ADMIN,
                                        PublicacionModeracionFixtures.ID_PUBLICACION,
                                        dto);

                        // Assert
                        assertAll(
                                        () -> assertEquals(0, resultado),
                                        () -> assertEquals(EstadoPublicacion.BLOQUEADA, pub.getEstado()),
                                        () -> assertEquals("Incumplimiento de normas de la comunidad",
                                                        pub.getMotivoBloqueo()),
                                        () -> assertEquals(0, dto.getTotalReservasFuturas()),
                                        () -> verify(publicacionRepo).save(pub),
                                        () -> verify(auditoriaService).registrarExito(eq(ADMIN), any(), any(),
                                                        eq(PublicacionModeracionFixtures.ID_PUBLICACION), anyString()));
                }

                @Test
                @DisplayName("CP-Admin-02 - Bloqueo fallido por motivo vacío o nulo")
                void bloquearPublicacion_motivoVacio_retornaDos() {
                        // Arrange
                        ModeracionPublicacionDTO dto = new ModeracionPublicacionDTO();
                        dto.setMotivoBloqueo("   ");

                        // Act
                        int resultado = publicacionService.bloquearPublicacion(ADMIN,
                                        PublicacionModeracionFixtures.ID_PUBLICACION,
                                        dto);

                        // Assert
                        assertAll(
                                        () -> assertEquals(2, resultado),
                                        () -> verify(publicacionRepo, never()).findByIdParaActualizar(any()),
                                        () -> verifyNoInteractions(auditoriaService));
                }

                @Test
                @DisplayName("CP-Admin-03 - Bloqueo fallido por publicación no encontrada")
                void bloquearPublicacion_noExiste_retornaUno() {
                        // Arrange
                        ModeracionPublicacionDTO dto = new ModeracionPublicacionDTO();
                        dto.setMotivoBloqueo("Motivo válido");

                        when(publicacionRepo.findByIdParaActualizar(PublicacionModeracionFixtures.ID_PUBLICACION))
                                        .thenReturn(Optional.empty());

                        // Act
                        int resultado = publicacionService.bloquearPublicacion(ADMIN,
                                        PublicacionModeracionFixtures.ID_PUBLICACION,
                                        dto);

                        // Assert
                        assertAll(
                                        () -> assertEquals(1, resultado),
                                        () -> verify(publicacionRepo, never()).save(any()));
                }

                @Test
                @DisplayName("CP-Admin-04 - Bloqueo fallido de publicación en estado incorrecto (BORRADOR)")
                void bloquearPublicacion_estadoBorrador_retornaTres() {
                        // Arrange
                        ModeracionPublicacionDTO dto = new ModeracionPublicacionDTO();
                        dto.setMotivoBloqueo("Motivo válido");

                        Publicacion pub = PublicacionModeracionFixtures
                                        .publicacionBorrador(PublicacionModeracionFixtures.ID_PUBLICACION);

                        when(publicacionRepo.findByIdParaActualizar(PublicacionModeracionFixtures.ID_PUBLICACION))
                                        .thenReturn(Optional.of(pub));

                        // Act
                        int resultado = publicacionService.bloquearPublicacion(ADMIN,
                                        PublicacionModeracionFixtures.ID_PUBLICACION,
                                        dto);

                        // Assert
                        assertAll(
                                        () -> assertEquals(3, resultado),
                                        () -> verify(publicacionRepo, never()).save(any()));
                }
        }

        @Nested
        @DisplayName("Consulta y Listado para Moderación")
        class ConsultaModeracion {

                @Test
                @DisplayName("Listar para moderación excluye borradores")
                void listarParaModeracion_excluyeBorradores() {
                        // Arrange
                        Publicacion pubActiva = PublicacionModeracionFixtures.publicacionActiva(new Usuario());
                        Publicacion pubBorrador = PublicacionModeracionFixtures.publicacionBorrador(2L);

                        when(publicacionRepo.findAll()).thenReturn(List.of(pubActiva, pubBorrador));

                        // Act
                        List<ModeracionPublicacionDTO> lista = publicacionService.listarParaModeracion(null);

                        // Assert
                        assertAll(
                                        () -> assertEquals(1, lista.size()),
                                        () -> assertEquals(1L, lista.get(0).getIdPublicacion()));
                }

                @Test
                @DisplayName("Obtener detalle para moderación retorna null si es borrador")
                void obtenerParaModeracion_siEsBorrador_retornaNull() {
                        // Arrange
                        Publicacion pubBorrador = PublicacionModeracionFixtures
                                        .publicacionBorrador(PublicacionModeracionFixtures.ID_PUBLICACION);

                        when(publicacionRepo.findById(PublicacionModeracionFixtures.ID_PUBLICACION))
                                        .thenReturn(Optional.of(pubBorrador));

                        // Act
                        ModeracionPublicacionDTO resultado = publicacionService
                                        .obtenerParaModeracion(PublicacionModeracionFixtures.ID_PUBLICACION);

                        // Assert
                        assertNull(resultado);
                }
        }

        @Test
        @DisplayName("Obtener detalle para moderación incluye reservas futuras confirmadas")
        void obtenerParaModeracion_conReservasFuturas_mapeaReservaModeracionDTO() {
                // Arrange
                Usuario anfitrion = new Usuario();
                anfitrion.setNombreUsuario("anfitrion1");
                anfitrion.setPrimerNombre("Carlos");
                anfitrion.setPrimerApellido("Gomez");

                Usuario huesped = new Usuario();
                huesped.setNombreUsuario("huesped1");
                huesped.setPrimerNombre("Ana");
                huesped.setPrimerApellido("Perez");

                Publicacion pub = PublicacionModeracionFixtures.publicacionActiva(anfitrion);
                Reserva reservaFutura = PublicacionModeracionFixtures.reservaFuturaConfirmada(100L, pub, huesped);

                when(publicacionRepo.findById(PublicacionModeracionFixtures.ID_PUBLICACION))
                                .thenReturn(Optional.of(pub));
                when(reservaRepo.findByPublicacionIdPublicacionAndEstadoAndFechaSalidaAfterOrderByFechaLlegadaAsc(
                                eq(PublicacionModeracionFixtures.ID_PUBLICACION), eq(Reserva.EstadoReserva.CONFIRMADA),
                                any(LocalDate.class)))
                                .thenReturn(List.of(reservaFutura));

                // Act
                ModeracionPublicacionDTO resultado = publicacionService
                                .obtenerParaModeracion(PublicacionModeracionFixtures.ID_PUBLICACION);

                // Assert
                assertAll(
                                () -> assertNotNull(resultado),
                                () -> assertEquals(1, resultado.getTotalReservasFuturas()),
                                () -> assertEquals(1, resultado.getReservasFuturas().size()),
                                () -> assertEquals(100L, resultado.getReservasFuturas().get(0).getIdReserva()),
                                () -> assertEquals("huesped1",
                                                resultado.getReservasFuturas().get(0).getNombreUsuarioHuesped()),
                                () -> assertEquals("Ana Perez",
                                                resultado.getReservasFuturas().get(0).getNombreHuesped()));
        }
}