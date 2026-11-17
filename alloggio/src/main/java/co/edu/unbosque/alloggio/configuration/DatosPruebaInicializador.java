package co.edu.unbosque.alloggio.configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.entity.Cotizacion;
import co.edu.unbosque.alloggio.entity.ImagenAlojamiento;
import co.edu.unbosque.alloggio.entity.Pago;
import co.edu.unbosque.alloggio.entity.Pago.EstadoPago;
import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad;
import co.edu.unbosque.alloggio.entity.PeriodoDisponibilidad.TipoPeriodo;
import co.edu.unbosque.alloggio.entity.PoliticaCancelacion;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.TipoAlojamiento;
import co.edu.unbosque.alloggio.entity.ReglaAlojamiento;
import co.edu.unbosque.alloggio.entity.Resena;
import co.edu.unbosque.alloggio.entity.Reserva;
import co.edu.unbosque.alloggio.entity.Reserva.EstadoReserva;
import co.edu.unbosque.alloggio.entity.ServicioAlojamiento;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.CotizacionRepository;
import co.edu.unbosque.alloggio.repository.ImagenAlojamientoRepository;
import co.edu.unbosque.alloggio.repository.PagoRepository;
import co.edu.unbosque.alloggio.repository.PeriodoDisponibilidadRepository;
import co.edu.unbosque.alloggio.repository.PoliticaCancelacionRepository;
import co.edu.unbosque.alloggio.repository.PublicacionRepository;
import co.edu.unbosque.alloggio.repository.ReglaAlojamientoRepository;
import co.edu.unbosque.alloggio.repository.ResenaRepository;
import co.edu.unbosque.alloggio.repository.ReservaRepository;
import co.edu.unbosque.alloggio.repository.ServicioAlojamientoRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;

/**
 * Precarga datos de prueba para probar reseñas, reservas y cancelaciones: un
 * anfitrión, tres huéspedes, tres publicaciones activas y reservas en todos los
 * estados (completadas con y sin reseña, confirmadas futuras y canceladas).
 *
 * Solo se ejecuta si el anfitrión de prueba no existe, así que no duplica datos
 * al reiniciar. Se desactiva con {@code alloggio.datos-prueba.habilitado=false}.
 *
 * Todos los usuarios de prueba usan la contraseña {@value #CLAVE_PRUEBA}.
 */
