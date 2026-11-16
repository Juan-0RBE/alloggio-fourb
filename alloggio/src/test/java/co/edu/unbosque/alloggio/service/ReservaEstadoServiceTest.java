package co.edu.unbosque.alloggio.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.alloggio.entity.Reserva.EstadoReserva;
import co.edu.unbosque.alloggio.repository.ReservaRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservaEstadoService - Módulo de actualización de estado por fecha")
class ReservaEstadoServiceTest {

    @Mock
    private ReservaRepository reservaRepo;

    @InjectMocks
    private ReservaEstadoService reservaEstadoService;

    @Nested
    @DisplayName("CU-021 - Completar reservas vencidas automáticamente")
    class CompletarReservasVencidas {

        @Test
        @DisplayName("CP-055 - Ejecución exitosa del completado de reservas vencidas")
        void completarReservasVencidas_ejecutaActualizacionRepositorio() {
            // Arrange
            when(reservaRepo.actualizarEstadoPorFechaSalida(
                    eq(EstadoReserva.CONFIRMADA),
                    eq(EstadoReserva.COMPLETADA),
                    any(LocalDate.class)))
                    .thenReturn(2);

            // Act
            reservaEstadoService.completarReservasVencidas();

            // Assert
            assertAll(
                    () -> verify(reservaRepo).actualizarEstadoPorFechaSalida(
                            eq(EstadoReserva.CONFIRMADA),
                            eq(EstadoReserva.COMPLETADA),
                            any(LocalDate.class)));
        }
    }
}