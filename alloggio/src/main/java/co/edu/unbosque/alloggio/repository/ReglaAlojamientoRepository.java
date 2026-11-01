package co.edu.unbosque.alloggio.repository;

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

}
