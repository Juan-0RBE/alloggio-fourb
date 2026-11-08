package co.edu.unbosque.alloggio.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.entity.Reserva.EstadoReserva;
import co.edu.unbosque.alloggio.repository.ReservaRepository;

/**
 * Pasa a COMPLETADA las reservas CONFIRMADAS cuya fecha de salida ya llegó.
 * Se ejecuta todos los días a las 00:05 y también una vez al arrancar
 * el aplicativo.
 */
@Service
public class ReservaEstadoService {

    private static final Logger log = LoggerFactory.getLogger(ReservaEstadoService.class);

    @Autowired
    private ReservaRepository reservaRepo;

    public ReservaEstadoService() {
    }

    /**
     * Completa las reservas cuya estadía terminó.
     */
    @Scheduled(cron = "0 5 0 * * *", zone = "America/Bogota")
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void completarReservasVencidas() {
        int cantidad = reservaRepo.actualizarEstadoPorFechaSalida(EstadoReserva.CONFIRMADA, EstadoReserva.COMPLETADA,
                LocalDate.now());
        if (cantidad > 0) {
            log.info("Reservas pasadas a COMPLETADA: {}", cantidad);
        }
    }
}