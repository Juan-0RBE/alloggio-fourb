package co.edu.unbosque.alloggio.service;

import static co.edu.unbosque.alloggio.testutil.AlojamientoFixtures.imagen;
import static co.edu.unbosque.alloggio.testutil.AlojamientoFixtures.imagenDto;
import static co.edu.unbosque.alloggio.testutil.AlojamientoFixtures.periodo;
import static co.edu.unbosque.alloggio.testutil.AlojamientoFixtures.regla;
import static co.edu.unbosque.alloggio.testutil.AlojamientoFixtures.reglaDto;
import static co.edu.unbosque.alloggio.testutil.AlojamientoFixtures.servicio;
import static co.edu.unbosque.alloggio.testutil.AlojamientoFixtures.servicioDto;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import co.edu.unbosque.alloggio.dto.AlojamientoDetalleDTO;
import co.edu.unbosque.alloggio.dto.AlojamientoResumenDTO;
import co.edu.unbosque.alloggio.dto.ImagenAlojamientoDTO;
import co.edu.unbosque.alloggio.dto.PaginaDTO;
import co.edu.unbosque.alloggio.dto.ReglaAlojamientoDTO;
import co.edu.unbosque.alloggio.dto.ResenaDTO;
import co.edu.unbosque.alloggio.dto.ServicioAlojamientoDTO;
import co.edu.unbosque.alloggio.entity.ImagenAlojamiento;
import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad;
import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad.TipoPeriodo;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.TipoAlojamiento;
import co.edu.unbosque.alloggio.entity.ReglaAlojamiento;
import co.edu.unbosque.alloggio.entity.ServicioAlojamiento;
import co.edu.unbosque.alloggio.repository.ImagenAlojamientoRepository;
import co.edu.unbosque.alloggio.repository.PeriodoDisponibilidadRepository;
import co.edu.unbosque.alloggio.repository.PublicacionRepository;
import co.edu.unbosque.alloggio.repository.ReglaAlojamientoRepository;
import co.edu.unbosque.alloggio.repository.ServicioAlojamientoRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 * Pruebas unitarias de AlojamientoService.
 * 
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AlojamientoService - búsqueda, detalle y reseñas en el detalle")
class AlojamientoServiceTest {

    @Mock
    private PublicacionRepository publicacionRepo;

    @Mock
    private ServicioAlojamientoRepository servicioRepo;

    @Mock
    private ReglaAlojamientoRepository reglaRepo;

    @Mock
    private ImagenAlojamientoRepository imagenRepo;

    @Mock
    private PeriodoDisponibilidadRepository periodoRepo;

    @Mock
    private ResenaService resenaService;

    @Mock
    private ModelMapper modelMapper;

    @Captor
    private ArgumentCaptor<Specification<Publicacion>> especificacion;

    @Captor
    private ArgumentCaptor<Pageable> paginacion;

    @InjectMocks
    private AlojamientoService alojamientoService;

    @Nested
    @DisplayName("CU-013 - Buscar alojamientos")
    class BuscarAlojamientos {

        @Test
        @DisplayName("CP-033 - Búsqueda de alojamientos exitosa (criterios válidos)")
        void validarCriterios_criteriosValidos_retorna0() {
            LocalDate llegada = LocalDate.now().plusDays(10);
            LocalDate salida = LocalDate.now().plusDays(13);

            int estado = alojamientoService.validarCriterios(llegada, salida, 100000.0, 400000.0, 4);

            assertEquals(0, estado);
        }

        @Test
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @DisplayName("CP-033 - Búsqueda de alojamientos exitosa (aplica todos los filtros y lista el resultado)")
        void buscar_todosLosFiltros_aplicaTodosLosFiltrosYDevuelveElListado() {
            // Arrange
            LocalDate llegada = LocalDate.now().plusDays(10);
            LocalDate salida = LocalDate.now().plusDays(13);
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            ImagenAlojamiento principal = imagen(1L, "https://img.test/casa-1.jpg", 1, pub);
            ImagenAlojamientoDTO principalDto = imagenDto(1L, "https://img.test/casa-1.jpg", 1);
            when(publicacionRepo.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(pub), PageRequest.of(0, 10), 1));
            when(imagenRepo.findFirstByPublicacionIdPublicacionOrderByOrdenAsc(ID_PUBLICACION))
                    .thenReturn(Optional.of(principal));
            when(modelMapper.map(principal, ImagenAlojamientoDTO.class)).thenReturn(principalDto);

