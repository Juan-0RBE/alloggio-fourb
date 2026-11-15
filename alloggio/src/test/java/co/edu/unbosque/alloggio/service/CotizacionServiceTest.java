package co.edu.unbosque.alloggio.service;

import static co.edu.unbosque.alloggio.testutil.AlojamientoFixtures.servicio;
import static co.edu.unbosque.alloggio.testutil.CotizacionFixtures.ID_COTIZACION;
import static co.edu.unbosque.alloggio.testutil.CotizacionFixtures.NOMBRE_HUESPED;
import static co.edu.unbosque.alloggio.testutil.CotizacionFixtures.cotizacionVigente;
import static co.edu.unbosque.alloggio.testutil.CotizacionFixtures.huesped;
import static co.edu.unbosque.alloggio.testutil.CotizacionFixtures.parametroTarifa;
import static co.edu.unbosque.alloggio.testutil.CotizacionFixtures.solicitud;
import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.ID_PUBLICACION;
import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.publicacion;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.usuarioActivo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.alloggio.dto.CotizacionDTO;
import co.edu.unbosque.alloggio.entity.Cotizacion;
import co.edu.unbosque.alloggio.entity.CotizacionServicio;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.ServicioAlojamiento;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.CotizacionRepository;
import co.edu.unbosque.alloggio.repository.ParametroTarifaRepository;
import co.edu.unbosque.alloggio.repository.PublicacionRepository;
import co.edu.unbosque.alloggio.repository.ServicioAlojamientoRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;

