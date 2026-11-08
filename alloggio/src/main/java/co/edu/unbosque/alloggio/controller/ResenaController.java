package co.edu.unbosque.alloggio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.alloggio.dto.ResenaDTO;
import co.edu.unbosque.alloggio.service.ResenaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador de reseñas y calificaciones.
 */
@RestController
@RequestMapping("/resena")
@CrossOrigin(origins = "*")
@Tag(name = "Reseña", description = "Calificar y reseñar alojamientos y moderar reseñas")
@SecurityRequirement(name = "bearerAuth")
public class ResenaController {

    @Autowired
    private ResenaService resenaService;

    /**
     * Crea la reseña de una reserva completada del usuario autenticado.
     * 
     * @param authentication
     * @param dto
     * @return
     */
    @Operation(summary = "Calificar y reseñar un alojamiento tras una estadía completada")
    @PostMapping("/crear")
    public ResponseEntity<Object> crear(Authentication authentication, @RequestBody ResenaDTO dto) {
        int estado = resenaService.crear(authentication.getName(), dto);
        return switch (estado) {
            case 0 -> ResponseEntity.status(HttpStatus.CREATED).body(resenaService.obtenerPorId(dto.getIdResena()));
            case 1 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("La reserva no existe");
            case 2 -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("La reserva no pertenece al usuario");
            case 3 -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Solo se puede reseñar una reserva completada");
            case 4 -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La calificación debe ser un número entero entre 1 y 5");
            case 5 -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El comentario es obligatorio y tiene un máximo de 1000 caracteres");
            case 6 -> ResponseEntity.status(HttpStatus.CONFLICT).body("La reserva ya tiene una reseña");
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al crear la reseña");
        };
    }

    /**
     * Lista todas las reseñas de un alojamiento, visibles y ocultas
     * (solo administrador).
     * 
     * @param idPublicacion
     * @return
     */
    @Operation(summary = "Listar las reseñas de un alojamiento, incluidas las ocultas (administrador)")
    @GetMapping("/alojamiento/{idPublicacion}")
    public ResponseEntity<List<ResenaDTO>> listarPorAlojamiento(@PathVariable Long idPublicacion) {
        return ResponseEntity.ok(resenaService.obtenerPorAlojamiento(idPublicacion));
    }

    /**
     * Oculta una reseña que incumple las normas (solo administrador).
     * 
     * @param authentication
     * @param idResena
     * @param dto
     * @return
     */
    @Operation(summary = "Ocultar una reseña que incumple las normas (administrador)")
    @PostMapping("/ocultar/{idResena}")
    public ResponseEntity<String> ocultar(Authentication authentication, @PathVariable Long idResena,
            @RequestBody ResenaDTO dto) {
        int estado = resenaService.ocultar(authentication.getName(), idResena, dto.getConfirmar());
        return switch (estado) {
            case 0 -> ResponseEntity.ok("Reseña ocultada exitosamente");
            case 1 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("La reseña no existe");
            case 2 -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Debe confirmar la acción para ocultar la reseña; no se realizó ningún cambio");
            case 3 -> ResponseEntity.status(HttpStatus.CONFLICT).body("La reseña ya está oculta");
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al ocultar la reseña");
        };
    }
}