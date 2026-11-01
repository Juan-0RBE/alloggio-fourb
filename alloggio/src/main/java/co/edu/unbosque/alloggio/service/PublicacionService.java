package co.edu.unbosque.alloggio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.dto.ImagenAlojamientoDTO;
import co.edu.unbosque.alloggio.dto.PublicacionDTO;
import co.edu.unbosque.alloggio.dto.ReglaAlojamientoDTO;
import co.edu.unbosque.alloggio.dto.ServicioAlojamientoDTO;
import co.edu.unbosque.alloggio.entity.ImagenAlojamiento;
import co.edu.unbosque.alloggio.entity.PoliticaCancelacion;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.ReglaAlojamiento;
import co.edu.unbosque.alloggio.entity.ServicioAlojamiento;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.ImagenAlojamientoRepository;
import co.edu.unbosque.alloggio.repository.PoliticaCancelacionRepository;
import co.edu.unbosque.alloggio.repository.PublicacionRepository;
import co.edu.unbosque.alloggio.repository.ReglaAlojamientoRepository;
import co.edu.unbosque.alloggio.repository.ServicioAlojamientoRepository;
import co.edu.unbosque.alloggio.repository.UsuarioRepository;

/**
 * Servicio para manejar la lógica de negocio de las publicaciones de los
 * anfitriones.
 */
@Service
public class PublicacionService {

    @Autowired
    private PublicacionRepository publicacionRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PoliticaCancelacionRepository politicaRepo;

    @Autowired
    private ServicioAlojamientoRepository servicioRepo;

    @Autowired
    private ReglaAlojamientoRepository reglaRepo;

    @Autowired
    private ImagenAlojamientoRepository imagenRepo;

    @Autowired
    private ModelMapper modelMapper;

    public PublicacionService() {
    }

    /**
     * Crea una nueva publicación para un anfitrión.
     * 
     * @param dto Datos de la publicación
     * @return 0 si es exitoso, 1 si el anfitrión no existe, 2 si la política de
     *         cancelación no existe
     */
    @Transactional
    public int crearPublicacion(PublicacionDTO dto) {
        Optional<Usuario> anfitrionOpt = usuarioRepo.findById(dto.getIdAnfitrion());
        if (!anfitrionOpt.isPresent()) {
            return 1; // El anfitrión no existe
        }

        Optional<PoliticaCancelacion> politicaOpt = politicaRepo.findById(dto.getIdPoliticaCancelacion());
        if (!politicaOpt.isPresent()) {
            return 2; // La política de cancelación no existe
        }

        Publicacion publicacion = modelMapper.map(dto, Publicacion.class);
        publicacion.setAnfitrion(anfitrionOpt.get());
        publicacion.setPoliticaCancelacion(politicaOpt.get());
        publicacion.setEstado(EstadoPublicacion.BORRADOR); // Por defecto se crea como borrador

        publicacionRepo.save(publicacion);
        return 0; // Publicación creada exitosamente
    }

    /**
     * Obtiene todas las publicaciones registradas en el sistema.
     * 
     * @return Lista de PublicacionDTO
     */
    public List<PublicacionDTO> obtenerTodas() {
        List<Publicacion> publicaciones = publicacionRepo.findAll();
        List<PublicacionDTO> dtoList = new ArrayList<>();

        for (Publicacion pub : publicaciones) {
            PublicacionDTO dto = modelMapper.map(pub, PublicacionDTO.class);
            dto.setIdAnfitrion(pub.getAnfitrion().getIdUsuario());
            dto.setIdPoliticaCancelacion(pub.getPoliticaCancelacion().getIdPolitica());
            dtoList.add(dto);
        }
        return dtoList;
    }

    /**
     * Obtiene las publicaciones asociadas a un anfitrión específico.
     * 
     * @param idAnfitrion ID del anfitrión
     * @return Lista de publicaciones del anfitrión
     */
    public List<PublicacionDTO> obtenerPorAnfitrion(Long idAnfitrion) {
        List<Publicacion> publicaciones = publicacionRepo.findByAnfitrionIdUsuario(idAnfitrion);
        List<PublicacionDTO> dtoList = new ArrayList<>();

        for (Publicacion pub : publicaciones) {
            PublicacionDTO dto = modelMapper.map(pub, PublicacionDTO.class);
            dto.setIdAnfitrion(pub.getAnfitrion().getIdUsuario());
            dto.setIdPoliticaCancelacion(pub.getPoliticaCancelacion().getIdPolitica());
            dtoList.add(dto);
        }
        return dtoList;
    }

