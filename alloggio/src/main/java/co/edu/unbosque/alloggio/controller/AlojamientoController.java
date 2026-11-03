package co.edu.unbosque.alloggio.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.alloggio.dto.AlojamientoDetalleDTO;
import co.edu.unbosque.alloggio.entity.Publicacion.TipoAlojamiento;
import co.edu.unbosque.alloggio.service.AlojamientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador público para buscar alojamientos y consultar su detalle. Todas
 * las
 * rutas son de solo lectura y no requieren autenticación.
 */
@RestController
@RequestMapping("/alojamiento")
@CrossOrigin
@Tag(name = "Alojamiento", description = "Búsqueda y consulta pública de alojamientos")
public class AlojamientoController {

    @Autowired
    private AlojamientoService alojamientoService;

    @Operation(summary = "Busca alojamientos activos. Todos los filtros son opcionales y se combinan; "
            + "la respuesta es paginada (página desde 0) y ordenada por precio por noche")
    @GetMapping("/buscar")
    public ResponseEntity<?> buscar(@RequestParam(required = false) String ciudad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaLlegada,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida,
            @RequestParam(required = false) Double precioMin, @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) Integer capacidad, @RequestParam(required = false) TipoAlojamiento tipo,
            @RequestParam(required = false) List<String> servicios, @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        int estado = alojamientoService.validarCriterios(fechaLlegada, fechaSalida, precioMin, precioMax,
                capacidad);
        if (estado != 0) {
            String mensaje = switch (estado) {
                case 1 -> "Debe indicar la fecha de llegada y la de salida juntas";
                case 2 -> "La fecha de salida debe ser posterior a la de llegada";
                case 3 -> "La fecha de llegada no puede ser anterior a hoy";
                case 4 -> "El precio mínimo no puede ser mayor que el máximo";
                case 5 -> "Los precios no pueden ser negativos y la capacidad debe ser al menos 1";
                default -> "Criterios de búsqueda inválidos";
            };
            return new ResponseEntity<>(mensaje, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(alojamientoService.buscar(ciudad, fechaLlegada, fechaSalida, precioMin,
                precioMax, capacidad, tipo, servicios, pagina, tamano), HttpStatus.OK);
    }

    @Operation(summary = "Consulta el detalle público de un alojamiento activo")
    @GetMapping("/detalle/{idPublicacion}")
    public ResponseEntity<?> obtenerDetalle(@PathVariable Long idPublicacion) {
        AlojamientoDetalleDTO detalle = alojamientoService.obtenerDetalle(idPublicacion);
        if (detalle == null) {
            return new ResponseEntity<>("El alojamiento no existe o no está disponible", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(detalle, HttpStatus.OK);
    }
}