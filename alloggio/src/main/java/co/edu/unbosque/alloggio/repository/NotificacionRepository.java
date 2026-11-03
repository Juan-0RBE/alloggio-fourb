package co.edu.unbosque.alloggio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.alloggio.entity.Notificacion;

/**
 * Repositorio para la entidad Notificacion.
 */
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /**
     * Busca las notificaciones de un usuario por su nombre de usuario, de la más
     * reciente a la
     * más antigua.
     * 
     * @param nombreUsuario
     * @return
     */
    List<Notificacion> findByUsuarioNombreUsuarioOrderByFechaDesc(String nombreUsuario);

}