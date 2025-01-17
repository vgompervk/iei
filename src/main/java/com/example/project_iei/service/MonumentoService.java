package com.example.project_iei.service;

import com.example.project_iei.Utilidades.Utilidades;
import com.example.project_iei.entity.Localidad;
import com.example.project_iei.entity.Monumento;
import com.example.project_iei.entity.Provincia;
import com.example.project_iei.repository.LocalidadRepository;
import com.example.project_iei.repository.MonumentoRepository;
import com.example.project_iei.repository.ProvinciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MonumentoService {

    @Autowired
    private MonumentoRepository monumentoRepository;
    @Autowired
    private LocalidadRepository localidadRepository;
    @Autowired
    private ProvinciaRepository provinciaRepository;

    public void borrarTodos(){
        monumentoRepository.deleteAll();
    }

    public void guardarMonumentos(List<Monumento> monumentos) {


        for(Monumento monumento : monumentos){
            String nombreProvincia = monumento.getProvincia().getNombre();
            Provincia provinciaExistente = provinciaRepository.findByNombre(Utilidades.anyadirTilde(nombreProvincia));
            if (provinciaExistente == null) {
                provinciaExistente = new Provincia();
                provinciaExistente.setNombre(Utilidades.anyadirTilde(nombreProvincia));
                provinciaExistente = provinciaRepository.saveAndFlush(provinciaExistente);
            }
            monumento.setProvincia(provinciaExistente);

            String nombreLocalidad = monumento.getLocalidad().getNombre();
            Localidad localidadExistente = localidadRepository.findByNombre(nombreLocalidad);
            if (localidadExistente == null) {
                localidadExistente = new Localidad();
                localidadExistente.setNombre(nombreLocalidad);
                localidadExistente.setProvincia(monumento.getProvincia());
                localidadExistente = localidadRepository.saveAndFlush(localidadExistente);
            }
            monumento.setLocalidad(localidadExistente);

            monumentoRepository.saveAndFlush(monumento);
        }
    }
}
