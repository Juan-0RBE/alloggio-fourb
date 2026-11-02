package co.edu.unbosque.alloggio.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.edu.unbosque.alloggio.entity.EventoAuditoria;

/**
 * Repositorio para la entidad EventoAuditoria. Las consultas con filtros
 * opcionales se arman con Specification en el servicio.
 */
public interface EventoAuditoriaRepository
        extends JpaRepository<EventoAuditoria, Long>, JpaSpecificationExecutor<EventoAuditoria> {

    /**
     * Consulta paginada con filtros. Carga el usuario junto con el evento para
     * evitar una consulta adicional por cada fila (N+1).
     */
    @Override
    @EntityGraph(attributePaths = "usuario")
    Page<EventoAuditoria> findAll(Specification<EventoAuditoria> spec, Pageable pageable);

}