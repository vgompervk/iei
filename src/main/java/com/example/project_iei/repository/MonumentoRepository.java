package com.example.project_iei.repository;

import com.example.project_iei.entity.Localidad;
import com.example.project_iei.entity.Monumento;
import com.example.project_iei.entity.TipoMonumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Repository
public interface MonumentoRepository extends JpaRepository<Monumento, Long>, JpaSpecificationExecutor<Monumento> {
    Monumento findByNombre(String nombre);
}
