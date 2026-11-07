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

import co.edu.unbosque.alloggio.dto.ReservaDTO;
import co.edu.unbosque.alloggio.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador para crear reservas.
 */
@RestController
@RequestMapping("/reserva")
@CrossOrigin(origins = "*")
@Tag(name = "Reserva", description = "Creación de reservas con pago incluido")
@SecurityRequirement(name = "bearerAuth")
public class ReservaController {

        @Autowired
        private ReservaService reservaService;

        /**
         * Crea una reserva a partir de una cotización vigente, cobrándola por la
         * pasarela de pruebas.
         * 
         * @param authentication
         * @param dto
         * @return
         */
        @Operation(summary = "Crear una reserva (incluye el pago)")
        @PostMapping("/crear")
        public ResponseEntity<Object> crear(Authentication authentication, @RequestBody ReservaDTO dto) {
                int estado = reservaService.crear(authentication.getName(), dto);
                return switch (estado) {
                        case 0 -> ResponseEntity.status(HttpStatus.CREATED)
                                        .body(reservaService.obtenerPorId(dto.getIdReserva()));
                        case 12 -> ResponseEntity.ok(reservaService.obtenerPorId(dto.getIdReserva()));
                        case 1 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("El huésped no existe");
                        case 2 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("La cotización no existe");
                        case 3 -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body("La cotización no pertenece al usuario");
                        case 4 ->
                                ResponseEntity.status(HttpStatus.GONE)
                                                .body("La cotización ya no está vigente; solicita una nueva");
                        case 5 -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body("La clave de idempotencia es obligatoria (máximo 100 caracteres)");
                        case 6 -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body("El método de pago es inválido; debe ser un identificador de la pasarela (pm_...)");
                        case 7 -> ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body("El alojamiento ya no está disponible para reservar");
                        case 8 -> ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body("El alojamiento ya no está disponible en las fechas indicadas");
                        case 9 -> ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                                        .body("La pasarela de pagos no está disponible; intenta de nuevo");
                        case 10 -> ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                                        .body("El pago fue rechazado: " + dto.getMotivoRechazo());
                        case 11 -> ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body("La cotización ya tiene un pago aprobado sin reserva; contacta a soporte");
                        case 14 -> ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body("Esa clave de idempotencia ya se usó con otra cotización");
                        default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error inesperado al crear la reserva");
                };
        }

        /**
         * Consultar historial de reservas.
         * 
         * @param authentication
         * @return
         */
        @Operation(summary = "Consultar mi historial de reservas")
        @GetMapping("/historial")
        public ResponseEntity<List<ReservaDTO>> historial(Authentication authentication) {
                return ResponseEntity.ok(reservaService.obtenerHistorial(authentication.getName()));
        }

        @Operation(summary = "Consultar el estado de una reserva")
        @GetMapping("/estado/{idReserva}")
        public ResponseEntity<Object> estado(Authentication authentication, @PathVariable Long idReserva) {
                int estado = reservaService.consultarEstado(authentication.getName(), idReserva);
                return switch (estado) {
                        case 0 -> ResponseEntity.ok(reservaService.obtenerPorId(idReserva));
                        case 1 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("La reserva no existe");
                        case 2 ->
                                ResponseEntity.status(HttpStatus.FORBIDDEN).body("La reserva no pertenece al usuario");
                        default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error inesperado al consultar la reserva");
                };
        }
}