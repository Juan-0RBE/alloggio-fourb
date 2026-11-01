package co.edu.unbosque.alloggio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.alloggio.dto.UsuarioDTO;
import co.edu.unbosque.alloggio.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/usuario")
@CrossOrigin(origins = "*")
@Tag(name = "Gestión de Usuarios", description = "Endpoints para administrar usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioServ;

    public UsuarioController() {
    }

    /**
     * Crea un nuevo usuario.
     * 
     * @param newUser
     * @return
     */
    @Operation(summary = "Crear un nuevo usuario")
    @PostMapping("/crear")
    public ResponseEntity<String> createUser(@RequestBody UsuarioDTO newUser) {
        int status = usuarioServ.create(newUser);

        if (status == 0) {
            return new ResponseEntity<>("Usuario creado exitosamente", HttpStatus.CREATED);
        } else if (status == 1) {
            return new ResponseEntity<>("Nombre de usuario ya está en uso", HttpStatus.CONFLICT);
        } else if (status == 2) {
            return new ResponseEntity<>("La contraseña no cumple con los requisitos de complejidad",
                    HttpStatus.BAD_REQUEST);
        } else if (status == 3) {
            return new ResponseEntity<>("Correo electrónico ya está en uso", HttpStatus.CONFLICT);
        } else if (status == 4) {
            return new ResponseEntity<>("Documento de identidad ya está en uso", HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Obtiene todos los usuarios.
     * 
     * @return
     */
    @Operation(summary = "Obtener todos los usuarios")
    @GetMapping("/obtener/todos")
    public ResponseEntity<List<UsuarioDTO>> getAllUsers() {
        List<UsuarioDTO> users = usuarioServ.getAll();
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * Obtiene un usuario por su ID.
     * 
     * @param id
     * @return
     */
    @Operation(summary = "Obtener un usuario por ID")
    @GetMapping("/obtener/{id}")
    public ResponseEntity<UsuarioDTO> getById(@PathVariable Long id) {
        UsuarioDTO found = usuarioServ.getById(id);
        if (found != null) {
            return new ResponseEntity<>(found, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Verifies if a user exists by their ID.
     * 
     * @param id
     * @return
     */
    @Operation(summary = "Verificar si un usuario existe")
    @GetMapping("/existe/{id}")
    public ResponseEntity<Boolean> existUser(@PathVariable Long id) {
        boolean exists = usuarioServ.exist(id);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    /**
     * Actualiza la información de un usuario por su ID.
     * 
     * @param id
     * @param newData
     * @return
     */
    @Operation(summary = "Actualizar la información de un usuario")
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UsuarioDTO newData) {
        int status = usuarioServ.updateById(id, newData);

        if (status == 0) {
            return new ResponseEntity<>("Usuario actualizado exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La contraseña no cumple con los requisitos de complejidad",
                    HttpStatus.BAD_REQUEST);
        } else if (status == 3) {
            return new ResponseEntity<>("Nombre de usuario ya está en uso", HttpStatus.CONFLICT);
        } else if (status == 4) {
            return new ResponseEntity<>("Correo electrónico ya está en uso", HttpStatus.CONFLICT);
        } else if (status == 5) {
            return new ResponseEntity<>("Documento de identidad ya está en uso", HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Actualiza la contraseña de un usuario por su ID.
     * 
     * @param id
     * @param newData
     * @return
     */
    @Operation(summary = "Actualizar la contraseña de un usuario")
    @PutMapping("/actualizar/contraseña/{id}")
    public ResponseEntity<String> updatePassword(@PathVariable Long id, @RequestBody UsuarioDTO newData) {
        int status = usuarioServ.updatePasswordById(id, newData);

        if (status == 0) {
            return new ResponseEntity<>("Contraseña actualizada exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La contraseña no cumple con los requisitos de complejidad",
                    HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Verifica la cuenta de un usuario.
     * 
     * @param correo
     * @return
     */
    @Operation(summary = "Verificar cuenta de usuario")
    @PutMapping("/verificar/{correo}")
    public ResponseEntity<String> verifyUser(@PathVariable String correo) {
        UsuarioDTO usuario = usuarioServ.getByCorreo(correo);
        if (usuario == null) {
            return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
        }
        if (usuario.getIsVerified()) {
            return new ResponseEntity<>("La cuenta ya está verificada", HttpStatus.OK);
        }
        usuario.setIsVerified(true);
        int status = usuarioServ.updateVerificationStatus(correo, usuario);
        if (status == 0) {
            return new ResponseEntity<>("Usuario verificado exitosamente", HttpStatus.OK);
        }
        return new ResponseEntity<>("Error al verificar usuario", HttpStatus.BAD_REQUEST);
    }

    @Operation(summary = "Eliminar un usuario por ID")
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        int status = usuarioServ.deleteById(id);

        if (status == 0) {
            return new ResponseEntity<>("Usuario eliminado exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Obtener el correo de un usuario por su nombre de usuario")
    @GetMapping("/correo/{nombreUsuario}")
    public ResponseEntity<String> getCorreoByUsername(@PathVariable String nombreUsuario) {
        UsuarioDTO found = usuarioServ.getByNombreUsuario(nombreUsuario);
        if (found != null) {
            return new ResponseEntity<>(found.getCorreoUsuario(), HttpStatus.OK);
        }
        return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
    }

    /**
     * Consulta el perfil del usuario autenticado (CU-004).
     * 
     * @param authentication usuario autenticado, tomado del token
     * @return
     */
    @Operation(summary = "Consultar el perfil del usuario autenticado")
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioDTO> getPerfil(Authentication authentication) {
        UsuarioDTO perfil = usuarioServ.getPerfil(authentication.getName());
        if (perfil != null) {
            return new ResponseEntity<>(perfil, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Actualiza los datos básicos del perfil del usuario autenticado.
     * 
     * @param authentication usuario autenticado, tomado del token
     * @param newData        primerNombre, segundoNombre, primerApellido,
     *                       segundoApellido y numeroTelefono
     * @return
     */
    @Operation(summary = "Actualizar el perfil del usuario autenticado")
    @PutMapping("/perfil")
    public ResponseEntity<String> updatePerfil(Authentication authentication, @RequestBody UsuarioDTO newData) {
        int status = usuarioServ.updatePerfil(authentication.getName(), newData);

        if (status == 0) {
            return new ResponseEntity<>("Perfil actualizado exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("El primer nombre es obligatorio y solo admite letras (máximo 50)",
                    HttpStatus.BAD_REQUEST);
        } else if (status == 3) {
            return new ResponseEntity<>("El segundo nombre solo admite letras (máximo 50)", HttpStatus.BAD_REQUEST);
        } else if (status == 4) {
            return new ResponseEntity<>("El primer apellido es obligatorio y solo admite letras (máximo 50)",
                    HttpStatus.BAD_REQUEST);
        } else if (status == 5) {
            return new ResponseEntity<>("El segundo apellido solo admite letras (máximo 50)", HttpStatus.BAD_REQUEST);
        } else if (status == 6) {
            return new ResponseEntity<>("El número de teléfono no es válido (de 7 a 15 dígitos)",
                    HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Desactiva la cuenta del usuario autenticado.
     * 
     * @param authentication usuario autenticado, tomado del token
     * @param datos          JSON con el campo confirmar
     * @return
     */
    @Operation(summary = "Desactivar la cuenta del usuario autenticado")
    @PostMapping("/desactivar")
    public ResponseEntity<String> desactivarCuenta(Authentication authentication, @RequestBody UsuarioDTO datos) {
        int status = usuarioServ.desactivarCuenta(authentication.getName(), datos);

        if (status == 0) {
            return new ResponseEntity<>("Cuenta desactivada exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La cuenta ya está inactiva", HttpStatus.CONFLICT);
        } else if (status == 3) {
            return new ResponseEntity<>("Operación cancelada: la cuenta continúa activa", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}