package com.example.project_iei.controller;

import com.example.project_iei.Utilidades.CsvConverter;
import com.example.project_iei.Utilidades.Utilidades;
import com.example.project_iei.Utilidades.XmlConverter;
import com.example.project_iei.entity.Monumento;
import com.example.project_iei.entity.TuplaMonumentosErrores;
import com.example.project_iei.mapper.MonumentoMapperFromCSV;
import com.example.project_iei.mapper.MonumentoMapperFromXML;
import com.example.project_iei.mapper.MonumentoMapperFromJSON;
import com.example.project_iei.service.LocalidadService;
import com.example.project_iei.service.MonumentoService;
import com.example.project_iei.service.ProvinciaService;
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
    private XmlConverter xmlConverter;
    @Autowired
    private CsvConverter csvConverter;
    @Autowired
    private MonumentoMapperFromXML monumentoMapperFromXML;
    @Autowired
    private MonumentoMapperFromCSV monumentoMapperFromCSV;
    @Autowired
    private MonumentoMapperFromJSON monumentoMapperFromJSON;
    @Autowired
    private MonumentoService monumentoService;
    @Autowired
    private LocalidadService localidadService;
    @Autowired
    private ProvinciaService provinciaService;


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
        List<Monumento> monumentosSinDuplicados = new ArrayList<>();
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
                        TuplaMonumentosErrores tupla = new TuplaMonumentosErrores();
                        tupla = monumentoMapperFromXML.mapJsonToMonumentos(jsonResult);
                        monumentosCargados.addAll(tupla.getMonumentos());
                        erroresReparados.addAll(tupla.getFallosReparados());
                        erroresRechazados.addAll(tupla.getFallosRechazados());
                        break;
                    }
                    case "Comunitat Valenciana": {
                        // Proceso para CSV
                        String filePathCSV = "C:\\projects-iei\\bienes_inmuebles_interes_cultural.csv";
                        String jsonResult = csvConverter.convert(new File(filePathCSV));
                        TuplaMonumentosErrores tupla = new TuplaMonumentosErrores();
                        tupla = monumentoMapperFromCSV.mapJsonToMonumentos(jsonResult);
                        monumentosCargados.addAll(tupla.getMonumentos());
                        erroresReparados.addAll(tupla.getFallosReparados());
                        erroresRechazados.addAll(tupla.getFallosRechazados());
                        break;
                    }
                    case "Euskadi": {
                        // Proceso para JSON
                        String filePathJSON = "C:\\projects-iei\\edificios.json";
                        File fileJSON = new File(filePathJSON);
                        String jsonResult = "";
                        List<String> lines = Files.readAllLines(fileJSON.toPath());
                        lines.removeIf(line -> line.trim().equals("\"address\" : \"\","));
                        jsonResult = String.join("", lines);
                        TuplaMonumentosErrores tupla = new TuplaMonumentosErrores();
                        tupla = monumentoMapperFromJSON.mapJsonToMonumentos(jsonResult);
                        monumentosCargados.addAll(tupla.getMonumentos());
                        erroresReparados.addAll(tupla.getFallosReparados());
                        erroresRechazados.addAll(tupla.getFallosRechazados());
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Fuente desconocida: " + fuente);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        monumentosSinDuplicados = monumentosCargados.stream()
                .filter(Utilidades.distinctByKey(Monumento::getNombre)).toList();

        registrosCargados = monumentosSinDuplicados.size();
        monumentoService.guardarMonumentos(monumentosCargados);

        model.addAttribute("cargadosCorrectos", registrosCargados);
        model.addAttribute("erroresReparados", erroresReparados);
        model.addAttribute("erroresRechazados", erroresRechazados);

        return "cargar";
    }

    @PostMapping("/almacen/borrar")
    public String borrarBaseDeDatos(Model model) {
        try {
            monumentoService.borrarTodos();
            localidadService.borrarTodos();
            provinciaService.borrarTodos();
            model.addAttribute("mensaje", "Base de datos borrada correctamente.");
        } catch (Exception e) {
            model.addAttribute("error", "Error al borrar la base de datos: " + e.getMessage());
        }
        return "cargar"; // Redirige al formulario de carga
    }
}
