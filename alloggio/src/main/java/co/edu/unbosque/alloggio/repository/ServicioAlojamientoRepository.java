package co.edu.unbosque.alloggio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.alloggio.entity.ServicioAlojamiento;

public interface ServicioAlojamientoRepository extends JpaRepository<ServicioAlojamiento, Long> {

    /**
     * Encuentra un servicio de alojamiento por su ID.
     * 
     * @param idServicio
     * @return
     */
    public Optional<ServicioAlojamiento> findByIdServicio(Long idServicio);

    /**
     * Elimina un servicio de alojamiento por su ID.
     * 
     * @param idServicio
     */
    public void deleteByIdServicio(Long idServicio);

    /**
     * Busca un servicio por su ID.
     * 
     * @param idPublicacion
     * @return
     */
    List<ServicioAlojamiento> findByPublicacionIdPublicacion(Long idPublicacion);

}