            // Act
            PaginaDTO<AlojamientoResumenDTO> pagina = alojamientoService.buscar(" Bogotá ", llegada, salida,
                    100000.0, 400000.0, 4, TipoAlojamiento.CASA, List.of("WiFi", "Piscina"), 0, 10);

            // Assert: el listado
            AlojamientoResumenDTO resumen = pagina.getContenido().get(0);
            assertAll(
                    () -> assertEquals(1, pagina.getContenido().size()),
                    () -> assertEquals(1L, pagina.getTotalElementos()),
                    () -> assertEquals(ID_PUBLICACION, resumen.getIdPublicacion()),
                    () -> assertEquals(pub.getTitulo(), resumen.getTitulo()),
                    () -> assertEquals(pub.getPrecioNoche(), resumen.getPrecioNoche()),
                    () -> assertSame(principalDto, resumen.getImagenPrincipal()));

            // Assert: paginación y orden (precio por noche ascendente)
            verify(publicacionRepo).findAll(especificacion.capture(), paginacion.capture());
            Pageable pageable = paginacion.getValue();
            assertAll(
                    () -> assertEquals(0, pageable.getPageNumber()),
                    () -> assertEquals(10, pageable.getPageSize()),
                    () -> assertEquals(Sort.Direction.ASC,
                            pageable.getSort().getOrderFor("precioNoche").getDirection()));

            // Assert: los filtros que arma la especificación (CA-25, CA-26, RN-04).

            CriteriaBuilder cb = mock(CriteriaBuilder.class, RETURNS_DEEP_STUBS);
            CriteriaQuery<?> query = mock(CriteriaQuery.class, RETURNS_DEEP_STUBS);
            Root<Publicacion> root = mock(Root.class, RETURNS_DEEP_STUBS);

            especificacion.getValue().toPredicate(root, query, cb);

            assertAll(
                    () -> verify(cb).equal(any(), eq(EstadoPublicacion.ACTIVA)),
                    () -> verify(cb).equal(any(), eq("bogotá")),
                    () -> verify(cb).greaterThanOrEqualTo(any(), eq(Double.valueOf(100000.0))),
                    () -> verify(cb).lessThanOrEqualTo(any(), eq(Double.valueOf(400000.0))),
                    () -> verify(cb).greaterThanOrEqualTo(any(), eq(Integer.valueOf(4))),
                    () -> verify(cb).equal(any(), eq(TipoAlojamiento.CASA)),
                    // disponibilidad: no debe existir un periodo que cruce el rango
                    () -> verify(cb).lessThan(any(), eq(salida)),
                    () -> verify(cb).greaterThan(any(), eq(llegada)),
                    () -> verify(cb).not(any()),
                    // un servicio exigido por cada nombre
                    () -> verify(cb).equal(any(), eq("wifi")),
                    () -> verify(cb).equal(any(), eq("piscina")),
                    // 1 subconsulta de disponibilidad + 2 de servicios
                    () -> verify(query, times(3)).subquery(Long.class),
                    () -> verify(cb, times(3)).exists(any()));
        }

        @Test
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @DisplayName("CP-034 - Búsqueda sin resultados")
        void buscar_sinResultados_devuelvePaginaVaciaSinError() {
            when(publicacionRepo.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<Publicacion>(List.of(), PageRequest.of(0, 10), 0));

            PaginaDTO<AlojamientoResumenDTO> pagina = alojamientoService.buscar("Leticia", null, null, null, null,
                    null, null, null, 0, 10);

            assertAll(
                    () -> assertNotNull(pagina),
                    () -> assertTrue(pagina.getContenido().isEmpty()),
                    () -> assertEquals(0L, pagina.getTotalElementos()),
                    () -> assertEquals(0, pagina.getTotalPaginas()));
            verifyNoInteractions(imagenRepo, modelMapper);
        }

