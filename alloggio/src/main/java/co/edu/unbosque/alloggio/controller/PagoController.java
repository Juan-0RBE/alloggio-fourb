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

import co.edu.unbosque.alloggio.dto.PagoDTO;
import co.edu.unbosque.alloggio.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador TEMPORAL para probar la pieza de pagos desde Swagger. El pago no
 * es un endpoint independiente (CU-019 incluye a CU-018): este controlador se
 * elimina en el paso 10, cuando POST /reserva/crear use PagoService.
 */
@RestController
@RequestMapping("/pago")
@CrossOrigin(origins = "*")
@Tag(name = "Pago (temporal)", description = "Prueba de la pasarela; se elimina en el paso 10")
@SecurityRequirement(name = "bearerAuth")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    /**
     * Cobra una cotización con un método de pago de prueba de Stripe.
     * 
     * @param authentication usuario autenticado (huésped)
     * @param idCotizacion
     * @param dto            metodoPago (por ejemplo, pm_card_visa) y
     *                       claveIdempotencia
     * @return 201 con el pago aprobado, 402 con el pago rechazado y su motivo, o el
     *         error correspondiente
     */
    @Operation(summary = "Cobrar una cotización (solo pruebas)")
    @PostMapping("/prueba/{idCotizacion}")
    public ResponseEntity<Object> cobrarPrueba(Authentication authentication, @PathVariable Long idCotizacion,
            @RequestBody PagoDTO dto) {
        int estado = pagoService.cobrar(authentication.getName(), idCotizacion, dto);
        return switch (estado) {
            case 0 -> ResponseEntity.status(HttpStatus.CREATED).body(pagoService.obtenerPorId(dto.getIdPago()));
            case 1 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("La cotización no existe");
            case 2 -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("La cotización no pertenece al usuario");
            case 3 ->
                ResponseEntity.status(HttpStatus.GONE).body("La cotización ya no está vigente; solicita una nueva");
            case 4 -> ResponseEntity.status(HttpStatus.CONFLICT).body("La cotización ya tiene un pago aprobado");
            case 5 -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El método de pago es inválido; debe ser un identificador de la pasarela (pm_...)");
            case 6 -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La clave de idempotencia es obligatoria (máximo 255 caracteres)");
            case 7 -> ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("La pasarela de pagos no está disponible; intenta de nuevo");
            case 8 ->
                ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(pagoService.obtenerPorId(dto.getIdPago()));
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inesperado al cobrar");
        };
    }
}