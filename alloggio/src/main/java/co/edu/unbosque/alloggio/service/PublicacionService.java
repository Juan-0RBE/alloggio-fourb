package co.edu.unbosque.alloggio.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.dto.ImagenAlojamientoDTO;
import co.edu.unbosque.alloggio.dto.ModeracionPublicacionDTO;
import co.edu.unbosque.alloggio.dto.PublicacionDTO;
import co.edu.unbosque.alloggio.dto.ReglaAlojamientoDTO;
import co.edu.unbosque.alloggio.dto.ReservaModeracionDTO;
import co.edu.unbosque.alloggio.dto.ServicioAlojamientoDTO;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.Accion;
import co.edu.unbosque.alloggio.entity.EventoAuditoria.TipoEntidad;
import co.edu.unbosque.alloggio.entity.ImagenAlojamiento;
import co.edu.unbosque.alloggio.entity.PoliticaCancelacion;
import co.edu.unbosque.alloggio.entity.Publicacion;
import co.edu.unbosque.alloggio.entity.Publicacion.EstadoPublicacion;
import co.edu.unbosque.alloggio.entity.ReglaAlojamiento;
import co.edu.unbosque.alloggio.entity.Reserva;
import co.edu.unbosque.alloggio.entity.Reserva.EstadoReserva;
import co.edu.unbosque.alloggio.entity.ServicioAlojamiento;
import co.edu.unbosque.alloggio.entity.Usuario;
import co.edu.unbosque.alloggio.repository.ImagenAlojamientoRepository;
import co.edu.unbosque.alloggio.repository.PoliticaCancelacionRepository;
import co.edu.unbosque.alloggio.repository.PublicacionRepository;
import co.edu.unbosque.alloggio.repository.ReglaAlojamientoRepository;
import co.edu.unbosque.alloggio.repository.ReservaRepository;
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
    private EventoAuditoriaService auditoriaService;

    @Autowired
    private ModelMapper modelMapper;

    private static final int LONGITUD_MAX_MOTIVO = 500;

    @Autowired
    private ReservaRepository reservaRepo;

    public PublicacionService() {
    }

    /**
     * Crea una nueva publicación para el anfitrión autenticado. El anfitrión se
     * toma de la sesión, no del cuerpo de la petición.
     * 
     * @param nombreUsuario
     * @param dto
     * @return 0 si es exitoso, 1 si el usuario no existe, 2 si la política de
     *         cancelación no existe, 3 si faltan campos obligatorios o son
     *         inválidos (ver validarDatos)
     */
    @Transactional
    public int crearPublicacion(String nombreUsuario, PublicacionDTO dto) {
        Optional<Usuario> anfitrionOpt = usuarioRepo.findByNombreUsuario(nombreUsuario);
        if (!anfitrionOpt.isPresent()) {
            return 1; // El anfitrión no existe
        }

        if (!validarDatos(dto).isEmpty()) {
            return 3; // Faltan campos obligatorios o son inválidos
        }

        Optional<PoliticaCancelacion> politicaOpt = politicaRepo.findById(dto.getIdPoliticaCancelacion());
        if (!politicaOpt.isPresent()) {
            return 2; // La política de cancelación no existe
        }

        Publicacion publicacion = modelMapper.map(dto, Publicacion.class);
        publicacion.setIdPublicacion(null); // Evita que un id enviado en el cuerpo sobrescriba otra publicación
        publicacion.setAnfitrion(anfitrionOpt.get());
        publicacion.setPoliticaCancelacion(politicaOpt.get());
        publicacion.setEstado(EstadoPublicacion.BORRADOR); // Por defecto se crea como borrador

        Publicacion guardada = publicacionRepo.save(publicacion);

        auditoriaService.registrarExito(nombreUsuario, Accion.CREAR_PUBLICACION, TipoEntidad.PUBLICACION,
                guardada.getIdPublicacion(), "El anfitrión creó la publicación: " + guardada.getTitulo());

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
     * Obtiene todas las publicaciones ACTIVAS registradas en el sistema.
     * 
     * @return Lista de PublicacionDTO
     */
    public List<PublicacionDTO> obtenerActivas() {
        List<Publicacion> publicaciones = publicacionRepo.findByEstado(Publicacion.EstadoPublicacion.ACTIVA);
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
     * @param idAnfitrion
     * @return
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
     * @param id
     * @return
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
     * Obtiene las publicaciones del anfitrión autenticado (CU-011). El anfitrión se
     * toma de la sesión, no de un parámetro de la petición.
     *
     * @param nombreUsuario
     * @return
     */
    @Transactional(readOnly = true)
    public List<PublicacionDTO> obtenerPropias(String nombreUsuario) {
        Optional<Usuario> anfitrion = usuarioRepo.findByNombreUsuario(nombreUsuario);
        if (!anfitrion.isPresent()) {
            return new ArrayList<>();
        }
        return obtenerPorAnfitrion(anfitrion.get().getIdUsuario());
    }

    /**
     * Actualiza una publicación existente. Solo el propietario o un administrador
     * pueden hacerlo
     * 
     * @param nombreUsuario
     * @param id
     * @param dto
     * @return
     */
    @Transactional
    public int actualizarPublicacion(String nombreUsuario, Long id, PublicacionDTO dto) {
        Optional<Publicacion> found = publicacionRepo.findByIdParaActualizar(id);
        if (!found.isPresent()) {
            return 1; // Publicación no encontrada
        }

        Publicacion pub = found.get();
        if (!puedeModificar(pub, nombreUsuario)) {
            registrarIntentoAjeno(nombreUsuario, id, "Intento de editar una publicación ajena");
            return 3; // La publicación no pertenece al usuario
        }
        if (pub.getEstado() == EstadoPublicacion.BLOQUEADA) {
            return 5;
        }
        if (!validarDatos(dto).isEmpty()) {
            return 4; // Faltan campos obligatorios o son inválidos
        }

        Optional<PoliticaCancelacion> politicaOpt = politicaRepo.findById(dto.getIdPoliticaCancelacion());
        if (!politicaOpt.isPresent()) {
            return 2; // Política de cancelación no encontrada
        }

        pub.setTitulo(dto.getTitulo());
        pub.setDescripcion(dto.getDescripcion());
        pub.setUbicacion(dto.getUbicacion());
        pub.setCiudad(dto.getCiudad());
        pub.setTipo(dto.getTipo());
        pub.setCapacidad(dto.getCapacidad());
        pub.setPrecioNoche(dto.getPrecioNoche());
        pub.setTarifaLimpieza(dto.getTarifaLimpieza());
        pub.setPoliticaCancelacion(politicaOpt.get());

        publicacionRepo.save(pub);

        auditoriaService.registrarExito(nombreUsuario, Accion.ACTUALIZAR_PUBLICACION, TipoEntidad.PUBLICACION, id,
                "Se actualizó la publicación: " + pub.getTitulo());

        return 0; // Actualizado exitosamente
    }

    /**
     * Elimina una publicación por su ID.
     * 
     * @param nombreUsuario nombre del usuario autenticado
     * @param id            ID de la publicación
     * @return 0 si es exitoso, 1 si no se encuentra, 2 si el usuario no es
     *         propietario ni administrador
     */
    @Transactional
    public int eliminarPublicacion(String nombreUsuario, Long id) {
        Optional<Publicacion> found = publicacionRepo.findByIdParaActualizar(id);
        if (!found.isPresent()) {
            return 1; // No encontrada
        }

        Publicacion pub = found.get();
        if (!puedeModificar(pub, nombreUsuario)) {
            registrarIntentoAjeno(nombreUsuario, id, "Intento de eliminar una publicación ajena");
            return 2; // La publicación no pertenece al usuario
        }
        if (pub.getEstado() == EstadoPublicacion.BLOQUEADA) {
            return 3;
        }
        String titulo = pub.getTitulo();
        publicacionRepo.delete(pub);

        auditoriaService.registrarExito(nombreUsuario, Accion.ELIMINAR_PUBLICACION, TipoEntidad.PUBLICACION, id,
                "Se eliminó la publicación: " + titulo);

        return 0;
    }

    // Gestion de servicios.

    /**
     * Agrega un servicio a una publicación existente.
     * 
     * @param nombreUsuario nombre del usuario autenticado
     * @param idPublicacion
     * @param dto
     * @return 0 si es exitoso, 1 si la publicación no existe, 2 si el usuario no
     *         es propietario ni administrador
     */
    @Transactional
    public int agregarServicioAPublicacion(String nombreUsuario, Long idPublicacion, ServicioAlojamientoDTO dto) {
        Optional<Publicacion> pubOpt = publicacionRepo.findById(idPublicacion);
        if (!pubOpt.isPresent()) {
            return 1; // Publicación no encontrada
        }
        if (!puedeModificar(pubOpt.get(), nombreUsuario)) {
            registrarIntentoAjeno(nombreUsuario, idPublicacion,
                    "Intento de agregar un servicio a una publicación ajena");
            return 2; // La publicación no pertenece al usuario
        }
        ServicioAlojamiento servicio = modelMapper.map(dto, ServicioAlojamiento.class);
        servicio.setPublicacion(pubOpt.get()); // Asocia la llave foránea
        servicioRepo.save(servicio);
        return 0;
    }

    /**
     * Elimina un servicio por su ID.
     * 
     * @param nombreUsuario nombre del usuario autenticado
     * @param idServicio
     * @return 0 si es exitoso, 1 si no se encuentra, 2 si el usuario no es
     *         propietario ni administrador de la publicación
     */
    @Transactional
    public int eliminarServicio(String nombreUsuario, Long idServicio) {
        Optional<ServicioAlojamiento> servicioOpt = servicioRepo.findById(idServicio);
        if (!servicioOpt.isPresent()) {
            return 1; // No encontrado
        }
        Publicacion pub = servicioOpt.get().getPublicacion();
        if (!puedeModificar(pub, nombreUsuario)) {
            registrarIntentoAjeno(nombreUsuario, pub.getIdPublicacion(),
                    "Intento de eliminar un servicio de una publicación ajena");
            return 2;
        }
        servicioRepo.delete(servicioOpt.get());
        return 0;
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
     * @param nombreUsuario nombre del usuario autenticado
     * @param idPublicacion
     * @param dto
     * @return 0 si es exitoso, 1 si la publicación no existe, 2 si el usuario no
     *         es propietario ni administrador
     */
    @Transactional
    public int agregarReglaAPublicacion(String nombreUsuario, Long idPublicacion, ReglaAlojamientoDTO dto) {
        Optional<Publicacion> pubOpt = publicacionRepo.findById(idPublicacion);
        if (!pubOpt.isPresent()) {
            return 1;
        }
        if (!puedeModificar(pubOpt.get(), nombreUsuario)) {
            registrarIntentoAjeno(nombreUsuario, idPublicacion, "Intento de agregar una regla a una publicación ajena");
            return 2;
        }
        ReglaAlojamiento regla = modelMapper.map(dto, ReglaAlojamiento.class);
        regla.setPublicacion(pubOpt.get());
        reglaRepo.save(regla);
        return 0;
    }

    /**
     * Elimina una regla por su ID.
     * 
     * @param nombreUsuario nombre del usuario autenticado
     * @param idRegla
     * @return 0 si es exitoso, 1 si no se encuentra, 2 si el usuario no es
     *         propietario ni administrador de la publicación
     */
    @Transactional
    public int eliminarRegla(String nombreUsuario, Long idRegla) {
        Optional<ReglaAlojamiento> reglaOpt = reglaRepo.findById(idRegla);
        if (!reglaOpt.isPresent()) {
            return 1;
        }
        Publicacion pub = reglaOpt.get().getPublicacion();
        if (!puedeModificar(pub, nombreUsuario)) {
            registrarIntentoAjeno(nombreUsuario, pub.getIdPublicacion(),
                    "Intento de eliminar una regla de una publicación ajena");
            return 2;
        }
        reglaRepo.delete(reglaOpt.get());
        return 0;
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
     * @param nombreUsuario nombre del usuario autenticado
     * @param idPublicacion
     * @param dto
     * @return 0 si es exitoso, 1 si la publicación no existe, 2 si el usuario no
     *         es propietario ni administrador
     */
    @Transactional
    public int agregarImagenAPublicacion(String nombreUsuario, Long idPublicacion, ImagenAlojamientoDTO dto) {
        Optional<Publicacion> pubOpt = publicacionRepo.findById(idPublicacion);
        if (!pubOpt.isPresent()) {
            return 1;
        }
        if (!puedeModificar(pubOpt.get(), nombreUsuario)) {
            registrarIntentoAjeno(nombreUsuario, idPublicacion,
                    "Intento de agregar una imagen a una publicación ajena");
            return 2;
        }
        ImagenAlojamiento imagen = modelMapper.map(dto, ImagenAlojamiento.class);
        imagen.setPublicacion(pubOpt.get());
        imagenRepo.save(imagen);
        return 0;
    }

    /**
     * Elimina una imagen por su ID.
     * 
     * @param nombreUsuario nombre del usuario autenticado
     * @param idImagen
     * @return 0 si es exitoso, 1 si no se encuentra, 2 si el usuario no es
     *         propietario ni administrador de la publicación
     */
    @Transactional
    public int eliminarImagen(String nombreUsuario, Long idImagen) {
        Optional<ImagenAlojamiento> imagenOpt = imagenRepo.findById(idImagen);
        if (!imagenOpt.isPresent()) {
            return 1;
        }
        Publicacion pub = imagenOpt.get().getPublicacion();
        if (!puedeModificar(pub, nombreUsuario)) {
            registrarIntentoAjeno(nombreUsuario, pub.getIdPublicacion(),
                    "Intento de eliminar una imagen de una publicación ajena");
            return 2;
        }
        imagenRepo.delete(imagenOpt.get());
        return 0;
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
        List<ImagenAlojamiento> entidades = imagenRepo.findByPublicacionIdPublicacionOrderByOrdenAsc(idPublicacion);

        List<ImagenAlojamientoDTO> dtos = new ArrayList<>();
        for (ImagenAlojamiento entidad : entidades) {
            dtos.add(modelMapper.map(entidad, ImagenAlojamientoDTO.class));
        }
        return dtos;
    }

    // Gestión de activación de publicación

    /**
     * Activa una publicación, cambiando su estado a ACTIVA si cumple con los
     * requisitos.
     * 
     * @param nombreUsuario
     * @param idPublicacion
     * @return 0 si es exitoso, 1 si no existe, 2 si no pertenece al usuario, 3 si
     *         no está en borrador o pausada, 4 si no cumple la información mínima
     */
    @Transactional
    public int activarPublicacion(String nombreUsuario, Long idPublicacion) {
        Optional<Publicacion> pubOpt = publicacionRepo.findByIdParaActualizar(idPublicacion);
        if (!pubOpt.isPresent()) {
            return 1; // Publicación no encontrada
        }

        Publicacion pub = pubOpt.get();
        if (!esPropietario(pub, nombreUsuario)) {
            auditoriaService.registrarDenegado(nombreUsuario, Accion.ACTIVAR_PUBLICACION, TipoEntidad.PUBLICACION,
                    idPublicacion, "Intento de activar una publicación ajena");
            return 2; // La publicación no pertenece al usuario
        }
        if (pub.getEstado() != EstadoPublicacion.BORRADOR && pub.getEstado() != EstadoPublicacion.PAUSADA) {
            return 3; // Solo se activan publicaciones en borrador o pausadas
        }
        if (!calcularDatosFaltantes(pub).isEmpty()) {
            return 4; // No cumple la información mínima
        }

        pub.setEstado(EstadoPublicacion.ACTIVA);
        publicacionRepo.save(pub);

        auditoriaService.registrarExito(nombreUsuario, Accion.ACTIVAR_PUBLICACION, TipoEntidad.PUBLICACION,
                idPublicacion, "El anfitrión activó la publicación: " + pub.getTitulo());

        return 0; // Publicación activada exitosamente
    }

    /**
     * Pausa una publicación, cambiando su estado a PAUSADA si cumple con los
     * requisitos.
     * 
     * @param nombreUsuario
     * @param idPublicacion
     * @return 0 si es exitoso, 1 si no existe, 2 si no pertenece al usuario, 3 si
     *         no está activa
     */
    @Transactional
    public int pausarPublicacion(String nombreUsuario, Long idPublicacion) {
        Optional<Publicacion> pubOpt = publicacionRepo.findById(idPublicacion);
        if (!pubOpt.isPresent()) {
            return 1; // Publicación no encontrada
        }

        Publicacion pub = pubOpt.get();
        if (!esPropietario(pub, nombreUsuario)) {
            auditoriaService.registrarDenegado(nombreUsuario, Accion.PAUSAR_PUBLICACION, TipoEntidad.PUBLICACION,
                    idPublicacion, "Intento de pausar una publicación ajena");
            return 2; // La publicación no pertenece al usuario
        }
        if (pub.getEstado() != EstadoPublicacion.ACTIVA) {
            return 3; // Solo se pausan publicaciones activas
        }

        pub.setEstado(EstadoPublicacion.PAUSADA);
        publicacionRepo.save(pub);

        auditoriaService.registrarExito(nombreUsuario, Accion.PAUSAR_PUBLICACION, TipoEntidad.PUBLICACION,
                idPublicacion, "El anfitrión pausó la publicación: " + pub.getTitulo());

        return 0; // Publicación pausada exitosamente
    }

    /**
     * Indica si la publicación pertenece al usuario autenticado.
     * 
     * @param pub
     * @param nombreUsuario
     * @return
     */
    private boolean esPropietario(Publicacion pub, String nombreUsuario) {
        return pub.getAnfitrion().getNombreUsuario().equals(nombreUsuario);
    }

    /**
     * Indica si el usuario puede modificar la publicación: es su propietario o un
     * administrador.
     * 
     * @param pub
     * @param nombreUsuario
     * @return
     */
    private boolean puedeModificar(Publicacion pub, String nombreUsuario) {
        return esPropietario(pub, nombreUsuario);
    }

    /**
     * Indica si el usuario autenticado tiene rol de administrador.
     * 
     * @param nombreUsuario
     * @return
     */
    private boolean esAdministrador(String nombreUsuario) {
        Optional<Usuario> usuarioOpt = usuarioRepo.findByNombreUsuario(nombreUsuario);
        return usuarioOpt.isPresent() && usuarioOpt.get().getRole() == Usuario.Role.ADMINISTRADOR;
    }

    /**
     * Registra en auditoría un intento de modificar una publicación ajena.
     * 
     * @param nombreUsuario
     * @param idPublicacion
     * @param detalle
     */
    private void registrarIntentoAjeno(String nombreUsuario, Long idPublicacion, String detalle) {
        auditoriaService.registrarDenegado(nombreUsuario, Accion.ACTUALIZAR_PUBLICACION, TipoEntidad.PUBLICACION,
                idPublicacion, detalle);
    }

    /**
     * Valida los campos obligatorios al crear o actualizar una publicación: textos
     * básicos, tipo, capacidad mayor que cero, precio positivo y
     * política de cancelación.
     * 
     * @param dto
     * @return
     */
    public List<String> validarDatos(PublicacionDTO dto) {
        List<String> invalidos = new ArrayList<>();

        if (textoVacio(dto.getTitulo())) {
            invalidos.add("título");
        }
        if (textoVacio(dto.getDescripcion())) {
            invalidos.add("descripción");
        }
        if (textoVacio(dto.getUbicacion())) {
            invalidos.add("ubicación");
        }
        if (textoVacio(dto.getCiudad())) {
            invalidos.add("ciudad");
        }
        if (dto.getTipo() == null) {
            invalidos.add("tipo de alojamiento");
        }
        if (dto.getCapacidad() == null || dto.getCapacidad() <= 0) {
            invalidos.add("capacidad (mayor que cero)");
        }
        if (dto.getPrecioNoche() == null || dto.getPrecioNoche() <= 0) {
            invalidos.add("precio por noche (positivo)");
        }
        if (dto.getTarifaLimpieza() == null || dto.getTarifaLimpieza() < 0) {
            invalidos.add("tarifa de limpieza (cero o mayor)");
        }
        if (dto.getIdPoliticaCancelacion() == null) {
            invalidos.add("política de cancelación");
        }
        return invalidos;
    }

    /**
     * Obtiene los datos faltantes para cumplir la información mínima de una
     * publicación.
     * 
     * @param idPublicacion
     * @return
     */
    @Transactional(readOnly = true)
    public List<String> obtenerDatosFaltantes(Long idPublicacion) {
        Optional<Publicacion> pubOpt = publicacionRepo.findById(idPublicacion);
        if (!pubOpt.isPresent()) {
            return null; // Publicación no existe
        }
        return calcularDatosFaltantes(pubOpt.get());
    }

    /**
     * Calcula los datos que no cumplen la información mínima para activar una
     * publicación.
     * 
     * @param publicacion
     * @return
     */
    private List<String> calcularDatosFaltantes(Publicacion publicacion) {
        List<String> faltantes = new ArrayList<>();

        if (textoVacio(publicacion.getTitulo())) {
            faltantes.add("título");
        }
        if (textoVacio(publicacion.getDescripcion())) {
            faltantes.add("descripción");
        }
        if (textoVacio(publicacion.getUbicacion())) {
            faltantes.add("ubicación");
        }
        if (textoVacio(publicacion.getCiudad())) {
            faltantes.add("ciudad");
        }
        if (publicacion.getTipo() == null) {
            faltantes.add("tipo de alojamiento");
        }
        if (publicacion.getCapacidad() == null || publicacion.getCapacidad() <= 0) {
            faltantes.add("capacidad (mayor que cero)");
        }
        if (publicacion.getPrecioNoche() == null || publicacion.getPrecioNoche() <= 0) {
            faltantes.add("precio por noche (positivo)");
        }
        if (imagenRepo.findByPublicacionIdPublicacionOrderByOrdenAsc(publicacion.getIdPublicacion()).isEmpty()) {
            faltantes.add("al menos una imagen");
        }
        return faltantes;
    }

    /**
     * Indica si un texto es nulo o vacío.
     * 
     * @param valor
     * @return
     */
    private boolean textoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    // Métodos relacionados con la moderación por parte del administrador

    /**
     * Bloquea una publicación ACTIVA o PAUSADA (CU-026). Las reservas futuras
     * confirmadas no se tocan: quedan para revisión manual (RN-14). La fila se
     * bloquea durante la transacción, así que no puede coincidir con una reserva,
     * una pausa o una edición del anfitrión.
     *
     * @param nombreAdmin   nombre del administrador autenticado
     * @param idPublicacion ID de la publicación
     * @param dto           trae el motivoBloqueo; devuelve totalReservasFuturas
     * @return 0 si es exitoso, 1 si no existe, 2 si el motivo falta o supera 500
     *         caracteres, 3 si la publicación no está ACTIVA ni PAUSADA
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int bloquearPublicacion(String nombreAdmin, Long idPublicacion, ModeracionPublicacionDTO dto) {
        String motivo = dto.getMotivoBloqueo() == null ? "" : dto.getMotivoBloqueo().trim();
        if (motivo.isEmpty() || motivo.length() > LONGITUD_MAX_MOTIVO) {
            return 2; // Motivo obligatorio (E1)
        }

        Optional<Publicacion> pubOpt = publicacionRepo.findByIdParaActualizar(idPublicacion);
        if (!pubOpt.isPresent()) {
            return 1; // Publicación no encontrada
        }

        Publicacion pub = pubOpt.get();
        if (pub.getEstado() != EstadoPublicacion.ACTIVA && pub.getEstado() != EstadoPublicacion.PAUSADA) {
            return 3; // Solo se bloquean publicaciones ACTIVAS o PAUSADAS (E2)
        }

        pub.setEstado(EstadoPublicacion.BLOQUEADA);
        pub.setMotivoBloqueo(motivo);
        publicacionRepo.save(pub);

        int futuras = reservaRepo.findByPublicacionIdPublicacionAndEstadoAndFechaSalidaAfterOrderByFechaLlegadaAsc(
                idPublicacion, EstadoReserva.CONFIRMADA, LocalDate.now()).size();

        auditoriaService.registrarExito(nombreAdmin, Accion.BLOQUEAR_PUBLICACION, TipoEntidad.PUBLICACION,
                idPublicacion, "Publicación bloqueada: " + pub.getTitulo() + ". Reservas futuras conservadas: "
                        + futuras + ". Motivo: " + motivo);

        dto.setTotalReservasFuturas(futuras);
        return 0; // Publicación bloqueada exitosamente
    }

    /**
     * Lista las publicaciones para el administrador, sin los borradores.
     *
     * @param estado filtro opcional por estado
     * @return resumen de cada publicación
     */
    @Transactional(readOnly = true)
    public List<ModeracionPublicacionDTO> listarParaModeracion(EstadoPublicacion estado) {
        List<ModeracionPublicacionDTO> lista = new ArrayList<>();
        for (Publicacion pub : publicacionRepo.findAll()) {
            if (pub.getEstado() == EstadoPublicacion.BORRADOR) {
                continue;
            }
            if (estado == null || pub.getEstado() == estado) {
                lista.add(convertirModeracion(pub, false));
            }
        }
        return lista;
    }

    /**
     * Detalle de una publicación para el administrador, con sus reservas futuras
     * confirmadas. Los servicios, reglas e imágenes se consultan con los endpoints
     * de /publicacion/{id}.
     *
     * @param idPublicacion ID de la publicación
     * @return el detalle, o null si no existe o es un borrador
     */
    @Transactional(readOnly = true)
    public ModeracionPublicacionDTO obtenerParaModeracion(Long idPublicacion) {
        Optional<Publicacion> pubOpt = publicacionRepo.findById(idPublicacion);
        if (!pubOpt.isPresent() || pubOpt.get().getEstado() == EstadoPublicacion.BORRADOR) {
            return null;
        }
        return convertirModeracion(pubOpt.get(), true);
    }

    /**
     * Convierte la publicación en su DTO de moderación. Se arma a mano dentro de la
     * transacción por las relaciones LAZY.
     */
    private ModeracionPublicacionDTO convertirModeracion(Publicacion pub, boolean detalle) {
        ModeracionPublicacionDTO dto = new ModeracionPublicacionDTO();
        dto.setIdPublicacion(pub.getIdPublicacion());
        dto.setTitulo(pub.getTitulo());
        dto.setCiudad(pub.getCiudad());
        dto.setEstado(pub.getEstado());
        dto.setMotivoBloqueo(pub.getMotivoBloqueo());
        dto.setNombreUsuarioAnfitrion(pub.getAnfitrion().getNombreUsuario());
        dto.setNombreAnfitrion(pub.getAnfitrion().getPrimerNombre() + " " + pub.getAnfitrion().getPrimerApellido());

        if (detalle) {
            dto.setUbicacion(pub.getUbicacion());
            dto.setDescripcion(pub.getDescripcion());
            dto.setTipo(pub.getTipo());
            dto.setCapacidad(pub.getCapacidad());
            dto.setPrecioNoche(pub.getPrecioNoche());

            List<ReservaModeracionDTO> reservas = new ArrayList<>();
            for (Reserva r : reservaRepo
                    .findByPublicacionIdPublicacionAndEstadoAndFechaSalidaAfterOrderByFechaLlegadaAsc(
                            pub.getIdPublicacion(), EstadoReserva.CONFIRMADA, LocalDate.now())) {
                ReservaModeracionDTO rd = new ReservaModeracionDTO();
                rd.setIdReserva(r.getIdReserva());
                rd.setFechaLlegada(r.getFechaLlegada());
                rd.setFechaSalida(r.getFechaSalida());
                rd.setTotal(r.getTotal());
                rd.setNombreUsuarioHuesped(r.getHuesped().getNombreUsuario());
                rd.setNombreHuesped(r.getHuesped().getPrimerNombre() + " " + r.getHuesped().getPrimerApellido());
                reservas.add(rd);
            }
            dto.setReservasFuturas(reservas);
            dto.setTotalReservasFuturas(reservas.size());
        }
        return dto;
    }
}