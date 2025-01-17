package com.example.project_iei.controller;

import com.example.project_iei.Utilidades.CsvConverter;
import com.example.project_iei.Utilidades.Utilidades;
import com.example.project_iei.Utilidades.XmlConverter;
import com.example.project_iei.entity.Monumento;
import com.example.project_iei.entity.ResultadoCargaMonumentos;
import com.example.project_iei.mapper.MonumentoMapperFromCSV;
import com.example.project_iei.mapper.MonumentoMapperFromXML;
import com.example.project_iei.mapper.MonumentoMapperFromJSON;
import com.example.project_iei.service.LocalidadService;
import com.example.project_iei.service.MonumentoService;
import com.example.project_iei.service.ProvinciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/almacen")
@Tag(name = "AlmacenController", description = "Controlador para gestionar el almacenamiento de monumentos")
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

    /**
     * Carga monumentos desde varias fuentes externas y los almacena en la base de datos.
     *
     * @param fuentes Lista de fuentes de las que se obtendrán los monumentos.
     * @return Lista de mensajes con información sobre los registros cargados y errores.
     */
    @PostMapping("/cargarMonumentos")
    @Operation(summary = "Cargar monumentos", description = "Carga monumentos desde fuentes externas y los guarda en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monumentos cargados correctamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "400", description = "Error en los datos proporcionados",
                    content = @Content)
    })
    public List<String> cargarAlmacen(@RequestBody List<String> fuentes) {
        List<Monumento> monumentosCargados = new ArrayList<>();
        List<String> erroresReparados = new ArrayList<>();
        List<String> erroresRechazados = new ArrayList<>();

        for (String fuente : fuentes) {
            try {
                HttpClient client = HttpClient.newHttpClient();
                switch (fuente) {
                    case "Castilla y León" -> {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8081/api/wrapper/cyl"))
                                .GET()
                                .build();

                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        ResultadoCargaMonumentos tupla = monumentoMapperFromXML.mapJsonToMonumentos(response.body());
                        monumentosCargados.addAll(tupla.getMonumentos());
                        erroresReparados.addAll(tupla.getFallosReparados());
                        erroresRechazados.addAll(tupla.getFallosRechazados());
                    }
                    case "Comunitat Valenciana" -> {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8082/api/wrapper/cv"))
                                .GET()
                                .build();

                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        ResultadoCargaMonumentos tupla = monumentoMapperFromCSV.mapJsonToMonumentos(response.body());
                        monumentosCargados.addAll(tupla.getMonumentos());
                        erroresReparados.addAll(tupla.getFallosReparados());
                        erroresRechazados.addAll(tupla.getFallosRechazados());
                    }
                    case "Euskadi" -> {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8083/api/wrapper/euskadi"))
                                .GET()
                                .build();

                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        ResultadoCargaMonumentos tupla = monumentoMapperFromJSON.mapJsonToMonumentos(response.body());
                        monumentosCargados.addAll(tupla.getMonumentos());
                        erroresReparados.addAll(tupla.getFallosReparados());
                        erroresRechazados.addAll(tupla.getFallosRechazados());
                    }
                    default -> throw new IllegalArgumentException("Fuente desconocida: " + fuente);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        List<Monumento> monumentosSinDuplicados = monumentosCargados.stream()
                .filter(Utilidades.distinctByKey(Monumento::getNombre)).toList();

        monumentoService.guardarMonumentos(monumentosSinDuplicados);

        List<String> respuesta = new ArrayList<>();
        respuesta.add("Monumentos cargados correctamente: " + monumentosSinDuplicados.size());
        respuesta.add("Errores reparados: " + erroresReparados.size());
        respuesta.addAll(erroresReparados);
        respuesta.add("Errores rechazados: " + erroresRechazados.size());
        respuesta.addAll(erroresRechazados);

        return respuesta;
    }

    /**
     * Borra todos los datos de la base de datos.
     *
     * @return Mensaje indicando el estado de la operación.
     */
    @DeleteMapping("/borrar")
    @Operation(summary = "Borrar base de datos", description = "Elimina todos los datos almacenados en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Base de datos borrada correctamente"),
            @ApiResponse(responseCode = "500", description = "Error al borrar la base de datos")
    })
    public String borrarBaseDeDatos() {
        try {
            monumentoService.borrarTodos();
            localidadService.borrarTodos();
            provinciaService.borrarTodos();
            return "Base de datos borrada correctamente.";
        } catch (Exception e) {
            return "Error al borrar la base de datos: " + e.getMessage();
        }
    }
}
