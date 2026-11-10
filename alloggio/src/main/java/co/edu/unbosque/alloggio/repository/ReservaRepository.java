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
         * @return
         */
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT r FROM Reserva r WHERE r.idReserva = :idReserva")
        Optional<Reserva> findByIdParaActualizar(@Param("idReserva") Long idReserva);

        /**
         * Cambia de estado, en una sola instrucción, las reservas en el estado actual
         * cuya fecha de salida ya llegó.
         * 
         * @param actual
         * @param nuevo
         * @param fecha
         * @return
         */
        @Modifying(clearAutomatically = true)
        @Query("UPDATE Reserva r SET r.estado = :nuevo WHERE r.estado = :actual AND r.fechaSalida <= :fecha")
        int actualizarEstadoPorFechaSalida(@Param("actual") EstadoReserva actual, @Param("nuevo") EstadoReserva nuevo,
                        @Param("fecha") LocalDate fecha);

        /**
         * Lista las reservas de una publicación en un estado cuya salida es posterior a
         * la fecha dada (reservas futuras o en curso).
         *
         * @param idPublicacion
         * @param estado
         * @param fecha
         * @return
         */
        List<Reserva> findByPublicacionIdPublicacionAndEstadoAndFechaSalidaAfterOrderByFechaLlegadaAsc(
                        Long idPublicacion, EstadoReserva estado, LocalDate fecha);
}