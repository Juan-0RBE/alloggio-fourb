package co.edu.unbosque.alloggio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.alloggio.entity.PoliticaCancelacion;

public interface PoliticaCancelacionRepository extends JpaRepository<PoliticaCancelacion, Long> {

    /**
     * Encuentra una política de cancelación por su ID.
     * 
     * @param idPolitica
     * @return
     */
    public Optional<PoliticaCancelacion> findByIdPolitica(Long idPolitica);

    /**
     * Encuentra una política de cancelación por su nombre.
     * 
     * @param nombre
     * @return
     */
    public Optional<PoliticaCancelacion> findByNombre(String nombre);

    public void deleteByNombre(String nombre);

}
