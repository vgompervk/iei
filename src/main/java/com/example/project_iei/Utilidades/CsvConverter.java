package com.example.project_iei.Utilidades;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class CsvConverter {

    public String convert(File csvFile) {
        try (FileReader fileReader = new FileReader(csvFile)) {
            // Configurar el parser para usar ";" como delimitador y manejar comillas
            var csvParser = new CSVParserBuilder()
                    .withSeparator(';')
                    .withQuoteChar('"')
                    .build();

            // Crear el lector del archivo CSV
            var csvReader = new CSVReaderBuilder(fileReader)
                    .withCSVParser(csvParser)
                    .build();

            // Leer todas las filas
            List<String[]> rows = csvReader.readAll();

            // Obtener los encabezados
            String[] headers = rows.get(0);
            List<Map<String, String>> data = new ArrayList<>();

            // Procesar cada fila de datos
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);

                // Validar que la fila tenga el número correcto de columnas
                if (row.length != headers.length) {
                    System.out.println("Advertencia: fila " + (i + 1) + " tiene un número incorrecto de columnas y será ignorada.");
                    continue; // Ignorar filas con errores
                }

                Map<String, String> map = new HashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    map.put(headers[j], row[j]); // Mapear cada columna con su encabezado
                }
                data.add(map);
            }

            // Convertir la lista de mapas a JSON
            ObjectMapper jsonMapper = new ObjectMapper();
            return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);

        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el archivo CSV", e);
        }

    }


}