/**
 * Pruebas unitarias de CotizacionService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CotizacionService - cotización y servicios adicionales")
class CotizacionServiceTest {

    @Mock
    private CotizacionRepository cotizacionRepo;

    @Mock
    private PublicacionRepository publicacionRepo;

    @Mock
    private ServicioAlojamientoRepository servicioRepo;

    @Mock
    private ParametroTarifaRepository parametroRepo;

    @Mock
    private UsuarioRepository usuarioRepo;

    @Mock
    private AlojamientoService alojamientoService;

    @InjectMocks
    private CotizacionService cotizacionService;

    /**
     * Compara montos por valor (compareTo).
     */
    private static void assertMonto(String esperado, BigDecimal real) {
        assertNotNull(real, "el monto es nulo; se esperaba " + esperado);
        assertEquals(0, new BigDecimal(esperado).compareTo(real), "se esperaba " + esperado + " y fue " + real);
    }

    /**
     * Deja listo todo lo que necesita un cotizar exitoso (sin el guardado).
     */
    private void prepararCotizacionPosible(Usuario huesped, Publicacion pub, LocalDate llegada, LocalDate salida) {
        when(usuarioRepo.findByNombreUsuario(NOMBRE_HUESPED)).thenReturn(Optional.of(huesped));
        when(publicacionRepo.findById(ID_PUBLICACION)).thenReturn(Optional.of(pub));
        when(alojamientoService.estaDisponible(ID_PUBLICACION, llegada, salida)).thenReturn(true);
        when(parametroRepo.findFirstByOrderByIdParametroTarifaAsc())
                .thenReturn(Optional.of(parametroTarifa(10.0, 5.0, 7)));
    }

    @Nested
    @DisplayName("CU-016 - Cotizar alojamiento")
    class CotizarAlojamiento {

        @Test
        @DisplayName("CP-040 - Cotización de alojamiento exitosa")
        void cotizar_datosValidosYDisponible_calculaYGuardaLaCotizacion() {
            // Arrange: 3 noches a 250.000, limpieza 40.000, servicio 10 %, sin descuento
            LocalDate llegada = LocalDate.now().plusDays(10);
            LocalDate salida = LocalDate.now().plusDays(13);
            Usuario huesped = huesped();
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            prepararCotizacionPosible(huesped, pub, llegada, salida);
            when(cotizacionRepo.save(any(Cotizacion.class))).thenAnswer(inv -> {
                Cotizacion c = inv.getArgument(0);
                c.setIdCotizacion(ID_COTIZACION);
                return c;
            });
            CotizacionDTO dto = solicitud(ID_PUBLICACION, llegada, salida);

            // Act
            int estado = cotizacionService.cotizar(NOMBRE_HUESPED, dto);

            // Assert
            ArgumentCaptor<Cotizacion> captor = ArgumentCaptor.forClass(Cotizacion.class);
            verify(cotizacionRepo).save(captor.capture());
            Cotizacion guardada = captor.getValue();
            assertAll(
                    () -> assertEquals(0, estado),
                    () -> assertEquals(ID_COTIZACION, dto.getIdCotizacion()),
                    () -> assertSame(huesped, guardada.getHuesped()),
                    () -> assertSame(pub, guardada.getPublicacion()),
                    () -> assertEquals(3, guardada.getNoches()),
                    () -> assertMonto("750000", guardada.getPrecioBase()),
                    () -> assertMonto("40000", guardada.getLimpieza()),
                    () -> assertMonto("75000", guardada.getServicio()),
                    () -> assertMonto("0", guardada.getDescuento()),
                    () -> assertMonto("865000", guardada.getTotal()),
                    () -> assertTrue(guardada.getServicios().isEmpty()),
                    // la cotización queda vigente unos 30 minutos
                    () -> assertTrue(guardada.getVigencia().isAfter(LocalDateTime.now().plusMinutes(29))));
        }

        @Test
        @DisplayName("Complementaria (CA-29, RN-09) - Cotización con descuento al llegar a las noches mínimas")
        void cotizar_nochesMinimasParaDescuento_aplicaElDescuento() {
            // 7 noches (justo el mínimo): base 1.750.000, servicio 10 % = 175.000,
            // descuento 5 % = 87.500
            LocalDate llegada = LocalDate.now().plusDays(10);
            LocalDate salida = LocalDate.now().plusDays(17);
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            prepararCotizacionPosible(huesped(), pub, llegada, salida);
            when(cotizacionRepo.save(any(Cotizacion.class))).thenAnswer(inv -> inv.getArgument(0));

            int estado = cotizacionService.cotizar(NOMBRE_HUESPED, solicitud(ID_PUBLICACION, llegada, salida));

            ArgumentCaptor<Cotizacion> captor = ArgumentCaptor.forClass(Cotizacion.class);
            verify(cotizacionRepo).save(captor.capture());
            Cotizacion guardada = captor.getValue();
            assertAll(
                    () -> assertEquals(0, estado),
                    () -> assertEquals(7, guardada.getNoches()),
                    () -> assertMonto("1750000", guardada.getPrecioBase()),
                    () -> assertMonto("175000", guardada.getServicio()),
                    () -> assertMonto("87500", guardada.getDescuento()),
                    () -> assertMonto("1877500", guardada.getTotal()));
        }

        @Test
        @DisplayName("CP-041 - Cotización con deseo de agregar servicios adicionales (la cotización creada admite servicios)")
        void cotizar_luegoAgregarServicios_laCotizacionCreadaAdmiteServicios() {
            // Arrange: se cotiza y la cotización guardada es la que luego se consulta
            LocalDate llegada = LocalDate.now().plusDays(10);
            LocalDate salida = LocalDate.now().plusDays(13);
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            prepararCotizacionPosible(huesped(), pub, llegada, salida);
            AtomicReference<Cotizacion> guardada = new AtomicReference<>();
            when(cotizacionRepo.save(any(Cotizacion.class))).thenAnswer(inv -> {
                Cotizacion c = inv.getArgument(0);
                c.setIdCotizacion(ID_COTIZACION);
                guardada.set(c);
                return c;
            });
            when(cotizacionRepo.findById(ID_COTIZACION)).thenAnswer(inv -> Optional.of(guardada.get()));
            ServicioAlojamiento desayuno = servicio(5L, "Desayuno", 20000.0, true, pub);
            when(servicioRepo.findByPublicacionIdPublicacion(ID_PUBLICACION)).thenReturn(List.of(desayuno));
            CotizacionDTO dto = solicitud(ID_PUBLICACION, llegada, salida);

            // Act
            int estadoCotizar = cotizacionService.cotizar(NOMBRE_HUESPED, dto);
            int estadoAgregar = cotizacionService.agregarServicios(NOMBRE_HUESPED, dto.getIdCotizacion(),
                    List.of(5L));

            // Assert: 865.000 + 20.000 x 3 noches
            assertAll(
                    () -> assertEquals(0, estadoCotizar),
                    () -> assertEquals(0, estadoAgregar),
                    () -> assertEquals(1, guardada.get().getServicios().size()),
                    () -> assertMonto("925000", guardada.get().getTotal()));
        }

        @Test
        @DisplayName("CP-042 - Cotización con fecha inicial posterior a la final")
        void cotizar_llegadaPosteriorASalida_retorna3YNoGeneraCotizacion() {
            LocalDate llegada = LocalDate.now().plusDays(13);
            LocalDate salida = LocalDate.now().plusDays(10);
            when(usuarioRepo.findByNombreUsuario(NOMBRE_HUESPED)).thenReturn(Optional.of(huesped()));
            when(publicacionRepo.findById(ID_PUBLICACION))
                    .thenReturn(Optional.of(publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA)));
            when(alojamientoService.validarCriterios(llegada, salida, null, null, null)).thenReturn(2);
            CotizacionDTO dto = solicitud(ID_PUBLICACION, llegada, salida);

            int estado = cotizacionService.cotizar(NOMBRE_HUESPED, dto);

            assertAll(
                    () -> assertEquals(3, estado),
                    () -> assertNull(dto.getIdCotizacion()),
                    () -> verify(cotizacionRepo, never()).save(any(Cotizacion.class)),
                    () -> verify(alojamientoService, never()).estaDisponible(any(), any(), any()));
        }

        @Test
        @DisplayName("CP-043 - Cotización con alojamiento no disponible en el periodo")
        void cotizar_alojamientoNoDisponible_retorna5YNoGeneraCotizacion() {
            LocalDate llegada = LocalDate.now().plusDays(10);
            LocalDate salida = LocalDate.now().plusDays(13);
            when(usuarioRepo.findByNombreUsuario(NOMBRE_HUESPED)).thenReturn(Optional.of(huesped()));
            when(publicacionRepo.findById(ID_PUBLICACION))
                    .thenReturn(Optional.of(publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA)));
            when(alojamientoService.estaDisponible(ID_PUBLICACION, llegada, salida)).thenReturn(false);
            CotizacionDTO dto = solicitud(ID_PUBLICACION, llegada, salida);

            int estado = cotizacionService.cotizar(NOMBRE_HUESPED, dto);

            assertAll(
                    () -> assertEquals(5, estado),
                    () -> assertNull(dto.getIdCotizacion()),
                    () -> verify(cotizacionRepo, never()).save(any(Cotizacion.class)),
                    () -> verifyNoInteractions(parametroRepo));
        }

        @ParameterizedTest(name = "publicación en estado {0}")
        @EnumSource(value = EstadoPublicacion.class, mode = EnumSource.Mode.EXCLUDE, names = { "ACTIVA" })
        @DisplayName("Complementaria (RN-04) - Solo se cotizan publicaciones ACTIVAS")
        void cotizar_publicacionNoActiva_retorna2YNoGeneraCotizacion(EstadoPublicacion estadoPublicacion) {
            when(usuarioRepo.findByNombreUsuario(NOMBRE_HUESPED)).thenReturn(Optional.of(huesped()));
            when(publicacionRepo.findById(ID_PUBLICACION))
                    .thenReturn(Optional.of(publicacion(usuarioActivo(), estadoPublicacion)));

            int estado = cotizacionService.cotizar(NOMBRE_HUESPED, solicitud(ID_PUBLICACION,
                    LocalDate.now().plusDays(10), LocalDate.now().plusDays(13)));

            assertEquals(2, estado);
            verify(cotizacionRepo, never()).save(any(Cotizacion.class));
        }
    }

    @Nested
    @DisplayName("CU-017 - Agregar servicios adicionales")
    class AgregarServiciosAdicionales {

        @Test
        @DisplayName("CP-044 - Agregado de servicios adicionales exitoso (recalcula el total)")
        void agregarServicios_serviciosAdicionales_recalculaElTotal() {
            // Arrange: costo por noche x 3 noches: 20.000 x 3 + 50.000 x 3 = 210.000
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            Cotizacion cotizacion = cotizacionVigente(huesped(), pub);
            when(cotizacionRepo.findById(ID_COTIZACION)).thenReturn(Optional.of(cotizacion));
            when(servicioRepo.findByPublicacionIdPublicacion(ID_PUBLICACION)).thenReturn(List.of(
                    servicio(1L, "WiFi", null, false, pub),
                    servicio(5L, "Desayuno", 20000.0, true, pub),
                    servicio(6L, "Transporte", 50000.0, true, pub)));

            int estado = cotizacionService.agregarServicios(NOMBRE_HUESPED, ID_COTIZACION, List.of(5L, 6L));

            verify(cotizacionRepo).save(cotizacion);
            assertAll(
                    () -> assertEquals(0, estado),
                    () -> assertEquals(2, cotizacion.getServicios().size()),
                    () -> assertEquals(5L, cotizacion.getServicios().get(0).getServicio().getIdServicio()),
                    () -> assertMonto("60000", cotizacion.getServicios().get(0).getCostoAplicado()),
                    () -> assertEquals(6L, cotizacion.getServicios().get(1).getServicio().getIdServicio()),
                    () -> assertMonto("150000", cotizacion.getServicios().get(1).getCostoAplicado()),
                    // 865.000 + 210.000
                    () -> assertMonto("1075000", cotizacion.getTotal()));
        }

        @Test
        @DisplayName("CP-044 - Agregado de servicios adicionales exitoso (muestra la cotización actualizada)")
        void obtenerPorId_conServiciosAdicionales_muestraServiciosYTotal() {
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            Cotizacion cotizacion = cotizacionVigente(huesped(), pub);
            cotizacion.getServicios().add(new CotizacionServicio(null, new BigDecimal("60000.00"), cotizacion,
                    servicio(5L, "Desayuno", 20000.0, true, pub)));
            cotizacion.getServicios().add(new CotizacionServicio(null, new BigDecimal("150000.00"), cotizacion,
                    servicio(6L, "Transporte", 50000.0, true, pub)));
            cotizacion.setTotal(new BigDecimal("1075000.00"));
            when(cotizacionRepo.findById(ID_COTIZACION)).thenReturn(Optional.of(cotizacion));

            CotizacionDTO dto = cotizacionService.obtenerPorId(ID_COTIZACION);

            assertAll(
                    () -> assertEquals(3, dto.getNoches()),
                    () -> assertEquals(2, dto.getServicios().size()),
                    () -> assertEquals("Desayuno", dto.getServicios().get(0).getNombre()),
                    () -> assertMonto("60000", dto.getServicios().get(0).getCostoAplicado()),
                    () -> assertEquals("Transporte", dto.getServicios().get(1).getNombre()),
                    () -> assertMonto("210000", dto.getServiciosAdicionales()),
                    () -> assertMonto("1075000", dto.getTotal()));
        }

        @ParameterizedTest(name = "selección: {0}")
        @NullAndEmptySource
        @DisplayName("CP-045 - Agregado de servicios sin seleccionar ninguno")
        void agregarServicios_sinSeleccionarServicios_laCotizacionQuedaSinCambios(List<Long> ids) {
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            Cotizacion cotizacion = cotizacionVigente(huesped(), pub);
            when(cotizacionRepo.findById(ID_COTIZACION)).thenReturn(Optional.of(cotizacion));
            when(servicioRepo.findByPublicacionIdPublicacion(ID_PUBLICACION))
                    .thenReturn(List.of(servicio(5L, "Desayuno", 20000.0, true, pub)));

            int estado = cotizacionService.agregarServicios(NOMBRE_HUESPED, ID_COTIZACION, ids);

            assertAll(
                    () -> assertEquals(0, estado),
                    () -> assertTrue(cotizacion.getServicios().isEmpty()),
                    () -> assertMonto("865000", cotizacion.getTotal()),
                    () -> verify(cotizacionRepo, never()).save(any(Cotizacion.class)));
        }

        @ParameterizedTest(name = "alojamiento con servicios solo incluidos: {0}")
        @ValueSource(booleans = { false, true })
        @DisplayName("CP-046 - Agregado de servicios en alojamiento sin servicios adicionales")
        void agregarServicios_alojamientoSinAdicionales_retorna4YNoCambiaLaCotizacion(boolean conIncluidos) {
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            Cotizacion cotizacion = cotizacionVigente(huesped(), pub);
            when(cotizacionRepo.findById(ID_COTIZACION)).thenReturn(Optional.of(cotizacion));
            List<ServicioAlojamiento> ofrecidos = new ArrayList<>();
            if (conIncluidos) {
                ofrecidos.add(servicio(1L, "WiFi", null, false, pub)); // incluido, no adicional
            }
            when(servicioRepo.findByPublicacionIdPublicacion(ID_PUBLICACION)).thenReturn(ofrecidos);

            int estado = cotizacionService.agregarServicios(NOMBRE_HUESPED, ID_COTIZACION, List.of(5L));

            assertAll(
                    () -> assertEquals(4, estado),
                    () -> assertTrue(cotizacion.getServicios().isEmpty()),
                    () -> assertMonto("865000", cotizacion.getTotal()),
                    () -> verify(cotizacionRepo, never()).save(any(Cotizacion.class)));
        }

        @Test
        @DisplayName("Complementaria (seguridad) - No se agregan servicios a la cotización de otro huésped")
        void agregarServicios_cotizacionAjena_retorna2YNoLaModifica() {
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            Cotizacion cotizacion = cotizacionVigente(huesped(), pub);
            when(cotizacionRepo.findById(ID_COTIZACION)).thenReturn(Optional.of(cotizacion));

            int estado = cotizacionService.agregarServicios("otrohuesped", ID_COTIZACION, List.of(5L));

            assertAll(
                    () -> assertEquals(2, estado),
                    () -> assertTrue(cotizacion.getServicios().isEmpty()),
                    () -> assertMonto("865000", cotizacion.getTotal()),
                    () -> verifyNoInteractions(servicioRepo),
                    () -> verify(cotizacionRepo, never()).save(any(Cotizacion.class)));
        }
    }
}