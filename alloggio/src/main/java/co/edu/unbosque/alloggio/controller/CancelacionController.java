package co.edu.unbosque.alloggio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.alloggio.dto.CancelacionDTO;
import co.edu.unbosque.alloggio.service.CancelacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador para cancelar reservas.
 */
@RestController
@RequestMapping("/reserva")
@CrossOrigin(origins = "*")
@Tag(name = "Cancelación", description = "Cancelación de reservas con devolución según la política")
@SecurityRequirement(name = "bearerAuth")
public class CancelacionController {

        @Autowired
        private CancelacionService cancelacionService;

        /**
         * Cancela una reserva confirmada aplicando la política de cancelación.
         * 
         * @param authentication
         * @param idReserva
         * @param dto
         * @return
         */
        @Operation(summary = "Cancelar una reserva")
        @PostMapping("/cancelar/{idReserva}")
        public ResponseEntity<Object> cancelar(Authentication authentication, @PathVariable Long idReserva,
                        @RequestBody CancelacionDTO dto) {
                int estado = cancelacionService.cancelar(authentication.getName(), idReserva, dto);
                return switch (estado) {
                        case 0 -> ResponseEntity.ok(dto);
                        case 1 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("El usuario no existe");
                        case 2 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("La reserva no existe");
                        case 3 ->
                                ResponseEntity.status(HttpStatus.FORBIDDEN).body("La reserva no pertenece al usuario");
                        case 4 -> ResponseEntity.status(HttpStatus.CONFLICT).body("La reserva no está confirmada");
                        case 5 -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body("El motivo es obligatorio (máximo 500 caracteres)");
                        case 6 -> ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body("La estadía ya comenzó; la reserva ya no se puede cancelar");
                        case 7 -> ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                                        .body("La pasarela no pudo procesar la devolución; la reserva sigue confirmada");
                        case 9 -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("No se encontró el pago aprobado de la reserva");
                        default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error inesperado al cancelar la reserva");
                };
        }
}