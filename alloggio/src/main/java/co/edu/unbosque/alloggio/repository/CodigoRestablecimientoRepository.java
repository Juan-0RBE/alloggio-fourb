package co.edu.unbosque.alloggio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.alloggio.entity.CodigoRestablecimiento;
import co.edu.unbosque.alloggio.entity.Usuario;

/**
 * Interface de repositorio para la entidad CodigoRestablecimiento.
 * 
 * CodigoRestablecimientoRepository
 */
public interface CodigoRestablecimientoRepository extends JpaRepository<CodigoRestablecimiento, Long> {

    /**
     * Obtiene el código vigente no usado más reciente de un usuario
     * 
     * @param usuario
     * @return
     */
    public Optional<CodigoRestablecimiento> findTopByUsuarioAndUsadoFalseOrderByFechaCreacionDesc(Usuario usuario);

    /**
     * Invalida los códigos pendientes de un usuario antes de generar uno nuevo.
     */
    @Modifying
    @Query("UPDATE CodigoRestablecimiento c SET c.usado = true WHERE c.usuario = :usuario AND c.usado = false")
    public int invalidarPendientes(@Param("usuario") Usuario usuario);
}
