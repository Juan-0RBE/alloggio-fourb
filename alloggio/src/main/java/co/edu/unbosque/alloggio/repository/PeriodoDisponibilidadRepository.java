package co.edu.unbosque.alloggio.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad;

/**
 * Repositorio para la entidad PeriodoDisponibilidad, que permite realizar
 * 
 * PeriodoDisponibilidadRepository
 */
public interface PeriodoDisponibilidadRepository extends JpaRepository<PeriodoDisponibilidad, Long> {

        /**
         * Obtiene los periodos de una publicación ordenados por fecha de inicio.
         * 
         * @param idPublicacion ID de la publicación
         * @return Lista de periodos de la publicación
         */
        List<PeriodoDisponibilidad> findByPublicacionIdPublicacionOrderByFechaInicioAsc(Long idPublicacion);

        /**
         * Encuentra los periodos de una publicación que se cruzan con el rango
         * [fechaInicio, fechaFin).
         * 
         * @param idPublicacion ID de la publicación
         * @param fechaInicio   inicio del rango consultado
         * @param fechaFin      fin del rango consultado (día de salida)
         * @return Lista de periodos que se cruzan con el rango
         */
        @Query("SELECT p FROM PeriodoDisponibilidad p WHERE p.publicacion.idPublicacion = :idPublicacion "
                        + "AND p.fechaInicio < :fechaFin AND p.fechaFin > :fechaInicio")
        List<PeriodoDisponibilidad> findIntersectingPeriods(@Param("idPublicacion") Long idPublicacion,
                        @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

        /**
         * 
         * @param idPublicacion
         * @param fecha
         * @return
         */
        List<PeriodoDisponibilidad> findByPublicacionIdPublicacionAndFechaFinAfterOrderByFechaInicioAsc(
                        Long idPublicacion, LocalDate fecha);

}
