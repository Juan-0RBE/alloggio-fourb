package co.edu.unbosque.alloggio.controller;

import static co.edu.unbosque.alloggio.testutil.CotizacionFixtures.ID_COTIZACION;
import static co.edu.unbosque.alloggio.testutil.CotizacionFixtures.NOMBRE_HUESPED;
import static org.junit.jupiter.api.Assertions.assertAll;
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

import co.edu.unbosque.alloggio.dto.CotizacionDTO;
import co.edu.unbosque.alloggio.service.CotizacionService;

/**
 * Pruebas unitarias de CotizacionController.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CotizacionController - respuestas de error de la cotización")
class CotizacionControllerTest {

    @Mock
    private CotizacionService cotizacionService;

    @InjectMocks
    private CotizacionController cotizacionController;

    private Authentication autenticacion;

    @BeforeEach
    void autenticarAlHuesped() {
        autenticacion = mock(Authentication.class);
        when(autenticacion.getName()).thenReturn(NOMBRE_HUESPED);
    }

    @Test
    @DisplayName("CP-042 - Cotización con fecha inicial posterior a la final: responde 400 e indica corregir las fechas")
    void cotizar_fechasInvalidas_retorna400() {
        CotizacionDTO dto = new CotizacionDTO();
        when(cotizacionService.cotizar(NOMBRE_HUESPED, dto)).thenReturn(3);

        ResponseEntity<Object> respuesta = cotizacionController.cotizar(autenticacion, dto);

        assertAll(
                () -> assertEquals(400, respuesta.getStatusCode().value()),
                () -> assertTrue(String.valueOf(respuesta.getBody()).contains("posterior")));
    }

    @Test
    @DisplayName("CP-043 - Cotización con alojamiento no disponible en el periodo: responde 409 e informa la falta de disponibilidad")
    void cotizar_alojamientoNoDisponible_retorna409() {
        CotizacionDTO dto = new CotizacionDTO();
        when(cotizacionService.cotizar(NOMBRE_HUESPED, dto)).thenReturn(5);

        ResponseEntity<Object> respuesta = cotizacionController.cotizar(autenticacion, dto);

        assertAll(
                () -> assertEquals(409, respuesta.getStatusCode().value()),
                () -> assertTrue(String.valueOf(respuesta.getBody()).contains("no está disponible")));
    }

    @Test
    @DisplayName("CP-046 - Agregado de servicios en alojamiento sin servicios adicionales: responde 409 e informa que no hay servicios")
    void agregarServicios_sinServiciosAdicionales_retorna409() {
        List<Long> ids = List.of(5L);
        when(cotizacionService.agregarServicios(NOMBRE_HUESPED, ID_COTIZACION, ids)).thenReturn(4);

        ResponseEntity<Object> respuesta = cotizacionController.agregarServicios(autenticacion, ID_COTIZACION, ids);

        assertAll(
                () -> assertEquals(409, respuesta.getStatusCode().value()),
                () -> assertTrue(String.valueOf(respuesta.getBody()).contains("no ofrece servicios adicionales")));
    }
}