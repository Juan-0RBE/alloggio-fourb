package co.edu.unbosque.alloggio.controller;

import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.ID_PUBLICACION;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import co.edu.unbosque.alloggio.service.AlojamientoService;

/**
 * Pruebas unitarias de AlojamientoController.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AlojamientoController - respuestas de error de la búsqueda y el detalle")
class AlojamientoControllerTest {

    @Mock
    private AlojamientoService alojamientoService;

    @InjectMocks
    private AlojamientoController alojamientoController;

    @Test
    @DisplayName("CP-035 - Búsqueda con fecha inicial mayor que la final: responde 400, pide corregir las fechas y no busca")
    void buscar_llegadaPosteriorASalida_retorna400YNoBusca() {
        LocalDate llegada = LocalDate.now().plusDays(13);
        LocalDate salida = LocalDate.now().plusDays(10);
        when(alojamientoService.validarCriterios(llegada, salida, null, null, null)).thenReturn(2);

        ResponseEntity<?> respuesta = alojamientoController.buscar(null, llegada, salida, null, null, null, null,
                null, 0, 10);

        assertAll(
                () -> assertEquals(400, respuesta.getStatusCode().value()),
                () -> assertTrue(String.valueOf(respuesta.getBody()).contains("posterior")),
                () -> verify(alojamientoService, never()).buscar(any(), any(), any(), any(), any(), any(), any(),
                        any(), anyInt(), anyInt()));
    }

    @Test
    @DisplayName("CP-037 - Consulta de detalle de alojamiento inexistente o no disponible: responde 404 e informa")
    void obtenerDetalle_alojamientoNoDisponible_retorna404() {
        when(alojamientoService.obtenerDetalle(ID_PUBLICACION)).thenReturn(null);

        ResponseEntity<?> respuesta = alojamientoController.obtenerDetalle(ID_PUBLICACION);

        assertAll(
                () -> assertEquals(404, respuesta.getStatusCode().value()),
                () -> assertTrue(String.valueOf(respuesta.getBody()).contains("no está disponible")));
    }
}