package com.example.project_iei.mapper;

import com.example.project_iei.Utilidades.Utilidades;
import com.example.project_iei.entity.*;
import com.example.project_iei.service.MonumentoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MonumentoMapperFromXML {

    @Autowired
    MonumentoService monumentoService;

    public ResultadoCargaMonumentos mapJsonToMonumentos(String json) throws IOException {
        ResultadoCargaMonumentos resultado = new ResultadoCargaMonumentos();

        List<Monumento> monumentos = new ArrayList<>();
        List<String> fallosReparados = new ArrayList<>();
        List<String> fallosRechazados = new ArrayList<>();

        // Parsear el JSON
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode monumentosNode = rootNode.get("monumento");

        if (monumentosNode.isArray()) {
            for (JsonNode monumentoNode : monumentosNode) {

                Monumento monumento = new Monumento();

                if (monumentoNode.get("nombre") != null) {
                    monumento.setNombre(monumentoNode.get("nombre").asText());
                }

                if (comprobacionMonumentoValido(monumentoNode).equals("OK")) {

                    if (monumentoNode.get("tipoMonumento") != null) {
                        monumento.setTipo(TipoMonumento.findTipoMonumento(monumentoNode.get("tipoMonumento").asText()));
                    }

                    if (monumentoNode.get("calle") != null) {
                        monumento.setDireccion(monumentoNode.get("calle").asText());
                    }

                    if (monumentoNode.get("codigoPostal") != null) {
                        monumento.setCodigo_postal(monumentoNode.get("codigoPostal").asText());
                    }

                    if(monumento.getCodigo_postal().length() == 4){
                        monumento.setCodigo_postal("0" + monumento.getCodigo_postal());
                    }

                    JsonNode coordenadasNode = monumentoNode.get("coordenadas");

                    monumento.setLatitud(coordenadasNode.get("latitud").asDouble());
                    monumento.setLongitud(coordenadasNode.get("longitud").asDouble());

                    if (monumentoNode.get("Descripcion") != null) {
                        monumento.setDescripcion(Jsoup.parse(monumentoNode.get("Descripcion").asText()).text());
                    }

                    if (monumentoNode.get("poblacion") != null) {
                        JsonNode poblacionNode = monumentoNode.get("poblacion");

                        monumento.setProvincia(new Provincia());
                        if (poblacionNode.get("provincia") != null) {
                            monumento.getProvincia().setNombre(poblacionNode.get("provincia").asText());
                        }
                        monumento.setLocalidad(new Localidad());
                        if (poblacionNode.get("localidad") != null) {
                            monumento.getLocalidad().setNombre(poblacionNode.get("localidad").asText());
                        }
                    }

                    if(!Utilidades.anyadirTilde(monumento.getProvincia().getNombre()).equals(monumento.getProvincia().getNombre())){
                        monumento.getProvincia().setNombre(Utilidades.anyadirTilde(monumento.getProvincia().getNombre()));
                        fallosReparados.add("Fuente de datos: CLE. " + monumento.getNombre() + ". Operacion realizada: Reparar acento e insertar" );
                    }
                    if (monumento.getCodigo_postal().length() == 4) {
                        monumento.setCodigo_postal("0" + monumento.getCodigo_postal());
                        fallosReparados.add("Fuente de datos: CLE. " + monumento.getNombre() + ". Operacion realizada: Reparar codigo postal e insertar" );
                    }
                    if(!monumentoService.existeMonumento(monumento.getNombre())){
                        monumentos.add(monumento);
                    }
                }else{
                    fallosRechazados.add("Fuente de datos: CLE. " + monumento.getNombre() + " " + comprobacionMonumentoValido(monumentoNode));
                }


            }
        }

        resultado.setMonumentos(monumentos);
        resultado.setFallosReparados(fallosReparados);
        resultado.setFallosRechazados(fallosRechazados);
        return resultado;
    }

    public static String comprobacionMonumentoValido(JsonNode node){

        JsonNode coordenadasNode = node.get("coordenadas");
        JsonNode poblacionNode = node.get("poblacion");

        if(coordenadasNode.get("latitud") == null || coordenadasNode.get("latitud").asText().isBlank()){
            return "(No se encontró la latitud)";
        }else if(coordenadasNode.get("longitud") == null || coordenadasNode.get("longitud").asText().isBlank()) {
            return "(No se encontró la longitud)";
        }else if(coordenadasNode.get("latitud").asDouble() < 20 || coordenadasNode.get("latitud").asDouble() > 50) {
            return "(Valor de latitud no válido : " + coordenadasNode.get("latitud").asText() + ")";
        }else if(coordenadasNode.get("longitud").asDouble() < -20 || coordenadasNode.get("longitud").asDouble() > 10) {
            return "(Valor de longitud no válido : " + coordenadasNode.get("longitud").asText() + ")";
        }else if(node.get("codigoPostal") == null || node.get("codigoPostal").asText().isBlank()){
            return "(No se encontró el codigo postal)";
        }else if(node.get("codigoPostal").asDouble() < 1001 || node.get("codigoPostal").asDouble() > 52006){ //
            return "(Valor de codigo postal no válido : " + node.get("codigoPostal").asText() + ")";
        }else if(poblacionNode.get("provincia") == null || poblacionNode.get("provincia").asText().isBlank()){
            return "(No se encontró la provincia)";
        }else if(!Utilidades.isProvinciaCLE(poblacionNode.get("provincia").asText())){
            return "(Valor de provincia no válido : " + poblacionNode.get("provincia").asText() + ")";
        }else{
            return "OK";
        }
    }
}
