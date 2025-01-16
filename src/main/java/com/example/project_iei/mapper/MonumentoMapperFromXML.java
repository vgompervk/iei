package com.example.project_iei.mapper;

import com.example.project_iei.Utilidades.Utilidades;
import com.example.project_iei.entity.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Component
public class MonumentoMapperFromXML {


    public static TuplaMonumentosErrores mapJsonToMonumentos(String json) throws IOException {
        TuplaMonumentosErrores resultado = new TuplaMonumentosErrores();

        List<Monumento> monumentos = new ArrayList<>();
        List<String> fallos = new ArrayList<>();

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
                    monumentos.add(monumento);
                }else if(comprobacionMonumentoValido(monumentoNode).equals("CHECK")){
                    fallos.add("Fuente de datos: CLE, " + monumento.getNombre() + ", " + comprobacionMonumentoValido(monumentoNode));
                }else{
                    fallos.add("Fuente de datos: CLE, " + monumento.getNombre() + ", " + comprobacionMonumentoValido(monumentoNode));
                }
            }
        }
        if(!fallos.isEmpty()){
            System.out.println("----------------------------------------------------------------------------------------------------------------------------");
            System.out.println("Los siguientes monumentos no han sido insertados en la base de datos:");
            for(String fallo : fallos){
                System.out.println(fallo);
            }
            System.out.println("----------------------------------------------------------------------------------------------------------------------------");
        }
        resultado.setMonumentos(monumentos);
        resultado.setFallosReparados(fallos);
        resultado.setFallosRechazados(fallos);
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
