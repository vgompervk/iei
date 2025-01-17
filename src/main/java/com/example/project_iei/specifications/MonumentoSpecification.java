package com.example.project_iei.specifications;

import com.example.project_iei.entity.Monumento;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class MonumentoSpecification {

    public static Specification<Monumento> buscarMonumentos(String localidad, String codigoPostal, String provincia, String tipo) {
        return (root, query, criteriaBuilder) -> {
            // Crea las condiciones dinámicas
            Predicate predicate = criteriaBuilder.conjunction();

            if (localidad != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(root.get("localidad").get("nombre"), "%" + localidad + "%"));
            }
            if (codigoPostal != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(root.get("codigo_postal"), "%" + codigoPostal + "%"));
            }
            if (provincia != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(root.get("provincia").get("nombre"), "%" + provincia + "%"));
            }
            if (tipo != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(root.get("tipo"), "%" + tipo + "%"));
            }

            return predicate;
        };
    }
}

