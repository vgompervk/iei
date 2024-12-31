package com.example.project_iei.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum TipoMonumento {
    YACIMIENTO_ARQUEOLOGICO("Yacimiento arqueológico", Arrays.asList("Yacimient", "Arqueológic", "Arqueológic", "Paleontológic")),
    IGLESIA_ERMITA("Iglesia-Ermita", Arrays.asList("Iglesia", "Ermita", "Basílica","Catedral")),
    MONASTERIO_CONVENTO("Monasterio-Convento", Arrays.asList("Monasterio", "Convento")),
    CASTILLO_FORTALEZA_TORRE("Castillo-Fortaleza-Torre", Arrays.asList("Castillo", "Fortaleza", "Torre")),
    EDIFICIO_SINGULAR("Edificio singular", Arrays.asList("Edificio", "Singular","Palacio","Ayuntamiento","Lonja")),
    PUENTE("Puente", Arrays.asList("Puente","Pont")),
    OTROS("Otros", Arrays.asList("Otros"));

    private final String descripcion;
    private final List<String> filtros;

    TipoMonumento(String descripcion, List<String> filtros) {
        this.descripcion = descripcion;
        this.filtros = filtros;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public List<String> getFiltros() {
        return filtros;
    }

    public static TipoMonumento findTipoMonumento(String texto) {
        if (texto == null || texto.isEmpty()) {
            return OTROS;
        }

        for (TipoMonumento tipo : TipoMonumento.values()) {
            if (tipo.getDescripcion().contains(texto)) {
                return tipo;
            }
            for (String filtro : tipo.getFiltros()) {
                if (texto.toLowerCase().contains(filtro.toLowerCase())) {
                    return tipo;
                }
            }
        }

        return OTROS; // Si no coincide con ningun tipo
    }

}
