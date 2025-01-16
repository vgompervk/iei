package com.example.project_iei.Utilidades;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jfr.Category;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
public class Utilidades implements Serializable{

    private static final String API_KEY = "915af228bbaa4e99ae9a3a215c93342b";

    public static List<String> getGeocodingInfo(double lat, double lng) {
        String url = String.format(
                "https://api.opencagedata.com/geocode/v1/json?q=%f+%f&key=%s",
                lat, lng, API_KEY);

        List<String> result = new ArrayList<>();

        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();

                // Parsear la respuesta JSON con Jackson
                JsonNode rootNode = mapper.readTree(responseBody);

                // Verificar resultados
                JsonNode results = rootNode.path("results");
                if (results.isArray() && results.size() > 0) {
                    JsonNode firstResult = results.get(0);

                    // Dirección formateada
                    String formattedAddress = firstResult.path("formatted").asText();
                    if(formattedAddress != null) {
                        result.add(formattedAddress); //Añadimos la direccion
                    }else {
                        result.add("");
                    }

                    // Obtenemos componentes para sacar el resto de informacion
                    JsonNode components = firstResult.path("components");

                    //Obtenemos el codigo postal
                    String postcode = components.path("postcode").asText(null); // null si no existe

                    if (postcode != null) {
                        result.add(postcode); //Añadimos el codigo postal
                    }else{
                        result.add("");
                    }

                    //Obtenemos la localidad
                    String locality = components.path("town").asText(null);

                    if(locality == null){
                        locality = components.path("village").asText(null);
                    }
                    if(locality == null){
                        locality = components.path("city").asText(null);
                    }
                    if (locality != null) {
                        result.add(locality); //Añadimos la localidad
                    }else{
                        result.add("");
                    }
                } else {
                    System.out.println("No se encontraron resultados para las coordenadas proporcionadas.");
                }
            } else {
                System.out.println("Error HTTP: " + response.code());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static <T> java.util.function.Predicate<T> distinctByKey(java.util.function.Function<? super T, ?> keyExtractor) {
        HashSet<Object> seen = new HashSet<>();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static boolean isProvinciaCV(String provincia){
        if(provincia.equalsIgnoreCase("Valencia") || provincia.equalsIgnoreCase("Castellón") ||
                provincia.equalsIgnoreCase("Alicante") || provincia.equalsIgnoreCase("Castellon")){
            return true;
        }else {
            return false;
        }
    }
    public static boolean isProvinciaCLE(String provincia){
        if(provincia.equalsIgnoreCase("León") || provincia.equalsIgnoreCase("Palencia") ||
                provincia.equalsIgnoreCase("Burgos") || provincia.equalsIgnoreCase("Zamora") ||
                provincia.equalsIgnoreCase("Valladolid") || provincia.equalsIgnoreCase("Soria") ||
                provincia.equalsIgnoreCase("Segovia") || provincia.equalsIgnoreCase("Salamanca")||
                provincia.equalsIgnoreCase("Leon") || provincia.equalsIgnoreCase("Avila")||
                provincia.equalsIgnoreCase("Ávila")){
            return true;
        }else {
            return false;
        }
    }
    public static boolean isProvinciaEUS(String provincia){
        if(provincia.equalsIgnoreCase("Gipuzkoa") || provincia.equalsIgnoreCase("Bizkaia") ||
                provincia.equalsIgnoreCase("Araba/Álava") || provincia.equalsIgnoreCase("Araba/Alava")){
            return true;
        }else {
            return false;
        }
    }

    public static String anyadirTilde(String provincia){
        switch (provincia.toLowerCase()) {
            // Comunidad Valenciana
            case "castellon":
                return "CASTELLÓN";
            // País Vasco
            case "araba/alava":
                return "Araba/Álava";
            // Castilla y León
            case "avila":
                return "Ávila";
            case "leon":
                return "León";
            default:
                return provincia;
        }
    }

}
