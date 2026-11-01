package co.edu.unbosque.alloggio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.alloggio.dto.PoliticaCancelacionDTO;
import co.edu.unbosque.alloggio.service.PoliticaCancelacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para la gestión del catálogo de políticas de cancelación.
 */
@RestController
@RequestMapping("/politica-cancelacion")
@CrossOrigin(origins = "*")
@Tag(name = "Gestión de Políticas de Cancelación", description = "Endpoints para administrar las políticas de cancelación disponibles en el sistema")
@SecurityRequirement(name = "bearerAuth")
public class PoliticaCancelacionController {

    @Autowired
    private PoliticaCancelacionService politicaServ;

    public PoliticaCancelacionController() {
    }

    /**
     * Crea una nueva política de cancelación en el sistema.
     * 
     * @param dto Datos de la política
     * @return ResponseEntity con el resultado
     */
    @Operation(summary = "Crear una nueva política de cancelación")
    @PostMapping("/crear")
    public ResponseEntity<String> crearPolitica(@RequestBody PoliticaCancelacionDTO dto) {
        int status = politicaServ.create(dto);

        if (status == 0) {
            return new ResponseEntity<>("Política de cancelación creada exitosamente", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Error al crear la política (datos nulos o inválidos)", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Obtiene todas las políticas de cancelación registradas.
     * 
     * @return Lista de políticas
     */
    @Operation(summary = "Obtener todas las políticas de cancelación")
    @GetMapping("/obtener/todas")
    public ResponseEntity<List<PoliticaCancelacionDTO>> obtenerTodas() {
        List<PoliticaCancelacionDTO> lista = politicaServ.getAll();
        if (lista.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    /**
     * Obtiene una política de cancelación por su ID.
     * 
     * @param id ID de la política
     * @return DTO de la política encontrada
     */
    @Operation(summary = "Obtener una política de cancelación por su ID")
    @GetMapping("/obtener/{id}")
    public ResponseEntity<PoliticaCancelacionDTO> obtenerPorId(@PathVariable Long id) {
        PoliticaCancelacionDTO dto = politicaServ.getById(id);
        if (dto != null) {
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Actualiza una política de cancelación existente.
     * 
     * @param id  ID de la política a actualizar
     * @param dto Nuevos datos
     * @return ResponseEntity con el resultado
     */
    @Operation(summary = "Actualizar una política de cancelación existente")
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<String> actualizarPolitica(@PathVariable Long id, @RequestBody PoliticaCancelacionDTO dto) {
        int status = politicaServ.updateById(id, dto);

        if (status == 0) {
            return new ResponseEntity<>("Política actualizada exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Política de cancelación no encontrada", HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Elimina una política de cancelación por su ID.
     * 
     * @param id ID de la política
     * @return ResponseEntity con el resultado
     */
    @Operation(summary = "Eliminar una política de cancelación por su ID")
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminarPolitica(@PathVariable Long id) {
        int status = politicaServ.deleteById(id);

        if (status == 0) {
            return new ResponseEntity<>("Política eliminada exitosamente", HttpStatus.OK);
        } else if (status == 1) {
            return new ResponseEntity<>("Política de cancelación no encontrada", HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>("Error desconocido", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}