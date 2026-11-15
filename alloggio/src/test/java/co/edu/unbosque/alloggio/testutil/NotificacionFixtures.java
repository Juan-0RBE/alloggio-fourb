package co.edu.unbosque.alloggio.testutil;

import co.edu.unbosque.alloggio.entity.Notificacion;
import co.edu.unbosque.alloggio.entity.Usuario;
import java.time.LocalDateTime;

public class NotificacionFixtures {

    public static final Long ID_NOTIFICACION = 10L;

    public static Notificacion notificacionValida(Usuario usuario, String mensaje) {
        Notificacion notif = new Notificacion();
        notif.setIdNotificacion(ID_NOTIFICACION);
        notif.setUsuario(usuario);
        notif.setTipoNotificacion(Notificacion.TipoNotificacion.RESERVA);
        notif.setMensaje(mensaje);
        notif.setFecha(LocalDateTime.now());
        notif.setEstaLeida(false);
        return notif;
    }
}