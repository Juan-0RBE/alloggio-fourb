package co.edu.unbosque.alloggio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.alloggio.dto.ImagenAlojamientoDTO;
import co.edu.unbosque.alloggio.dto.PublicacionDTO;
import co.edu.unbosque.alloggio.dto.ReglaAlojamientoDTO;
import co.edu.unbosque.alloggio.dto.ServicioAlojamientoDTO;
import co.edu.unbosque.alloggio.service.PublicacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para la gestión de publicaciones de alojamientos.
 */
@RestController
@RequestMapping("/publicacion")
@CrossOrigin(origins = "*")
@Tag(name = "Gestión de Publicaciones", description = "Endpoints para administrar publicaciones de alojamientos")
@SecurityRequirement(name = "bearerAuth")
public class PublicacionController {

    @Autowired
    private PublicacionService publicacionServ;

    public PublicacionController() {
    }

    /**
     * Crea una nueva publicación de alojamiento. El anfitrión es el usuario
     * autenticado.
     * 
     * @param authentication
     * @param dto
     * @return
     */
    @Operation(summary = "Crear una nueva publicación de alojamiento (el anfitrión es el usuario autenticado)")
    @PostMapping("/crear")
    public ResponseEntity<String> crearPublicacion(Authentication authentication, @RequestBody PublicacionDTO dto) {
        int status = publicacionServ.crearPublicacion(authentication.getName(), dto);

        if (status == 0) {
            return new ResponseEntity<>("Publicación creada exitosamente", HttpStatus.CREATED);
        } else if (status == 1) {
            return new ResponseEntity<>("El anfitrión no existe", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La política de cancelación no existe", HttpStatus.NOT_FOUND);
        } else if (status == 3) {
            String detalle = String.join(", ", publicacionServ.validarDatos(dto));
            return new ResponseEntity<>("Faltan campos obligatorios o son inválidos: " + detalle,
                    HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Obtiene todas las publicaciones registradas.
     * 
     * @return
     */
    @Operation(summary = "Obtener todas las publicaciones")
    @GetMapping("/obtener/todas")
    public ResponseEntity<List<PublicacionDTO>> obtenerTodas() {
        List<PublicacionDTO> publicaciones = publicacionServ.obtenerTodas();
        if (publicaciones.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(publicaciones, HttpStatus.OK);
    }

    /**
     * Obtiene las publicaciones asociadas a un anfitrión específico.
     * 
     * @param idAnfitrion
     * @return
     */
    @Operation(summary = "Obtener publicaciones por ID de anfitrión")
    @GetMapping("/anfitrion/{idAnfitrion}")
    public ResponseEntity<List<PublicacionDTO>> obtenerPorAnfitrion(@PathVariable Long idAnfitrion) {
        List<PublicacionDTO> publicaciones = publicacionServ.obtenerPorAnfitrion(idAnfitrion);
        if (publicaciones.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(publicaciones, HttpStatus.OK);
    }

    /**
     * Obtiene una publicación por su ID.
     * 
     * @param id
     * @return
     */
    @Operation(summary = "Obtener una publicación por su ID")
    @GetMapping("/obtener/{id}")
    public ResponseEntity<PublicacionDTO> obtenerPorId(@PathVariable Long id) {
        PublicacionDTO found = publicacionServ.obtenerPorId(id);
        if (found != null) {
            return new ResponseEntity<>(found, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Actualiza una publicación existente. Solo el propietario o un administrador.
     * 
     * @param authentication
     * @param id
     * @param dto
     * @return
     */
    @Operation(summary = "Actualizar una publicación existente (propietario o administrador)")
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<String> actualizarPublicacion(Authentication authentication, @PathVariable Long id,
            @RequestBody PublicacionDTO dto) {
        int status = publicacionServ.actualizarPublicacion(authentication.getName(), id, dto);

        if (status == 0) {
            return new ResponseEntity<>("Publicación actualizada exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Publicación no encontrada", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La política de cancelación no existe", HttpStatus.NOT_FOUND);
        } else if (status == 3) {
            return new ResponseEntity<>("La publicación no pertenece al usuario autenticado", HttpStatus.FORBIDDEN);
        } else if (status == 4) {
            String detalle = String.join(", ", publicacionServ.validarDatos(dto));
            return new ResponseEntity<>("Faltan campos obligatorios o son inválidos: " + detalle,
                    HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Elimina una publicación por su ID. Solo el propietario o un administrador.
     * 
     * @param authentication
     * @param id
     * @return
     */
    @Operation(summary = "Eliminar una publicación por su ID (propietario o administrador)")
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminarPublicacion(Authentication authentication, @PathVariable Long id) {
        int status = publicacionServ.eliminarPublicacion(authentication.getName(), id);

        if (status == 0) {
            return new ResponseEntity<>("Publicación eliminada exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Publicación no encontrada", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La publicación no pertenece al usuario autenticado", HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoints de servicios

    /**
     * Agrega un servicio a una publicación existente.
     * 
     * @param authentication
     * @param idPublicacion
     * @param dto
     * @return
     */
    @Operation(summary = "Agregar un servicio a una publicación existente")
    @PostMapping("/{idPublicacion}/servicios/crear")
    public ResponseEntity<String> agregarServicio(Authentication authentication, @PathVariable Long idPublicacion,
            @RequestBody ServicioAlojamientoDTO dto) {
        int status = publicacionServ.agregarServicioAPublicacion(authentication.getName(), idPublicacion, dto);
        if (status == 0) {
            return new ResponseEntity<>("Servicio agregado exitosamente", HttpStatus.CREATED);
        } else if (status == 1) {
            return new ResponseEntity<>("Publicación no encontrada", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La publicación no pertenece al usuario autenticado", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Elimina un servicio de alojamiento por su ID.
     * 
     * @param authentication
     * @param idServicio
     * @return
     */
    @Operation(summary = "Eliminar un servicio de alojamiento por su ID")
    @DeleteMapping("/servicios/eliminar/{idServicio}")
    public ResponseEntity<String> eliminarServicio(Authentication authentication, @PathVariable Long idServicio) {
        int status = publicacionServ.eliminarServicio(authentication.getName(), idServicio);
        if (status == 0) {
            return new ResponseEntity<>("Servicio eliminado exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Servicio no encontrado", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La publicación no pertenece al usuario autenticado", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Obtiene todos los servicios asociados a una publicación específica.
     * 
     * @param idPublicacion
     * @return
     */
    @Operation(summary = "Obtener los servicios asociados a una publicación")
    @GetMapping("/{idPublicacion}/servicios")
    public ResponseEntity<List<ServicioAlojamientoDTO>> obtenerServiciosPorPublicacion(
            @PathVariable Long idPublicacion) {
        List<ServicioAlojamientoDTO> servicios = publicacionServ.obtenerServiciosPorPublicacion(idPublicacion);
        if (servicios == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (servicios.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(servicios, HttpStatus.OK);
    }

    // Endpoints de reglas

    /**
     * Agrega una regla a una publicación existente.
     * 
     * @param authentication
     * @param idPublicacion
     * @param dto
     * @return
     */
    @Operation(summary = "Agregar una regla a una publicación existente")
    @PostMapping("/{idPublicacion}/reglas/crear")
    public ResponseEntity<String> agregarRegla(Authentication authentication, @PathVariable Long idPublicacion,
            @RequestBody ReglaAlojamientoDTO dto) {
        int status = publicacionServ.agregarReglaAPublicacion(authentication.getName(), idPublicacion, dto);
        if (status == 0) {
            return new ResponseEntity<>("Regla agregada exitosamente", HttpStatus.CREATED);
        } else if (status == 1) {
            return new ResponseEntity<>("Publicación no encontrada", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La publicación no pertenece al usuario autenticado", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Elimina una regla de alojamiento por su ID.
     * 
     * @param authentication
     * @param idRegla
     * @return
     */
    @Operation(summary = "Eliminar una regla de alojamiento por su ID")
    @DeleteMapping("/reglas/eliminar/{idRegla}")
    public ResponseEntity<String> eliminarRegla(Authentication authentication, @PathVariable Long idRegla) {
        int status = publicacionServ.eliminarRegla(authentication.getName(), idRegla);
        if (status == 0) {
            return new ResponseEntity<>("Regla eliminada exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Regla no encontrada", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La publicación no pertenece al usuario autenticado", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Obtiene todas las reglas asociadas a una publicación específica.
     * 
     * @param idPublicacion
     * @return
     */
    @Operation(summary = "Obtener las reglas asociadas a una publicación")
    @GetMapping("/{idPublicacion}/reglas")
    public ResponseEntity<List<ReglaAlojamientoDTO>> obtenerReglasPorPublicacion(@PathVariable Long idPublicacion) {
        List<ReglaAlojamientoDTO> reglas = publicacionServ.obtenerReglasPorPublicacion(idPublicacion);
        if (reglas == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (reglas.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(reglas, HttpStatus.OK);
    }

    // Endpoints de imagenes

    /**
     * Agrega una imagen a una publicación existente.
     * 
     * @param authentication
     * @param idPublicacion
     * @param dto
     * @return
     */
    @Operation(summary = "Agregar una imagen a una publicación existente")
    @PostMapping("/{idPublicacion}/imagenes/crear")
    public ResponseEntity<String> agregarImagen(Authentication authentication, @PathVariable Long idPublicacion,
            @RequestBody ImagenAlojamientoDTO dto) {
        int status = publicacionServ.agregarImagenAPublicacion(authentication.getName(), idPublicacion, dto);
        if (status == 0) {
            return new ResponseEntity<>("Imagen agregada exitosamente", HttpStatus.CREATED);
        } else if (status == 1) {
            return new ResponseEntity<>("Publicación no encontrada", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La publicación no pertenece al usuario autenticado", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Elimina una imagen de alojamiento por su ID.
     * 
     * @param authentication
     * @param idImagen
     * @return
     */
    @Operation(summary = "Eliminar una imagen de alojamiento por su ID")
    @DeleteMapping("/imagenes/eliminar/{idImagen}")
    public ResponseEntity<String> eliminarImagen(Authentication authentication, @PathVariable Long idImagen) {
        int status = publicacionServ.eliminarImagen(authentication.getName(), idImagen);
        if (status == 0) {
            return new ResponseEntity<>("Imagen eliminada exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Imagen no encontrada", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La publicación no pertenece al usuario autenticado", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Obtiene todas las imágenes asociadas a una publicación específica, ordenadas
     * por el campo "orden".
     * 
     * @param idPublicacion
     * @return
     */
    @Operation(summary = "Obtener las imágenes asociadas a una publicación (ordenadas)")
    @GetMapping("/{idPublicacion}/imagenes")
    public ResponseEntity<List<ImagenAlojamientoDTO>> obtenerImagenesPorPublicacion(@PathVariable Long idPublicacion) {
        List<ImagenAlojamientoDTO> imagenes = publicacionServ.obtenerImagenesPorPublicacion(idPublicacion);
        if (imagenes == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (imagenes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(imagenes, HttpStatus.OK);
    }

    // Endpoints de activacion de publicación

    /**
     * Activa una publicación, permitiendo que esté disponible en búsquedas y
     * reservas.
     * 
     * @param authentication
     * @param id
     * @return
     */
    @Operation(summary = "Activar una publicación (queda disponible en búsquedas y reservas)")
    @PutMapping("/activar/{id}")
    public ResponseEntity<String> activarPublicacion(Authentication authentication, @PathVariable Long id) {
        int status = publicacionServ.activarPublicacion(authentication.getName(), id);

        if (status == 0) {
            return new ResponseEntity<>("Publicación activada exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Publicación no encontrada", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La publicación no pertenece al usuario autenticado", HttpStatus.FORBIDDEN);
        } else if (status == 3) {
            return new ResponseEntity<>("Solo se pueden activar publicaciones en borrador o pausadas",
                    HttpStatus.CONFLICT);
        } else if (status == 4) {
            List<String> faltantes = publicacionServ.obtenerDatosFaltantes(id);
            String detalle = (faltantes == null) ? "" : String.join(", ", faltantes);
            return new ResponseEntity<>("La publicación no cumple la información mínima. Faltan: " + detalle,
                    HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Pausa una publicación activa, impidiendo que esté disponible en búsquedas y
     * reservas.
     * 
     * @param authentication
     * @param id
     * @return
     */
    @Operation(summary = "Pausar una publicación activa (deja de aparecer en búsquedas y de recibir reservas)")
    @PutMapping("/pausar/{id}")
    public ResponseEntity<String> pausarPublicacion(Authentication authentication, @PathVariable Long id) {
        int status = publicacionServ.pausarPublicacion(authentication.getName(), id);

        if (status == 0) {
            return new ResponseEntity<>("Publicación pausada exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Publicación no encontrada", HttpStatus.NOT_FOUND);
        } else if (status == 2) {
            return new ResponseEntity<>("La publicación no pertenece al usuario autenticado", HttpStatus.FORBIDDEN);
        } else if (status == 3) {
            return new ResponseEntity<>("Solo se pueden pausar publicaciones activas", HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}