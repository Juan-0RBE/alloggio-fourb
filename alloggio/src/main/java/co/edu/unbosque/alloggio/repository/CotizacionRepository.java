package co.edu.unbosque.alloggio.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.alloggio.entity.Cotizacion;

/**
 * Repositorio para la entidad Cotizacion.
 */
public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {
}