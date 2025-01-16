package com.example.project_iei;

import com.example.project_iei.Utilidades.CsvConverter;
import com.example.project_iei.Utilidades.Utilidades;
import com.example.project_iei.Utilidades.XmlConverter;
import com.example.project_iei.entity.Monumento;
import com.example.project_iei.mapper.MonumentoMapperFromCSV;
import com.example.project_iei.mapper.MonumentoMapperFromJSON;
import com.example.project_iei.mapper.MonumentoMapperFromXML;
import com.example.project_iei.service.MonumentoService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@SpringBootApplication
public class ProjectIeiApplication {

	public static void main(String[] args) {

		ApplicationContext context = SpringApplication.run(ProjectIeiApplication.class, args);

		List<Monumento> monumentos = new ArrayList<>();

		MonumentoService monumentoService = context.getBean(MonumentoService.class);

		XmlConverter xmlConverter = context.getBean(XmlConverter.class);

		CsvConverter csvConverter = context.getBean(CsvConverter.class);

		// RUTA DEL ARCHIVO--------------------------------------------------------------------------------------------

		String filePathXML = "C:\\projects-iei\\monumentos.xml";
		File fileXML = new File(filePathXML);
		String filePathCSV = "C:\\projects-iei\\bienes_inmuebles_interes_cultural.csv";
		File fileCSV = new File(filePathCSV);
		String filePathJSON = "C:\\projects-iei\\edificios.json";
		File fileJSON = new File(filePathJSON);

		String jsonResult = "";

		//SI ES UN XML------------------------------------------------------------------------------------------------

//		jsonResult = xmlConverter.convert(fileXML);
//		MonumentoMapperFromXML mapperXML = new MonumentoMapperFromXML();
//
//		try {
//			monumentos = mapperXML.mapJsonToMonumentos(jsonResult);
//		}catch (IOException e){
//			System.out.println("Error: " + e);
//		}


		//SI ES CSV------------------------------------------------------------------------------------------------


		try {
			jsonResult = csvConverter.convert(fileCSV);
		} catch (Exception e) {
			System.out.println("Error al convertir CSV a JSON: " + e.getMessage());
		}

		MonumentoMapperFromCSV mapperCSV = new MonumentoMapperFromCSV();

		try {
			monumentos = mapperCSV.mapJsonToMonumentos(jsonResult);
		}catch (IOException e){
			System.out.println("Error: " + e);
		}





		//SI ES JSON------------------------------------------------------------------------------------------------



//		try {
//			List<String> lines = Files.readAllLines(fileJSON.toPath());
//
//			lines.removeIf(line -> line.trim().equals("\"address\" : \"\","));
//			jsonResult = String.join("", lines);
//		}catch (IOException e){
//			e.printStackTrace();
//		}
//
//		MonumentoMapperFromJSON mapperJSON = new MonumentoMapperFromJSON();
//
//		try {
//			monumentos = mapperJSON.mapJsonToMonumentos(jsonResult);
//		}catch (IOException e){
//			System.out.println("Error: " + e);
//		}


		monumentoService.guardarMonumentos(monumentos);

	}

}
