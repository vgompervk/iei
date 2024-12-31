package com.example.project_iei.mapper;

import com.example.project_iei.Utilidades.Utilidades;
import com.example.project_iei.entity.Localidad;
import com.example.project_iei.entity.Monumento;
import com.example.project_iei.entity.Provincia;
import com.example.project_iei.entity.TipoMonumento;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MonumentoMapperFromXML {


    public static List<Monumento> mapJsonToMonumentos(String json) throws IOException {
        List<Monumento> monumentos = new ArrayList<>();
        List<Monumento> monumentosFaileds = new ArrayList<>();

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

                JsonNode coordenadasNode = monumentoNode.get("coordenadas");

                if (!coordenadasNode.get("latitud").asText().isBlank() && coordenadasNode.get("latitud").asDouble() != 0
                        && !coordenadasNode.get("longitud").asText().isBlank() && coordenadasNode.get("longitud").asDouble() != 0) {
                    List<String> infoGeocoding = Utilidades.getGeocodingInfo(coordenadasNode.get("latitud").asDouble(), coordenadasNode.get("longitud").asDouble());
                    if (monumentoNode.get("tipoMonumento") != null) {
                        monumento.setTipo(TipoMonumento.findTipoMonumento(monumentoNode.get("tipoMonumento").asText()));
                    }

                    if (monumentoNode.get("calle") != null) {
                        monumento.setDireccion(monumentoNode.get("calle").asText());
                    }else{
                        monumento.setDireccion(infoGeocoding.get(0));
                    }

                    if (monumentoNode.get("codigoPostal") != null) {
                        monumento.setCodigo_postal(monumentoNode.get("codigoPostal").asText());
                    }else{
                        monumento.setCodigo_postal(infoGeocoding.get(1));
                    }

                    if(monumento.getCodigo_postal().length() == 4){
                        monumento.setCodigo_postal("0" + monumento.getCodigo_postal());
                    }

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
                        }else{
                            monumento.getLocalidad().setNombre(infoGeocoding.get(2));
                        }
                    }
                    monumentos.add(monumento);
                }else{
                    monumentosFaileds.add(monumento);
                }
            }
        }
        if(!monumentosFaileds.isEmpty()){
            System.out.println("----------------------------------------------------------------------------------------------------------------------------");
            System.out.println("Los siguientes monumentos no han sido insertados en la base de datos porque no tienen valores de longitud o latitud validos:");
            for(Monumento monumento : monumentosFaileds){
                System.out.println("\nMonumento: " + monumento.getNombre());
            }
            System.out.println("----------------------------------------------------------------------------------------------------------------------------");
        }
        return monumentos;
    }
}
