package com.example.project_iei.repository;

import com.example.project_iei.entity.Localidad;
import com.example.project_iei.entity.Provincia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvinciaRepository extends JpaRepository<Provincia, Long> {
    Provincia findByNombre(String nombre);
}
