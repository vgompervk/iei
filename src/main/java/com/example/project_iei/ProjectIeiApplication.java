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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.*;

@SpringBootApplication
public class ProjectIeiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectIeiApplication.class, args);
    }
}
