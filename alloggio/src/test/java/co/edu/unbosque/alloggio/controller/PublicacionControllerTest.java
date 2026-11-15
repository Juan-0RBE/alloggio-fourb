package co.edu.unbosque.alloggio.controller;

import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.ID_PUBLICACION;
import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.dtoPublicacionValido;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.NOMBRE_USUARIO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import co.edu.unbosque.alloggio.dto.PublicacionDTO;
import co.edu.unbosque.alloggio.service.PublicacionService;

/**
 * Pruebas unitarias de PublicacionController.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PublicacionController - mensajes de validación")
class PublicacionControllerTest {

    @Mock
    private PublicacionService publicacionServ;

    @InjectMocks
    private PublicacionController publicacionController;

    private Authentication autenticacion;

    @BeforeEach
    void autenticarAlAnfitrion() {
        autenticacion = mock(Authentication.class);
        when(autenticacion.getName()).thenReturn(NOMBRE_USUARIO);
    }

    @Test
    @DisplayName("CP-019 - Creación con campos faltantes: responde 400 y señala los campos")
    void crearPublicacion_camposFaltantes_retorna400ConLosCampos() {
        PublicacionDTO dto = new PublicacionDTO();
        when(publicacionServ.crearPublicacion(NOMBRE_USUARIO, dto)).thenReturn(3);
        when(publicacionServ.validarDatos(dto)).thenReturn(List.of("título", "ciudad"));

        ResponseEntity<String> respuesta = publicacionController.crearPublicacion(autenticacion, dto);

        assertEquals(400, respuesta.getStatusCode().value());
        assertTrue(respuesta.getBody().contains("título"));
        assertTrue(respuesta.getBody().contains("ciudad"));
    }

    @Test
    @DisplayName("CP-020 - Creación con datos incorrectos: responde 400 e indica el valor a corregir")
    void crearPublicacion_datosIncorrectos_retorna400ConElValorACorregir() {
        PublicacionDTO dto = dtoPublicacionValido();
        dto.setCapacidad(0);
        when(publicacionServ.crearPublicacion(NOMBRE_USUARIO, dto)).thenReturn(3);
        when(publicacionServ.validarDatos(dto)).thenReturn(List.of("capacidad (mayor que cero)"));

        ResponseEntity<String> respuesta = publicacionController.crearPublicacion(autenticacion, dto);

        assertEquals(400, respuesta.getStatusCode().value());
        assertTrue(respuesta.getBody().contains("capacidad (mayor que cero)"));
    }

    @Test
    @DisplayName("CP-023 - Edición con valor inválido: responde 400 e indica el valor a corregir")
    void actualizarPublicacion_valorInvalido_retorna400ConElValorACorregir() {
        PublicacionDTO dto = dtoPublicacionValido();
        dto.setPrecioNoche(-1.0);
        when(publicacionServ.actualizarPublicacion(NOMBRE_USUARIO, ID_PUBLICACION, dto)).thenReturn(4);
        when(publicacionServ.validarDatos(dto)).thenReturn(List.of("precio por noche (positivo)"));

        ResponseEntity<String> respuesta = publicacionController.actualizarPublicacion(autenticacion,
                ID_PUBLICACION, dto);

        assertEquals(400, respuesta.getStatusCode().value());
        assertTrue(respuesta.getBody().contains("precio por noche (positivo)"));
    }

    @Test
    @DisplayName("CP-025 - Activación con información incompleta: responde 400 e indica los datos faltantes")
    void activarPublicacion_informacionIncompleta_retorna400ConLosDatosFaltantes() {
        when(publicacionServ.activarPublicacion(NOMBRE_USUARIO, ID_PUBLICACION)).thenReturn(4);
        when(publicacionServ.obtenerDatosFaltantes(ID_PUBLICACION))
                .thenReturn(List.of("al menos una imagen", "descripción"));

        ResponseEntity<String> respuesta = publicacionController.activarPublicacion(autenticacion,
                ID_PUBLICACION);

        assertEquals(400, respuesta.getStatusCode().value());
        assertTrue(respuesta.getBody().contains("al menos una imagen"));
        assertTrue(respuesta.getBody().contains("descripción"));
    }
}