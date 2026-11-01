package co.edu.unbosque.alloggio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.alloggio.dto.PoliticaCancelacionDTO;
import co.edu.unbosque.alloggio.entity.PoliticaCancelacion;
import co.edu.unbosque.alloggio.repository.PoliticaCancelacionRepository;

@Service
public class PoliticaCancelacionService implements CRUDOperation<PoliticaCancelacionDTO> {

    @Autowired
    private PoliticaCancelacionRepository politicaRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public int create(PoliticaCancelacionDTO newData) {
        if (newData == null) {
            return 1; // Error por datos nulos
        }
        PoliticaCancelacion entidad = modelMapper.map(newData, PoliticaCancelacion.class);
        politicaRepo.save(entidad);
        return 0; // Creado exitosamente
    }

    @Override
    @Transactional(readOnly = true)
    public List<PoliticaCancelacionDTO> getAll() {
        List<PoliticaCancelacion> entidades = politicaRepo.findAll();
        List<PoliticaCancelacionDTO> dtoList = new ArrayList<>();

        for (PoliticaCancelacion entidad : entidades) {
            dtoList.add(modelMapper.map(entidad, PoliticaCancelacionDTO.class));
        }
        return dtoList;
    }

    @Override
    @Transactional
    public int deleteById(Long id) {
        Optional<PoliticaCancelacion> found = politicaRepo.findById(id);
        if (found.isPresent()) {
            politicaRepo.delete(found.get());
            return 0; // Eliminado exitosamente
        }
        return 1; // No encontrado
    }

    @Override
    @Transactional
    public int updateById(Long id, PoliticaCancelacionDTO newData) {
        Optional<PoliticaCancelacion> found = politicaRepo.findById(id);
        if (!found.isPresent()) {
            return 1; // No encontrado
        }

        PoliticaCancelacion entidad = found.get();
        entidad.setNombre(newData.getNombre());
        entidad.setPorcentajeDevolucion(newData.getPorcentajeDevolucion());
        entidad.setCondiciones(newData.getCondiciones());
        entidad.setPlazoCancelacion(newData.getPlazoCancelacion());

        politicaRepo.save(entidad);
        return 0; // Actualizado exitosamente
    }

    @Override
    @Transactional(readOnly = true)
    public Long count() {
        return politicaRepo.count();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exist(Long id) {
        return politicaRepo.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PoliticaCancelacionDTO getById(Long id) {
        Optional<PoliticaCancelacion> found = politicaRepo.findById(id);
        if (found.isPresent()) {
            return modelMapper.map(found.get(), PoliticaCancelacionDTO.class);
        }
        return null; // No encontrado
    }
}