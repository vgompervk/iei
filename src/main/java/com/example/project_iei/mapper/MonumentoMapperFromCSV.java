package com.example.project_iei.mapper;

import com.example.project_iei.Utilidades.Utilidades;
import com.example.project_iei.entity.Localidad;
import com.example.project_iei.entity.Monumento;
import com.example.project_iei.entity.Provincia;
import com.example.project_iei.entity.TipoMonumento;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.proj4j.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MonumentoMapperFromCSV {

    private static final String UTM_ZONE = "30";

    public static List<Monumento> mapJsonToMonumentos(String json) throws IOException {
        List<Monumento> monumentos = new ArrayList<>();
        List<String> fallos = new ArrayList<>();

        List<String> datosUtm = new ArrayList<>();

        // Parsear el JSON
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);

        if (rootNode.isArray()) {
            for (JsonNode node : rootNode) {
                Monumento monumento = new Monumento();

                if (node.get("DENOMINACION") != null) {
                    monumento.setNombre(node.get("DENOMINACION").asText());
                }


                if (comprobacionMonumentoValido(node).equals("OK")) {

                    if (node.get("CATEGORIA") != null && node.get("DENOMINACION") != null) {
                        monumento.setTipo(TipoMonumento.findTipoMonumento(node.get("CATEGORIA").asText()));
                        if (monumento.getTipo().equals(TipoMonumento.OTROS)) {
                            monumento.setTipo(TipoMonumento.findTipoMonumento(node.get("DENOMINACION").asText()));
                        }
                    }

                    monumento.setLocalidad(new Localidad());

                    datosUtm = convertUTMToLatLng(node.get("UTMESTE").asDouble(), node.get("UTMNORTE").asDouble(), UTM_ZONE);
                    monumento.setLatitud(Double.valueOf(datosUtm.get(0)));
                    monumento.setLongitud(Double.valueOf(datosUtm.get(1)));
                    monumento.setDireccion(datosUtm.get(2));
                    monumento.setCodigo_postal(datosUtm.get(3));
                    monumento.getLocalidad().setNombre(datosUtm.get(4));
                    datosUtm.clear();

                    if (monumento.getCodigo_postal().length() == 4) {
                        monumento.setCodigo_postal("0" + monumento.getCodigo_postal());
                    }


                    if (node.get("DENOMINACION") != null && node.get("CLASIFICACION") != null) {
                        monumento.setDescripcion(node.get("CLASIFICACION").asText() + ": " + node.get("DENOMINACION").asText());
                    } else if (node.get("DENOMINACION") != null) {
                        monumento.setDescripcion(node.get("DENOMINACION").asText());
                    } else if (node.get("CLASIFICACION") != null) {
                        monumento.setDescripcion(node.get("CLASIFICACION").asText());
                    }

                    monumento.setProvincia(new Provincia());
                    if (node.get("PROVINCIA") != null) {
                        monumento.getProvincia().setNombre(node.get("PROVINCIA").asText());
                    }

                    // Agregar el objeto a la lista
                    if(monumento.getCodigo_postal() != null && !monumento.getCodigo_postal().isBlank()) {
                        monumentos.add(monumento);
                    }else{
                        fallos.add(monumento.getNombre() + " : " + "(No se pudo obtener el codigo postal a través de la API)");
                    }
                } else {
                    fallos.add(monumento.getNombre() + " : " + comprobacionMonumentoValido(node));
                }
            }
        }
        if (!fallos.isEmpty()) {
            System.out.println("----------------------------------------------------------------------------------------------------------------------------");
            System.out.println("Los siguientes monumentos no han sido insertados en la base de datos:");
            for (String fallo : fallos) {
                System.out.println(fallo);
            }
            System.out.println("----------------------------------------------------------------------------------------------------------------------------");
        }
        return monumentos;
    }


    public static List<String> convertUTMToLatLng(double utmE, double utmN, String utmZone) {
        // Crear sistemas de referencia
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem utmCrs = crsFactory.createFromName("EPSG:326" + utmZone); // EPSG para UTM Norte
        CoordinateReferenceSystem wgs84 = crsFactory.createFromName("EPSG:4326"); // WGS84 Lat/Lng

        // Configurar la transformación
        CoordinateTransformFactory transformFactory = new CoordinateTransformFactory();
        CoordinateTransform transform = transformFactory.createTransform(utmCrs, wgs84);

        // Transformar coordenadas
        ProjCoordinate source = new ProjCoordinate(utmN, utmE);
        ProjCoordinate target = new ProjCoordinate();
        transform.transform(source, target);

        List<String> result = new ArrayList<>();

        result.add(String.valueOf(target.x)); //Longitud
        result.add(String.valueOf(target.y)); //Latitud
        result.addAll(Utilidades.getGeocodingInfo(target.y, target.x));

        return result;
    }

    public static String comprobacionMonumentoValido(JsonNode node) {

        if (node.get("UTMESTE") == null || node.get("UTMESTE").asText().isBlank()) {
            return "(No se encontró el valor de UTMESTE)";
        } else if (node.get("UTMNORTE") == null || node.get("UTMNORTE").asText().isBlank()) {
            return "(No se encontró el valor de UTMNORTE)";
        }else if (node.get("PROVINCIA") == null || node.get("PROVINCIA").asText().isBlank()) {
            return "(No se encontró la provincia)";
        }else if(!Utilidades.isProvinciaCV(node.get("PROVINCIA").asText())){
            return "(Valor de provincia no válido : " + node.get("PROVINCIA").asText() + ")";
        } else {
            return "OK";
        }
    }

}
