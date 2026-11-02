package co.edu.unbosque.alloggio.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.dto.EventoAuditoriaDTO;
import co.edu.unbosque.alloggio.entity.EventoAuditoria;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Accion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Resultado;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.TipoEntidad;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.EventoAuditoriaRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;

/**
 * Servicio de auditoría (RN-15). Los demás servicios llaman a registrarExito,
 * registrarDenegado o registrar con una sola línea. Este servicio solo depende
 * de repositorios, así que no genera dependencias circulares.
 */
@Service
public class EventoAuditoriaService {

    private static final int LONGITUD_MAX_DESCRIPCION = 500;
    private static final int TAMANO_MAX_PAGINA = 100;

    @Autowired
    private EventoAuditoriaRepository auditoriaRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    /**
     * Registra un evento de auditoría. Se une a la transacción del servicio que lo
     * llama: el cambio y su registro se guardan juntos.
     * 
     * @param nombreUsuario nombre de usuario de quien ejecuta la acción (null o
     *                      inexistente si no hay usuario autenticado)
     * @param accion        acción realizada
     * @param entidadTipo   tipo de entidad afectada
     * @param entidadId     id de la entidad afectada (puede ser null)
     * @param resultado     resultado de la acción
     * @param descripcion   detalle opcional
     */
    @Transactional
    public void registrar(String nombreUsuario, Accion accion, TipoEntidad entidadTipo, Long entidadId,
            Resultado resultado, String descripcion) {
        Usuario usuario = null;
        if (nombreUsuario != null) {
            usuario = usuarioRepo.findByNombreUsuario(nombreUsuario).orElse(null);
        }
        auditoriaRepo.save(new EventoAuditoria(usuario, accion, entidadTipo, entidadId, resultado,
                recortar(descripcion)));
    }

    /**
     * Atajo para registrar una acción exitosa.
     */
    @Transactional
    public void registrarExito(String nombreUsuario, Accion accion, TipoEntidad entidadTipo, Long entidadId,
            String descripcion) {
        registrar(nombreUsuario, accion, entidadTipo, entidadId, Resultado.EXITO, descripcion);
    }

    /**
     * Atajo para registrar una acción denegada (por ejemplo, intentar editar una
     * publicación ajena).
     */
    @Transactional
    public void registrarDenegado(String nombreUsuario, Accion accion, TipoEntidad entidadTipo, Long entidadId,
            String descripcion) {
        registrar(nombreUsuario, accion, entidadTipo, entidadId, Resultado.DENEGADO, descripcion);
    }

    /**
     * Consulta el log de auditoría aplicando solo los filtros recibidos.
     * 
     * @param fechaDesde
     * @param fechaHasta
     * @param correoUsuario
     * @param accion
     * @param entidadTipo
     * @param entidadId
     * @param resultado
     * @param pagina
     * @param tamano
     * @return
     */
    @Transactional(readOnly = true)
    public List<EventoAuditoriaDTO> consultar(LocalDate fechaDesde, LocalDate fechaHasta, String correoUsuario,
            Accion accion, TipoEntidad entidadTipo, Long entidadId, Resultado resultado, int pagina, int tamano) {

        Specification<EventoAuditoria> spec = (root, query, cb) -> {
            List<Predicate> filtros = new ArrayList<>();
            if (fechaDesde != null) {
                filtros.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("fechaHora"),
                        fechaDesde.atStartOfDay()));
            }
            if (fechaHasta != null) {
                filtros.add(cb.lessThan(root.<LocalDateTime>get("fechaHora"),
                        fechaHasta.plusDays(1).atStartOfDay()));
            }
            if (correoUsuario != null && !correoUsuario.isBlank()) {
                filtros.add(cb.equal(root.get("usuario").get("correoUsuario"), correoUsuario.trim()));
            }
            if (accion != null) {
                filtros.add(cb.equal(root.get("accion"), accion));
            }
            if (entidadTipo != null) {
                filtros.add(cb.equal(root.get("entidadTipo"), entidadTipo));
            }
            if (entidadId != null) {
                filtros.add(cb.equal(root.get("entidadId"), entidadId));
            }
            if (resultado != null) {
                filtros.add(cb.equal(root.get("resultado"), resultado));
            }
            return cb.and(filtros.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 1), TAMANO_MAX_PAGINA),
                Sort.by(Sort.Direction.DESC, "fechaHora").and(Sort.by(Sort.Direction.DESC, "idAuditoria")));

        List<EventoAuditoriaDTO> resultadoDto = new ArrayList<>();
        for (EventoAuditoria e : auditoriaRepo.findAll(spec, pageable).getContent()) {
            resultadoDto.add(convertir(e));
        }
        return resultadoDto;
    }

    private EventoAuditoriaDTO convertir(EventoAuditoria e) {
        EventoAuditoriaDTO dto = new EventoAuditoriaDTO();
        dto.setIdAuditoria(e.getIdAuditoria());
        if (e.getUsuario() != null) {
            dto.setIdUsuario(e.getUsuario().getIdUsuario());
            dto.setCorreoUsuario(e.getUsuario().getCorreoUsuario());
        }
        dto.setAccion(e.getAccion());
        dto.setEntidadTipo(e.getEntidadTipo());
        dto.setEntidadId(e.getEntidadId());
        dto.setFechaHora(e.getFechaHora());
        dto.setDescripcion(e.getDescripcion());
        dto.setResultado(e.getResultado());
        return dto;
    }

    private String recortar(String texto) {
        if (texto == null || texto.length() <= LONGITUD_MAX_DESCRIPCION) {
            return texto;
        }
        return texto.substring(0, LONGITUD_MAX_DESCRIPCION);
    }
}