@Component
@Order(100)
@ConditionalOnProperty(name = "alloggio.datos-prueba.habilitado", havingValue = "true", matchIfMissing = true)
public class DatosPruebaInicializador implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosPruebaInicializador.class);

    static final String CLAVE_PRUEBA = "Prueba123*";
    static final String USUARIO_ANFITRION = "anfitrion.demo";

    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private PoliticaCancelacionRepository politicaRepo;
    @Autowired
    private PublicacionRepository publicacionRepo;
    @Autowired
    private ImagenAlojamientoRepository imagenRepo;
    @Autowired
    private ServicioAlojamientoRepository servicioRepo;
    @Autowired
    private ReglaAlojamientoRepository reglaRepo;
    @Autowired
    private CotizacionRepository cotizacionRepo;
    @Autowired
    private ReservaRepository reservaRepo;
    @Autowired
    private PagoRepository pagoRepo;
    @Autowired
    private PeriodoDisponibilidadRepository periodoRepo;
    @Autowired
    private ResenaRepository resenaRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepo.findByNombreUsuario(USUARIO_ANFITRION).isPresent()) {
            log.info("Los datos de prueba ya existen, omitiendo la precarga...");
            return;
        }

        // Políticas de cancelación
        PoliticaCancelacion flexible = politica("Flexible", 100.0, 1,
                "Devolución del 100% si cancelas al menos 1 día antes de la llegada.");
        PoliticaCancelacion moderada = politica("Moderada", 50.0, 5,
                "Devolución del 50% si cancelas al menos 5 días antes de la llegada.");
        PoliticaCancelacion estricta = politica("Estricta", 20.0, 14,
                "Devolución del 20% si cancelas al menos 14 días antes de la llegada.");

        // Usuarios
        Usuario anfitrion = usuario(USUARIO_ANFITRION, "anfitrion.demo@alloggio.test", "900000001", "Mariana",
                "Restrepo", "3001110001");
        Usuario ana = usuario("huesped.ana", "ana.huesped@alloggio.test", "900000002", "Ana", "Gómez",
                "3001110002");
        Usuario carlos = usuario("huesped.carlos", "carlos.huesped@alloggio.test", "900000003", "Carlos",
                "Duarte", "3001110003");
        Usuario laura = usuario("huesped.laura", "laura.huesped@alloggio.test", "900000004", "Laura",
                "Martínez", "3001110004");

        // Publicaciones
        Publicacion villa = publicacion(anfitrion, moderada, "Villa Moderna Vista al Valle",
                "Villa privada con vista panorámica, piscina y cocina equipada. Ideal para descansar en familia.",
                "Vereda El Retiro, km 3", "Medellín", TipoAlojamiento.CASA, 6, 550000.0, 85000.0);
        imagenes(villa,
                "https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1600565193348-f74bd3c7ccdf?auto=format&fit=crop&w=1200&q=80");
        servicio(villa, "Parqueadero", null, false);
        servicio(villa, "Wi-Fi", null, false);
        servicio(villa, "Piscina climatizada", 100000.0, true);
        regla(villa, "No se permiten fiestas ni eventos.");
        regla(villa, "Silencio después de las 10 p. m.");

        Publicacion cabana = publicacion(anfitrion, flexible, "Cabaña Alpina en el Bosque",
                "Cabaña de madera junto al embalse, con chimenea y terraza para ver el atardecer.",
                "Sector La Piedra, vía principal", "Guatapé", TipoAlojamiento.CABAÑA, 4, 320000.0, 50000.0);
        imagenes(cabana,
                "https://images.unsplash.com/photo-1587061949409-02df41d5e562?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1449158743715-0a90ebb6d2d8?auto=format&fit=crop&w=1200&q=80");
        servicio(cabana, "Wi-Fi", null, false);
        servicio(cabana, "Kayak", 60000.0, true);
        regla(cabana, "Se admiten mascotas pequeñas.");

        Publicacion loft = publicacion(anfitrion, estricta, "Loft Urbano Minimalista",
                "Loft luminoso en el centro, a pasos de restaurantes y transporte público.",
                "Calle 85 # 12-30", "Bogotá", TipoAlojamiento.APARTAMENTO, 2, 210000.0, 30000.0);
        imagenes(loft,
                "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1200&q=80");
        servicio(loft, "Wi-Fi", null, false);
        servicio(loft, "Lavandería", 25000.0, true);
        regla(loft, "No se permite fumar.");

        LocalDate hoy = LocalDate.now();

        // Completadas CON reseña: llenan el promedio y la lista del detalle
        Reserva r1 = reserva(ana, villa, hoy.minusDays(60), hoy.minusDays(56), EstadoReserva.COMPLETADA);
        resena(r1, 5, "Excelente estadía, la vista es espectacular y todo estaba impecable.", true);
        Reserva r2 = reserva(carlos, villa, hoy.minusDays(40), hoy.minusDays(37), EstadoReserva.COMPLETADA);
        resena(r2, 4, "Muy bonita y tranquila. El acceso en carro es un poco empinado.", true);
        Reserva r3 = reserva(laura, cabana, hoy.minusDays(30), hoy.minusDays(28), EstadoReserva.COMPLETADA);
        resena(r3, 5, "La chimenea y la terraza son lo mejor. Volvería sin pensarlo.", true);
        // Reseña oculta por moderación: no se muestra ni cuenta en el promedio
        Reserva r4 = reserva(carlos, loft, hoy.minusDays(25), hoy.minusDays(23), EstadoReserva.COMPLETADA);
        resena(r4, 1, "Comentario ofensivo de prueba para la moderación del administrador.", false);

        // Completadas SIN reseña: para probar el formulario de reseña
        reserva(ana, cabana, hoy.minusDays(15), hoy.minusDays(12), EstadoReserva.COMPLETADA);
        reserva(ana, loft, hoy.minusDays(10), hoy.minusDays(8), EstadoReserva.COMPLETADA);
        reserva(carlos, cabana, hoy.minusDays(7), hoy.minusDays(5), EstadoReserva.COMPLETADA);
        reserva(laura, villa, hoy.minusDays(20), hoy.minusDays(17), EstadoReserva.COMPLETADA);

        // Confirmadas futuras: no se pueden reseñar, sí cancelar. Están dentro del
        // plazo de la política Estricta (14 días), así que no corresponde devolución y
        // la cancelación no necesita llamar a Stripe con la referencia ficticia
        reserva(ana, loft, hoy.plusDays(10), hoy.plusDays(12), EstadoReserva.CONFIRMADA);
        reserva(laura, loft, hoy.plusDays(3), hoy.plusDays(5), EstadoReserva.CONFIRMADA);

        // Cancelada
        reserva(carlos, villa, hoy.plusDays(40), hoy.plusDays(42), EstadoReserva.CANCELADA);

        log.info("Datos de prueba precargados. Usuarios: {}, huesped.ana, huesped.carlos, huesped.laura "
                + "(contraseña {})", USUARIO_ANFITRION, CLAVE_PRUEBA);
    }

    private PoliticaCancelacion politica(String nombre, Double porcentaje, Integer plazo, String condiciones) {
        return politicaRepo.findByNombre(nombre).orElseGet(
                () -> politicaRepo.save(new PoliticaCancelacion(null, nombre, porcentaje, condiciones, plazo)));
    }

    private Usuario usuario(String nombreUsuario, String correo, String documento, String nombre,
            String apellido, String telefono) {
        Usuario u = new Usuario(null, correo, nombreUsuario, passwordEncoder.encode(CLAVE_PRUEBA), documento,
                nombre, null, apellido, null, LocalDate.of(1995, 5, 20), null, telefono, Usuario.ESTADO_ACTIVA,
                true, true, true, true, true, true, Usuario.Role.USUARIO);
        u.setEdad((int) ChronoUnit.YEARS.between(u.getFechaNacimiento(), LocalDate.now()));
        return usuarioRepo.save(u);
    }

    private Publicacion publicacion(Usuario anfitrion, PoliticaCancelacion politica, String titulo,
            String descripcion, String ubicacion, String ciudad, TipoAlojamiento tipo, int capacidad,
            Double precioNoche, Double tarifaLimpieza) {
        Publicacion p = new Publicacion();
        p.setTitulo(titulo);
        p.setDescripcion(descripcion);
        p.setUbicacion(ubicacion);
        p.setCiudad(ciudad);
        p.setTipo(tipo);
        p.setCapacidad(capacidad);
        p.setPrecioNoche(precioNoche);
        p.setTarifaLimpieza(tarifaLimpieza);
        p.setEstado(EstadoPublicacion.ACTIVA);
        p.setAnfitrion(anfitrion);
        p.setPoliticaCancelacion(politica);
        return publicacionRepo.save(p);
    }

    private void imagenes(Publicacion pub, String... urls) {
        for (int i = 0; i < urls.length; i++) {
            imagenRepo.save(new ImagenAlojamiento(null, urls[i], i + 1, pub));
        }
    }

    private void servicio(Publicacion pub, String nombre, Double costo, boolean adicional) {
        servicioRepo.save(new ServicioAlojamiento(null, nombre, costo, adicional, pub));
    }

    private void regla(Publicacion pub, String descripcion) {
        reglaRepo.save(new ReglaAlojamiento(null, descripcion, pub));
    }

    /**
     * Crea una reserva con su cotización y su pago aprobado, como si se hubiera
     * pagado por la pasarela. Las reservas no canceladas bloquean sus fechas.
     */
    private Reserva reserva(Usuario huesped, Publicacion pub, LocalDate llegada, LocalDate salida,
            EstadoReserva estado) {
        int noches = (int) ChronoUnit.DAYS.between(llegada, salida);
        BigDecimal base = dinero(pub.getPrecioNoche() * noches);
        BigDecimal limpieza = dinero(pub.getTarifaLimpieza());
        BigDecimal servicio = base.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = base.add(limpieza).add(servicio);

        Cotizacion cot = new Cotizacion();
        cot.setHuesped(huesped);
        cot.setPublicacion(pub);
        cot.setFechaLlegada(llegada);
        cot.setFechaSalida(salida);
        cot.setNoches(noches);
        cot.setPrecioBase(base);
        cot.setLimpieza(limpieza);
        cot.setServicio(servicio);
        cot.setDescuento(BigDecimal.ZERO.setScale(2));
        cot.setTotal(total);
        cot.setVigencia(llegada.atStartOfDay().minusDays(1));
        cot = cotizacionRepo.save(cot);

        Reserva r = new Reserva();
        r.setHuesped(huesped);
        r.setPublicacion(pub);
        r.setCotizacion(cot);
        r.setFechaLlegada(llegada);
        r.setFechaSalida(salida);
        r.setTotal(total);
        r.setEstado(estado);
        r.setClaveIdempotencia("datos-prueba-" + cot.getIdCotizacion());
        r.setFechaCreacion(LocalDateTime.now().minusDays(Math.max(1, ChronoUnit.DAYS.between(llegada,
                LocalDate.now()) + 7)));
        r = reservaRepo.save(r);

        Pago pago = new Pago();
        pago.setCotizacion(cot);
        pago.setReserva(r);
        pago.setMonto(total);
        pago.setMoneda("COP");
        pago.setEstado(EstadoPago.APROBADO);
        // Referencia ficticia: una cancelación con devolución de estas reservas fallará en Stripe
        pago.setReferenciaPasarela("pi_datos_prueba_" + r.getIdReserva());
        pago.setMarcaTarjeta("visa");
        pago.setUltimosCuatro("4242");
        pago.setFecha(r.getFechaCreacion());
        pagoRepo.save(pago);

        if (estado != EstadoReserva.CANCELADA) {
            periodoRepo.save(new PeriodoDisponibilidad(null, llegada, salida, TipoPeriodo.RESERVADO, pub, r));
        }
        return r;
    }

    private void resena(Reserva reserva, int calificacion, String comentario, boolean visible) {
        Resena resena = new Resena();
        resena.setReserva(reserva);
        resena.setCalificacion(calificacion);
        resena.setComentario(comentario);
        resena.setVisible(visible);
        resena.setFecha(reserva.getFechaSalida().atTime(12, 0));
        resenaRepo.save(resena);
    }

    private BigDecimal dinero(Double valor) {
        return BigDecimal.valueOf(valor == null ? 0 : valor).setScale(2, RoundingMode.HALF_UP);
    }
}
