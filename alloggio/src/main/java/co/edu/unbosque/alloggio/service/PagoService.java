package co.edu.unbosque.alloggio.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.dto.PagoDTO;
import co.edu.unbosque.alloggio.dto.ResultadoPasarelaDTO;
import co.edu.unbosque.alloggio.entity.Cotizacion;
import co.edu.unbosque.alloggio.entity.Pago;
import co.edu.unbosque.alloggio.entity.Pago.EstadoPago;
import co.edu.unbosque.alloggio.entity.Reserva;
import co.edu.unbosque.alloggio.util.exception.PasarelaException;
import co.edu.unbosque.alloggio.repository.CotizacionRepository;
import co.edu.unbosque.alloggio.repository.PagoRepository;

/**
 * Servicio interno de pagos. El pago no es un endpoint independiente:
 * lo usa la creación de la reserva.
 */
@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    /** Moneda en la que cobra la plataforma. */
    private static final String MONEDA = "COP";

    @Autowired
    private PagoRepository pagoRepo;

    @Autowired
    private CotizacionRepository cotizacionRepo;

    @Autowired
    private PasarelaPagoService pasarela;

    public PagoService() {
    }

    /**
     * Cobra una cotización vigente a través de la pasarela y registra el intento
     * (APROBADO o RECHAZADO). Si el cobro se completa, el id del pago queda en
     * dto.idPago. No crea reservas ni bloquea fechas.
     * 
     * @param nombreUsuario
     * @param idCotizacion
     * @param dto
     * @return
     */
    @Transactional
    public int cobrar(String nombreUsuario, Long idCotizacion, PagoDTO dto) {
        if (idCotizacion == null) {
            return 1;
        }
        Optional<Cotizacion> cotOpt = cotizacionRepo.findById(idCotizacion);
        if (!cotOpt.isPresent()) {
            return 1; // Cotización no encontrada
        }
        Cotizacion cotizacion = cotOpt.get();

        if (!cotizacion.getHuesped().getNombreUsuario().equals(nombreUsuario)) {
            return 2; // La cotización no pertenece al usuario
        }
        if (LocalDateTime.now().isAfter(cotizacion.getVigencia())) {
            return 3; // La cotización venció
        }
        if (pagoRepo.existsByCotizacionIdCotizacionAndEstado(idCotizacion, EstadoPago.APROBADO)) {
            return 4; // Ya fue pagada
        }

        // Solo se aceptan identificadores de la pasarela, nunca números de tarjeta
        String metodo = dto.getMetodoPago();
        if (metodo == null || !metodo.matches("^pm_[A-Za-z0-9_]{3,100}$")) {
            return 5;
        }
        String clave = dto.getClaveIdempotencia();
        if (clave == null || clave.isBlank() || clave.length() > 255) {
            return 6;
        }

        ResultadoPasarelaDTO resultado;
        try {
            resultado = pasarela.cobrar(cotizacion.getTotal(), MONEDA, metodo, "COT-" + idCotizacion, clave);
        } catch (PasarelaException e) {
            // No se sabe si hubo cobro: no se registra nada y se informa la falla
            log.error("Falla al cobrar la cotización {}: {}", idCotizacion, e.getMessage());
            return 7;
        }

        Pago pago = new Pago();
        pago.setCotizacion(cotizacion);
        pago.setMonto(cotizacion.getTotal());
        pago.setMoneda(MONEDA);
        pago.setFecha(LocalDateTime.now());
        pago.setReferenciaPasarela(resultado.getReferencia());
        if (resultado.isAprobado()) {
            pago.setEstado(EstadoPago.APROBADO);
            pago.setMarcaTarjeta(resultado.getMarcaTarjeta());
            pago.setUltimosCuatro(resultado.getUltimosCuatro());
        } else {
            pago.setEstado(EstadoPago.RECHAZADO);
            pago.setCodigoRechazo(resultado.getCodigoRechazo());
            pago.setMotivoRechazo(resultado.getMotivoRechazo());
        }
        Pago guardado = pagoRepo.save(pago);
        dto.setIdPago(guardado.getIdPago());

        return resultado.isAprobado() ? 0 : 8;
    }

    /**
     * Obtiene un pago por su ID. Nunca incluye datos sensibles: solo marca y
     * últimos cuatro dígitos.
     * 
     * @param idPago
     * @return
     */
    @Transactional(readOnly = true)
    public PagoDTO obtenerPorId(Long idPago) {
        Optional<Pago> found = pagoRepo.findById(idPago);
        if (!found.isPresent()) {
            return null;
        }
        Pago pago = found.get();

        PagoDTO dto = new PagoDTO();
        dto.setIdPago(pago.getIdPago());
        dto.setIdCotizacion(pago.getCotizacion().getIdCotizacion());
        dto.setMonto(pago.getMonto());
        dto.setMoneda(pago.getMoneda());
        dto.setEstado(pago.getEstado().name());
        dto.setReferenciaPasarela(pago.getReferenciaPasarela());
        dto.setFecha(pago.getFecha());
        dto.setMarcaTarjeta(pago.getMarcaTarjeta());
        dto.setUltimosCuatro(pago.getUltimosCuatro());
        dto.setMotivoRechazo(pago.getMotivoRechazo());
        return dto;
    }

    /**
     * Enlaza el pago aprobado con su reserva.
     * 
     * @param idPago
     * @param reserva
     */
    @Transactional
    public void vincularReserva(Long idPago, Reserva reserva) {
        pagoRepo.findById(idPago).ifPresent(p -> {
            p.setReserva(reserva);
            pagoRepo.save(p);
        });
    }
}