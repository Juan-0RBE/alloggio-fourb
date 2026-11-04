package co.edu.unbosque.alloggio.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import co.edu.unbosque.alloggio.entity.ParametroTarifa;
import co.edu.unbosque.alloggio.repository.ParametroTarifaRepository;

/**
 * Crea al arrancar la aplicación la fila de parámetros de tarifa si la tabla
 * está vacía. Los valores son iniciales y se pueden cambiar en la base de datos
 * sin modificar el código (RN-09).
 */
@Component
public class ParametroTarifaInicializador implements CommandLineRunner {

    @Autowired
    private ParametroTarifaRepository parametroRepo;

    @Override
    public void run(String... args) {
        if (parametroRepo.count() == 0) {
            // 8 % de tarifa de servicio, 5 % de descuento desde 7 noches
            parametroRepo.save(new ParametroTarifa(null, 8.0, 5.0, 7));
        }
    }
}