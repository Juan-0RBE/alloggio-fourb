package co.edu.unbosque.alloggio.controller;

import static co.edu.unbosque.alloggio.testutil.ResenaFixtures.ID_RESENA;
import static co.edu.unbosque.alloggio.testutil.ResenaFixtures.ID_RESERVA;
import static co.edu.unbosque.alloggio.testutil.ResenaFixtures.NOMBRE_ADMIN;
import static co.edu.unbosque.alloggio.testutil.ResenaFixtures.solicitudResena;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.NOMBRE_USUARIO;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import co.edu.unbosque.alloggio.dto.ResenaDTO;
import co.edu.unbosque.alloggio.service.ResenaService;

/**
 * Pruebas unitarias de ResenaController. Solo se prueban las respuestas de
 * rechazo que el documento pide informar.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResenaController - rechazos al reseñar y moderar")
class ResenaControllerTest {

    @Mock
    private ResenaService resenaService;

    @InjectMocks
    private ResenaController resenaController;

    private Authentication autenticar(String nombre) {
        Authentication autenticacion = mock(Authentication.class);
        when(autenticacion.getName()).thenReturn(nombre);
        return autenticacion;
    }

    @Test
    @DisplayName("CP-062 - Calificación sin reserva completada asociada: responde 409 y no registra la reseña")
    void crear_reservaNoCompletada_retorna409() {
        ResenaDTO dto = solicitudResena(ID_RESERVA, 5, "Excelente");
        when(resenaService.crear(NOMBRE_USUARIO, dto)).thenReturn(3);

        ResponseEntity<Object> respuesta = resenaController.crear(autenticar(NOMBRE_USUARIO), dto);

        assertAll(
                () -> assertEquals(409, respuesta.getStatusCode().value()),
                () -> assertTrue(String.valueOf(respuesta.getBody()).contains("reserva completada")));
    }

    @Test
    @DisplayName("CP-064 - Moderación de reseña sin confirmar: responde 400 e indica que no se realizó ningún cambio")
    void ocultar_sinConfirmar_retorna400() {
        ResenaDTO dto = new ResenaDTO();
        dto.setConfirmar(false);
        when(resenaService.ocultar(NOMBRE_ADMIN, ID_RESENA, false)).thenReturn(2);

        ResponseEntity<String> respuesta = resenaController.ocultar(autenticar(NOMBRE_ADMIN), ID_RESENA, dto);

        assertAll(
                () -> assertEquals(400, respuesta.getStatusCode().value()),
                () -> assertTrue(respuesta.getBody().contains("no se realizó ningún cambio")));
    }

    @Test
    @DisplayName("CP-065 - Moderación de reseña ya ocultada: responde 409 e informa que no se puede repetir")
    void ocultar_resenaYaOculta_retorna409() {
        ResenaDTO dto = new ResenaDTO();
        dto.setConfirmar(true);
        when(resenaService.ocultar(NOMBRE_ADMIN, ID_RESENA, true)).thenReturn(3);

        ResponseEntity<String> respuesta = resenaController.ocultar(autenticar(NOMBRE_ADMIN), ID_RESENA, dto);

        assertAll(
                () -> assertEquals(409, respuesta.getStatusCode().value()),
                () -> assertTrue(respuesta.getBody().contains("ya está oculta")));
    }
}