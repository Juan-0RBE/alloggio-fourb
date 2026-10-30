package co.edu.unbosque.alloggio.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unbosque.alloggio.dto.UsuarioDTO;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;

/**
 * Servicio para manejar operaciones relacionadas con usuarios.
 */
@Service
public class UsuarioService implements CRUDOperation<UsuarioDTO> {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UsuarioService() {
    }

    /**
     * Crea un nuevo usuario.
     * 
     * @param newData los datos del usuario a crear
     * @return 0 si es exitoso, 1 si el nombre de usuario ya está en uso, 2 si la
     *         contraseña no cumple con los requisitos, 3 si el correo ya está en
     *         uso, 4 si el ID ya está en uso
     */
    @Override
    public int create(UsuarioDTO newData) {
        Usuario entity = modelMapper.map(newData, Usuario.class);
        if (findUsernameAlreadyTaken(entity.getNombreUsuario())) {
            return 1; // Nombre de usuario ya está en uso
        }
        if (!newData.getClaveUsuario().matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$")) {
            return 2; // La contraseña no cumple con los requisitos de complejidad
        }
        if (findEmailAlreadyTaken(entity.getCorreoUsuario())) {
            return 3; // El correo ya está en uso
        }
        if (findIDAlreadyTaken(entity.getDocumentoIdentidad())) {
            return 4; // El documento de identidad ya está en uso
        }
        int edad = Period.between(entity.getFechaNacimiento(), LocalDate.now()).getYears();
        System.out.println("Edad: " + edad);
        entity.setEdad(edad);
        entity.setRole(Usuario.Role.USUARIO);
        entity.setIsVerified(true);
        if (newData.getRole() != null) {
            entity.setRole(newData.getRole());
        }
        entity.setClaveUsuario(passwordEncoder.encode(entity.getClaveUsuario()));
        usuarioRepo.save(entity);
        return 0; // Usuario creado exitosamente
    }

    /**
     * Obtiene todos los usuarios.
     * 
     * @return una lista de DTOs de usuario
     */
    @Override
    public List<UsuarioDTO> getAll() {
        List<Usuario> entityList = usuarioRepo.findAll();
        List<UsuarioDTO> dtoList = new ArrayList<>();
        entityList.forEach(entity -> {
            UsuarioDTO dto = modelMapper.map(entity, UsuarioDTO.class);
            dtoList.add(dto);
        });
        return dtoList;
    }

    /**
     * Elimina un usuario por su ID.
     * 
     * @param id el ID del usuario a eliminar
     * @return 0 si es exitoso, 1 si el usuario no se encuentra
     */
    @Override
    public int deleteById(Long id) {
        Optional<Usuario> found = usuarioRepo.findById(id);
        if (found.isPresent()) {
            usuarioRepo.delete(found.get());
            return 0; // Usuario eliminado exitosamente
        } else {
            return 1; // Usuario no encontrado
        }
    }

    /**
     * Actualiza un usuario por su ID.
     * 
     * @param id      el ID del usuario a actualizar
     * @param newData los datos actualizados del usuario
     * @return 0 si es exitoso, 1 si el usuario no se encuentra, 2 si el nombre de
     *         usuario ya está en uso
     */
    @Override
    public int updateById(Long id, UsuarioDTO newData) {
        Optional<Usuario> found = usuarioRepo.findById(id);

        if (!found.isPresent()) {
            return 1; // Usuario no encontrado
        }

        Optional<Usuario> usernameFound = usuarioRepo.findByNombreUsuario(newData.getNombreUsuario());
        if (usernameFound.isPresent() && !usernameFound.get().getIdUsuario().equals(id)) {
            return 2; // Nombre de usuario ya está en uso
        }

        Usuario temp = found.get();
        temp.setNombreUsuario(newData.getNombreUsuario());
        temp.setDocumentoIdentidad(newData.getDocumentoIdentidad());

        temp.setPrimerNombre(newData.getPrimerNombre());
        temp.setSegundoNombre(newData.getSegundoNombre());

        temp.setPrimerApellido(newData.getPrimerApellido());
        temp.setSegundoApellido(newData.getSegundoApellido());

        temp.setFechaNacimiento(newData.getFechaNacimiento());
        temp.setEdad(Period.between(newData.getFechaNacimiento(), LocalDate.now()).getYears());

        temp.setNumeroTelefono(newData.getNumeroTelefono());

        if (newData.getRole() != null) {
            temp.setRole(newData.getRole());
        }

        usuarioRepo.save(temp);
        return 0; // Usuario actualizado exitosamente
    }

