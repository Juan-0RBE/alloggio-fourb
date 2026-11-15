package co.edu.unbosque.alloggio.testutil;

import static co.edu.unbosque.alloggio.testutil.UsuarioFixtures.usuarioActivo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import co.edu.unbosque.alloggio.dto.CotizacionDTO;
import co.edu.unbosque.alloggio.entity.Cotizacion;
import co.edu.unbosque.alloggio.entity.ParametroTarifa;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Usuario;

/**
 * Datos de prueba reutilizables para cotizaciones.
 */
public final class CotizacionFixtures {

    public static final String NOMBRE_HUESPED = "mariahuesped";
    public static final Long ID_COTIZACION = 7L;

    private CotizacionFixtures() {
    }

    /**
     * Huésped que cotiza (distinto del anfitrión de los fixtures).
     */
    public static Usuario huesped() {
        Usuario u = usuarioActivo();
        u.setIdUsuario(3L);
        u.setNombreUsuario(NOMBRE_HUESPED);
        u.setCorreoUsuario("maria.huesped@correo.com");
        u.setDocumentoIdentidad("3333333333");
        return u;
    }

    /**
     * Solicitud de cotización como la envía el huésped.
     */
    public static CotizacionDTO solicitud(Long idPublicacion, LocalDate llegada, LocalDate salida) {
        CotizacionDTO dto = new CotizacionDTO();
        dto.setIdPublicacion(idPublicacion);
        dto.setFechaLlegada(llegada);
        dto.setFechaSalida(salida);
        return dto;
    }

    /**
     * Tarifas de la plataforma: % de servicio, % de descuento y noches mínimas.
     */
    public static ParametroTarifa parametroTarifa(double porcentajeServicio, double porcentajeDescuento,
            int nochesMinimasDescuento) {
        ParametroTarifa p = new ParametroTarifa();
        p.setIdParametroTarifa(1L);
        p.setPorcentajeServicio(porcentajeServicio);
        p.setPorcentajeDescuento(porcentajeDescuento);
        p.setNochesMinimasDescuento(nochesMinimasDescuento);
        return p;
    }

    /**
     * Cotización vigente de 3 noches sobre la publicación de los fixtures (250.000
     * por noche, limpieza 40.000, servicio 10 %, sin descuento): total 865.000,00.
     */
    public static Cotizacion cotizacionVigente(Usuario huesped, Publicacion publicacion) {
        Cotizacion c = new Cotizacion();
        c.setIdCotizacion(ID_COTIZACION);
        c.setHuesped(huesped);
        c.setPublicacion(publicacion);
        c.setFechaLlegada(LocalDate.now().plusDays(10));
        c.setFechaSalida(LocalDate.now().plusDays(13));
        c.setNoches(3);
        c.setPrecioBase(new BigDecimal("750000.00"));
        c.setLimpieza(new BigDecimal("40000.00"));
        c.setServicio(new BigDecimal("75000.00"));
        c.setDescuento(new BigDecimal("0.00"));
        c.setTotal(new BigDecimal("865000.00"));
        c.setVigencia(LocalDateTime.now().plusMinutes(20));
        return c;
    }
}