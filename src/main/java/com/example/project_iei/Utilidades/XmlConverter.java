package com.example.project_iei.Utilidades;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;


@Component
public class XmlConverter {

    public String convert(File xmlFile) {
        try {

            // Instancias de XmlMapper y ObjectMapper
            XmlMapper xmlMapper = new XmlMapper();
            ObjectMapper objectMapper = new ObjectMapper();

            // Leer el archivo XML y convertirlo a un JsonNode
            JsonNode jsonNode = xmlMapper.readTree(xmlFile);

            // Convertir el JsonNode a un String JSON
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);

            // Imprimir el JSON resultante
            return jsonString;

        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}