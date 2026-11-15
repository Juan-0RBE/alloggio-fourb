package co.edu.unbosque.alloggio.service;

import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.ID_PUBLICACION;
import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.publicacion;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.NOMBRE_USUARIO;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.usuarioActivo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.alloggio.dto.PeriodoDisponibilidadDTO;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Accion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.TipoEntidad;
import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad;
import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad.TipoPeriodo;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.repository.PeriodoDisponibilidadRepository;
import co.edu.unbosque.alloggio.repository.PublicacionRepository;

/**
 * Pruebas unitarias de PeriodoDisponibilidadService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PeriodoDisponibilidadService - módulo de disponibilidad")
class PeriodoDisponibilidadServiceTest {

    private static final Long ID_PERIODO = 5L;

    @Mock
    private PeriodoDisponibilidadRepository periodoRepo;

    @Mock
    private PublicacionRepository publicacionRepo;

    @Mock
    private EventoAuditoriaService auditoriaService;

    @InjectMocks
    private PeriodoDisponibilidadService disponibilidadService;

    // Fechas relativas al dia actual
    private final LocalDate inicio = LocalDate.now().plusDays(10);
    private final LocalDate fin = LocalDate.now().plusDays(15);

    private PeriodoDisponibilidadDTO solicitudDeBloqueo() {
        PeriodoDisponibilidadDTO dto = new PeriodoDisponibilidadDTO();
        dto.setIdPublicacion(ID_PUBLICACION);
        dto.setFechaInicio(inicio);
        dto.setFechaFin(fin);
        return dto;
    }

    private PeriodoDisponibilidad periodo(TipoPeriodo tipo, Publicacion pub) {
        PeriodoDisponibilidad p = new PeriodoDisponibilidad();
        p.setIdPeriodoDisponibilidad(ID_PERIODO);
        p.setFechaInicio(inicio);
        p.setFechaFin(fin);
        p.setTipo(tipo);
        p.setPublicacion(pub);
        return p;
    }

    // CU-012 - Gestionar disponibilidad

    @Nested
    @DisplayName("CU-012 - Gestionar disponibilidad")
    class GestionarDisponibilidad {

        @Test
        @DisplayName("CP-031 - Gestión de disponibilidad exitosa: se bloquea el rango")
        void bloquearRango_rangoLibre_guardaElBloqueoYAudita() {
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            when(publicacionRepo.findByIdParaActualizar(ID_PUBLICACION)).thenReturn(Optional.of(pub));
            when(periodoRepo.save(any(PeriodoDisponibilidad.class))).thenAnswer(invocacion -> {
                PeriodoDisponibilidad p = invocacion.getArgument(0);
                p.setIdPeriodoDisponibilidad(ID_PERIODO);
                return p;
            });

            int resultado = disponibilidadService.bloquearRango(NOMBRE_USUARIO, solicitudDeBloqueo());

            assertEquals(0, resultado);
            ArgumentCaptor<PeriodoDisponibilidad> captor = ArgumentCaptor.forClass(PeriodoDisponibilidad.class);
            verify(periodoRepo).save(captor.capture());
            PeriodoDisponibilidad guardado = captor.getValue();
            assertAll("bloqueo guardado",
                    () -> assertEquals(TipoPeriodo.BLOQUEADO, guardado.getTipo()),
                    () -> assertEquals(inicio, guardado.getFechaInicio()),
                    () -> assertEquals(fin, guardado.getFechaFin()),
                    () -> assertSame(pub, guardado.getPublicacion()));
            verify(auditoriaService).registrarExito(eq(NOMBRE_USUARIO), eq(Accion.BLOQUEAR_RANGO),
                    eq(TipoEntidad.PERIODO_DISPONIBILIDAD), eq(ID_PERIODO), anyString());
        }

        @Test
        @DisplayName("CP-031 - Gestión de disponibilidad exitosa: el calendario refleja el rango")
        void consultarCalendario_conBloqueoGuardado_muestraElRangoBloqueado() {
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            when(periodoRepo.findByPublicacionIdPublicacionOrderByFechaInicioAsc(ID_PUBLICACION))
                    .thenReturn(List.of(periodo(TipoPeriodo.BLOQUEADO, pub)));

            List<PeriodoDisponibilidadDTO> calendario = disponibilidadService.consultarCalendario(ID_PUBLICACION);

            assertEquals(1, calendario.size());
            PeriodoDisponibilidadDTO fila = calendario.get(0);
            assertAll("calendario",
                    () -> assertEquals(TipoPeriodo.BLOQUEADO, fila.getTipo()),
                    () -> assertEquals(inicio, fila.getFechaInicio()),
                    () -> assertEquals(fin, fila.getFechaFin()),
                    () -> assertEquals(ID_PUBLICACION, fila.getIdPublicacion()),
                    () -> assertNull(fila.getIdReserva()));
        }

        @Test
        @DisplayName("CP-032 - Gestión de disponibilidad con cruce de reserva confirmada")
        void bloquearRango_seCruzaConReserva_retorna5YElCalendarioNoCambia() {
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            when(publicacionRepo.findByIdParaActualizar(ID_PUBLICACION)).thenReturn(Optional.of(pub));
            when(periodoRepo.findIntersectingPeriods(ID_PUBLICACION, inicio, fin))
                    .thenReturn(List.of(periodo(TipoPeriodo.RESERVADO, pub)));

            int resultado = disponibilidadService.bloquearRango(NOMBRE_USUARIO, solicitudDeBloqueo());

            assertEquals(5, resultado);
            verify(periodoRepo, never()).save(any(PeriodoDisponibilidad.class));
            verify(periodoRepo, never()).delete(any(PeriodoDisponibilidad.class));
            verifyNoInteractions(auditoriaService);
        }

        @Test
        @DisplayName("Complementaria (HU-011) - Desbloquear un rango lo habilita de nuevo")
        void desbloquearRango_bloqueoDelPropietario_eliminaElBloqueoYAudita() {
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            PeriodoDisponibilidad bloqueo = periodo(TipoPeriodo.BLOQUEADO, pub);
            when(periodoRepo.findById(ID_PERIODO)).thenReturn(Optional.of(bloqueo));

            int resultado = disponibilidadService.desbloquearRango(NOMBRE_USUARIO, ID_PERIODO);

            assertEquals(0, resultado);
            verify(periodoRepo).delete(bloqueo);
            verify(auditoriaService).registrarExito(eq(NOMBRE_USUARIO), eq(Accion.DESBLOQUEAR_RANGO),
                    eq(TipoEntidad.PERIODO_DISPONIBILIDAD), eq(ID_PERIODO), anyString());
        }

        @Test
        @DisplayName("Complementaria (RN-08) - Una reserva no se puede desbloquear desde el calendario (3)")
        void desbloquearRango_periodoReservado_retorna3YNoLoElimina() {
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            PeriodoDisponibilidad reserva = periodo(TipoPeriodo.RESERVADO, pub);
            when(periodoRepo.findById(ID_PERIODO)).thenReturn(Optional.of(reserva));

            int resultado = disponibilidadService.desbloquearRango(NOMBRE_USUARIO, ID_PERIODO);

            assertEquals(3, resultado);
            verify(periodoRepo, never()).delete(any(PeriodoDisponibilidad.class));
        }
    }

    // Cálculo de disponibilidad

    @Nested
    @DisplayName("Disponibilidad de una publicación")
    class EstaDisponible {

        @Test
        @DisplayName("Complementaria - Publicación activa y rango libre: disponible")
        void estaDisponible_publicacionActivaSinCruces_retornaTrue() {
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            when(publicacionRepo.findById(ID_PUBLICACION)).thenReturn(Optional.of(pub));

            assertTrue(disponibilidadService.estaDisponible(ID_PUBLICACION, inicio, fin));
        }

        @Test
        @DisplayName("Complementaria (CA-22) - Un rango bloqueado no se muestra como disponible")
        void estaDisponible_rangoBloqueado_retornaFalse() {
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            when(publicacionRepo.findById(ID_PUBLICACION)).thenReturn(Optional.of(pub));
            when(periodoRepo.findIntersectingPeriods(ID_PUBLICACION, inicio, fin))
                    .thenReturn(List.of(periodo(TipoPeriodo.BLOQUEADO, pub)));

            assertFalse(disponibilidadService.estaDisponible(ID_PUBLICACION, inicio, fin));
        }

        @ParameterizedTest(name = "estado {0}")
        @EnumSource(value = EstadoPublicacion.class, names = { "PAUSADA", "BLOQUEADA" })
        @DisplayName("Complementaria (RN-04) - Solo las publicaciones ACTIVAS pueden recibir reservas")
        void estaDisponible_publicacionNoActiva_retornaFalseSinConsultarElCalendario(EstadoPublicacion estado) {
            Publicacion pub = publicacion(usuarioActivo(), estado);
            when(publicacionRepo.findById(ID_PUBLICACION)).thenReturn(Optional.of(pub));

            assertFalse(disponibilidadService.estaDisponible(ID_PUBLICACION, inicio, fin));
            verify(periodoRepo, never()).findIntersectingPeriods(any(), any(), any());
        }
    }
}