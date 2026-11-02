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
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Accion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.TipoEntidad;
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

    @Autowired
    private EventoAuditoriaService auditoriaService;

    private static final String REGEX_TELEFONO = "^\\+?\\d{7,15}$";

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
        entity.setEstaActivo(true);
        entity.setEstadoCuenta("ACTIVA");
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

    // Lógica de actualización de usuario y de activación de cuenta

    /**
     * Consulta el perfil del usuario autenticado (CU-004, flujo principal). La
     * clave no se incluye en el resultado.
     * 
     * @param nombreUsuario nombre del usuario autenticado
     * @return el DTO del perfil sin clave, o null si no se encuentra
     */
    public UsuarioDTO getPerfil(String nombreUsuario) {
        Optional<Usuario> found = usuarioRepo.findByNombreUsuario(nombreUsuario);
        if (found.isPresent()) {
            UsuarioDTO dto = modelMapper.map(found.get(), UsuarioDTO.class);
            dto.setClaveUsuario(null);
            return dto;
        }
        return null; // Usuario no encontrado
    }

    /**
     * Valida un nombre o apellido.
     * 
     * @param valor       texto a validar
     * @param obligatorio true si no puede venir vacío
     * @return true si es válido
     */
    private boolean nombreValido(String valor, boolean obligatorio) {
        if (valor == null || valor.trim().isEmpty()) {
            return !obligatorio;
        }
        return valor.trim().matches("^\\p{L}[\\p{L} '\\-]{0,49}$"); //
    }

    /**
     * Convierte un texto vacío en null y recorta los espacios.
     * 
     * @param valor texto a normalizar
     * @return el texto recortado o null si está vacío
     */
    private String vacioANulo(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return valor.trim();
    }

    /**
     * Actualiza los datos básicos del perfil del usuario autenticado.
     * Solo se modifican primer y segundo nombre, primer y segundo apellido y
     * teléfono. El nombre de usuario, el correo, el documento, la clave y el rol
     * no se tocan.
     * 
     * @param nombreUsuario nombre del usuario autenticado
     * @param newData       datos nuevos del perfil
     * @return 0 si es exitoso, 1 si el usuario no se encuentra, 2 si el primer
     *         nombre es inválido, 3 si el segundo nombre es inválido, 4 si el
     *         primer apellido es inválido, 5 si el segundo apellido es inválido,
     *         6 si el teléfono es inválido
     */
    public int updatePerfil(String nombreUsuario, UsuarioDTO newData) {
        Optional<Usuario> found = usuarioRepo.findByNombreUsuario(nombreUsuario);

        if (!found.isPresent()) {
            return 1; // Usuario no encontrado
        }
        if (!nombreValido(newData.getPrimerNombre(), true)) {
            return 2; // Primer nombre inválido
        }
        if (!nombreValido(newData.getSegundoNombre(), false)) {
            return 3; // Segundo nombre inválido
        }
        if (!nombreValido(newData.getPrimerApellido(), true)) {
            return 4; // Primer apellido inválido
        }
        if (!nombreValido(newData.getSegundoApellido(), false)) {
            return 5; // Segundo apellido inválido
        }
        if (newData.getNumeroTelefono() == null || !newData.getNumeroTelefono().trim().matches(REGEX_TELEFONO)) {
            return 6; // Teléfono inválido
        }

        Usuario temp = found.get();
        temp.setPrimerNombre(newData.getPrimerNombre().trim());
        temp.setSegundoNombre(vacioANulo(newData.getSegundoNombre()));
        temp.setPrimerApellido(newData.getPrimerApellido().trim());
        temp.setSegundoApellido(vacioANulo(newData.getSegundoApellido()));
        temp.setNumeroTelefono(newData.getNumeroTelefono().trim());

        usuarioRepo.save(temp);
        return 0; // Perfil actualizado exitosamente
    }

    /**
     * Desactiva la cuenta del usuario autenticado (CU-005).
     * 
     * @param nombreUsuario nombre del usuario autenticado
     * @param datos         DTO que trae el campo confirmar
     * @return 0 si es exitoso, 1 si el usuario no se encuentra, 2 si la cuenta ya
     *         está inactiva, 3 si el usuario no confirmó (no se modifica nada)
     */
    public int desactivarCuenta(String nombreUsuario, UsuarioDTO datos) {
        Optional<Usuario> found = usuarioRepo.findByNombreUsuario(nombreUsuario);

        if (!found.isPresent()) {
            return 1; // Usuario no encontrado
        }

        Usuario temp = found.get();
        if (Boolean.FALSE.equals(temp.getEstaActivo())) {
            return 2; // La cuenta ya está inactiva
        }
        if (!Boolean.TRUE.equals(datos.getConfirmar())) {
            return 3; // El usuario no confirmó
        }

        temp.setEstaActivo(false);
        usuarioRepo.save(temp);
        auditoriaService.registrarExito(nombreUsuario, Accion.DESACTIVAR_CUENTA, TipoEntidad.USUARIO,
                temp.getIdUsuario(), "El usuario desactivó su cuenta");

        // Registro en auditoria
        return 0; // Cuenta desactivada exitosamente
    }

    /**
     * Reactiva la cuenta de un usuario inactivo. Es pública porque el
     * usuario inactivo no puede iniciar sesión; por eso exige las credenciales.
     * 
     * @param datos
     * @return 0 si es exitoso, 1 si las credenciales son incorrectas, 2 si la
     *         cuenta está restringida por un administrador, 3 si la cuenta ya está
     *         activa, 4 si el usuario no confirmó (no se modifica nada)
     */
    public int reactivarCuenta(UsuarioDTO datos) {
        if (datos.getNombreUsuario() == null || datos.getClaveUsuario() == null) {
            return 1; // Credenciales incorrectas
        }

        Optional<Usuario> found = usuarioRepo.findByNombreUsuario(datos.getNombreUsuario());
        if (!found.isPresent() || !passwordEncoder.matches(datos.getClaveUsuario(), found.get().getClaveUsuario())) {
            return 1; // Credenciales incorrectas
        }

        Usuario temp = found.get();
        if (Usuario.ESTADO_RESTRINGIDA.equals(temp.getEstadoCuenta())) {
            return 2; // Cuenta restringida: solo la levanta un administrador
        }
        if (!Boolean.FALSE.equals(temp.getEstaActivo())) {
            return 3; // La cuenta ya está activa
        }
        if (!Boolean.TRUE.equals(datos.getConfirmar())) {
            return 4; // El usuario no confirmó
        }

        temp.setEstaActivo(true);
        usuarioRepo.save(temp);
        auditoriaService.registrarExito(temp.getNombreUsuario(), Accion.ACTIVAR_CUENTA, TipoEntidad.USUARIO,
                temp.getIdUsuario(), "El usuario reactivó su cuenta");

        return 0; // Cuenta reactivada exitosamente
    }

}
