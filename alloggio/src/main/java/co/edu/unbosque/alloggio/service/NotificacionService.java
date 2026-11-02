package co.edu.unbosque.alloggio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.dto.NotificacionDTO;
import co.edu.unbosque.alloggio.entity.Notificacion;
import co.edu.unbosque.alloggio.entity.Notificacion.TipoNotificacion;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.NotificacionRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;

/**
 * Servicio de notificaciones. Los demás servicios crean notificaciones con una
 * sola línea usando notificarUsuario o notificarAOtroUsuario. Solo depende de
 * repositorios.
 */
@Service
public class NotificacionService {

    private static final int LONGITUD_MAX_MENSAJE = 500;

    @Autowired
    private NotificacionRepository notificacionRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    /**
     * Crea una notificación para el usuario autenticado (por ejemplo, el huésped
     * que acaba de reservar).
     * 
     * @param nombreUsuario
     * @param tipo
     * @param mensaje
     */
    @Transactional
    public void notificarUsuario(String nombreUsuario, TipoNotificacion tipo, String mensaje) {
        usuarioRepo.findByNombreUsuario(nombreUsuario).ifPresent(u -> guardar(u, tipo, mensaje));
    }

    /**
     * Crea una notificación para otro usuario distinto del que ejecuta la acción
     * (por ejemplo, el anfitrión cuando un huésped reserva su publicación).
     * 
     * @param destinatario
     * @param tipo
     * @param mensaje
     */
    @Transactional
    public void notificarAOtroUsuario(Usuario destinatario, TipoNotificacion tipo, String mensaje) {
        guardar(destinatario, tipo, mensaje);
    }

    /**
     * Devuelve la bandeja del usuario, ordenada por fecha descendente. Cada
     * elemento indica si ya fue leída.
     * 
     * @param nombreUsuario
     * @return
     */
    @Transactional(readOnly = true)
    public List<NotificacionDTO> obtenerMisNotificaciones(String nombreUsuario) {
        List<NotificacionDTO> dtoList = new ArrayList<>();
        for (Notificacion n : notificacionRepo.findByUsuarioNombreUsuarioOrderByFechaDesc(nombreUsuario)) {
            dtoList.add(convertir(n));
        }
        return dtoList;
    }

    /**
     * Marca una notificación propia como leída.
     * 
     * @param nombreUsuario
     * @param idNotificacion
     * @return
     */
    @Transactional
    public int marcarLeida(String nombreUsuario, Long idNotificacion) {
        Optional<Notificacion> notifOpt = notificacionRepo.findById(idNotificacion);
        if (!notifOpt.isPresent()) {
            return 1; // No encontrada
        }

        Notificacion notif = notifOpt.get();
        if (!notif.getUsuario().getNombreUsuario().equals(nombreUsuario)) {
            return 2; // No autorizada
        }

        notif.setEstaLeida(true);
        notificacionRepo.save(notif);
        return 0;
    }

    private void guardar(Usuario destinatario, TipoNotificacion tipo, String mensaje) {
        String texto = mensaje;
        if (texto != null && texto.length() > LONGITUD_MAX_MENSAJE) {
            texto = texto.substring(0, LONGITUD_MAX_MENSAJE);
        }
        notificacionRepo.save(new Notificacion(destinatario, tipo, texto));
    }

    private NotificacionDTO convertir(Notificacion n) {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setIdNotificacion(n.getIdNotificacion());
        dto.setTipoNotificacion(n.getTipoNotificacion());
        dto.setMensaje(n.getMensaje());
        dto.setFecha(n.getFecha());
        dto.setEstaLeida(n.getEstaLeida());
        dto.setIdUsuario(n.getUsuario().getIdUsuario());
        return dto;
    }
}