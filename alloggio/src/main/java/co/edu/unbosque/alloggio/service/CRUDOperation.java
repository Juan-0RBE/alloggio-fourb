package co.edu.unbosque.alloggio.service;

import java.util.List;

/**
 * Interfaz para operaciones CRUD básicas.
 * 
 * @param <D>
 */
public interface CRUDOperation<D> {

    /**
     * Crea una nueva entidad en la base de datos.
     * 
     * @param newData
     * @return
     */
    public int create(D newData);

    /**
     * Obtiene todas las entidades de la base de datos.
     * 
     * @return
     */
    public List<D> getAll();

    /**
     * Elimina una entidad por su ID.
     * 
     * @param id
     * @return
     */
    public int deleteById(Long id);

    /**
     * Actualiza una entidad por su ID.
     * 
     * @param id
     * @param newData
     * @return
     */
    public int updateById(Long id, D newData);

    /**
     * Cuenta el número de entidades en la base de datos.
     * 
     * @return
     */
    public Long count();

    /**
     * Verifica si una entidad existe por su ID.
     * 
     * @param id
     * @return
     */
    public boolean exist(Long id);

    /**
     * Obtiene una entidad por su ID.
     * 
     * @param id
     * @return
     */
    public D getById(Long id);

}
