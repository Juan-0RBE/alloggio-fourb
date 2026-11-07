package co.edu.unbosque.alloggio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.alloggio.entity.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /**
     * Busca la reserva que un huésped creó con una clave de idempotencia.
     * 
     * @param idUsuario
     * @param claveIdempotencia
     * @return
     */
    Optional<Reserva> findByHuespedIdUsuarioAndClaveIdempotencia(Long idUsuario, String claveIdempotencia);

    /**
     * Busca la reserva creada a partir de una cotización.
     * 
     * @param idCotizacion
     * @return
     */
    Optional<Reserva> findByCotizacionIdCotizacion(Long idCotizacion);

    /**
     * Lista las reservas de un usuario
     * 
     * @param nombreUsuario
     * @return
     */
    List<Reserva> findByHuespedNombreUsuarioOrderByFechaLlegadaDescIdReservaDesc(String nombreUsuario);

}