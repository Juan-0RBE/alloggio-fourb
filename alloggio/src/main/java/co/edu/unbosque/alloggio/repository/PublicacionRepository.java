package co.edu.unbosque.alloggio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;

/**
 * Repositorio para la gestión de entidades Publicacion.
 */
public interface PublicacionRepository extends JpaRepository<Publicacion, Long>, JpaSpecificationExecutor<Publicacion> {

    /**
     * Encuentra todas las publicaciones asociadas a un anfitrión específico.
     * 
     * @param idUsuario ID del anfitrión
     * @return Lista de publicaciones del anfitrión
     */
    List<Publicacion> findByAnfitrionIdUsuario(Long idUsuario);

    List<Publicacion> findByEstado(EstadoPublicacion estado);

    /**
     * Encuentra publicaciones por su ciudad.
     * 
     * @param ciudad Nombre de la ciudad
     * @return Lista de publicaciones en esa ciudad
     */
    List<Publicacion> findByCiudadIgnoreCase(String ciudad);
}