package co.edu.unbosque.alloggio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.alloggio.entity.ReglaAlojamiento;

public interface ReglaAlojamientoRepository extends JpaRepository<ReglaAlojamiento, Long> {

    /**
     * Encuentra una regla de alojamiento por su ID.
     * 
     * @param idRegla
     * @return
     */
    public Optional<ReglaAlojamiento> findByIdRegla(Long idRegla);

    /**
     * Elimina una regla de alojamiento por su ID.
     * 
     * @param idRegla
     */
    public void deleteByIdRegla(Long idRegla);

    /**
     * 
     * @param idPublicacion
     * @return
     */
    List<ReglaAlojamiento> findByPublicacionIdPublicacion(Long idPublicacion);

}
