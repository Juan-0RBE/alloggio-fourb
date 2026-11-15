package co.edu.unbosque.alloggio.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.alloggio.dto.CancelacionDTO;
import co.edu.unbosque.alloggio.dto.ResultadoPasarelaDTO;
import co.edu.unbosque.alloggio.entity.Cancelacion;
import co.edu.unbosque.alloggio.entity.Pago;
import co.edu.unbosque.alloggio.entity.PoliticaCancelacion;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Reserva;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.CancelacionRepository;
import co.edu.unbosque.alloggio.repository.PagoRepository;
import co.edu.unbosque.alloggio.repository.PeriodoDisponibilidadRepository;
import co.edu.unbosque.alloggio.repository.ReservaRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelacionService - Módulo de Cancelaciones")
class CancelacionServiceTest {

    @Mock
    private ReservaRepository reservaRepo;

    @Mock
    private PagoRepository pagoRepo;

    @Mock
    private PeriodoDisponibilidadRepository periodoRepo;

    @Mock
    private CancelacionRepository cancelacionRepo;

    @Mock
    private UsuarioRepository usuarioRepo;

    @Mock
    private PasarelaPagoService pasarela;

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private EventoAuditoriaService auditoriaService;

    @InjectMocks
    private CancelacionService cancelacionService;

    @Nested
    @DisplayName("CU-020 - Cancelación de reserva")
    class CancelacionReserva {

        @Test
        @DisplayName("CP-052 - Cancelación de reserva exitosa con reembolso aprobado")
        void cancelar_reservaConfirmadaYConDevolucion_cancelaYDevuelveDinero() {
            // Arrange
            String nombreUsuario = "huespedTest";
            Long idReserva = 1L;
            CancelacionDTO dto = new CancelacionDTO();
            dto.setMotivo("Cambio de planes");

            Usuario usuario = new Usuario();
            usuario.setNombreUsuario(nombreUsuario);

            PoliticaCancelacion politica = new PoliticaCancelacion();
            politica.setPlazoCancelacion(5);
            politica.setPorcentajeDevolucion(100.0);

            Publicacion pub = new Publicacion();
            pub.setTitulo("Apto Test");
            pub.setPoliticaCancelacion(politica);

            Reserva reserva = new Reserva();
            reserva.setIdReserva(idReserva);
            reserva.setHuesped(usuario);
            reserva.setPublicacion(pub);
            reserva.setEstado(Reserva.EstadoReserva.CONFIRMADA);
            reserva.setFechaLlegada(LocalDate.now().plusDays(10));
            reserva.setFechaSalida(LocalDate.now().plusDays(15));
            reserva.setTotal(new BigDecimal("500000.00"));

            Pago pago = new Pago();
            pago.setReferenciaPasarela("ref_pago_123");

            ResultadoPasarelaDTO resultadoPasarela = new ResultadoPasarelaDTO();
            resultadoPasarela.setAprobado(true);
            resultadoPasarela.setReferencia("ref_reembolso_456");

            Cancelacion cancelacionGuardada = new Cancelacion();
            cancelacionGuardada.setIdCancelacion(10L);
            cancelacionGuardada.setFecha(java.time.LocalDateTime.now());

            when(usuarioRepo.findByNombreUsuario(nombreUsuario)).thenReturn(Optional.of(usuario));
            when(reservaRepo.findByIdParaActualizar(idReserva)).thenReturn(Optional.of(reserva));
            when(pagoRepo.findFirstByReservaIdReservaAndEstado(idReserva, Pago.EstadoPago.APROBADO))
                    .thenReturn(Optional.of(pago));
            when(pasarela.reembolsar(anyString(), any(), anyString())).thenReturn(resultadoPasarela);
            when(cancelacionRepo.save(any(Cancelacion.class))).thenReturn(cancelacionGuardada);

            // Act
            int resultado = cancelacionService.cancelar(nombreUsuario, idReserva, dto);

            // Assert
            assertAll(
                    () -> assertEquals(0, resultado),
                    () -> assertEquals(Reserva.EstadoReserva.CANCELADA, reserva.getEstado()),
                    () -> verify(reservaRepo).save(reserva),
                    () -> verify(cancelacionRepo).save(any(Cancelacion.class)),
                    () -> verify(notificacionService).notificarUsuario(eq(nombreUsuario), any(), anyString()),
                    () -> verify(auditoriaService).registrarExito(eq(nombreUsuario), any(), any(), eq(idReserva),
                            anyString()));
        }

        @Test
        @DisplayName("CP-053 - Cancelación rechazada por motivo inválido")
        void cancelar_motivoInvalido_retornaCinco() {
            // Arrange
            String nombreUsuario = "huespedTest";
            CancelacionDTO dto = new CancelacionDTO();
            dto.setMotivo(""); // Motivo vacío inválido

            Usuario usuario = new Usuario();
            usuario.setNombreUsuario(nombreUsuario);

            when(usuarioRepo.findByNombreUsuario(nombreUsuario)).thenReturn(Optional.of(usuario));

            // Act
            int resultado = cancelacionService.cancelar(nombreUsuario, 1L, dto);

            // Assert
            assertAll(
                    () -> assertEquals(5, resultado),
                    () -> verify(reservaRepo, never()).findByIdParaActualizar(any()));
        }
    }
}