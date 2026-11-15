package co.edu.unbosque.alloggio.service;

import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.CLAVE_CIFRADA;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.CLAVE_VALIDA;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.NOMBRE_USUARIO;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.dtoRegistroValido;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.entidadDesde;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.usuarioActivo;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.usuarioInactivo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.alloggio.dto.UsuarioDTO;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Accion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.TipoEntidad;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;

/**
 * Pruebas unitarias de UsuarioService para el módulo de cuentas.
 * 
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - módulo de cuentas")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EventoAuditoriaService auditoriaService;

    @InjectMocks
    private UsuarioService usuarioService;

    // Fuentes de datos para pruebas parametrizadas

    private static Arguments caso(String descripcion, Consumer<UsuarioDTO> mutacion, Object esperado) {
        return Arguments.of(descripcion, mutacion, esperado);
    }

    /**
     * CP-003: cada fila rompe un solo dato del registro válido.
     */
    static Stream<Arguments> registrosConDatosFaltantesOInvalidos() {
        return Stream.of(
                caso("sin correo", d -> d.setCorreoUsuario(null), "correoUsuario"),
                caso("correo con formato inválido", d -> d.setCorreoUsuario("correo-sin-arroba"), "correoUsuario"),
                caso("sin nombre de usuario", d -> d.setNombreUsuario(" "), "nombreUsuario"),
                caso("sin contraseña", d -> d.setClaveUsuario(null), "claveUsuario"),
                caso("sin documento de identidad", d -> d.setDocumentoIdentidad(null), "documentoIdentidad"),
                caso("sin primer nombre", d -> d.setPrimerNombre(null), "primerNombre"),
                caso("primer nombre con números", d -> d.setPrimerNombre("Juan123"), "primerNombre"),
                caso("sin primer apellido", d -> d.setPrimerApellido(""), "primerApellido"),
                caso("sin fecha de nacimiento", d -> d.setFechaNacimiento(null), "fechaNacimiento"),
                caso("fecha de nacimiento futura", d -> d.setFechaNacimiento(LocalDate.now().plusDays(1)),
                        "fechaNacimiento"),
                caso("sin teléfono", d -> d.setNumeroTelefono(null), "numeroTelefono"),
                caso("teléfono con letras", d -> d.setNumeroTelefono("abc123"), "numeroTelefono"));
    }

    /**
     * CP-013: cada fila trae un valor inválido y el código de retorno esperado.
     */
    static Stream<Arguments> perfilesConValorInvalido() {
        return Stream.of(
                caso("primer nombre vacío", d -> d.setPrimerNombre(""), 2),
                caso("primer nombre con números", d -> d.setPrimerNombre("Ana123"), 2),
                caso("segundo nombre con símbolos", d -> d.setSegundoNombre("Mar@"), 3),
                caso("primer apellido vacío", d -> d.setPrimerApellido(null), 4),
                caso("segundo apellido con números", d -> d.setSegundoApellido("123"), 5),
                caso("teléfono con letras", d -> d.setNumeroTelefono("abc"), 6),
                caso("teléfono muy corto", d -> d.setNumeroTelefono("12345"), 6),
                caso("teléfono nulo", d -> d.setNumeroTelefono(null), 6));
    }

    private static UsuarioDTO dtoPerfilValido() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setPrimerNombre("Andrés");
        dto.setSegundoNombre("Felipe");
        dto.setPrimerApellido("López");
        dto.setSegundoApellido("Rojas");
        dto.setNumeroTelefono("+573001112233");
        return dto;
    }

    // CU-001 - Registrar usuario

    @Nested
    @DisplayName("CU-001 - Registrar usuario")
    class RegistrarUsuario {

        @Test
        @DisplayName("CP-001 - Registro de usuario exitoso")
        void crear_datosValidos_creaCuentaActivaConRolUsuarioYClaveCifrada() {
            UsuarioDTO dto = dtoRegistroValido();
            when(modelMapper.map(dto, Usuario.class)).thenReturn(entidadDesde(dto));
            when(passwordEncoder.encode(CLAVE_VALIDA)).thenReturn(CLAVE_CIFRADA);

            int resultado = usuarioService.create(dto);

            assertEquals(0, resultado);
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepo).save(captor.capture());
            Usuario guardado = captor.getValue();
            assertAll("cuenta creada",
                    () -> assertNull(guardado.getIdUsuario()),
                    () -> assertEquals(Usuario.Role.USUARIO, guardado.getRole()),
                    () -> assertTrue(guardado.getEstaActivo()),
                    () -> assertEquals(Usuario.ESTADO_ACTIVA, guardado.getEstadoCuenta()),
                    () -> assertEquals(25, guardado.getEdad()),
                    // RQNF-003: la contraseña nunca se guarda en texto plano
                    () -> assertEquals(CLAVE_CIFRADA, guardado.getClaveUsuario()),
                    () -> assertNotEquals(CLAVE_VALIDA, guardado.getClaveUsuario()));
        }

        @Test
        @DisplayName("CP-002 - Registro con correo ya existente")
        void crear_correoExistente_retorna3YNoCreaCuenta() {
            UsuarioDTO dto = dtoRegistroValido();
            when(modelMapper.map(dto, Usuario.class)).thenReturn(entidadDesde(dto));
            when(usuarioRepo.findByCorreoUsuario(dto.getCorreoUsuario())).thenReturn(Optional.of(usuarioActivo()));

            int resultado = usuarioService.create(dto);

            assertEquals(3, resultado);
            verify(usuarioRepo, never()).save(any(Usuario.class));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("co.edu.unbosque.alloggio.service.UsuarioServiceTest#registrosConDatosFaltantesOInvalidos")
        @DisplayName("CP-003 - Registro con datos faltantes o inválidos")
        void crear_datosFaltantesOInvalidos_retorna5IndicaElCampoYNoCreaCuenta(String descripcion,
                Consumer<UsuarioDTO> mutacion, String campoEsperado) {
            UsuarioDTO dto = dtoRegistroValido();
            mutacion.accept(dto);

            assertTrue(usuarioService.camposInvalidosRegistro(dto).contains(campoEsperado),
                    "Debe señalar el campo a corregir: " + campoEsperado);
            assertEquals(5, usuarioService.create(dto));
            verify(usuarioRepo, never()).save(any(Usuario.class));
        }

        @Test
        @DisplayName("CP-003 - Registro con contraseña que no cumple la complejidad")
        void crear_claveDebil_retorna2YNoCreaCuenta() {
            UsuarioDTO dto = dtoRegistroValido();
            dto.setClaveUsuario("abc");
            when(modelMapper.map(dto, Usuario.class)).thenReturn(entidadDesde(dto));

            int resultado = usuarioService.create(dto);

            assertEquals(2, resultado);
            verify(usuarioRepo, never()).save(any(Usuario.class));
        }
    }

    // CU-003 - Recuperar contraseña

    @Nested
    @DisplayName("CU-003 (apoyo) - Actualización de contraseña")
    class ActualizarContrasena {

        @Test
        @DisplayName("Complementaria (CA-11, RQNF-003) - La nueva contraseña se guarda cifrada")
        void updatePasswordById_claveValida_guardaClaveCifrada() {
            Usuario usuario = usuarioActivo();
            when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.encode("NuevaClave1!")).thenReturn("HASH_NUEVO");
            UsuarioDTO dto = new UsuarioDTO();
            dto.setClaveUsuario("NuevaClave1!");

            int resultado = usuarioService.updatePasswordById(1L, dto);

            assertEquals(0, resultado);
            assertEquals("HASH_NUEVO", usuario.getClaveUsuario());
            verify(usuarioRepo).save(usuario);
        }

    }

    // CU-004 - Gestionar perfil

    @Nested
    @DisplayName("CU-004 - Gestionar perfil")
    class GestionarPerfil {

        @Test
        @DisplayName("CP-011 - Actualización de perfil exitosa")
        void updatePerfil_datosValidos_actualizaSoloLosCamposPermitidos() {
            Usuario usuario = usuarioActivo();
            when(usuarioRepo.findByNombreUsuario(NOMBRE_USUARIO)).thenReturn(Optional.of(usuario));
            UsuarioDTO nuevos = dtoPerfilValido();
            nuevos.setPrimerNombre("  Andrés  ");
            nuevos.setSegundoNombre("   ");
            nuevos.setSegundoApellido(null);
            String correoOriginal = usuario.getCorreoUsuario();

            int resultado = usuarioService.updatePerfil(NOMBRE_USUARIO, nuevos);

            assertEquals(0, resultado);
            verify(usuarioRepo).save(usuario);
            assertAll("perfil actualizado",
                    () -> assertEquals("Andrés", usuario.getPrimerNombre()),
                    () -> assertNull(usuario.getSegundoNombre()),
                    () -> assertEquals("López", usuario.getPrimerApellido()),
                    () -> assertNull(usuario.getSegundoApellido()),
                    () -> assertEquals("+573001112233", usuario.getNumeroTelefono()),
                    // Los campos no editables no se tocan
                    () -> assertEquals(correoOriginal, usuario.getCorreoUsuario()),
                    () -> assertEquals(NOMBRE_USUARIO, usuario.getNombreUsuario()),
                    () -> assertEquals(CLAVE_CIFRADA, usuario.getClaveUsuario()),
                    () -> assertEquals(Usuario.Role.USUARIO, usuario.getRole()));
        }

        @Test
        @DisplayName("CP-012 - Consulta de perfil sin modificaciones")
        void getPerfil_soloConsulta_devuelveDatosSinClaveYNoGuarda() {
            Usuario usuario = usuarioActivo();
            UsuarioDTO mapeado = new UsuarioDTO();
            mapeado.setNombreUsuario(NOMBRE_USUARIO);
            mapeado.setPrimerNombre("Juan");
            mapeado.setClaveUsuario(CLAVE_CIFRADA);
            when(usuarioRepo.findByNombreUsuario(NOMBRE_USUARIO)).thenReturn(Optional.of(usuario));
            when(modelMapper.map(usuario, UsuarioDTO.class)).thenReturn(mapeado);

            UsuarioDTO perfil = usuarioService.getPerfil(NOMBRE_USUARIO);

            assertNotNull(perfil);
            assertEquals("Juan", perfil.getPrimerNombre());
            assertNull(perfil.getClaveUsuario(), "El perfil nunca debe exponer la clave");
            verify(usuarioRepo, never()).save(any(Usuario.class));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("co.edu.unbosque.alloggio.service.UsuarioServiceTest#perfilesConValorInvalido")
        @DisplayName("CP-013 - Actualización de perfil con valor inválido")
        void updatePerfil_valorInvalido_retornaCodigoDelCampoYNoGuarda(String descripcion,
                Consumer<UsuarioDTO> mutacion, int codigoEsperado) {
            Usuario usuario = usuarioActivo();
            when(usuarioRepo.findByNombreUsuario(NOMBRE_USUARIO)).thenReturn(Optional.of(usuario));
            UsuarioDTO dto = dtoPerfilValido();
            mutacion.accept(dto);

            int resultado = usuarioService.updatePerfil(NOMBRE_USUARIO, dto);

            assertEquals(codigoEsperado, resultado);
            verify(usuarioRepo, never()).save(any(Usuario.class));
            assertEquals("Juan", usuario.getPrimerNombre(), "El perfil no debe cambiar");
        }
    }

    // CU-005 - Desactivar cuenta

    @Nested
    @DisplayName("CU-005 - Desactivar cuenta")
    class DesactivarCuenta {

        @Test
        @DisplayName("CP-014 - Desactivación de cuenta exitosa")
        void desactivarCuenta_confirmada_dejaLaCuentaInactivaYAudita() {
            Usuario usuario = usuarioActivo();
            when(usuarioRepo.findByNombreUsuario(NOMBRE_USUARIO)).thenReturn(Optional.of(usuario));
            UsuarioDTO datos = new UsuarioDTO();
            datos.setConfirmar(true);

            int resultado = usuarioService.desactivarCuenta(NOMBRE_USUARIO, datos);

            assertEquals(0, resultado);
            assertFalse(usuario.getEstaActivo());
            verify(usuarioRepo).save(usuario);
            verify(auditoriaService).registrarExito(eq(NOMBRE_USUARIO), eq(Accion.DESACTIVAR_CUENTA),
                    eq(TipoEntidad.USUARIO), eq(1L), anyString());
        }

        @Test
        @DisplayName("CP-015 - Desactivación de cuenta sin confirmar")
        void desactivarCuenta_sinConfirmar_retorna3YLaCuentaSigueActiva() {
            Usuario usuario = usuarioActivo();
            when(usuarioRepo.findByNombreUsuario(NOMBRE_USUARIO)).thenReturn(Optional.of(usuario));
            UsuarioDTO datos = new UsuarioDTO();
            datos.setConfirmar(false);

            int resultado = usuarioService.desactivarCuenta(NOMBRE_USUARIO, datos);

            assertEquals(3, resultado);
            assertTrue(usuario.getEstaActivo());
            verify(usuarioRepo, never()).save(any(Usuario.class));
            verifyNoInteractions(auditoriaService);
        }

    }

    // CU-006 - Activar cuenta

    @Nested
    @DisplayName("CU-006 - Activar cuenta")
    class ActivarCuenta {

        private UsuarioDTO credencialesConfirmadas(boolean confirmar) {
            UsuarioDTO datos = new UsuarioDTO();
            datos.setNombreUsuario(NOMBRE_USUARIO);
            datos.setClaveUsuario(CLAVE_VALIDA);
            datos.setConfirmar(confirmar);
            return datos;
        }

        @Test
        @DisplayName("CP-016 - Reactivación de cuenta exitosa")
        void reactivarCuenta_credencialesCorrectasYConfirmada_activaLaCuentaYAudita() {
            Usuario usuario = usuarioInactivo();
            when(usuarioRepo.findByNombreUsuario(NOMBRE_USUARIO)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches(CLAVE_VALIDA, CLAVE_CIFRADA)).thenReturn(true);

            int resultado = usuarioService.reactivarCuenta(credencialesConfirmadas(true));

            assertEquals(0, resultado);
            assertTrue(usuario.getEstaActivo());
            verify(usuarioRepo).save(usuario);
            verify(auditoriaService).registrarExito(eq(NOMBRE_USUARIO), eq(Accion.ACTIVAR_CUENTA),
                    eq(TipoEntidad.USUARIO), eq(1L), anyString());
        }

        @Test
        @DisplayName("CP-017 - Reactivación de cuenta sin confirmar")
        void reactivarCuenta_sinConfirmar_retorna4YLaCuentaSigueInactiva() {
            Usuario usuario = usuarioInactivo();
            when(usuarioRepo.findByNombreUsuario(NOMBRE_USUARIO)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches(CLAVE_VALIDA, CLAVE_CIFRADA)).thenReturn(true);

            int resultado = usuarioService.reactivarCuenta(credencialesConfirmadas(false));

            assertEquals(4, resultado);
            assertFalse(usuario.getEstaActivo());
            verify(usuarioRepo, never()).save(any(Usuario.class));
            verifyNoInteractions(auditoriaService);
        }
    }
}