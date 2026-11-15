package co.edu.unbosque.alloggio.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.alloggio.dto.PagoDTO;
import co.edu.unbosque.alloggio.dto.ResultadoPasarelaDTO;
import co.edu.unbosque.alloggio.entity.Cotizacion;
import co.edu.unbosque.alloggio.entity.Pago;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.CotizacionRepository;
import co.edu.unbosque.alloggio.repository.PagoRepository;
import co.edu.unbosque.alloggio.util.exception.PasarelaException;

@ExtendWith(MockitoExtension.class)
@DisplayName("PagoService - Módulo de Pago y Cobro")
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepo;

    @Mock
    private CotizacionRepository cotizacionRepo;

    @Mock
    private PasarelaPagoService pasarela;

    @InjectMocks
    private PagoService pagoService;

    private static final String USUARIO = "huespedTest";
    private static final Long ID_COTIZACION = 1L;

    private Cotizacion crearCotizacionValida() {
        Usuario huesped = new Usuario();
        huesped.setNombreUsuario(USUARIO);

        Cotizacion cot = new Cotizacion();
        cot.setIdCotizacion(ID_COTIZACION);
        cot.setHuesped(huesped);
        cot.setTotal(new BigDecimal("865000.00"));
        cot.setVigencia(LocalDateTime.now().plusMinutes(30)); // Vigente
        return cot;
    }

    private PagoDTO crearPagoDTOValido() {
        PagoDTO dto = new PagoDTO();
        dto.setMetodoPago("pm_card_visa");
        dto.setClaveIdempotencia("key_idempotencia_abc");
        return dto;
    }

    @Nested
    @DisplayName("CU-018 - Procesar cobro de cotización")
    class CobrarCotizacion {

        @Test
        @DisplayName("CP-047 - Pago exitoso con datos válidos y pasarela aprobada")
        void cobrar_datosValidosYPasarelaAprobada_retornaCeroYRegistraPago() {
            Cotizacion cot = crearCotizacionValida();
            PagoDTO dto = crearPagoDTOValido();

            ResultadoPasarelaDTO resultadoPasarela = new ResultadoPasarelaDTO();
            resultadoPasarela.setAprobado(true);
            resultadoPasarela.setReferencia("ref_stripe_123");
            resultadoPasarela.setMarcaTarjeta("Visa");
            resultadoPasarela.setUltimosCuatro("4242");

            when(cotizacionRepo.findById(ID_COTIZACION)).thenReturn(Optional.of(cot));
            when(pagoRepo.existsByCotizacionIdCotizacionAndEstado(ID_COTIZACION, Pago.EstadoPago.APROBADO))
                    .thenReturn(false);
            when(pasarela.cobrar(any(), eq("COP"), eq("pm_card_visa"), anyString(), eq("key_idempotencia_abc")))
                    .thenReturn(resultadoPasarela);

            when(pagoRepo.save(any(Pago.class))).thenAnswer(inv -> {
                Pago p = inv.getArgument(0);
                p.setIdPago(10L);
                return p;
            });

            int estado = pagoService.cobrar(USUARIO, ID_COTIZACION, dto);

            assertAll(
                    () -> assertEquals(0, estado),
                    () -> assertEquals(10L, dto.getIdPago()),
                    () -> verify(pagoRepo).save(any(Pago.class)),
                    () -> verify(pasarela).cobrar(any(), eq("COP"), eq("pm_card_visa"), anyString(),
                            eq("key_idempotencia_abc")));
        }

        @Test
        @DisplayName("CP-048 - Pago con token de pasarela inválido")
        void cobrar_metodoPagoInvalido_retornaCodigoCincoYNoLlamaPasarela() {
            Cotizacion cot = crearCotizacionValida();
            PagoDTO dto = crearPagoDTOValido();
            dto.setMetodoPago("tarjeta_invalida_sin_pm"); // No cumple con el regex ^pm_[A-Za-z0-9_]{3,100}$

            when(cotizacionRepo.findById(ID_COTIZACION)).thenReturn(Optional.of(cot));
            when(pagoRepo.existsByCotizacionIdCotizacionAndEstado(ID_COTIZACION, Pago.EstadoPago.APROBADO))
                    .thenReturn(false);

            int estado = pagoService.cobrar(USUARIO, ID_COTIZACION, dto);

            assertAll(
                    () -> assertEquals(5, estado),
                    () -> verify(pasarela, never()).cobrar(any(), any(), any(), any(), any()),
                    () -> verify(pagoRepo, never()).save(any(Pago.class)));
        }

        @Test
        @DisplayName("Complementaria - Cotización vencida retorna código tres")
        void cobrar_cotizacionVencida_retornaCodigoTres() {
            Cotizacion cot = crearCotizacionValida();
            cot.setVigencia(LocalDateTime.now().minusMinutes(5)); // Vencida
            PagoDTO dto = crearPagoDTOValido();

            when(cotizacionRepo.findById(ID_COTIZACION)).thenReturn(Optional.of(cot));

            int estado = pagoService.cobrar(USUARIO, ID_COTIZACION, dto);

            assertAll(
                    () -> assertEquals(3, estado),
                    () -> verify(pasarela, never()).cobrar(any(), any(), any(), any(), any()),
                    () -> verify(pagoRepo, never()).save(any(Pago.class)));
        }

        @Test
        @DisplayName("Complementaria - Excepción en la pasarela retorna código siete")
        void cobrar_excepcionPasarela_retornaCodigoSieteSinGuardarNada() {
            Cotizacion cot = crearCotizacionValida();
            PagoDTO dto = crearPagoDTOValido();

            when(cotizacionRepo.findById(ID_COTIZACION)).thenReturn(Optional.of(cot));
            when(pagoRepo.existsByCotizacionIdCotizacionAndEstado(ID_COTIZACION, Pago.EstadoPago.APROBADO))
                    .thenReturn(false);
            when(pasarela.cobrar(any(), any(), any(), any(), any()))
                    .thenThrow(new PasarelaException("Error de conexión con Stripe"));

            int estado = pagoService.cobrar(USUARIO, ID_COTIZACION, dto);

            assertAll(
                    () -> assertEquals(7, estado),
                    () -> verify(pagoRepo, never()).save(any(Pago.class)));
        }
    }
}