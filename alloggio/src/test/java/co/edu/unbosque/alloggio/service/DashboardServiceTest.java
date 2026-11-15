package co.edu.unbosque.alloggio.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.alloggio.dto.DashboardDTO;
import co.edu.unbosque.alloggio.repository.PublicacionRepository;
import co.edu.unbosque.alloggio.repository.ReservaRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;
import co.edu.unbosque.alloggio.testutil.DashboardFixtures;

/**
 * Pruebas unitarias de DashboardService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService - Módulo de Indicadores del Dashboard")
class DashboardServiceTest {

    @Mock
    private UsuarioRepository usuarioRepo;

    @Mock
    private PublicacionRepository publicacionRepo;

    @Mock
    private ReservaRepository reservaRepo;

    @InjectMocks
    private DashboardService dashboardService;

    @Nested
    @DisplayName("CU-029 - Obtención de indicadores del dashboard")
    class ObtenerIndicadores {

        @Test
        @DisplayName("CP-076 - Obtener indicadores con datos exitosamente")
        void obtenerIndicadores_conDatos_retornaDashboardConsolidado() {
            // Arrange
            when(usuarioRepo.count()).thenReturn(8L);
            when(usuarioRepo.contarPorRol()).thenReturn(DashboardFixtures.contarPorRolMock());
            when(usuarioRepo.contarPorEstadoCuenta()).thenReturn(DashboardFixtures.contarPorEstadoCuentaMock());

            when(publicacionRepo.contarPorEstado()).thenReturn(DashboardFixtures.contarPorEstadoPublicacionMock());
            when(reservaRepo.contarPorEstado()).thenReturn(DashboardFixtures.contarPorEstadoReservaMock());
            when(reservaRepo.sumarTotalPorEstados(any())).thenReturn(new BigDecimal("900000.00"));
            when(reservaRepo.ciudadesConMayorDemanda(any(), any())).thenReturn(DashboardFixtures.ciudadesDemandaMock());

            // Act
            DashboardDTO dto = dashboardService.obtenerIndicadores();

            // Assert
            assertAll(
                    () -> assertNotNull(dto),
                    () -> assertEquals(8L, dto.getTotalUsuarios()),
                    () -> assertEquals(5L, dto.getUsuariosPorRol().get("USUARIO")), // Corregido de "HUESPED" a
                                                                                    // "USUARIO"
                    () -> assertEquals(4L, dto.getPublicacionesActivas()), // Corregido acorde al fixture (4L)
                    () -> assertEquals(4L, dto.getTotalReservas()),
                    () -> assertEquals(0, new BigDecimal("900000.00").compareTo(dto.getIngresosEstimados())),
                    () -> assertEquals(1, dto.getCiudadesDemanda().size()),
                    () -> assertEquals("Bogotá", dto.getCiudadesDemanda().get(0).getCiudad()),
                    () -> assertFalse(dto.isSinDatos()));
        }

        @Test
        @DisplayName("CP-077 - Obtener indicadores sin datos marca sinDatos como true")
        void obtenerIndicadores_sinDatos_retornaDashboardVacio() {
            // Arrange
            when(usuarioRepo.count()).thenReturn(0L);
            when(usuarioRepo.contarPorRol()).thenReturn(java.util.List.of());
            when(usuarioRepo.contarPorEstadoCuenta()).thenReturn(java.util.List.of());

            when(publicacionRepo.contarPorEstado()).thenReturn(java.util.List.of());
            when(reservaRepo.contarPorEstado()).thenReturn(java.util.List.of());
            when(reservaRepo.sumarTotalPorEstados(any())).thenReturn(null);
            when(reservaRepo.ciudadesConMayorDemanda(any(), any())).thenReturn(java.util.List.of());

            // Act
            DashboardDTO dto = dashboardService.obtenerIndicadores();

            // Assert
            assertAll(
                    () -> assertNotNull(dto),
                    () -> assertEquals(0L, dto.getTotalUsuarios()),
                    () -> assertEquals(0L, dto.getTotalPublicaciones()),
                    () -> assertEquals(0L, dto.getTotalReservas()),
                    () -> assertEquals(0, BigDecimal.ZERO.setScale(2).compareTo(dto.getIngresosEstimados())),
                    () -> assertEquals(0, dto.getCiudadesDemanda().size()),
                    () -> org.junit.jupiter.api.Assertions.assertTrue(dto.isSinDatos()));
        }
    }
}