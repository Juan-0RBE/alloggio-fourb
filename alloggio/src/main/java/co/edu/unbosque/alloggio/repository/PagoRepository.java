package co.edu.unbosque.alloggio.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.alloggio.entity.Pago;
import co.edu.unbosque.alloggio.entity.Pago.EstadoPago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    /**
     * Indica si la cotización ya tiene un pago en el estado dado (por ejemplo,
     * APROBADO), para no cobrarla dos veces.
     * 
     * @param idCotizacion
     * @param estado
     * @return true si existe al menos un pago
     */
    boolean existsByCotizacionIdCotizacionAndEstado(Long idCotizacion, EstadoPago estado);
}