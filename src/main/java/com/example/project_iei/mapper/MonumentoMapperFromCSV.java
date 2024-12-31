package com.example.project_iei.mapper;

import com.example.project_iei.Utilidades.Utilidades;
import com.example.project_iei.entity.Localidad;
import com.example.project_iei.entity.Monumento;
import com.example.project_iei.entity.Provincia;
import com.example.project_iei.entity.TipoMonumento;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.locationtech.proj4j.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MonumentoMapperFromCSV {

    private static final String UTM_ZONE = "30";

    public static List<Monumento> mapJsonToMonumentos(String json) throws IOException {
        List<Monumento> monumentos = new ArrayList<>();
        List<Monumento> monumentosFaileds = new ArrayList<>();

        List<String> datosUtm = new ArrayList<>();

        // Parsear el JSON
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);

        if (rootNode.isArray()) {
            for (JsonNode node : rootNode) {
                Monumento monumento = new Monumento();

                if(node.get("DENOMINACION") != null) {
                    monumento.setNombre(node.get("DENOMINACION").asText());
                }
                if(!node.get("UTMESTE").asText().isBlank() && !node.get("UTMNORTE").asText().isBlank()
                && node.get("UTMESTE").asDouble() != 0 && node.get("UTMNORTE").asDouble() != 0) {

                if(node.get("CATEGORIA") != null && node.get("DENOMINACION") != null) {
                    monumento.setTipo(TipoMonumento.findTipoMonumento(node.get("CATEGORIA").asText()));
                    if(monumento.getTipo().equals(TipoMonumento.OTROS)){
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

                if(monumento.getCodigo_postal().length() == 4){
                    monumento.setCodigo_postal("0" + monumento.getCodigo_postal());
                }


                if(node.get("DENOMINACION") != null && node.get("CLASIFICACION") != null) {
                    monumento.setDescripcion(node.get("CLASIFICACION").asText() + ": " + node.get("DENOMINACION").asText());
                }else if(node.get("DENOMINACION") != null) {
                    monumento.setDescripcion(node.get("DENOMINACION").asText());
                }else if(node.get("CLASIFICACION") != null){
                    monumento.setDescripcion(node.get("CLASIFICACION").asText());
                }

                monumento.setProvincia(new Provincia());
                if(node.get("PROVINCIA") != null || Objects.equals(node.get("PROVINCIA").asText(), "ALICANTE")
                        || Objects.equals(node.get("PROVINCIA").asText(), "VALENCIA") || Objects.equals(node.get("PROVINCIA").asText(), "CASTELLÓN")) {
                    monumento.getProvincia().setNombre(node.get("PROVINCIA").asText());
                }

                // Agregar el objeto a la lista
                monumentos.add(monumento);
                }else{
                    monumentosFaileds.add(monumento);
                }
            }
        }
        if(!monumentosFaileds.isEmpty()){
            System.out.println("----------------------------------------------------------------------------------------------------------------------------");
            System.out.println("Los siguientes monumentos no han sido insertados en la base de datos porque no tienen valores de UTMEste o UTMNorte validos:");
            for(Monumento monumento : monumentosFaileds){
                System.out.println("\nMonumento: " + monumento.getNombre());
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

}
