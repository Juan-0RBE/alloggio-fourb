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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.alloggio.dto.ModeracionPublicacionDTO;
import co.edu.unbosque.alloggio.dto.UsuarioDTO;
import co.edu.unbosque.alloggio.dto.UsuarioModeracionDTO;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.service.PublicacionService;
import co.edu.unbosque.alloggio.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador de moderación y administració.
 */
@RestController
@RequestMapping("/moderacion")
@CrossOrigin(origins = "*")
@Tag(name = "Moderación", description = "Bloqueo de publicaciones y restricción de usuarios (administrador)")
@SecurityRequirement(name = "bearerAuth")
public class ModeracionController {

    @Autowired
    private PublicacionService publicacionServ;

    @Autowired
    private UsuarioService usuarioServ;

    public ModeracionController() {
    }

    @Operation(summary = "Listar publicaciones (sin borradores), con filtro opcional por estado")
    @GetMapping("/publicacion/listar")
    public ResponseEntity<List<ModeracionPublicacionDTO>> listarPublicaciones(
            @RequestParam(required = false) EstadoPublicacion estado) {
        List<ModeracionPublicacionDTO> lista = publicacionServ.listarParaModeracion(estado);
        if (lista.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    @Operation(summary = "Detalle de una publicación con sus reservas futuras confirmadas")
    @GetMapping("/publicacion/{id}")
    public ResponseEntity<ModeracionPublicacionDTO> obtenerPublicacion(@PathVariable Long id) {
        ModeracionPublicacionDTO dto = publicacionServ.obtenerParaModeracion(id);
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(summary = "Bloquear una publicación activa o pausada (motivo obligatorio)")
    @PostMapping("/publicacion/bloquear/{id}")
    public ResponseEntity<String> bloquearPublicacion(Authentication authentication, @PathVariable Long id,
            @RequestBody ModeracionPublicacionDTO dto) {
        int status = publicacionServ.bloquearPublicacion(authentication.getName(), id, dto);

        if (status == 0) {
            return new ResponseEntity<>("Publicación bloqueada exitosamente. Reservas futuras conservadas para "
                    + "revisión manual: " + dto.getTotalReservasFuturas(), HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Publicación no encontrada", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("El motivo es obligatorio y admite máximo 500 caracteres",
                    HttpStatus.BAD_REQUEST);
        } else if (status == 3) {
            return new ResponseEntity<>("Solo se pueden bloquear publicaciones activas o pausadas",
                    HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Listar perfiles de usuario, con filtro opcional por estadoCuenta")
    @GetMapping("/usuario/listar")
    public ResponseEntity<List<UsuarioModeracionDTO>> listarUsuarios(
            @RequestParam(required = false) String estadoCuenta) {
        List<UsuarioModeracionDTO> lista = usuarioServ.listarParaModeracion(estadoCuenta);
        if (lista.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    @Operation(summary = "Perfil de un usuario")
    @GetMapping("/usuario/{id}")
    public ResponseEntity<UsuarioModeracionDTO> obtenerUsuario(@PathVariable Long id) {
        UsuarioModeracionDTO dto = usuarioServ.obtenerParaModeracion(id);
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(summary = "Restringir la cuenta de un usuario (requiere confirmar: true)")
    @PostMapping("/usuario/restringir/{id}")
    public ResponseEntity<String> restringirUsuario(Authentication authentication, @PathVariable Long id,
            @RequestBody UsuarioDTO datos) {
        int status = usuarioServ.restringirUsuario(authentication.getName(), id, datos);

        if (status == 0) {
            return new ResponseEntity<>("Cuenta restringida exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("No puedes restringir tu propia cuenta", HttpStatus.FORBIDDEN);
        } else if (status == 3) {
            return new ResponseEntity<>("No se puede restringir a un administrador", HttpStatus.FORBIDDEN);
        } else if (status == 4) {
            return new ResponseEntity<>("La cuenta ya está restringida", HttpStatus.CONFLICT);
        } else if (status == 5) {
            return new ResponseEntity<>("Operación cancelada: la cuenta no se modificó", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}