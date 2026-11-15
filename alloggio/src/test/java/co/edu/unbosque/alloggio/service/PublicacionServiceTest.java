package co.edu.unbosque.alloggio.service;

import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.ID_PUBLICACION;
import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.OTRO_USUARIO;
import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.dtoDe;
import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.dtoPublicacionValido;
import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.politica;
import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.publicacion;
import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.publicacionDesde;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.NOMBRE_USUARIO;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.usuarioActivo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import co.edu.unbosque.alloggio.dto.PublicacionDTO;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Accion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.TipoEntidad;
import co.edu.unbosque.alloggio.entity.ImagenAlojamiento;
import co.edu.unbosque.alloggio.entity.PoliticaCancelacion;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.ImagenAlojamientoRepository;
import co.edu.unbosque.alloggio.repository.PoliticaCancelacionRepository;
import co.edu.unbosque.alloggio.repository.PublicacionRepository;
import co.edu.unbosque.alloggio.repository.ReservaRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;

/**
 * Pruebas unitarias de PublicacionService.
 *
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PublicacionService - módulo de publicaciones")
class PublicacionServiceTest {

    @Mock
    private PublicacionRepository publicacionRepo;

    @Mock
    private UsuarioRepository usuarioRepo;

    @Mock
    private PoliticaCancelacionRepository politicaRepo;

    @Mock
    private ImagenAlojamientoRepository imagenRepo;

    @Mock
    private ReservaRepository reservaRepo;

    @Mock
    private EventoAuditoriaService auditoriaService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PublicacionService publicacionService;

    // Fuentes de datos para pruebas parametrizadas

    private static Arguments casoDto(String descripcion, Consumer<PublicacionDTO> mutacion, String esperado) {
        return Arguments.of(descripcion, mutacion, esperado);
    }

    private static Arguments casoIncompleta(String descripcion, Consumer<Publicacion> mutacion, boolean conImagen,
            String faltante) {
        return Arguments.of(descripcion, mutacion, conImagen, faltante);
    }

    /**
     * CP-020: cada fila trae un valor incorrecto y el texto con que se reporta.
     */
    static Stream<Arguments> datosIncorrectosDePublicacion() {
        return Stream.of(
                casoDto("capacidad en cero", d -> d.setCapacidad(0), "capacidad (mayor que cero)"),
                casoDto("capacidad negativa", d -> d.setCapacidad(-3), "capacidad (mayor que cero)"),
                casoDto("precio por noche en cero", d -> d.setPrecioNoche(0.0), "precio por noche (positivo)"),
                casoDto("precio por noche negativo", d -> d.setPrecioNoche(-150000.0),
                        "precio por noche (positivo)"));
    }

    /**
     * CP-025: cada fila deja incompleta la información mínima de la publicación.
     */
    static Stream<Arguments> publicacionesConInformacionIncompleta() {
        return Stream.of(
                casoIncompleta("sin imágenes", p -> {
                }, false, "al menos una imagen"),
                casoIncompleta("sin título", p -> p.setTitulo(null), true, "título"),
                casoIncompleta("precio por noche en cero", p -> p.setPrecioNoche(0.0), true,
                        "precio por noche (positivo)"));
    }

    // CU-007 - Crear publicación

    @Nested
    @DisplayName("CU-007 - Crear publicación")
    class CrearPublicacion {

        @Test
        @DisplayName("CP-018 - Creación de publicación exitosa")
        void crearPublicacion_datosValidos_creaBorradorAsociadoAlAnfitrion() {
            Usuario anfitrion = usuarioActivo();
            PoliticaCancelacion politica = politica(1L);
            PublicacionDTO dto = dtoPublicacionValido();
            // El cliente intenta ingresar un ID y un estado: el service debe ignorarlos
            dto.setIdPublicacion(99L);
            dto.setEstado(EstadoPublicacion.ACTIVA);
            AtomicReference<Long> idAlGuardar = new AtomicReference<>();
            when(usuarioRepo.findByNombreUsuario(NOMBRE_USUARIO)).thenReturn(Optional.of(anfitrion));
            when(politicaRepo.findById(1L)).thenReturn(Optional.of(politica));
            when(modelMapper.map(dto, Publicacion.class)).thenReturn(publicacionDesde(dto));
            when(publicacionRepo.save(any(Publicacion.class))).thenAnswer(invocacion -> {
                Publicacion p = invocacion.getArgument(0);
                idAlGuardar.set(p.getIdPublicacion());
                p.setIdPublicacion(ID_PUBLICACION);
                return p;
            });

            int resultado = publicacionService.crearPublicacion(NOMBRE_USUARIO, dto);

            assertEquals(0, resultado);
            ArgumentCaptor<Publicacion> captor = ArgumentCaptor.forClass(Publicacion.class);
            verify(publicacionRepo).save(captor.capture());
            Publicacion guardada = captor.getValue();
            assertAll("publicación creada",
                    () -> assertEquals(EstadoPublicacion.BORRADOR, guardada.getEstado()),
                    () -> assertSame(anfitrion, guardada.getAnfitrion()),
                    () -> assertSame(politica, guardada.getPoliticaCancelacion()),
                    // Un ID enviado en el cuerpo no puede sobrescribir otra publicación
                    () -> assertNull(idAlGuardar.get()));
            // RN-15: la creación deja registro en auditoría
            verify(auditoriaService).registrarExito(eq(NOMBRE_USUARIO), eq(Accion.CREAR_PUBLICACION),
                    eq(TipoEntidad.PUBLICACION), eq(ID_PUBLICACION), anyString());
        }

        @Test
        @DisplayName("CP-019 - Creación de publicación con campos faltantes")
        void crearPublicacion_camposFaltantes_retorna3SeñalaLosCamposYNoCrea() {
            PublicacionDTO vacio = new PublicacionDTO();
            when(usuarioRepo.findByNombreUsuario(NOMBRE_USUARIO)).thenReturn(Optional.of(usuarioActivo()));

            List<String> faltantes = publicacionService.validarDatos(vacio);
            int resultado = publicacionService.crearPublicacion(NOMBRE_USUARIO, vacio);

            assertTrue(faltantes.containsAll(List.of("título", "descripción", "ubicación", "ciudad",
                    "tipo de alojamiento", "capacidad (mayor que cero)", "precio por noche (positivo)",
                    "tarifa de limpieza (cero o mayor)", "política de cancelación")),
                    "Debe señalar todos los campos faltantes, pero señaló: " + faltantes);
            assertEquals(3, resultado);
            verify(publicacionRepo, never()).save(any(Publicacion.class));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("co.edu.unbosque.alloggio.service.PublicacionServiceTest#datosIncorrectosDePublicacion")
        @DisplayName("CP-020 - Creación de publicación con datos incorrectos")
        void crearPublicacion_datosIncorrectos_retorna3IndicaElValorYNoCrea(String descripcion,
                Consumer<PublicacionDTO> mutacion, String esperado) {
            PublicacionDTO dto = dtoPublicacionValido();
            mutacion.accept(dto);
            when(usuarioRepo.findByNombreUsuario(NOMBRE_USUARIO)).thenReturn(Optional.of(usuarioActivo()));

            assertTrue(publicacionService.validarDatos(dto).contains(esperado),
                    "Debe indicar el valor a corregir: " + esperado);
            assertEquals(3, publicacionService.crearPublicacion(NOMBRE_USUARIO, dto));
            verify(publicacionRepo, never()).save(any(Publicacion.class));
        }
    }

    // CU-008 - Editar publicación

    @Nested
    @DisplayName("CU-008 - Editar publicación")
    class EditarPublicacion {

        @Test
        @DisplayName("CP-021 - Edición de publicación exitosa")
        void actualizarPublicacion_propietarioConDatosValidos_guardaLosCambios() {
            Publicacion existente = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            PoliticaCancelacion nuevaPolitica = politica(2L);
            PublicacionDTO cambios = dtoPublicacionValido();
            cambios.setTitulo("Casa renovada en Villa de Leyva");
            cambios.setCapacidad(8);
            cambios.setPrecioNoche(320000.0);
            cambios.setIdPoliticaCancelacion(2L);
            when(publicacionRepo.findByIdParaActualizar(ID_PUBLICACION)).thenReturn(Optional.of(existente));
            when(politicaRepo.findById(2L)).thenReturn(Optional.of(nuevaPolitica));

            int resultado = publicacionService.actualizarPublicacion(NOMBRE_USUARIO, ID_PUBLICACION, cambios);

            assertEquals(0, resultado);
            verify(publicacionRepo).save(existente);
            assertAll("publicación actualizada",
                    () -> assertEquals("Casa renovada en Villa de Leyva", existente.getTitulo()),
                    () -> assertEquals(8, existente.getCapacidad()),
                    () -> assertEquals(320000.0, existente.getPrecioNoche()),
                    () -> assertSame(nuevaPolitica, existente.getPoliticaCancelacion()),
                    // Editar los datos no cambia el estado de la publicación
                    () -> assertEquals(EstadoPublicacion.ACTIVA, existente.getEstado()));
            verify(auditoriaService).registrarExito(eq(NOMBRE_USUARIO), eq(Accion.ACTUALIZAR_PUBLICACION),
                    eq(TipoEntidad.PUBLICACION), eq(ID_PUBLICACION), anyString());
        }

        @Test
        @DisplayName("CP-022 - Edición de publicación ajena")
        void actualizarPublicacion_usuarioQueNoEsPropietario_retorna3YNoCambiaNada() {
            Publicacion existente = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            PublicacionDTO cambios = dtoPublicacionValido();
            cambios.setTitulo("Título que no debe quedar");
            when(publicacionRepo.findByIdParaActualizar(ID_PUBLICACION)).thenReturn(Optional.of(existente));

            int resultado = publicacionService.actualizarPublicacion(OTRO_USUARIO, ID_PUBLICACION, cambios);

            assertEquals(3, resultado);
            verify(publicacionRepo, never()).save(any(Publicacion.class));
            assertEquals("Casa campestre en Villa de Leyva", existente.getTitulo());
            // El intento queda registrado como denegado (RN-15)
            verify(auditoriaService).registrarDenegado(eq(OTRO_USUARIO), eq(Accion.ACTUALIZAR_PUBLICACION),
                    eq(TipoEntidad.PUBLICACION), eq(ID_PUBLICACION), anyString());
        }

        @Test
        @DisplayName("CP-023 - Edición de publicación con valor inválido")
        void actualizarPublicacion_valorInvalido_retorna4YNoCambiaLaPublicacion() {
            Publicacion existente = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            PublicacionDTO cambios = dtoPublicacionValido();
            cambios.setCapacidad(0);
            cambios.setPrecioNoche(-1.0);
            when(publicacionRepo.findByIdParaActualizar(ID_PUBLICACION)).thenReturn(Optional.of(existente));

            int resultado = publicacionService.actualizarPublicacion(NOMBRE_USUARIO, ID_PUBLICACION, cambios);

            assertEquals(4, resultado);
            assertTrue(publicacionService.validarDatos(cambios).contains("capacidad (mayor que cero)"));
            verify(publicacionRepo, never()).save(any(Publicacion.class));
            assertEquals(6, existente.getCapacidad());
            assertEquals(250000.0, existente.getPrecioNoche());
        }

        @Test
        @DisplayName("Complementaria (RN-03, RN-14) - Una publicación bloqueada no se puede editar (5)")
        void actualizarPublicacion_publicacionBloqueada_retorna5() {
            Publicacion bloqueada = publicacion(usuarioActivo(), EstadoPublicacion.BLOQUEADA);
            when(publicacionRepo.findByIdParaActualizar(ID_PUBLICACION)).thenReturn(Optional.of(bloqueada));

            int resultado = publicacionService.actualizarPublicacion(NOMBRE_USUARIO, ID_PUBLICACION,
                    dtoPublicacionValido());

            assertEquals(5, resultado);
            verify(publicacionRepo, never()).save(any(Publicacion.class));
        }
    }

    // CU-009 - Activar publicación

    @Nested
    @DisplayName("CU-009 - Activar publicación")
    class ActivarPublicacion {

        @ParameterizedTest(name = "desde {0}")
        @EnumSource(value = EstadoPublicacion.class, names = { "BORRADOR", "PAUSADA" })
        @DisplayName("CP-024 - Activación de publicación exitosa")
        void activarPublicacion_borradorOPausadaCompleta_quedaActiva(EstadoPublicacion estadoInicial) {
            Publicacion pub = publicacion(usuarioActivo(), estadoInicial);
            when(publicacionRepo.findByIdParaActualizar(ID_PUBLICACION)).thenReturn(Optional.of(pub));
            when(imagenRepo.findByPublicacionIdPublicacionOrderByOrdenAsc(ID_PUBLICACION))
                    .thenReturn(List.of(new ImagenAlojamiento()));

            int resultado = publicacionService.activarPublicacion(NOMBRE_USUARIO, ID_PUBLICACION);

            assertEquals(0, resultado);
            assertEquals(EstadoPublicacion.ACTIVA, pub.getEstado());
            verify(publicacionRepo).save(pub);
            verify(auditoriaService).registrarExito(eq(NOMBRE_USUARIO), eq(Accion.ACTIVAR_PUBLICACION),
                    eq(TipoEntidad.PUBLICACION), eq(ID_PUBLICACION), anyString());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("co.edu.unbosque.alloggio.service.PublicacionServiceTest#publicacionesConInformacionIncompleta")
        @DisplayName("CP-025 - Activación con información mínima incompleta")
        void activarPublicacion_informacionIncompleta_retorna4IndicaLosDatosFaltantes(String descripcion,
                Consumer<Publicacion> mutacion, boolean conImagen, String faltante) {
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.BORRADOR);
            mutacion.accept(pub);
            List<ImagenAlojamiento> imagenes = conImagen ? List.of(new ImagenAlojamiento()) : List.of();
            when(publicacionRepo.findByIdParaActualizar(ID_PUBLICACION)).thenReturn(Optional.of(pub));
            when(publicacionRepo.findById(ID_PUBLICACION)).thenReturn(Optional.of(pub));
            when(imagenRepo.findByPublicacionIdPublicacionOrderByOrdenAsc(ID_PUBLICACION)).thenReturn(imagenes);

            int resultado = publicacionService.activarPublicacion(NOMBRE_USUARIO, ID_PUBLICACION);

            assertEquals(4, resultado);
            assertTrue(publicacionService.obtenerDatosFaltantes(ID_PUBLICACION).contains(faltante),
                    "Debe indicar el dato faltante: " + faltante);
            assertEquals(EstadoPublicacion.BORRADOR, pub.getEstado());
            verify(publicacionRepo, never()).save(any(Publicacion.class));
        }

        @Test
        @DisplayName("Complementaria (RN-03, RN-14) - Una publicación bloqueada no se puede activar (3)")
        void activarPublicacion_publicacionBloqueada_retorna3YSigueBloqueada() {
            Publicacion bloqueada = publicacion(usuarioActivo(), EstadoPublicacion.BLOQUEADA);
            when(publicacionRepo.findByIdParaActualizar(ID_PUBLICACION)).thenReturn(Optional.of(bloqueada));

            int resultado = publicacionService.activarPublicacion(NOMBRE_USUARIO, ID_PUBLICACION);

            assertEquals(3, resultado);
            assertEquals(EstadoPublicacion.BLOQUEADA, bloqueada.getEstado());
            verify(publicacionRepo, never()).save(any(Publicacion.class));
        }
    }

    // CU-010 - Pausar publicación

    @Nested
    @DisplayName("CU-010 - Pausar publicación")
    class PausarPublicacion {

        @Test
        @DisplayName("CP-026 - Pausa de publicación exitosa")
        void pausarPublicacion_publicacionActiva_quedaPausada() {
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            when(publicacionRepo.findById(ID_PUBLICACION)).thenReturn(Optional.of(pub));

            int resultado = publicacionService.pausarPublicacion(NOMBRE_USUARIO, ID_PUBLICACION);

            assertEquals(0, resultado);
            // RN-04: solo las ACTIVAS aparecen en búsquedas; al pausar deja de aparecer
            assertEquals(EstadoPublicacion.PAUSADA, pub.getEstado());
            verify(publicacionRepo).save(pub);
            verify(auditoriaService).registrarExito(eq(NOMBRE_USUARIO), eq(Accion.PAUSAR_PUBLICACION),
                    eq(TipoEntidad.PUBLICACION), eq(ID_PUBLICACION), anyString());
        }

        @Test
        @DisplayName("CP-027 - Pausa de publicación con reservas futuras confirmadas")
        void pausarPublicacion_conReservasFuturas_pausaSinTocarLasReservas() {
            Publicacion pub = publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA);
            when(publicacionRepo.findById(ID_PUBLICACION)).thenReturn(Optional.of(pub));

            int resultado = publicacionService.pausarPublicacion(NOMBRE_USUARIO, ID_PUBLICACION);

            assertEquals(0, resultado);
            assertEquals(EstadoPublicacion.PAUSADA, pub.getEstado());
            // Prueba de resguardo: pausar nunca debe consultar, cancelar ni modificar
            // reservas
            verifyNoInteractions(reservaRepo);
        }

        @ParameterizedTest(name = "estado {0}")
        @EnumSource(value = EstadoPublicacion.class, mode = EnumSource.Mode.EXCLUDE, names = { "ACTIVA" })
        @DisplayName("CP-028 - Pausa de publicación no activa")
        void pausarPublicacion_publicacionNoActiva_retorna3YElEstadoNoCambia(EstadoPublicacion estado) {
            Publicacion pub = publicacion(usuarioActivo(), estado);
            when(publicacionRepo.findById(ID_PUBLICACION)).thenReturn(Optional.of(pub));

            int resultado = publicacionService.pausarPublicacion(NOMBRE_USUARIO, ID_PUBLICACION);

            assertEquals(3, resultado);
            assertEquals(estado, pub.getEstado());
            verify(publicacionRepo, never()).save(any(Publicacion.class));
        }
    }

    // CU-011 - Consultar publicaciones propias

    @Nested
    @DisplayName("CU-011 - Consultar publicaciones propias")
    class ConsultarPublicacionesPropias {

        @Test
        @DisplayName("CP-029 - Consulta de publicaciones propias exitosa")
        void obtenerPropias_anfitrionConPublicaciones_devuelveSoloLasSuyasConSuEstado() {
            Usuario anfitrion = usuarioActivo();
            Publicacion borrador = publicacion(anfitrion, EstadoPublicacion.BORRADOR);
            Publicacion activa = publicacion(anfitrion, EstadoPublicacion.ACTIVA);
            activa.setIdPublicacion(11L);
            when(usuarioRepo.findByNombreUsuario(NOMBRE_USUARIO)).thenReturn(Optional.of(anfitrion));
            when(publicacionRepo.findByAnfitrionIdUsuario(anfitrion.getIdUsuario()))
                    .thenReturn(List.of(borrador, activa));
            when(modelMapper.map(borrador, PublicacionDTO.class)).thenReturn(dtoDe(borrador));
            when(modelMapper.map(activa, PublicacionDTO.class)).thenReturn(dtoDe(activa));

            List<PublicacionDTO> lista = publicacionService.obtenerPropias(NOMBRE_USUARIO);

            assertEquals(2, lista.size());
            assertAll("listado del anfitrión",
                    () -> assertEquals(EstadoPublicacion.BORRADOR, lista.get(0).getEstado()),
                    () -> assertEquals(EstadoPublicacion.ACTIVA, lista.get(1).getEstado()),
                    () -> assertEquals(anfitrion.getIdUsuario(), lista.get(0).getIdAnfitrion()),
                    () -> assertEquals(anfitrion.getIdUsuario(), lista.get(1).getIdAnfitrion()));
            // Nunca se consulta el listado completo de la plataforma
            verify(publicacionRepo, never()).findAll();
        }

        @Test
        @DisplayName("CP-030 - Consulta de publicaciones sin publicaciones registradas")
        void obtenerPropias_anfitrionSinPublicaciones_devuelveListaVaciaNoNula() {
            Usuario anfitrion = usuarioActivo();
            when(usuarioRepo.findByNombreUsuario(NOMBRE_USUARIO)).thenReturn(Optional.of(anfitrion));
            when(publicacionRepo.findByAnfitrionIdUsuario(anfitrion.getIdUsuario())).thenReturn(List.of());

            List<PublicacionDTO> lista = publicacionService.obtenerPropias(NOMBRE_USUARIO);

            assertNotNull(lista);
            assertTrue(lista.isEmpty());
        }
    }
}