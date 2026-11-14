package co.edu.unbosque.alloggio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.alloggio.dto.ArchivoExportadoDTO;
import co.edu.unbosque.alloggio.dto.DashboardDTO;
import co.edu.unbosque.alloggio.service.DashboardService;
import co.edu.unbosque.alloggio.service.ExportacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Dashboard de indicadores y exportación (CU-029 y CU-030). Solo administrador.
 */
@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*", exposedHeaders = "Content-Disposition")
@Tag(name = "Dashboard", description = "Indicadores de hospedajes y exportación a PDF o Excel (solo administrador)")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    @Autowired
    private DashboardService dashboardServ;

    @Autowired
    private ExportacionService exportacionServ;

    /**
     * Consulta los indicadores básicos: usuarios, publicaciones activas, reservas
     * por estado, ingresos estimados y ciudades con mayor demanda.
     * 
     * @return indicadores en JSON (en cero si no hay datos)
     */
    @Operation(summary = "Consultar los indicadores del dashboard")
    @GetMapping("/indicadores")
    public ResponseEntity<DashboardDTO> indicadores() {
        return new ResponseEntity<>(dashboardServ.obtenerIndicadores(), HttpStatus.OK);
    }

    /**
     * Exporta los indicadores del dashboard en PDF o Excel y registra la
     * exportación en auditoría.
     * 
     * @param authentication administrador autenticado, tomado del token
     * @param formato        PDF o EXCEL
     * @return el archivo como descarga
     */
    @Operation(summary = "Exportar los indicadores en PDF o Excel")
    @GetMapping("/exportar")
    public ResponseEntity<Object> exportar(Authentication authentication,
            @RequestParam(name = "formato") String formato) {
        ArchivoExportadoDTO archivo = new ArchivoExportadoDTO();
        int estado = exportacionServ.exportar(authentication.getName(), formato, archivo);
        return switch (estado) {
            case 0 -> ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename(archivo.getNombreArchivo()).build().toString())
                    .contentType(MediaType.parseMediaType(archivo.getTipoContenido())).body(archivo.getContenido());
            case 1 -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato inválido: use PDF o EXCEL");
            case 2 -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo generar el archivo");
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al exportar los indicadores");
        };
    }
}