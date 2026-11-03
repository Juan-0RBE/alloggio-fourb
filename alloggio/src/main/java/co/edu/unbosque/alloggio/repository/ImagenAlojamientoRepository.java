package co.edu.unbosque.alloggio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.alloggio.entity.ImagenAlojamiento;

public interface ImagenAlojamientoRepository extends JpaRepository<ImagenAlojamiento, Long> {

    /**
     * Encuentra una imagen de alojamiento por su ID.
     * 
     * @param idImagen
     * @return
     */
    public Optional<ImagenAlojamiento> findByIdImagen(Long idImagen);

    /**
     * Encuentra todas las imágenes de alojamiento asociadas a un alojamiento
     * específico, ordenadas por el campo "orden" de manera ascendente.
     * 
     * @param alojamientoId
     * @return
     */
    public List<ImagenAlojamiento> findByPublicacionIdPublicacionOrderByOrdenAsc(Long idPublicacion);

    /**
     * Elimina una imagen de alojamiento por su ID.
     * 
     * @param idImagen
     */
    public void deleteByIdImagen(Long idImagen);

    /**
     * 
     * 
     * @param idPublicacion
     * @return
     */
    Optional<ImagenAlojamiento> findFirstByPublicacionIdPublicacionOrderByOrdenAsc(Long idPublicacion);

}
