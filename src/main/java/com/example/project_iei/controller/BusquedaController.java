package com.example.project_iei.controller;

import com.example.project_iei.entity.Monumento;
import com.example.project_iei.service.MonumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/busqueda")
@CrossOrigin(origins = "http://localhost:8080")
@Tag(name = "BusquedaController", description = "Controlador para la búsqueda de monumentos")
public class BusquedaController {

    @Autowired
    private MonumentoService monumentoService;

    /**
     * Busca monumentos según los parámetros proporcionados (localidad, código postal, provincia o tipo).
     *
     * @param localidad    Localidad en la que se encuentran los monumentos.
     * @param codigoPostal Código postal asociado a los monumentos.
     * @param provincia    Provincia en la que se encuentran los monumentos.
     * @param tipo         Tipo de monumento (según la clasificación).
     * @return Lista de monumentos que coinciden con los filtros.
     */
    @GetMapping("/buscarMonumentos")
    @Operation(summary = "Buscar monumentos", description = "Busca monumentos según los filtros proporcionados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de monumentos encontrados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Monumento.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta, parámetros no válidos",
                    content = @Content)
    })
    public ResponseEntity<List<Monumento>> buscarMonumentos(
            @RequestParam(required = false) String localidad,
            @RequestParam(required = false) String codigoPostal,
            @RequestParam(required = false) String provincia,
            @RequestParam(required = false) String tipo) {

        List<Monumento> monumentos = monumentoService.buscarMonumentos(localidad, codigoPostal, provincia, tipo);
        return ResponseEntity.ok(monumentos);
    }

    /**
     * Recupera todos los monumentos almacenados en la base de datos.
     *
     * @return Lista completa de monumentos.
     */
    @GetMapping("/getAllMonumentos")
    @Operation(summary = "Obtener todos los monumentos", description = "Recupera todos los monumentos de la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista completa de monumentos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Monumento.class)))
    })
    public ResponseEntity<List<Monumento>> getAllMonumentos() {
        List<Monumento> monumentos = monumentoService.getAllMonumentos();
        return ResponseEntity.ok(monumentos);
    }
}
