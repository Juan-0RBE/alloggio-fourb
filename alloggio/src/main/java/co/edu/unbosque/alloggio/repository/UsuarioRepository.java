package co.edu.unbosque.alloggio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.alloggio.entity.Usuario;

/**
 * Interfaz de repositorio para la gestión de entidades Usuario.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Encuentra un usuario por su nombre de usuario.
     * 
     * @param nombreUsuario
     * @return
     */
    public Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    /**
     * Encuentra un usuario por su dirección de correo electrónico.
     * 
     * @param correoUsuario
     * @return
     */
    public Optional<Usuario> findByCorreoUsuario(String correoUsuario);

    /**
     * Encuentra un usuario por su número de documento de identidad.
     * 
     * @param documentoIdentidad
     * @return
     */
    public Optional<Usuario> findByDocumentoIdentidad(String documentoIdentidad);

    /**
     * Elimina un usuario por su dirección de correo electrónico.
     * 
     * @param correoUsuario
     */
    public void deleteByCorreoUsuario(String correoUsuario);

    /**
     * Elimina un usuario por su nombre de usuario.
     * 
     * @param nombreUsuario
     * @return
     */
    public void deleteByNombreUsuario(String nombreUsuario);
}
