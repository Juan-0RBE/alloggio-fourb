package co.edu.unbosque.alloggio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import jakarta.persistence.LockModeType;

/**
 * Repositorio para la gestión de entidades Publicacion.
 */
public interface PublicacionRepository extends JpaRepository<Publicacion, Long>, JpaSpecificationExecutor<Publicacion> {

    /**
     * Encuentra todas las publicaciones asociadas a un anfitrión específico.
     * 
     * @param idUsuario
     * @return
     */
    List<Publicacion> findByAnfitrionIdUsuario(Long idUsuario);

    List<Publicacion> findByEstado(EstadoPublicacion estado);

    /**
     * Encuentra publicaciones por su ciudad.
     * 
     * @param ciudad
     * @return
     */
    List<Publicacion> findByCiudadIgnoreCase(String ciudad);

    /**
     * Obtiene una publicación bloqueando su fila (SELECT ... FOR UPDATE) hasta que
     * termine la transacción.
     * 
     * @param idPublicacion
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Publicacion p WHERE p.idPublicacion = :idPublicacion")
    Optional<Publicacion> findByIdParaActualizar(@Param("idPublicacion") Long idPublicacion);

    /**
     * Cuenta las publicaciones por estado. Cada fila es [estado, cantidad].
     * 
     * @return
     */
    @Query("SELECT p.estado, COUNT(p) FROM Publicacion p GROUP BY p.estado")
    List<Object[]> contarPorEstado();
}