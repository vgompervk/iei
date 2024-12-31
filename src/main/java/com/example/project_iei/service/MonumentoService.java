package com.example.project_iei.service;

import com.example.project_iei.entity.Localidad;
import com.example.project_iei.entity.Monumento;
import com.example.project_iei.entity.Provincia;
import com.example.project_iei.repository.LocalidadRepository;
import com.example.project_iei.repository.MonumentoRepository;
import com.example.project_iei.repository.ProvinciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonumentoService {

    @Autowired
    private MonumentoRepository monumentoRepository;
    @Autowired
    private LocalidadRepository localidadRepository;
    @Autowired
    private ProvinciaRepository provinciaRepository;

    public void guardarMonumentos(List<Monumento> monumentos) {
        for(Monumento monumento : monumentos){

            if (monumento.getLocalidad() != null && monumento.getLocalidad().getNombre() != null) {
                String nombreLocalidad = monumento.getLocalidad().getNombre();
                Localidad localidadExistente = localidadRepository.findByNombre(nombreLocalidad);
                if (localidadExistente == null) {
                    localidadExistente = new Localidad();
                    localidadExistente.setNombre(nombreLocalidad);
                    localidadExistente = localidadRepository.saveAndFlush(localidadExistente);
                }
                monumento.setLocalidad(localidadExistente);
            }

            if (monumento.getProvincia() != null && monumento.getProvincia().getNombre() != null) {
                String nombreProvincia = monumento.getProvincia().getNombre();
                Provincia provinciaExistente = provinciaRepository.findByNombre(nombreProvincia);
                if (provinciaExistente == null) {
                    provinciaExistente = new Provincia();
                    provinciaExistente.setNombre(nombreProvincia);
                    provinciaExistente = provinciaRepository.saveAndFlush(provinciaExistente);
                }
                monumento.setProvincia(provinciaExistente);
            }

            monumentoRepository.saveAndFlush(monumento);
        }
    }
}
