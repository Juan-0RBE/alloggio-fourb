package co.edu.unbosque.alloggio.service;

import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.ID_PUBLICACION;
import static co.edu.unbosque.alloggio.testutil.PublicacionFixtures.publicacion;
import static co.edu.unbosque.alloggio.testutil.ResenaFixtures.resenaVisible;
import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.usuarioActivo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.alloggio.dto.ResenaDTO;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.Resena;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.ResenaRepository;

/**
 * Pruebas unitarias de ResenaService para la consulta de reseñas.
 * Casos de prueba que cubre: CP-038 y CP-039.
 * (CP-061 a CP-065 se prueban en el módulo "Reseñas y moderación".)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResenaService - consulta de reseñas y calificaciones")
class ResenaServiceTest {

    @Mock
    private ResenaRepository resenaRepo;

    @InjectMocks
    private ResenaService resenaService;

    @Nested
    @DisplayName("CU-015 - Consultar reseñas y calificaciones")
    class ConsultarResenas {

        @Test
        @DisplayName("CP-038 - Consulta de reseñas y calificaciones exitosa (reseñas visibles con su autor)")
        void obtenerVisiblesRecientes_conResenasVisibles_devuelveDtosConAutor() {
            // Arrange
            Usuario huesped = usuarioActivo(); // Juan Pérez
            Resena primera = resenaVisible(1L, 5, "Excelente lugar", huesped,
                    publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA));
            Resena segunda = resenaVisible(2L, 4, "Muy cómodo", huesped,
                    publicacion(usuarioActivo(), EstadoPublicacion.ACTIVA));
            when(resenaRepo.findTop10ByReservaPublicacionIdPublicacionAndVisibleTrueOrderByFechaDescIdResenaDesc(
                    ID_PUBLICACION)).thenReturn(List.of(primera, segunda));

            // Act
            List<ResenaDTO> resenas = resenaService.obtenerVisiblesRecientes(ID_PUBLICACION);

            // Assert
            assertAll(
                    () -> assertEquals(2, resenas.size()),
                    () -> assertEquals(1L, resenas.get(0).getIdResena()),
                    () -> assertEquals(5, resenas.get(0).getCalificacion()),
                    () -> assertEquals("Excelente lugar", resenas.get(0).getComentario()),
                    () -> assertEquals("Juan Pérez", resenas.get(0).getAutor()),
                    () -> assertTrue(resenas.get(0).getVisible()),
                    () -> assertEquals("Muy cómodo", resenas.get(1).getComentario()));
            verify(resenaRepo, never())
                    .findByReservaPublicacionIdPublicacionOrderByFechaDescIdResenaDesc(any());
        }

        @Test
        @DisplayName("CP-038 - Consulta de reseñas y calificaciones exitosa (promedio a un decimal)")
        void calcularPromedio_conResenasVisibles_redondeaAUnDecimal() {
            when(resenaRepo.promedioVisible(ID_PUBLICACION)).thenReturn(4.3333);

            Double promedio = resenaService.calcularPromedio(ID_PUBLICACION);

            assertEquals(4.3, promedio, 0.0001);
        }

        @Test
        @DisplayName("CP-039 - Consulta de alojamiento sin reseñas visibles")
        void consultarResenas_sinResenasVisibles_listaVaciaPromedioNuloYTotalCero() {
            // findTop10... devuelve lista vacía por defecto; el promedio se simula nulo
            // porque Mockito devolvería 0.0 para un Double sin simular
            when(resenaRepo.promedioVisible(ID_PUBLICACION)).thenReturn(null);

            List<ResenaDTO> resenas = resenaService.obtenerVisiblesRecientes(ID_PUBLICACION);
            Double promedio = resenaService.calcularPromedio(ID_PUBLICACION);
            long total = resenaService.contarVisibles(ID_PUBLICACION);

            assertAll(
                    () -> assertTrue(resenas.isEmpty()),
                    () -> assertNull(promedio),
                    () -> assertEquals(0L, total));
        }
    }
}