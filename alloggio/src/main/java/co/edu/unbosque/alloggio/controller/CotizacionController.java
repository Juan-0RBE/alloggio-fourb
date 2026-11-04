package co.edu.unbosque.alloggio.controller;

import java.util.List;

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

import co.edu.unbosque.alloggio.dto.CotizacionDTO;
import co.edu.unbosque.alloggio.service.CotizacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.DeleteMapping;

/**
 * Controlador para cotizar alojamientos y agregar servicios
 * adicionales
 * a la cotización. Accesible para usuarios autenticados.
 */
@RestController
@RequestMapping("/cotizacion")
@CrossOrigin(origins = "*")
@Tag(name = "Cotización", description = "Cotización de alojamientos y servicios adicionales")
@SecurityRequirement(name = "bearerAuth")

public class CotizacionController {

    @Autowired
    private CotizacionService cotizacionService;

    /**
     * Cotiza un alojamiento para un rango de fechas.
     * 
     * @param authentication usuario autenticado (huésped)
     * @param dto            idPublicacion, fechaLlegada y fechaSalida
     * @return 201 con la cotización calculada, o el error correspondiente
     */
    @Operation(summary = "Cotizar un alojamiento por fechas")
    @PostMapping("/crear")
    public ResponseEntity<Object> cotizar(Authentication authentication, @RequestBody CotizacionDTO dto) {
        int estado = cotizacionService.cotizar(authentication.getName(), dto);
        return switch (estado) {
            case 0 ->
                ResponseEntity.status(HttpStatus.CREATED).body(cotizacionService.obtenerPorId(dto.getIdCotizacion()));
            case 1 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("El huésped no existe");
            case 2 -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El alojamiento no existe o no está disponible para cotizar");
            case 3 -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Las fechas son obligatorias y la salida debe ser posterior a la llegada");
            case 4 ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La fecha de llegada no puede ser anterior a hoy");
            case 5 -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El alojamiento no está disponible en las fechas indicadas");
            case 6 -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Las tarifas de la plataforma no están configuradas");
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inesperado al cotizar");
        };
    }

    /**
     * Agrega servicios adicionales a una cotización y recalcula el total.
     * 
     * @param authentication usuario autenticado (huésped)
     * @param idCotizacion
     * @param idsServicios   ids de los servicios adicionales a agregar
     * @return 200 con la cotización actualizada, o el error correspondiente
     */
    @Operation(summary = "Agregar servicios adicionales a una cotización")
    @PostMapping("/servicios/{idCotizacion}")
    public ResponseEntity<Object> agregarServicios(Authentication authentication, @PathVariable Long idCotizacion,
            @RequestBody List<Long> idsServicios) {
        int estado = cotizacionService.agregarServicios(authentication.getName(), idCotizacion, idsServicios);
        return switch (estado) {
            case 0 -> ResponseEntity.ok(cotizacionService.obtenerPorId(idCotizacion));
            case 1 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("La cotización no existe");
            case 2 -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("La cotización no pertenece al usuario");
            case 3 ->
                ResponseEntity.status(HttpStatus.GONE).body("La cotización ya no está vigente; solicita una nueva");
            case 4 -> ResponseEntity.status(HttpStatus.CONFLICT).body("El alojamiento no ofrece servicios adicionales");
            case 5 -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Algún servicio no existe, no es adicional o no pertenece al alojamiento");
            default ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inesperado al agregar servicios");
        };
    }

    /**
     * Quita un servicio adicional de una cotización y recalcula el total.
     * 
     * @param authentication usuario autenticado
     * @param idCotizacion
     * @param idServicio     id del servicio adicional a quitar
     * @return 200 con la cotización actualizada, o el error correspondiente
     */
    @Operation(summary = "Quitar un servicio adicional de una cotización")
    @DeleteMapping("/servicios/{idCotizacion}/{idServicio}")
    public ResponseEntity<Object> quitarServicio(Authentication authentication, @PathVariable Long idCotizacion,
            @PathVariable Long idServicio) {
        int estado = cotizacionService.quitarServicio(authentication.getName(), idCotizacion, idServicio);
        return switch (estado) {
            case 0 -> ResponseEntity.ok(cotizacionService.obtenerPorId(idCotizacion));
            case 1 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("La cotización no existe");
            case 2 -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("La cotización no pertenece al usuario");
            case 3 ->
                ResponseEntity.status(HttpStatus.GONE).body("La cotización ya no está vigente; solicita una nueva");
            case 4 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("El servicio no está en la cotización");
            default ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inesperado al quitar el servicio");
        };
    }
}