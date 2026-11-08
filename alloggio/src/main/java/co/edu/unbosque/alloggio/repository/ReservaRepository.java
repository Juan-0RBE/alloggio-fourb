package co.edu.unbosque.alloggio.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.alloggio.entity.Reserva;
import co.edu.unbosque.alloggio.entity.Reserva.EstadoReserva;
import jakarta.persistence.LockModeType;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /**
     * Busca la reserva que un huésped creó con una clave de idempotencia.
     * 
     * @param idUsuario         ID del huésped
     * @param claveIdempotencia clave enviada por el cliente
     * @return la reserva, si existe
     */
    Optional<Reserva> findByHuespedIdUsuarioAndClaveIdempotencia(Long idUsuario, String claveIdempotencia);

    /**
     * Busca la reserva creada a partir de una cotización.
     * 
     * @param idCotizacion
     * @return la reserva, si existe
     */
    Optional<Reserva> findByCotizacionIdCotizacion(Long idCotizacion);

    /**
     * Lista las reservas de un huésped, de la llegada más reciente a la más
     * antigua.
     * 
     * @param nombreUsuario
     * @return historial de reservas del huésped
     */
    List<Reserva> findByHuespedNombreUsuarioOrderByFechaLlegadaDescIdReservaDesc(String nombreUsuario);

    /**
     * Obtiene una reserva bloqueando su fila (SELECT ... FOR UPDATE) hasta que
     * termine la transacción. Evita que dos cancelaciones simultáneas de la misma
     * reserva se procesen a la vez. Debe llamarse dentro de una transacción.
     * 
     * @param idReserva
     * @return la reserva, ya bloqueada
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reserva r WHERE r.idReserva = :idReserva")
    Optional<Reserva> findByIdParaActualizar(@Param("idReserva") Long idReserva);

    /**
     * Cambia de estado, en una sola instrucción, las reservas en el estado actual
     * cuya fecha de salida ya llegó.
     * 
     * @param actual estado que deben tener
     * @param nuevo  estado al que pasan
     * @param fecha  se afectan las de salida igual o anterior a esta fecha
     * @return cantidad de reservas actualizadas
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Reserva r SET r.estado = :nuevo WHERE r.estado = :actual AND r.fechaSalida <= :fecha")
    int actualizarEstadoPorFechaSalida(@Param("actual") EstadoReserva actual, @Param("nuevo") EstadoReserva nuevo,
            @Param("fecha") LocalDate fecha);
}