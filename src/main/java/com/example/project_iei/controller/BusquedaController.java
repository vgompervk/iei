package com.example.project_iei.controller;

import com.example.project_iei.entity.Monumento;
import com.example.project_iei.entity.TipoMonumento;
import com.example.project_iei.service.MonumentoService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/busqueda")
public class BusquedaController {

    @Autowired
    private MonumentoService monumentoService;

    @GetMapping("/buscarMonumentos")
    public ResponseEntity<List<Monumento>> buscarMonumentos(@RequestParam(required = false) String localidad,
                                           @RequestParam(required = false) String codigoPostal,
                                           @RequestParam(required = false) String provincia,
                                           @RequestParam(required = false) String tipo) {


        List<Monumento> monumentos = monumentoService.buscarMonumentos(localidad, codigoPostal, provincia, tipo);

        return ResponseEntity.ok(monumentos);
    }
}
