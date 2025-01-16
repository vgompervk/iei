package com.example.project_iei.controller;

import com.example.project_iei.Utilidades.CsvConverter;
import com.example.project_iei.Utilidades.XmlConverter;
import com.example.project_iei.entity.Monumento;
import com.example.project_iei.mapper.MonumentoMapperFromCSV;
import com.example.project_iei.mapper.MonumentoMapperFromXML;
import com.example.project_iei.mapper.MonumentoMapperFromJSON;
import com.example.project_iei.service.MonumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AlmacenController {
    @Autowired
    private  XmlConverter xmlConverter;
    @Autowired
    private  CsvConverter csvConverter;
    @Autowired
    private  MonumentoMapperFromXML monumentoMapperFromXML;
    @Autowired
    private  MonumentoMapperFromCSV monumentoMapperFromCSV;
    @Autowired
    private  MonumentoMapperFromJSON monumentoMapperFromJSON;
    @Autowired
    private  MonumentoService monumentoService;


    @GetMapping("/")
    public String redireccionarFormulario() {
        // Redirige a la página del formulario de carga
        return "redirect:/almacen/cargar";
    }

    @GetMapping("/almacen/cargar")
    public String mostrarFormulario(Model model) {
        // Inicializamos valores por defecto
        model.addAttribute("cargadosCorrectos", 0);
        model.addAttribute("erroresReparados", new ArrayList<>());
        model.addAttribute("erroresRechazados", new ArrayList<>());
        return "cargar"; // Muestra el formulario
    }

    @PostMapping("/almacen/cargar")
    public String cargarAlmacen(@RequestParam(value = "fuente", required = false) List<String> fuentes, Model model) {
        if (fuentes == null || fuentes.isEmpty()) {
            model.addAttribute("error", "Debe seleccionar al menos una fuente.");
            return "cargar";
        }

        List<Monumento> monumentosCargados = new ArrayList<>();
        List<String> erroresReparados = new ArrayList<>();
        List<String> erroresRechazados = new ArrayList<>();
        int registrosCargados = 0;

        for (String fuente : fuentes) {
            try {
                switch (fuente) {
                    case "Castilla y León": {
                        // Proceso para XML
                        String filePathXML = "C:\\projects-iei\\monumentos.xml";
                        String jsonResult = xmlConverter.convert(new File(filePathXML));
                        monumentosCargados.addAll(monumentoMapperFromXML.mapJsonToMonumentos(jsonResult));
                        break;
                    }
                    case "Comunitat Valenciana": {
                        // Proceso para CSV
                        String filePathCSV = "C:\\projects-iei\\bienes_inmuebles_interes_cultural.csv";
                        String jsonResult = csvConverter.convert(new File(filePathCSV));
                        monumentosCargados.addAll(monumentoMapperFromCSV.mapJsonToMonumentos(jsonResult));
                        break;
                    }
                    case "Euskadi": {
                        // Proceso para JSON
                        String filePathJSON = "C:\\projects-iei\\edificios.json";
                        String jsonResult = new String(Files.readAllBytes(new File(filePathJSON).toPath()));
                        monumentosCargados.addAll(monumentoMapperFromJSON.mapJsonToMonumentos(jsonResult));
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Fuente desconocida: " + fuente);
                }
            } catch (Exception e) {
                erroresRechazados.add("Fuente: " + fuente + ", Error: " + e.getMessage());
            }
        }

        registrosCargados = monumentosCargados.size();
        monumentoService.guardarMonumentos(monumentosCargados);

        model.addAttribute("cargadosCorrectos", registrosCargados);
        model.addAttribute("erroresReparados", erroresReparados);
        model.addAttribute("erroresRechazados", erroresRechazados);

        return "cargar";
    }
}
