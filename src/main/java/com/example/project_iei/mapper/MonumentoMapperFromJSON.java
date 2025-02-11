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
public class MonumentoMapperFromJSON {

    @Autowired
    MonumentoService monumentoService;

    public ResultadoCargaMonumentos mapJsonToMonumentos(String json) throws IOException {
        ResultadoCargaMonumentos resultado = new ResultadoCargaMonumentos();

        List<Monumento> monumentos = new ArrayList<>();
        List<String> fallosRechazados = new ArrayList<>();
        List<String> fallosReparados = new ArrayList<>();

        // Parsear el JSON
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);

        if (rootNode.isArray()) {
            for (JsonNode monumentoNode : rootNode) {

                Monumento monumento = new Monumento();

                if (monumentoNode.get("documentName") != null) {
                    monumento.setNombre(monumentoNode.get("documentName").asText());
                    monumento.setTipo(TipoMonumento.findTipoMonumento(monumentoNode.get("documentName").asText()));
                }
                if (comprobacionMonumentoValido(monumentoNode).equals("OK")) {

                    if (monumentoNode.get("address") != null) {
                        monumento.setDireccion(Jsoup.parse(monumentoNode.get("address").asText()).text());
                    }

                    if (!monumentoNode.get("postalCode").asText().isBlank()) {
                        monumento.setCodigo_postal(monumentoNode.get("postalCode").asText());
                    }

                    if (monumento.getCodigo_postal().length() == 4) {
                        monumento.setCodigo_postal("0" + monumento.getCodigo_postal());
                    }

                    monumento.setLocalidad(new Localidad());

                    if (monumentoNode.get("municipality") != null) {
                        monumento.getLocalidad().setNombre(monumentoNode.get("municipality").asText());
                    }

                    monumento.setLongitud(monumentoNode.get("lonwgs84").asDouble());
                    monumento.setLatitud(monumentoNode.get("latwgs84").asDouble());

                    if (monumentoNode.get("documentDescription") != null) {
                        monumento.setDescripcion(monumentoNode.get("documentDescription").asText());
                    }


                    monumento.setProvincia(new Provincia());

                    if (monumentoNode.get("territory") != null) {
                        monumento.getProvincia().setNombre(monumentoNode.get("territory").asText());
                    }

                    if (!Utilidades.anyadirTilde(monumento.getProvincia().getNombre()).equals(monumento.getProvincia().getNombre())) {
                        monumento.getProvincia().setNombre(Utilidades.anyadirTilde(monumento.getProvincia().getNombre()));
                        fallosReparados.add("Fuente de datos: EUS. " + monumento.getNombre() + ". Operacion realizada: Reparar acento e insertar");
                    }
                    if (monumento.getCodigo_postal().length() == 4) {
                        monumento.setCodigo_postal("0" + monumento.getCodigo_postal());
                        fallosReparados.add("Fuente de datos: EUS. " + monumento.getNombre() + ". Operacion realizada: Reparar codigo postal e insertar");
                    }

                    if (!monumentoService.existeMonumento(monumento.getNombre())) {
                        monumentos.add(monumento);
                    }
                } else {
                    fallosRechazados.add("Fuente de datos: EUS. " + monumento.getNombre() + " " + comprobacionMonumentoValido(monumentoNode));
                }

            }
        }
        resultado.setMonumentos(monumentos);
        resultado.setFallosReparados(fallosReparados);
        resultado.setFallosRechazados(fallosRechazados);
        return resultado;
    }

    public static String comprobacionMonumentoValido(JsonNode node) {
        if (node.get("latwgs84") == null || node.get("latwgs84").asText().isBlank()) {
            return "(No se encontró la latitud)";
        } else if (node.get("lonwgs84") == null || node.get("lonwgs84").asText().isBlank()) {
            return "(No se encontró la longitud)";
        } else if (node.get("latwgs84").asDouble() == 0 || node.get("latwgs84").asDouble() < 20 || node.get("latwgs84").asDouble() > 50) {
            return "(Valor de latitud no válido : " + node.get("latwgs84").asText() + ")";
        } else if (node.get("lonwgs84").asDouble() < -20 || node.get("lonwgs84").asDouble() > 10 || node.get("lonwgs84").asDouble() == 0) {
            return "(Valor de longitud no válido : " + node.get("lonwgs84").asText() + ")";
        } else if (node.get("municipality") == null || node.get("municipality").asText().isBlank()) {
            return "(No se encontró la localidad)";
        } else if (node.get("postalCode") == null || node.get("postalCode").asText().isBlank()) {
            return "(No se encontró el código postal)";
        } else if (node.get("postalCode").asDouble() < 1001 || node.get("postalCode").asDouble() > 52006) {
            return "(Valor de codigo postal no válido : " + node.get("postalCode").asText() + ")";
        } else if (node.get("documentDescription") == null || node.get("documentDescription").asText().isBlank()) {
            return "(No se encontró la descripcion)";
        } else if (node.get("territory") == null || node.get("territory").asText().isBlank()) {
            return "(No se encontró la provincia)";
        } else if (!Utilidades.isProvinciaEUS(node.get("territory").asText())) {
            return "(Valor de provincia no válido : " + node.get("territory").asText() + ")";
        } else {
            return "OK";
        }
    }

}