    /**
     * Obtiene una publicación por su ID.
     * 
     * @param id ID de la publicación
     * @return El DTO de la publicación si se encuentra, o null
     */
    public PublicacionDTO obtenerPorId(Long id) {
        Optional<Publicacion> found = publicacionRepo.findById(id);
        if (found.isPresent()) {
            Publicacion pub = found.get();
            PublicacionDTO dto = modelMapper.map(pub, PublicacionDTO.class);
            dto.setIdAnfitrion(pub.getAnfitrion().getIdUsuario());
            dto.setIdPoliticaCancelacion(pub.getPoliticaCancelacion().getIdPolitica());
            return dto;
        }
        return null;
    }

    /**
     * Actualiza una publicación existente.
     * 
     * @param id  ID de la publicación a actualizar
     * @param dto Nuevos datos
     * @return 0 si es exitoso, 1 si no se encuentra la publicación, 2 si la
     *         política no existe
     */
    @Transactional
    public int actualizarPublicacion(Long id, PublicacionDTO dto) {
        Optional<Publicacion> found = publicacionRepo.findById(id);
        if (!found.isPresent()) {
            return 1; // Publicación no encontrada
        }

        Optional<PoliticaCancelacion> politicaOpt = politicaRepo.findById(dto.getIdPoliticaCancelacion());
        if (!politicaOpt.isPresent()) {
            return 2; // Política de cancelación no encontrada
        }

        Publicacion pub = found.get();
        pub.setTitulo(dto.getTitulo());
        pub.setDescripcion(dto.getDescripcion());
        pub.setUbicacion(dto.getUbicacion());
        pub.setCiudad(dto.getCiudad());
        pub.setTipo(dto.getTipo());
        pub.setCapacidad(dto.getCapacidad());
        pub.setPrecioNoche(dto.getPrecioNoche());
        pub.setPoliticaCancelacion(politicaOpt.get());

        publicacionRepo.save(pub);
        return 0; // Actualizado exitosamente
    }

    /**
     * Elimina una publicación por su ID.
     * 
     * @param id ID de la publicación
     * @return 0 si es exitoso, 1 si no se encuentra
     */
    @Transactional
    public int eliminarPublicacion(Long id) {
        Optional<Publicacion> found = publicacionRepo.findById(id);
        if (found.isPresent()) {
            publicacionRepo.delete(found.get());
            return 0;
        }
        return 1; // No encontrada
    }

    // Gestion de servicios.

    /**
     * Agrega un servicio a una publicación existente.
     * 
     * @param idPublicacion
     * @param dto
     * @return
     */
    @Transactional
    public int agregarServicioAPublicacion(Long idPublicacion, ServicioAlojamientoDTO dto) {
        Optional<Publicacion> pubOpt = publicacionRepo.findById(idPublicacion);
        if (!pubOpt.isPresent()) {
            return 1; // Publicación no encontrada
        }
        ServicioAlojamiento servicio = modelMapper.map(dto, ServicioAlojamiento.class);
        servicio.setPublicacion(pubOpt.get()); // Asocia la llave foránea
        servicioRepo.save(servicio);
        return 0;
    }

    /**
     * Elimina un servicio por su ID.
     * 
     * @param idServicio
     * @return
     */
    @Transactional
    public int eliminarServicio(Long idServicio) {
        if (servicioRepo.existsById(idServicio)) {
            servicioRepo.deleteById(idServicio);
            return 0;
        }
        return 1; // No encontrado
    }

