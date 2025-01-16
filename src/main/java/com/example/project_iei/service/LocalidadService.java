package com.example.project_iei.service;

import com.example.project_iei.repository.LocalidadRepository;
import com.example.project_iei.repository.MonumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocalidadService {

    @Autowired
    private LocalidadRepository localidadRepository;

    public void borrarTodos(){
        localidadRepository.deleteAll();
    }

}
