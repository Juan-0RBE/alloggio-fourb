package co.edu.unbosque.alloggio.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.alloggio.dto.PeriodoDisponibilidadDTO;
import co.edu.unbosque.alloggio.service.PeriodoDisponibilidadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/disponibilidad")
@CrossOrigin(origins = "*")
@Tag(name = "Gestión de Disponibilidad", description = "Endpoints para administrar el calendario de las publicaciones")
@SecurityRequirement(name = "bearerAuth")
public class PeriodoDisponibilidadController {

    @Autowired
    private PeriodoDisponibilidadService disponibilidadServ;

    public PeriodoDisponibilidadController() {
    }

    /**
     * Bloquea un rango de fechas de una publicación del anfitrión autenticado.
     * 
     * @param authentication
     * @param dto
     * @return
     */
    @Operation(summary = "Bloquear un rango de fechas de una publicación (fechaFin es el día de salida, no incluido)")
    @PostMapping("/bloquear")
    public ResponseEntity<String> bloquearRango(Authentication authentication,
            @RequestBody PeriodoDisponibilidadDTO dto) {
        int status = disponibilidadServ.bloquearRango(authentication.getName(), dto);

        if (status == 0) {
            return new ResponseEntity<>("Rango bloqueado exitosamente", HttpStatus.CREATED);
        } else if (status == 1) {
            return new ResponseEntity<>("Publicación no encontrada", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La publicación no pertenece al usuario autenticado", HttpStatus.FORBIDDEN);
        } else if (status == 3) {
            return new ResponseEntity<>(
                    "Las fechas son obligatorias y la fecha de fin debe ser posterior a la de inicio",
                    HttpStatus.BAD_REQUEST);
        } else if (status == 4) {
            return new ResponseEntity<>("No se puede bloquear un rango que inicia en el pasado",
                    HttpStatus.BAD_REQUEST);
        } else if (status == 5) {
            return new ResponseEntity<>("El rango se cruza con una reserva confirmada", HttpStatus.CONFLICT);
        } else if (status == 6) {
            return new ResponseEntity<>("El rango se cruza con otro bloqueo existente", HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Elimina un bloqueo del calendario.
     * 
     * @param authentication
     * @param idPeriodo
     * @return
     */
    @Operation(summary = "Desbloquear un rango (elimina un bloqueo del anfitrión)")
    @DeleteMapping("/desbloquear/{idPeriodo}")
    public ResponseEntity<String> desbloquearRango(Authentication authentication, @PathVariable Long idPeriodo) {
        int status = disponibilidadServ.desbloquearRango(authentication.getName(), idPeriodo);

        if (status == 0) {
            return new ResponseEntity<>("Rango desbloqueado exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Periodo no encontrado", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La publicación no pertenece al usuario autenticado", HttpStatus.FORBIDDEN);
        } else if (status == 3) {
            return new ResponseEntity<>("Solo se pueden desbloquear los bloqueos del anfitrión, no las reservas",
                    HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Consulta el calendario de una publicación propia, incluyendo los periodos
     * bloqueados y reservados.
     * 
     * @param authentication
     * @param idPublicacion
     * @return
     */
    @Operation(summary = "Consultar el calendario, con los periodos bloqueados y reservados de una publicación propia")
    @GetMapping("/calendario/{idPublicacion}")
    public ResponseEntity<List<PeriodoDisponibilidadDTO>> consultarCalendario(Authentication authentication,
            @PathVariable Long idPublicacion) {
        int status = disponibilidadServ.validarPropietario(authentication.getName(), idPublicacion);

        if (status == 1) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(disponibilidadServ.consultarCalendario(idPublicacion), HttpStatus.OK);
    }

    /**
     * Verifica si una publicación se puede reservar en un rango de fechas.
     * 
     * @param idPublicacion
     * @param fechaInicio
     * @param fechaFin
     * @return
     */
    @Operation(summary = "Verificar si una publicación está disponible en un rango de fechas")
    @GetMapping("/verificar")
    public ResponseEntity<Boolean> estaDisponible(@RequestParam Long idPublicacion,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        boolean disponible = disponibilidadServ.estaDisponible(idPublicacion, fechaInicio, fechaFin);
        return new ResponseEntity<>(disponible, HttpStatus.OK);
    }
}
