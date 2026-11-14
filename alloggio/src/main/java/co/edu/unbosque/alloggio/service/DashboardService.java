package co.edu.unbosque.alloggio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.dto.CiudadDemandaDTO;
import co.edu.unbosque.alloggio.dto.DashboardDTO;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.Reserva.EstadoReserva;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.PublicacionRepository;
import co.edu.unbosque.alloggio.repository.ReservaRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;

/**
 * Servicio del dashboard del administrador.
 */
@Service
public class DashboardService {

    /** Cantidad de ciudades que se muestran en "mayor demanda". */
    private static final int TOP_CIUDADES = 5;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PublicacionRepository publicacionRepo;

    @Autowired
    private ReservaRepository reservaRepo;

    /**
     * Calcula los indicadores básicos del dashboard sobre todo el histórico. Si no
     * hay datos, devuelve contadores en cero, ingresos en cero y ciudades vacías.
     * 
     * @return indicadores consolidados
     */
    @Transactional(readOnly = true)
    public DashboardDTO obtenerIndicadores() {
        DashboardDTO dto = new DashboardDTO();
        dto.setFechaGeneracion(LocalDateTime.now());

        // Usuarios
        dto.setTotalUsuarios(usuarioRepo.count());
        Map<String, Long> porRol = new LinkedHashMap<>();
        for (Usuario.Role rol : Usuario.Role.values()) {
            porRol.put(rol.name(), 0L);
        }
        dto.setUsuariosPorRol(llenarMapa(porRol, usuarioRepo.contarPorRol()));

        Map<String, Long> porEstadoCuenta = new LinkedHashMap<>();
        porEstadoCuenta.put(Usuario.ESTADO_ACTIVA, 0L);
        porEstadoCuenta.put(Usuario.ESTADO_RESTRINGIDA, 0L);
        dto.setUsuariosPorEstadoCuenta(llenarMapa(porEstadoCuenta, usuarioRepo.contarPorEstadoCuenta()));

        // Publicaciones
        Map<String, Long> porEstadoPublicacion = new LinkedHashMap<>();
        for (EstadoPublicacion estado : EstadoPublicacion.values()) {
            porEstadoPublicacion.put(estado.name(), 0L);
        }
        llenarMapa(porEstadoPublicacion, publicacionRepo.contarPorEstado());
        dto.setPublicacionesPorEstado(porEstadoPublicacion);
        dto.setTotalPublicaciones(sumar(porEstadoPublicacion));
        dto.setPublicacionesActivas(porEstadoPublicacion.get(EstadoPublicacion.ACTIVA.name()));

        // Reservas por estado
        Map<String, Long> porEstadoReserva = new LinkedHashMap<>();
        for (EstadoReserva estado : EstadoReserva.values()) {
            porEstadoReserva.put(estado.name(), 0L);
        }
        llenarMapa(porEstadoReserva, reservaRepo.contarPorEstado());
        dto.setReservasPorEstado(porEstadoReserva);
        dto.setTotalReservas(sumar(porEstadoReserva));

        // Ingresos estimados: reservas CONFIRMADA y COMPLETADA
        BigDecimal ingresos = reservaRepo
                .sumarTotalPorEstados(List.of(EstadoReserva.CONFIRMADA, EstadoReserva.COMPLETADA));
        dto.setIngresosEstimados(
                ingresos == null ? BigDecimal.ZERO.setScale(2) : ingresos.setScale(2, RoundingMode.HALF_UP));

        // Ciudades con mayor demanda (reservas no canceladas)
        List<CiudadDemandaDTO> ciudades = new ArrayList<>();
        for (Object[] fila : reservaRepo.ciudadesConMayorDemanda(EstadoReserva.CANCELADA,
                PageRequest.of(0, TOP_CIUDADES))) {
            BigDecimal total = fila[2] == null ? BigDecimal.ZERO : (BigDecimal) fila[2];
            ciudades.add(new CiudadDemandaDTO((String) fila[0], ((Number) fila[1]).longValue(),
                    total.setScale(2, RoundingMode.HALF_UP)));
        }
        dto.setCiudadesDemanda(ciudades);

        dto.setSinDatos(dto.getTotalPublicaciones() == 0 && dto.getTotalReservas() == 0);
        return dto;
    }

    /**
     * Copia al mapa los conteos de una consulta agrupada ([clave, cantidad]).
     * Las claves que ya estaban en el mapa conservan su orden.
     */
    private Map<String, Long> llenarMapa(Map<String, Long> mapa, List<Object[]> filas) {
        for (Object[] fila : filas) {
            mapa.put(String.valueOf(fila[0]), ((Number) fila[1]).longValue());
        }
        return mapa;
    }

    private long sumar(Map<String, Long> mapa) {
        long total = 0;
        for (long valor : mapa.values()) {
            total += valor;
        }
        return total;
    }
}