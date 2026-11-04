package co.edu.unbosque.alloggio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.alloggio.entity.ParametroTarifa;

/**
 * Repositorio para la entidad ParametroTarifa.
 */
public interface ParametroTarifaRepository extends JpaRepository<ParametroTarifa, Long> {

    /**
     * Obtiene los parámetros de tarifa vigentes.
     * 
     * @return Los parámetros de tarifa, si existen
     */
    Optional<ParametroTarifa> findFirstByOrderByIdParametroTarifaAsc();
}