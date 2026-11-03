package co.edu.unbosque.alloggio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.alloggio.dto.NotificacionDTO;
import co.edu.unbosque.alloggio.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/notificacion")
@CrossOrigin(origins = "*")
@Tag(name = "Gestión de Notificaciones", description = "Endpoints para consultar las notificaciones del usuario")
@SecurityRequirement(name = "bearerAuth")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionServ;

    @Operation(summary = "Obtener la bandeja de notificaciones del usuario autenticado, de la más reciente a la más antigua")
    @GetMapping("/mis-notificaciones")
    public ResponseEntity<List<NotificacionDTO>> obtenerMisNotificaciones(Authentication authentication) {
        List<NotificacionDTO> lista = notificacionServ.obtenerMisNotificaciones(authentication.getName());
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    @Operation(summary = "Marcar notificación propia como leída")
    @PutMapping("/leer/{idNotificacion}")
    public ResponseEntity<String> marcarComoLeida(Authentication authentication, @PathVariable Long idNotificacion) {
        int status = notificacionServ.marcarLeida(authentication.getName(), idNotificacion);
        if (status == 0) {
            return new ResponseEntity<>("Notificación marcada como leída", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Notificación no encontrada", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("No autorizado para modificar esta notificación", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}