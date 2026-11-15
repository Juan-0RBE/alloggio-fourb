package co.edu.unbosque.alloggio.testutil;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class DashboardFixtures {

    public static List<Object[]> contarPorRolMock() {
        List<Object[]> resultado = new java.util.ArrayList<>();

        resultado.add(new Object[] { "USUARIO", 5L });
        resultado.add(new Object[] { "ADMINISTRADOR", 3L });

        return resultado;
    }

    public static List<Object[]> contarPorEstadoCuentaMock() {
        List<Object[]> resultado = new java.util.ArrayList<>();

        resultado.add(new Object[] { "ACTIVA", 7L });
        resultado.add(new Object[] { "RESTRINGIDA", 1L });

        return resultado;
    }

    public static List<Object[]> contarPorEstadoPublicacionMock() {
        List<Object[]> resultado = new java.util.ArrayList<>();

        resultado.add(new Object[] { "ACTIVA", 4L });
        resultado.add(new Object[] { "PAUSADA", 1L });

        return resultado;
    }

    public static List<Object[]> contarPorEstadoReservaMock() {
        List<Object[]> resultado = new java.util.ArrayList<>();

        resultado.add(new Object[] { "CONFIRMADA", 2L });
        resultado.add(new Object[] { "COMPLETADA", 1L });
        resultado.add(new Object[] { "CANCELADA", 1L });

        return resultado;
    }

    public static List<Object[]> ciudadesDemandaMock() {
        List<Object[]> resultado = new java.util.ArrayList<>();

        resultado.add(new Object[] { "Bogotá", 3L, new BigDecimal("450000.00") });

        return resultado;
    }
}