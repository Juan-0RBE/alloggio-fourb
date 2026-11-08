package co.edu.unbosque.alloggio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.alloggio.entity.Resena;
import jakarta.persistence.LockModeType;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    /**
     * Indica si una reserva ya tiene su reseña (una por reserva).
     * 
     * @param idReserva
     * @return true si ya existe
     */
    boolean existsByReservaIdReserva(Long idReserva);

    /**
     * Las 10 reseñas visibles más recientes de un alojamiento, para su detalle.
     * 
     * @param idPublicacion
     * @return reseñas visibles, de la más reciente a la más antigua
     */
    List<Resena> findTop10ByReservaPublicacionIdPublicacionAndVisibleTrueOrderByFechaDescIdResenaDesc(
            Long idPublicacion);

    /**
     * Cantidad de reseñas visibles de un alojamiento.
     * 
     * @param idPublicacion
     * @return total de reseñas visibles
     */
    long countByReservaPublicacionIdPublicacionAndVisibleTrue(Long idPublicacion);

    /**
     * Promedio de calificación de las reseñas visibles de un alojamiento. Se
     * calcula al consultar, así que ocultar una reseña lo corrige solo.
     * 
     * @param idPublicacion
     * @return el promedio, o null si no hay reseñas visibles
     */
    @Query("SELECT AVG(r.calificacion) FROM Resena r "
            + "WHERE r.reserva.publicacion.idPublicacion = :idPublicacion AND r.visible = true")
    Double promedioVisible(@Param("idPublicacion") Long idPublicacion);

    /**
     * Todas las reseñas de un alojamiento, visibles u ocultas, para el
     * administrador.
     * 
     * @param idPublicacion
     * @return reseñas de la más reciente a la más antigua
     */
    List<Resena> findByReservaPublicacionIdPublicacionOrderByFechaDescIdResenaDesc(Long idPublicacion);

    /**
     * Obtiene una reseña bloqueando su fila (SELECT ... FOR UPDATE) hasta que
     * termine la transacción. Evita que dos administradores la oculten a la vez.
     * Debe llamarse dentro de una transacción.
     * 
     * @param idResena
     * @return la reseña, ya bloqueada
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Resena r WHERE r.idResena = :idResena")
    Optional<Resena> findByIdParaActualizar(@Param("idResena") Long idResena);
}