package com.example.project_iei.service;

import com.example.project_iei.entity.Provincia;
import com.example.project_iei.repository.LocalidadRepository;
import com.example.project_iei.repository.ProvinciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProvinciaService {

    @Autowired
    private ProvinciaRepository provinciaRepository;
}