        @Test
        @DisplayName("CP-035 - Búsqueda con fecha inicial mayor que la final")
        void validarCriterios_llegadaPosteriorASalida_retorna2() {
            LocalDate llegada = LocalDate.now().plusDays(13);
            LocalDate salida = LocalDate.now().plusDays(10);

            int estado = alojamientoService.validarCriterios(llegada, salida, null, null, null);

            assertEquals(2, estado);
            // No se consulta nada: la búsqueda se rechaza antes de llegar al repositorio
            verifyNoInteractions(publicacionRepo);
        }
    }

    @Nested
    @DisplayName("CU-014 - Consultar detalle de alojamiento")
    class ConsultarDetalle {

        @Test
        @DisplayName("CP-036 - Consulta de detalle de alojamiento exitosa")
        void obtenerDetalle_alojamientoActivo_devuelveDetalleCompleto() {
            // Arrange
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            when(publicacionRepo.findById(ID_PUBLICACION)).thenReturn(Optional.of(pub));

            ServicioAlojamiento servicio = servicio(1L, "WiFi", null, false, pub);
            ServicioAlojamientoDTO servicioDto = servicioDto(1L, "WiFi", null, false);
            when(servicioRepo.findByPublicacionIdPublicacion(ID_PUBLICACION)).thenReturn(List.of(servicio));
            when(modelMapper.map(servicio, ServicioAlojamientoDTO.class)).thenReturn(servicioDto);

            ReglaAlojamiento regla = regla(1L, "No fumar", pub);
            ReglaAlojamientoDTO reglaDto = reglaDto(1L, "No fumar");
            when(reglaRepo.findByPublicacionIdPublicacion(ID_PUBLICACION)).thenReturn(List.of(regla));
            when(modelMapper.map(regla, ReglaAlojamientoDTO.class)).thenReturn(reglaDto);

            ImagenAlojamiento imagen = imagen(1L, "https://img.test/casa-1.jpg", 1, pub);
            ImagenAlojamientoDTO imagenDto = imagenDto(1L, "https://img.test/casa-1.jpg", 1);
            when(imagenRepo.findByPublicacionIdPublicacionOrderByOrdenAsc(ID_PUBLICACION))
                    .thenReturn(List.of(imagen));
            when(modelMapper.map(imagen, ImagenAlojamientoDTO.class)).thenReturn(imagenDto);

            LocalDate hoy = LocalDate.now();
            PeriodoDisponibilidad bloqueo = periodo(1L, hoy.plusDays(5), hoy.plusDays(8), TipoPeriodo.BLOQUEADO,
                    pub);
            PeriodoDisponibilidad reservado = periodo(2L, hoy.plusDays(20), hoy.plusDays(23),
                    TipoPeriodo.RESERVADO, pub);
            when(periodoRepo.findByPublicacionIdPublicacionAndFechaFinAfterOrderByFechaInicioAsc(
                    eq(ID_PUBLICACION), any(LocalDate.class))).thenReturn(List.of(bloqueo, reservado));

            AlojamientoDetalleDTO detalle = alojamientoService.obtenerDetalle(ID_PUBLICACION);

            // Assert: CA-28 (descripción, servicios, reglas, imágenes, ubicación
            // textual, calendario y precio) más la política de cancelación
            assertNotNull(detalle);
            assertAll(
                    () -> assertEquals(ID_PUBLICACION, detalle.getIdPublicacion()),
                    () -> assertEquals(pub.getDescripcion(), detalle.getDescripcion()),
                    () -> assertEquals(pub.getUbicacion(), detalle.getUbicacion()),
                    () -> assertEquals(pub.getPrecioNoche(), detalle.getPrecioNoche()),
                    () -> assertEquals(1L, detalle.getPoliticaCancelacion().getIdPolitica()),
                    () -> assertEquals("Flexible", detalle.getPoliticaCancelacion().getNombre()),
                    () -> assertEquals(1, detalle.getServicios().size()),
                    () -> assertSame(servicioDto, detalle.getServicios().get(0)),
                    () -> assertSame(reglaDto, detalle.getReglas().get(0)),
                    () -> assertSame(imagenDto, detalle.getImagenes().get(0)),
                    // calendario: bloqueos y reservas salen como simples rangos ocupados
                    () -> assertEquals(2, detalle.getCalendarioOcupado().size()),
                    () -> assertEquals(hoy.plusDays(5), detalle.getCalendarioOcupado().get(0).getFechaInicio()),
                    () -> assertEquals(hoy.plusDays(8), detalle.getCalendarioOcupado().get(0).getFechaFin()),
                    () -> assertEquals(hoy.plusDays(20), detalle.getCalendarioOcupado().get(1).getFechaInicio()),
                    // solo se piden los periodos que terminan después de hoy
                    () -> verify(periodoRepo).findByPublicacionIdPublicacionAndFechaFinAfterOrderByFechaInicioAsc(
                            ID_PUBLICACION, hoy));
        }

        @Test
        @DisplayName("CP-037 - Consulta de detalle de alojamiento inexistente o no disponible (inexistente)")
        void obtenerDetalle_alojamientoInexistente_retornaNull() {
            when(publicacionRepo.findById(999L)).thenReturn(Optional.empty());

            AlojamientoDetalleDTO detalle = alojamientoService.obtenerDetalle(999L);

            assertNull(detalle);
            verifyNoInteractions(servicioRepo, reglaRepo, imagenRepo, periodoRepo, resenaService);
        }

        @ParameterizedTest(name = "alojamiento en estado {0}")
        @EnumSource(value = EstadoPublicacion.class, mode = EnumSource.Mode.EXCLUDE, names = { "ACTIVA" })
        @DisplayName("CP-037 - Consulta de detalle de alojamiento inexistente o no disponible (no activo, RN-04)")
        void obtenerDetalle_publicacionNoActiva_retornaNull(EstadoPublicacion estado) {
            Publicacion pub = publicacion(usuarioActivo(), estado);
            when(publicacionRepo.findById(ID_PUBLICACION)).thenReturn(Optional.of(pub));

            AlojamientoDetalleDTO detalle = alojamientoService.obtenerDetalle(ID_PUBLICACION);

            assertNull(detalle);
            verifyNoInteractions(servicioRepo, reglaRepo, imagenRepo, periodoRepo, resenaService);
        }
    }

    @Nested
    @DisplayName("CU-015 - Consultar reseñas y calificaciones (dentro del detalle)")
    class ResenasEnElDetalle {

        @Test
        @DisplayName("CP-038 - Consulta de reseñas y calificaciones exitosa (el detalle las incluye)")
        void obtenerDetalle_conResenasVisibles_incluyePromedioTotalYResenas() {
            // Arrange
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            when(publicacionRepo.findById(ID_PUBLICACION)).thenReturn(Optional.of(pub));
            ResenaDTO resena = new ResenaDTO();
            resena.setIdResena(1L);
            resena.setCalificacion(5);
            resena.setComentario("Excelente lugar");
            resena.setAutor("Juan Pérez");
            when(resenaService.calcularPromedio(ID_PUBLICACION)).thenReturn(4.5);
            when(resenaService.contarVisibles(ID_PUBLICACION)).thenReturn(2L);
            when(resenaService.obtenerVisiblesRecientes(ID_PUBLICACION)).thenReturn(List.of(resena));

            AlojamientoDetalleDTO detalle = alojamientoService.obtenerDetalle(ID_PUBLICACION);

            // Assert
            assertAll(
                    () -> assertEquals(4.5, detalle.getPromedioCalificacion(), 0.0001),
                    () -> assertEquals(2L, detalle.getTotalResenas()),
                    () -> assertEquals(1, detalle.getResenas().size()),
                    () -> assertSame(resena, detalle.getResenas().get(0)));
        }

        @Test
        @DisplayName("CP-039 - Consulta de alojamiento sin reseñas visibles (el detalle no falla)")
        void obtenerDetalle_sinResenasVisibles_promedioNuloTotalCeroYListaVacia() {
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            when(publicacionRepo.findById(ID_PUBLICACION)).thenReturn(Optional.of(pub));
            when(resenaService.calcularPromedio(ID_PUBLICACION)).thenReturn(null);
            when(resenaService.contarVisibles(ID_PUBLICACION)).thenReturn(0L);
            when(resenaService.obtenerVisiblesRecientes(ID_PUBLICACION)).thenReturn(List.of());

            AlojamientoDetalleDTO detalle = alojamientoService.obtenerDetalle(ID_PUBLICACION);

            assertNotNull(detalle);
            assertAll(
                    () -> assertNull(detalle.getPromedioCalificacion()),
                    () -> assertEquals(0L, detalle.getTotalResenas()),
                    () -> assertTrue(detalle.getResenas().isEmpty()));
        }
    }
}