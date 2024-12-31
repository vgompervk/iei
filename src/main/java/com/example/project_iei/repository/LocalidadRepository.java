package com.example.project_iei.repository;

import com.example.project_iei.entity.Localidad;
import com.example.project_iei.entity.Monumento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalidadRepository extends JpaRepository<Localidad, Long> {
    Localidad findByNombre(String nombre);
}