    /**
     * Actualiza la contraseña de un usuario por su ID.
     * 
     * @param id      el ID del usuario cuya contraseña se va a actualizar
     * @param newData los datos del usuario que contienen la nueva contraseña
     * @return 0 si es exitoso, 1 si el usuario no se encuentra, 2 si la contraseña
     *         no cumple con los requisitos de complejidad
     */
    public int updatePasswordById(Long id, UsuarioDTO newData) {
        Optional<Usuario> prevEntity = usuarioRepo.findById(id);

        if (!prevEntity.isPresent()) {
            return 1; // Usuario no encontrado
        }

        if (!newData.getClaveUsuario().matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$")) {
            return 2; // La contraseña no cumple con los requisitos de complejidad
        }

        Usuario temp = prevEntity.get();

        temp.setClaveUsuario(passwordEncoder.encode(newData.getClaveUsuario()));
        usuarioRepo.save(temp);
        return 0; // Contraseña actualizada exitosamente

    }

    /**
     * Cuenta la cantidad de usuarios.
     * 
     * @return el número total de usuarios
     */
    @Override
    public Long count() {
        return usuarioRepo.count();
    }

    /**
     * Verifica si un usuario existe por su ID.
     * 
     * @param id el ID del usuario a verificar
     * @return true si el usuario existe, false de lo contrario
     */
    @Override
    public boolean exist(Long id) {
        return usuarioRepo.existsById(id);
    }

    /**
     * Obtiene un usuario por su ID.
     * 
     * @param id el ID del usuario a obtener
     * @return el DTO del usuario si se encuentra, null de lo contrario
     */
    @Override
    public UsuarioDTO getById(Long id) {
        Optional<Usuario> found = usuarioRepo.findById(id);
        if (found.isPresent()) {
            return modelMapper.map(found.get(), UsuarioDTO.class);
        }
        return null; // Usuario no encontrado
    }

    /**
     * Obtiene un usuario por su correo electrónico.
     * 
     * @param correo
     * @return
     */
    public UsuarioDTO getByCorreo(String correo) {
        Optional<Usuario> found = usuarioRepo.findByCorreoUsuario(correo);
        if (found.isPresent()) {
            return modelMapper.map(found.get(), UsuarioDTO.class);
        }
        return null; // Usuario no encontrado
    }

    /**
     * Obtiene un usuario por su nombre de usuario.
     * 
     * @param nombreUsuario
     * @return
     */
    public UsuarioDTO getByNombreUsuario(String nombreUsuario) {
        Optional<Usuario> found = usuarioRepo.findByNombreUsuario(nombreUsuario);
        if (found.isPresent()) {
            return modelMapper.map(found.get(), UsuarioDTO.class);
        }
        return null; // Usuario no encontrado
    }

    /**
     * Verifica si un nombre de usuario ya está en uso.
     * 
     * @param nombreUsuario
     * @return
     */
    public boolean findUsernameAlreadyTaken(String nombreUsuario) {
        Optional<Usuario> found = usuarioRepo.findByNombreUsuario(nombreUsuario);
        return found.isPresent();
    }

    /**
     * Verifica si un correo electrónico ya está en uso.
     * 
     * @param correoUsuario
     * @return
     */
    public boolean findEmailAlreadyTaken(String correoUsuario) {
        Optional<Usuario> found = usuarioRepo.findByCorreoUsuario(correoUsuario);
        return found.isPresent();
    }

    /**
     * Verifica si un documento de identidad ya está en uso.
     * 
     * @param documentoIdentidad
     * @return
     */
    public boolean findIDAlreadyTaken(String documentoIdentidad) {
        Optional<Usuario> found = usuarioRepo.findByDocumentoIdentidad(documentoIdentidad);
        return found.isPresent();
    }

    /**
     * Valida las credenciales de un usuario.
     * 
     * @param nombreUsuario
     * @param claveUsuario
     * @return
     */
    public int validateCredentials(String nombreUsuario, String claveUsuario) {
        Optional<Usuario> found = usuarioRepo.findByNombreUsuario(nombreUsuario);
        if (found.isPresent()) {
            Usuario user = found.get();
            if (passwordEncoder.matches(claveUsuario, user.getClaveUsuario())) {
                return 0; // Credenciales válidas
            } else {
                return 2; // Contraseña incorrecta
            }
        } else {
            return 1; // Usuario no encontrado
        }
    }

    /**
     * Updates the verification status of a usuario by ID.
     * 
     * @param id
     * @param newData
     * @return
     */
    /*
     * public int updateVerificationStatus(Long id, UsuarioDTO newData) {
     * Optional<Usuario> prevEntity = usuarioRepo.findById(id);
     * 
     * if (prevEntity.isPresent()) {
     * Usuario temp = prevEntity.get();
     * 
     * temp.setIsVerified(newData.getIsVerified());
     * 
     * usuarioRepo.save(temp);
     * 
     * return 0;
     * }
     * 
     * return 1;
     * }
     */

    /**
     * Updates the verification status of a usuario by correo.
     * 
     * @param correo
     * @param newData
     * @return
     */
    public int updateVerificationStatus(String correo, UsuarioDTO newData) {

        Optional<Usuario> prevEntity = usuarioRepo.findByCorreoUsuario(correo);

        if (prevEntity.isPresent()) {

            Usuario temp = prevEntity.get();

            temp.setIsVerified(newData.getIsVerified());

            usuarioRepo.save(temp);

            return 0;
        }

        return 1;
    }

}
