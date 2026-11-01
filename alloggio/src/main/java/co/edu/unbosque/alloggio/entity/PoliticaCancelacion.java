package co.edu.unbosque.alloggio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "POLITICA_CANCELACION")
public class PoliticaCancelacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPolitica;

    @Column(name = "Nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "Porcentaje_Devolucion", nullable = false)
    private Double porcentajeDevolucion;

    @Column(name = "Condiciones", length = 500, nullable = false)
    private String condiciones;

    @Column(name = "Plazo_Cancelacion", nullable = false)
    private Integer plazoCancelacion;

    PoliticaCancelacion() {
    }

    public PoliticaCancelacion(Long idPolitica, String nombre, Double porcentajeDevolucion, String condiciones,
            Integer plazoCancelacion) {
        this.idPolitica = idPolitica;
        this.nombre = nombre;
        this.porcentajeDevolucion = porcentajeDevolucion;
        this.condiciones = condiciones;
        this.plazoCancelacion = plazoCancelacion;
    }

    public Long getIdPolitica() {
        return idPolitica;
    }

    public void setIdPolitica(Long idPolitica) {
        this.idPolitica = idPolitica;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPorcentajeDevolucion() {
        return porcentajeDevolucion;
    }

    public void setPorcentajeDevolucion(Double porcentajeDevolucion) {
        this.porcentajeDevolucion = porcentajeDevolucion;
    }

    public String getCondiciones() {
        return condiciones;
    }

    public void setCondiciones(String condiciones) {
        this.condiciones = condiciones;
    }

    public Integer getPlazoCancelacion() {
        return plazoCancelacion;
    }

    public void setPlazoCancelacion(Integer plazoCancelacion) {
        this.plazoCancelacion = plazoCancelacion;
    }

}
