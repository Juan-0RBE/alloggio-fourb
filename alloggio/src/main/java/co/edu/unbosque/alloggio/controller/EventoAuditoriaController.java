package co.edu.unbosque.alloggio.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.alloggio.dto.EventoAuditoriaDTO;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Accion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Resultado;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.TipoEntidad;
import co.edu.unbosque.alloggio.service.EventoAuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auditoria")
@CrossOrigin(origins = "*")
@Tag(name = "Gestión de Auditoría", description = "Endpoint para consultar el log de auditoría")
@SecurityRequirement(name = "bearerAuth")
public class EventoAuditoriaController {

    @Autowired
    private EventoAuditoriaService auditoriaServ;

    @Operation(summary = "Consultar el log de auditoría con filtros opcionales")
    @GetMapping("/consultar")
    public ResponseEntity<?> consultar(
            @RequestParam(name = "fechaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(name = "correoUsuario", required = false) String correoUsuario,
            @RequestParam(name = "accion", required = false) Accion accion,
            @RequestParam(name = "entidadTipo", required = false) TipoEntidad entidadTipo,
            @RequestParam(name = "entidadId", required = false) Long entidadId,
            @RequestParam(name = "resultado", required = false) Resultado resultado,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamano", defaultValue = "20") int tamano) {

        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            return new ResponseEntity<>("Rango de fechas inválido: la fecha inicial no puede ser posterior a la final",
                    HttpStatus.BAD_REQUEST);
        }

        List<EventoAuditoriaDTO> lista = auditoriaServ.consultar(fechaDesde, fechaHasta, correoUsuario, accion,
                entidadTipo, entidadId, resultado, pagina, tamano);
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }
}