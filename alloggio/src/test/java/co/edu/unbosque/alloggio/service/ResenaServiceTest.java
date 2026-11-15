package co.edu.unbosque.alloggio.service;

import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.ID_PUBLICACION;
import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.publicacion;
import static co.edu.unbosque.alloggio.testutil.ResenaFixtures.ID_RESENA;
import static co.edu.unbosque.alloggio.testutil.ResenaFixtures.ID_RESERVA;
import static co.edu.unbosque.alloggio.testutil.ResenaFixtures.NOMBRE_ADMIN;
import static co.edu.unbosque.alloggio.testutil.ResenaFixtures.reserva;
import static co.edu.unbosque.alloggio.testutil.ResenaFixtures.resenaVisible;
import static co.edu.unbosque.alloggio.testutil.ResenaFixtures.solicitudResena;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.NOMBRE_USUARIO;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.usuarioActivo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.alloggio.dto.ResenaDTO;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Accion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.TipoEntidad;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.Reserva;
import co.edu.unbosque.alloggio.entity.Reserva.EstadoReserva;
import co.edu.unbosque.alloggio.entity.Resena;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.ReservaRepository;
import co.edu.unbosque.alloggio.repository.ResenaRepository;

/**
 * Pruebas unitarias de ResenaService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResenaService - reseñas, calificaciones y moderación")
class ResenaServiceTest {

        @Mock
        private ResenaRepository resenaRepo;

        @Mock
        private ReservaRepository reservaRepo;

        @Mock
        private EventoAuditoriaService auditoriaService;

        @InjectMocks
        private ResenaService resenaService;

        @Nested
        @DisplayName("CU-015 - Consultar reseñas y calificaciones")
        class ConsultarResenas {

                @Test
                @DisplayName("CP-038 - Consulta de reseñas y calificaciones exitosa (reseñas visibles con su autor)")
                void obtenerVisiblesRecientes_conResenasVisibles_devuelveDtosConAutor() {
                        // Arrange
                        Usuario huesped = usuarioActivo(); // Juan Pérez
                        Resena primera = resenaVisible(1L, 5, "Excelente lugar", huesped,
                                        publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA));
                        Resena segunda = resenaVisible(2L, 4, "Muy cómodo", huesped,
                                        publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA));
                        when(resenaRepo.findTop10ByReservaPublicacionIdPublicacionAndVisibleTrueOrderByFechaDescIdResenaDesc(
                                        ID_PUBLICACION)).thenReturn(List.of(primera, segunda));

                        // Act
                        List<ResenaDTO> resenas = resenaService.obtenerVisiblesRecientes(ID_PUBLICACION);

                        // Assert
                        assertAll(
                                        () -> assertEquals(2, resenas.size()),
                                        () -> assertEquals(1L, resenas.get(0).getIdResena()),
                                        () -> assertEquals(5, resenas.get(0).getCalificacion()),
                                        () -> assertEquals("Excelente lugar", resenas.get(0).getComentario()),
                                        () -> assertEquals("Juan Pérez", resenas.get(0).getAutor()),
                                        () -> assertTrue(resenas.get(0).getVisible()),
                                        () -> assertEquals("Muy cómodo", resenas.get(1).getComentario()));
                        verify(resenaRepo, never())
                                        .findByReservaPublicacionIdPublicacionOrderByFechaDescIdResenaDesc(any());
                }

                @Test
                @DisplayName("CP-038 - Consulta de reseñas y calificaciones exitosa (promedio a un decimal)")
                void calcularPromedio_conResenasVisibles_redondeaAUnDecimal() {
                        when(resenaRepo.promedioVisible(ID_PUBLICACION)).thenReturn(4.3333);

                        Double promedio = resenaService.calcularPromedio(ID_PUBLICACION);

                        assertEquals(4.3, promedio, 0.0001);
                }

                @Test
                @DisplayName("CP-039 - Consulta de alojamiento sin reseñas visibles")
                void consultarResenas_sinResenasVisibles_listaVaciaPromedioNuloYTotalCero() {
                        // findTop10 devuelve lista vacía por defecto; el promedio se simula nulo
                        // porque Mockito devolvería 0.0 para un Double sin simular
                        when(resenaRepo.promedioVisible(ID_PUBLICACION)).thenReturn(null);

                        List<ResenaDTO> resenas = resenaService.obtenerVisiblesRecientes(ID_PUBLICACION);
                        Double promedio = resenaService.calcularPromedio(ID_PUBLICACION);
                        long total = resenaService.contarVisibles(ID_PUBLICACION);

                        assertAll(
                                        () -> assertTrue(resenas.isEmpty()),
                                        () -> assertNull(promedio),
                                        () -> assertEquals(0L, total));
                }
        }

        @Nested
        @DisplayName("CU-024 - Calificar y reseñar alojamiento")
        class CalificarYResenar {

                private final Usuario huesped = usuarioActivo();
                private final Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);

                @Test
                @DisplayName("CP-061 - Calificación y reseña de alojamiento exitosa")
                void crear_reservaCompletadaDelHuesped_registraCalificacionYResena() {
                        // Arrange
                        Reserva completada = reserva(EstadoReserva.COMPLETADA, huesped, pub);
                        when(reservaRepo.findByIdParaActualizar(ID_RESERVA)).thenReturn(Optional.of(completada));
                        when(resenaRepo.save(any(Resena.class))).thenAnswer(inv -> {
                                Resena r = inv.getArgument(0);
                                r.setIdResena(ID_RESENA);
                                return r;
                        });
                        ResenaDTO dto = solicitudResena(ID_RESERVA, 5, "  Excelente lugar, muy limpio  ");

                        // Act
                        int estado = resenaService.crear(NOMBRE_USUARIO, dto);

                        // Assert: queda vinculada a la reserva (y de esta forma al huésped y al
                        // alojamiento)
                        ArgumentCaptor<Resena> captor = ArgumentCaptor.forClass(Resena.class);
                        verify(resenaRepo).save(captor.capture());
                        Resena guardada = captor.getValue();
                        assertAll(
                                        () -> assertEquals(0, estado),
                                        () -> assertEquals(ID_RESENA, dto.getIdResena()),
                                        () -> assertEquals(5, guardada.getCalificacion()),
                                        () -> assertEquals("Excelente lugar, muy limpio", guardada.getComentario()),
                                        () -> assertSame(completada, guardada.getReserva()),
                                        () -> assertSame(huesped, guardada.getReserva().getHuesped()),
                                        () -> assertSame(pub, guardada.getReserva().getPublicacion()),
                                        () -> assertNotNull(guardada.getFecha()),
                                        () -> assertTrue(guardada.getVisible()),
                                        () -> verify(auditoriaService).registrarExito(eq(NOMBRE_USUARIO),
                                                        eq(Accion.CREAR_RESENA),
                                                        eq(TipoEntidad.RESENA), eq(ID_RESENA), anyString()));
                }

                @ParameterizedTest(name = "reserva en estado {0}")
                @EnumSource(value = EstadoReserva.class, mode = EnumSource.Mode.EXCLUDE, names = { "COMPLETADA" })
                @DisplayName("CP-062 - Calificación sin reserva completada asociada (reserva no completada)")
                void crear_reservaNoCompletada_retorna3YNoRegistra(EstadoReserva estadoReserva) {
                        when(reservaRepo.findByIdParaActualizar(ID_RESERVA))
                                        .thenReturn(Optional.of(reserva(estadoReserva, huesped, pub)));

                        int estado = resenaService.crear(NOMBRE_USUARIO, solicitudResena(ID_RESERVA, 5, "Excelente"));

                        assertEquals(3, estado);
                        verify(resenaRepo, never()).save(any(Resena.class));
                        verifyNoInteractions(auditoriaService);
                }

                @Test
                @DisplayName("CP-062 - Calificación sin reserva completada asociada (la reserva no existe)")
                void crear_reservaInexistente_retorna1YNoRegistra() {

                        int estado = resenaService.crear(NOMBRE_USUARIO, solicitudResena(999L, 5, "Excelente"));

                        assertEquals(1, estado);
                        verify(resenaRepo, never()).save(any(Resena.class));
                }

                @Test
                @DisplayName("CP-062 - Calificación sin reserva completada asociada (reserva de otro huésped)")
                void crear_reservaAjena_retorna2RegistraDenegadoYNoRegistra() {
                        Reserva ajena = reserva(EstadoReserva.COMPLETADA, huesped, pub);
                        when(reservaRepo.findByIdParaActualizar(ID_RESERVA)).thenReturn(Optional.of(ajena));

                        int estado = resenaService.crear("otrohuesped", solicitudResena(ID_RESERVA, 5, "Excelente"));

                        assertEquals(2, estado);
                        verify(resenaRepo, never()).save(any(Resena.class));
                        verify(auditoriaService).registrarDenegado(eq("otrohuesped"), eq(Accion.CREAR_RESENA),
                                        eq(TipoEntidad.RESERVA), eq(ID_RESERVA), anyString());
                }

                @ParameterizedTest(name = "calificación: {0}")
                @NullSource
                @ValueSource(ints = { 0, 6, -1 })
                @DisplayName("Complementaria (CU-024 paso 4) - La calificación debe estar entre 1 y 5")
                void crear_calificacionFueraDeRango_retorna4YNoRegistra(Integer calificacion) {
                        when(reservaRepo.findByIdParaActualizar(ID_RESERVA))
                                        .thenReturn(Optional.of(reserva(EstadoReserva.COMPLETADA, huesped, pub)));

                        int estado = resenaService.crear(NOMBRE_USUARIO,
                                        solicitudResena(ID_RESERVA, calificacion, "Comentario"));

                        assertEquals(4, estado);
                        verify(resenaRepo, never()).save(any(Resena.class));
                }

                @Test
                @DisplayName("Complementaria (RN-17, RN-18) - Una reserva tiene una sola reseña")
                void crear_reservaYaResenada_retorna6YNoDuplica() {
                        when(reservaRepo.findByIdParaActualizar(ID_RESERVA))
                                        .thenReturn(Optional.of(reserva(EstadoReserva.COMPLETADA, huesped, pub)));
                        when(resenaRepo.existsByReservaIdReserva(ID_RESERVA)).thenReturn(true);

                        int estado = resenaService.crear(NOMBRE_USUARIO, solicitudResena(ID_RESERVA, 5, "Excelente"));

                        assertEquals(6, estado);
                        verify(resenaRepo, never()).save(any(Resena.class));
                        verifyNoInteractions(auditoriaService);
                }
        }

        @Nested
        @DisplayName("CU-025 - Moderar reseña")
        class ModerarResena {

                private Resena resena;

                private Resena resenaDeModeracion() {
                        return resenaVisible(ID_RESENA, 1, "Comentario que incumple las normas", usuarioActivo(),
                                        publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA));
                }

                @Test
                @DisplayName("CP-063 - Moderación de reseña exitosa")
                void ocultar_resenaVisibleConConfirmacion_laOcultaYRegistraAuditoria() {
                        resena = resenaDeModeracion();
                        when(resenaRepo.findByIdParaActualizar(ID_RESENA)).thenReturn(Optional.of(resena));

                        int estado = resenaService.ocultar(NOMBRE_ADMIN, ID_RESENA, true);

                        // La reseña no se borra: queda oculta y por eso deja de mostrarse y de contarse
                        assertAll(
                                        () -> assertEquals(0, estado),
                                        () -> assertFalse(resena.getVisible()),
                                        () -> verify(resenaRepo).save(resena),
                                        () -> verify(auditoriaService).registrarExito(eq(NOMBRE_ADMIN),
                                                        eq(Accion.OCULTAR_RESENA),
                                                        eq(TipoEntidad.RESENA), eq(ID_RESENA), anyString()));
                }

                @ParameterizedTest(name = "confirmación: {0}")
                @NullSource
                @ValueSource(booleans = { false })
                @DisplayName("CP-064 - Moderación de reseña sin confirmar")
                void ocultar_sinConfirmar_retorna2YNoModificaLaResena(Boolean confirmar) {
                        resena = resenaDeModeracion();
                        when(resenaRepo.findByIdParaActualizar(ID_RESENA)).thenReturn(Optional.of(resena));

                        int estado = resenaService.ocultar(NOMBRE_ADMIN, ID_RESENA, confirmar);

                        assertAll(
                                        () -> assertEquals(2, estado),
                                        () -> assertTrue(resena.getVisible()),
                                        () -> verify(resenaRepo, never()).save(any(Resena.class)),
                                        () -> verifyNoInteractions(auditoriaService));
                }

                @Test
                @DisplayName("CP-065 - Moderación de reseña ya ocultada")
                void ocultar_resenaYaOculta_retorna3SinRepetirLaOperacion() {
                        resena = resenaDeModeracion();
                        resena.setVisible(false);
                        when(resenaRepo.findByIdParaActualizar(ID_RESENA)).thenReturn(Optional.of(resena));

                        int estado = resenaService.ocultar(NOMBRE_ADMIN, ID_RESENA, true);

                        assertAll(
                                        () -> assertEquals(3, estado),
                                        () -> assertFalse(resena.getVisible()),
                                        () -> verify(resenaRepo, never()).save(any(Resena.class)),
                                        () -> verifyNoInteractions(auditoriaService));
                }
        }
}