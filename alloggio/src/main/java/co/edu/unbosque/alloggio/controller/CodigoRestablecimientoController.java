package co.edu.unbosque.alloggio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.alloggio.dto.CodigoRestablecimientoDTO;
import co.edu.unbosque.alloggio.service.CodigoRestablecimientoService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/restablecimiento")
@CrossOrigin(origins = "*")
@Tag(name = "Restablecimiento de contraseña", description = "Endpoints públicos para recuperar la contraseña")
public class CodigoRestablecimientoController {

    @Autowired
    private CodigoRestablecimientoService codigoServ;

    public CodigoRestablecimientoController() {
    }

    @Operation(summary = "Solicitar código de recuperación por correo")
    @PostMapping("/solicitar")
    public ResponseEntity<String> solicitarCodigo(@RequestBody CodigoRestablecimientoDTO data) {

        int status = codigoServ.solicitarCodigo(data.getCorreoUsuario());

        if (status == 0) {
            return new ResponseEntity<>("Código enviado al correo registrado", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("No existe una cuenta asociada al correo", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("No fue posible enviar el correo, intenta de nuevo",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Confirmar el código y definir la nueva contraseña")
    @PostMapping("/confirmar")
    public ResponseEntity<String> confirmarCambio(@RequestBody CodigoRestablecimientoDTO data) {
        int status = codigoServ.confirmarCambio(data);

        if (status == 0) {
            return new ResponseEntity<>("Contraseña actualizada exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("No existe una cuenta asociada al correo", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("El código es inválido o expiró, solicita uno nuevo", HttpStatus.BAD_REQUEST);
        } else if (status == 3) {
            return new ResponseEntity<>("La contraseña no cumple con los requisitos de complejidad",
                    HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
