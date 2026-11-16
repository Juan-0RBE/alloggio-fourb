package co.edu.unbosque.alloggio.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import co.edu.unbosque.alloggio.dto.CodigoRestablecimientoDTO;
import co.edu.unbosque.alloggio.dto.UsuarioDTO;
import co.edu.unbosque.alloggio.entity.CodigoRestablecimiento;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.CodigoRestablecimientoRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;
import jakarta.transaction.Transactional;

/**
 * Servicio para el flujo de recuperación de la contraseña mediante código.
 * 
 * CodigoRestablecimientoService
 */
@Service
public class CodigoRestablecimientoService {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private CodigoRestablecimientoRepository codigoRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private UsuarioService usuarioServ;

    @Autowired
    private EmailService emailServ;

    @Value("${app.recuperacion.expiracion-minutos:15}")
    private int minutosVigencia;

    public CodigoRestablecimientoService() {
    }

    /**
     * Genera un código de recuperación, lo registra y lo envía por correo.
     *
     * @param correo correo registrado del usuario
     * @return 0 si es exitoso, 1 si el correo no está registrado, 2 si falló el
     *         envío del correo
     */
    @Transactional
    public int solicitarCodigo(String correo) {
        Optional<Usuario> found = usuarioRepo.findByCorreoUsuario(correo);
        if (!found.isPresent()) {
            return 1; // No hay una cuenta asociada al correo
        }
        Usuario usuario = found.get();

        codigoRepo.invalidarPendientes(usuario);

        LocalDateTime ahora = LocalDateTime.now();
        CodigoRestablecimiento nuevo = new CodigoRestablecimiento();
        nuevo.setCodigo(generarCodigo());
        nuevo.setFechaCreacion(ahora);
        nuevo.setFechaExpiracion(ahora.plusMinutes(minutosVigencia));
        nuevo.setUsuario(usuario);
        nuevo.setUsado(false);
        codigoRepo.save(nuevo);

        if (!emailServ.enviarEmailDeRecuperacion(correo, nuevo.getCodigo(), minutosVigencia)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 2; // Falló el envío; se revierte el registro del código
        }
        return 0; // Código generado y enviado
    }

    private String generarCodigo() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    /**
     * Busca el código vigente del usuario y comprueba que coincida y no haya expirado.
     */
    private Optional<CodigoRestablecimiento> obtenerCodigoValido(Usuario usuario, String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        Optional<CodigoRestablecimiento> vigente = codigoRepo
                .findTopByUsuarioAndUsadoFalseOrderByFechaCreacionDesc(usuario);

        if (!vigente.isPresent()
                || !vigente.get().getCodigo().equals(codigo.trim())
                || vigente.get().getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }
        return vigente;
    }

    /**
     * Valida el código y, si es correcto, actualiza la contraseña.
     *
     * @return 0 si es exitoso, 1 si el correo no está registrado, 2 si el código es
     *         inválido o expiró, 3 si la contraseña no cumple los requisitos
     */
    @Transactional
    public int confirmarCambio(CodigoRestablecimientoDTO dto) {
        Optional<Usuario> found = usuarioRepo.findByCorreoUsuario(dto.getCorreoUsuario());
        if (!found.isPresent()) {
            return 1;
        }
        Usuario usuario = found.get();

        Optional<CodigoRestablecimiento> vigente = obtenerCodigoValido(usuario, dto.getCodigo());
        if (!vigente.isPresent()) {
            return 2;
        }

        if (dto.getNuevaClave() == null) {
            return 3;
        }
        UsuarioDTO claveDTO = new UsuarioDTO();
        claveDTO.setClaveUsuario(dto.getNuevaClave());
        if (usuarioServ.updatePasswordById(usuario.getIdUsuario(), claveDTO) != 0) {
            return 3;
        }

        CodigoRestablecimiento codigo = vigente.get();
        codigo.setUsado(true);
        codigoRepo.save(codigo);
        return 0;
    }

    /**
     * Comprueba que el código sea válido sin consumirlo ni cambiar la contraseña.
     *
     * @return 0 si el código es válido, 1 si el correo no está registrado, 2 si el
     *         código es inválido o expiró
     */
    public int verificarCodigo(String correo, String codigo) {
        Optional<Usuario> found = usuarioRepo.findByCorreoUsuario(correo);
        if (!found.isPresent()) {
            return 1;
        }
        return obtenerCodigoValido(found.get(), codigo).isPresent() ? 0 : 2;
    }

}