    /**
     * Obtiene todos los servicios asociados a una publicación específica.
     * 
     * @param idPublicacion
     * @return
     */
    @Transactional(readOnly = true)
    public List<ServicioAlojamientoDTO> obtenerServiciosPorPublicacion(Long idPublicacion) {
        if (!publicacionRepo.existsById(idPublicacion)) {
            return null; // Publicación no existe
        }
        List<ServicioAlojamiento> entidades = servicioRepo.findAll().stream()
                .filter(s -> s.getPublicacion() != null && s.getPublicacion().getIdPublicacion().equals(idPublicacion))
                .toList();

        List<ServicioAlojamientoDTO> dtos = new ArrayList<>();
        for (ServicioAlojamiento entidad : entidades) {
            dtos.add(modelMapper.map(entidad, ServicioAlojamientoDTO.class));
        }
        return dtos;
    }

    // Gestion de reglas

    /**
     * Agrega una regla a una publicación existente.
     * 
     * @param idPublicacion
     * @param dto
     * @return
     */
    @Transactional
    public int agregarReglaAPublicacion(Long idPublicacion, ReglaAlojamientoDTO dto) {
        Optional<Publicacion> pubOpt = publicacionRepo.findById(idPublicacion);
        if (!pubOpt.isPresent()) {
            return 1;
        }
        ReglaAlojamiento regla = modelMapper.map(dto, ReglaAlojamiento.class);
        regla.setPublicacion(pubOpt.get());
        reglaRepo.save(regla);
        return 0;
    }

    /**
     * Elimina una regla por su ID.
     * 
     * @param idRegla
     * @return
     */
    @Transactional
    public int eliminarRegla(Long idRegla) {
        if (reglaRepo.existsById(idRegla)) {
            reglaRepo.deleteById(idRegla);
            return 0;
        }
        return 1;
    }

    /**
     * Obtiene todas las reglas asociadas a una publicación específica.
     * 
     * @param idPublicacion
     * @return
     */
    @Transactional(readOnly = true)
    public List<ReglaAlojamientoDTO> obtenerReglasPorPublicacion(Long idPublicacion) {
        if (!publicacionRepo.existsById(idPublicacion)) {
            return null;
        }
        List<ReglaAlojamiento> entidades = reglaRepo.findAll().stream()
                .filter(r -> r.getPublicacion() != null && r.getPublicacion().getIdPublicacion().equals(idPublicacion))
                .toList();

        List<ReglaAlojamientoDTO> dtos = new ArrayList<>();
        for (ReglaAlojamiento entidad : entidades) {
            dtos.add(modelMapper.map(entidad, ReglaAlojamientoDTO.class));
        }
        return dtos;
    }

    // Gestion de imagenes

    /**
     * Agrega una imagen a una publicación existente.
     * 
     * @param idPublicacion
     * @param dto
     * @return
     */
    @Transactional
    public int agregarImagenAPublicacion(Long idPublicacion, ImagenAlojamientoDTO dto) {
        Optional<Publicacion> pubOpt = publicacionRepo.findById(idPublicacion);
        if (!pubOpt.isPresent()) {
            return 1;
        }
        ImagenAlojamiento imagen = modelMapper.map(dto, ImagenAlojamiento.class);
        imagen.setPublicacion(pubOpt.get());
        imagenRepo.save(imagen);
        return 0;
    }

    /**
     * Elimina una imagen por su ID.
     * 
     * @param idImagen
     * @return
     */
    @Transactional
    public int eliminarImagen(Long idImagen) {
        if (imagenRepo.existsById(idImagen)) {
            imagenRepo.deleteById(idImagen);
            return 0;
        }
        return 1;
    }

    /**
     * Obtiene todas las imágenes asociadas a una publicación específica, ordenadas
     * por el campo "orden".
     * 
     * @param idPublicacion
     * @return
     */
    @Transactional(readOnly = true)
    public List<ImagenAlojamientoDTO> obtenerImagenesPorPublicacion(Long idPublicacion) {
        if (!publicacionRepo.existsById(idPublicacion)) {
            return null;
        }
        // Usamos la consulta ordenada que ya tienes en ImagenAlojamientoRepository
        List<ImagenAlojamiento> entidades = imagenRepo.findByPublicacionIdPublicacionOrderByOrdenAsc(idPublicacion);

        List<ImagenAlojamientoDTO> dtos = new ArrayList<>();
        for (ImagenAlojamiento entidad : entidades) {
            dtos.add(modelMapper.map(entidad, ImagenAlojamientoDTO.class));
        }
        return dtos;
    }
}