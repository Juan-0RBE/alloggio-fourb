package co.edu.unbosque.alloggio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.alloggio.dto.PagoDTO;
import co.edu.unbosque.alloggio.dto.ReservaDTO;
import co.edu.unbosque.alloggio.entity.Cotizacion;
import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.Reserva;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.CotizacionRepository;
import co.edu.unbosque.alloggio.repository.PeriodoDisponibilidadRepository;
import co.edu.unbosque.alloggio.repository.PublicacionRepository;
import co.edu.unbosque.alloggio.repository.ReservaRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservaService - Módulo de Creación de Reservas")
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepo;

    @Mock
    private CotizacionRepository cotizacionRepo;

    @Mock
    private PublicacionRepository publicacionRepo;

    @Mock
    private PeriodoDisponibilidadRepository periodoRepo;

    @Mock
    private UsuarioRepository usuarioRepo;

    @Mock
    private PagoService pagoService;

    @Mock
    private PasarelaPagoService pasarela;

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private EventoAuditoriaService auditoriaService;

    @InjectMocks
    private ReservaService reservaService;

    private static final String HUESPED = "huespedTest";
    private static final Long ID_COTIZACION = 1L;
    private static final Long ID_PUBLICACION = 10L;

    private Usuario crearHuesped() {
        Usuario u = new Usuario();
        u.setIdUsuario(100L);
        u.setNombreUsuario(HUESPED);
        u.setEstadoCuenta("ACTIVA");
        return u;
    }

    private Publicacion crearPublicacion(Usuario anfitrion) {
        Publicacion p = new Publicacion();
        p.setIdPublicacion(ID_PUBLICACION);
        p.setTitulo("Apto Test");
        p.setEstado(EstadoPublicacion.ACTIVA);
        p.setAnfitrion(anfitrion);
        return p;
    }

    private Cotizacion crearCotizacion(Usuario huesped, Publicacion pub) {
        Cotizacion c = new Cotizacion();
        c.setIdCotizacion(ID_COTIZACION);
        c.setHuesped(huesped);
        c.setPublicacion(pub);
        c.setFechaLlegada(LocalDate.now().plusDays(5));
        c.setFechaSalida(LocalDate.now().plusDays(8));
        c.setTotal(new BigDecimal("500000.00"));
        c.setVigencia(LocalDateTime.now().plusHours(1));
        return c;
    }

    private ReservaDTO crearReservaDTO() {
        ReservaDTO dto = new ReservaDTO();
        dto.setIdCotizacion(ID_COTIZACION);
        dto.setMetodoPago("pm_card_visa");
        dto.setClaveIdempotencia("key_idempotencia_123");
        return dto;
    }

    @Nested
    @DisplayName("CU-019 - Creación de reserva")
    class CreacionReserva {

        @Test
        @DisplayName("CP-049 - Creación de reserva exitosa con pago aprobado y disponibilidad")
        void crear_datosValidosPagoAprobado_creaReservaYBloqueaFechas() {
            Usuario huesped = crearHuesped();
            Publicacion pub = crearPublicacion(huesped);
            Cotizacion cot = crearCotizacion(huesped, pub);
            ReservaDTO dto = crearReservaDTO();

            when(usuarioRepo.findByNombreUsuario(HUESPED)).thenReturn(Optional.of(huesped));
            when(cotizacionRepo.findById(ID_COTIZACION)).thenReturn(Optional.of(cot));
            when(publicacionRepo.findByIdParaActualizar(ID_PUBLICACION)).thenReturn(Optional.of(pub));
            when(reservaRepo.findByHuespedIdUsuarioAndClaveIdempotencia(anyLong(), anyString()))
                    .thenReturn(Optional.empty());
            when(reservaRepo.findByCotizacionIdCotizacion(ID_COTIZACION)).thenReturn(Optional.empty());
            when(periodoRepo.findIntersectingPeriods(eq(ID_PUBLICACION), any(), any())).thenReturn(List.of());

            // Simular pago exitoso (estado 0 devuelto por pagoService.cobrar)
            when(pagoService.cobrar(eq(HUESPED), eq(ID_COTIZACION), any(PagoDTO.class))).thenAnswer(inv -> {
                PagoDTO pDto = inv.getArgument(2);
                pDto.setIdPago(50L);
                return 0;
            });

            PagoDTO pagoAprobadoDTO = new PagoDTO();
            pagoAprobadoDTO.setIdPago(50L);
            pagoAprobadoDTO.setReferenciaPasarela("ref_stripe_999");
            pagoAprobadoDTO.setMonto(new BigDecimal("500000.00"));
            when(pagoService.obtenerPorId(50L)).thenReturn(pagoAprobadoDTO);

            when(reservaRepo.save(any(Reserva.class))).thenAnswer(inv -> {
                Reserva r = inv.getArgument(0);
                r.setIdReserva(77L);
                return r;
            });

            int resultado = reservaService.crear(HUESPED, dto);

            assertThat(resultado).isEqualTo(0);
            assertThat(dto.getIdReserva()).isEqualTo(77L);
            verify(reservaRepo).save(any(Reserva.class));
            verify(periodoRepo).save(any(PeriodoDisponibilidad.class));
            verify(pagoService).vincularReserva(eq(50L), any(Reserva.class));
            verify(notificacionService).notificarUsuario(eq(HUESPED), any(), anyString());
            verify(notificacionService).notificarAOtroUsuario(eq(pub.getAnfitrion()), any(), anyString());
            verify(auditoriaService).registrarExito(eq(HUESPED), any(), any(), eq(77L), anyString());
        }

        @Test
        @DisplayName("CP-050 - Creación de reserva con pago no aprobado (rechazado)")
        void crear_pagoRechazado_noCreaReservaYRetornaDiez() {
            Usuario huesped = crearHuesped();
            Publicacion pub = crearPublicacion(huesped);
            Cotizacion cot = crearCotizacion(huesped, pub);
            ReservaDTO dto = crearReservaDTO();

            when(usuarioRepo.findByNombreUsuario(HUESPED)).thenReturn(Optional.of(huesped));
            when(cotizacionRepo.findById(ID_COTIZACION)).thenReturn(Optional.of(cot));
            when(publicacionRepo.findByIdParaActualizar(ID_PUBLICACION)).thenReturn(Optional.of(pub));
            when(reservaRepo.findByHuespedIdUsuarioAndClaveIdempotencia(anyLong(), anyString()))
                    .thenReturn(Optional.empty());
            when(reservaRepo.findByCotizacionIdCotizacion(ID_COTIZACION)).thenReturn(Optional.empty());
            when(periodoRepo.findIntersectingPeriods(any(), any(), any())).thenReturn(List.of());

            // Simular pago rechazado (estado 8 devuelto por pagoService.cobrar)
            when(pagoService.cobrar(eq(HUESPED), eq(ID_COTIZACION), any(PagoDTO.class))).thenAnswer(inv -> {
                PagoDTO pDto = inv.getArgument(2);
                pDto.setIdPago(51L);
                return 8;
            });

            PagoDTO pagoRechazadoDTO = new PagoDTO();
            pagoRechazadoDTO.setIdPago(51L);
            pagoRechazadoDTO.setMotivoRechazo("Fondos insuficientes");
            when(pagoService.obtenerPorId(51L)).thenReturn(pagoRechazadoDTO);

            int resultado = reservaService.crear(HUESPED, dto);

            assertThat(resultado).isEqualTo(10);
            assertThat(dto.getMotivoRechazo()).isEqualTo("Fondos insuficientes");
            verify(reservaRepo, never()).save(any(Reserva.class));
            verify(auditoriaService).registrar(eq(HUESPED), any(), any(), eq(51L), any(), anyString());
        }

        @Test
        @DisplayName("CP-051 - Creación de reserva con rango ya no disponible (conflicto)")
        void crear_rangoNoDisponible_retornaOchoYNoCreaReserva() {
            Usuario huesped = crearHuesped();
            Publicacion pub = crearPublicacion(huesped);
            Cotizacion cot = crearCotizacion(huesped, pub);
            ReservaDTO dto = crearReservaDTO();

            when(usuarioRepo.findByNombreUsuario(HUESPED)).thenReturn(Optional.of(huesped));
            when(cotizacionRepo.findById(ID_COTIZACION)).thenReturn(Optional.of(cot));
            when(publicacionRepo.findByIdParaActualizar(ID_PUBLICACION)).thenReturn(Optional.of(pub));
            when(reservaRepo.findByHuespedIdUsuarioAndClaveIdempotencia(anyLong(), anyString()))
                    .thenReturn(Optional.empty());
            when(reservaRepo.findByCotizacionIdCotizacion(ID_COTIZACION)).thenReturn(Optional.empty());

            // Simular conflicto de disponibilidad
            when(periodoRepo.findIntersectingPeriods(eq(ID_PUBLICACION), any(), any()))
                    .thenReturn(List.of(new PeriodoDisponibilidad()));

            int resultado = reservaService.crear(HUESPED, dto);

            assertThat(resultado).isEqualTo(8);
            verify(reservaRepo, never()).save(any(Reserva.class));
            verifyNoInteractions(pagoService);
        }
    }
}