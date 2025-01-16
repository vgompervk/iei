package com.example.project_iei.mapper;

import com.example.project_iei.Utilidades.Utilidades;
import com.example.project_iei.entity.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.C;
import org.locationtech.proj4j.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
@Component
public class MonumentoMapperFromCSV {


    public static TuplaMonumentosErrores mapJsonToMonumentos(String json) throws IOException {
        TuplaMonumentosErrores resultado = new TuplaMonumentosErrores();

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

                    datosUtm = convertUTMToLatLng(node.get("UTMESTE").asInt(), node.get("UTMNORTE").asInt());
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
        resultado.setMonumentos(monumentos);
        resultado.setFallosReparados(fallos);
        resultado.setFallosRechazados(fallos);
        return resultado;
    }



    public static List<String> convertUTMToLatLng(int utmE, int utmN) {
        List<String> result = new ArrayList<>();

        // Configura el driver de Chrome
        System.setProperty("webdriver.chrome.driver", "C:\\projects-iei\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);
        try {
            // Navegar a la página
            driver.get("https://franzpc.com/apps/conversor-coordenadas-geograficas-utm.html");

            // Esperar y hacer clic en el radio button "utm"
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Rellenar los campos de entrada de coordenadas
            WebElement inputZone = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("utmZone")));
            inputZone.sendKeys("30");

            Select inputHemi = new Select(driver.findElement(By.id("utmHemi")));
            inputHemi.selectByValue("N");

            WebElement inputY = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("utmEasting")));
            inputY.clear();
            inputY.sendKeys(String.valueOf(utmN));

            WebElement inputX = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("utmNorthing")));
            inputX.clear();
            inputX.sendKeys(String.valueOf(utmE));

            // Hacer clic en el botón de calcular
            WebElement calculateButton = driver.findElement(By.xpath("//button[contains(text(), 'Convertir UTM Estándar')]"));
            calculateButton.click();

            // Esperar a que aparezcan los resultados
            WebElement latitud = driver.findElement(By.id("decimalLatitude"));
            WebElement longitud = driver.findElement(By.id("decimalLongitude"));

            // Agregar los resultados a la lista
            result.add(longitud.getAttribute("value"));
            result.add(latitud.getAttribute("value"));
            result.addAll(Utilidades.getGeocodingInfo(Double.valueOf(latitud.getAttribute("value")),Double.valueOf(longitud.getAttribute("value"))));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Cerrar el navegador
            driver.quit();
        }

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
