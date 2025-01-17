package com.example.project_iei.mapper;

import com.example.project_iei.Utilidades.Utilidades;
import com.example.project_iei.entity.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class MonumentoMapperFromCSV {


    public static ResultadoCargaMonumentos mapJsonToMonumentos(String json) throws IOException {
        ResultadoCargaMonumentos resultado = new ResultadoCargaMonumentos();

        List<Monumento> monumentos = new ArrayList<>();
        List<String> fallosReparados = new ArrayList<>();
        List<String> fallosRechazados = new ArrayList<>();

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
                    monumento.setLongitud(Double.valueOf(datosUtm.get(0)));
                    monumento.setLatitud(Double.valueOf(datosUtm.get(1)));
                    monumento.setDireccion(datosUtm.get(2));
                    monumento.setCodigo_postal(datosUtm.get(3));
                    monumento.getLocalidad().setNombre(datosUtm.get(4));
                    datosUtm.clear();


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
                    if (monumento.getCodigo_postal() != null && !monumento.getCodigo_postal().isBlank()) {
                        if(!Utilidades.anyadirTilde(monumento.getProvincia().getNombre()).equals(monumento.getProvincia().getNombre())){
                            monumento.getProvincia().setNombre(Utilidades.anyadirTilde(monumento.getProvincia().getNombre()));
                            fallosReparados.add("Fuente de datos: CV. " + monumento.getNombre() + ". Operacion realizada: Reparar acento e insertar" );
                        }
                        if (monumento.getCodigo_postal().length() == 4) {
                            monumento.setCodigo_postal("0" + monumento.getCodigo_postal());
                            fallosReparados.add("Fuente de datos: CV. " + monumento.getNombre() + ". Operacion realizada: Reparar codigo postal e insertar" );
                        }
                        monumentos.add(monumento);
                    } else {
                        fallosRechazados.add("Fuente de datos: CV. " + monumento.getNombre() + " : " + "(No se pudo obtener el codigo postal a través de la API)");
                    }
                }else{
                    fallosRechazados.add("Fuente de datos: CV. " + monumento.getNombre() + " " + comprobacionMonumentoValido(node));
                }

            }
        }
        resultado.setMonumentos(monumentos);
        resultado.setFallosReparados(fallosReparados);
        resultado.setFallosRechazados(fallosRechazados);
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
            driver.get("http://atlascajamarca.pe/conversor/index.html");

            // Esperar y hacer clic en el radio button "utm"
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            // Rellenar los campos de entrada de coordenadas
            WebElement inputZone = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("txtZone3")));
            inputZone.sendKeys("30");

            WebElement inputX = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("txtX3")));
            inputX.clear();
            inputX.sendKeys(String.valueOf(utmN));

            WebElement inputY = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("txtY3")));
            inputY.clear();
            inputY.sendKeys(String.valueOf(utmE));

            // Hacer clic en el botón de calcular
            WebElement calculateButton = driver.findElement(By.xpath("//img[@onclick='cmdUTM2Lat_click();']"));
            calculateButton.click();

            // Esperar a que aparezcan los resultados
            WebElement latitud = driver.findElement(By.id("txtLat4"));
            WebElement longitud = driver.findElement(By.id("txtLon4"));

            // Agregar los resultados a la lista
            result.add(longitud.getAttribute("value"));
            result.add(latitud.getAttribute("value"));
            result.addAll(Utilidades.getGeocodingInfo(Double.valueOf(latitud.getAttribute("value")), Double.valueOf(longitud.getAttribute("value"))));
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
        } else if (node.get("PROVINCIA") == null || node.get("PROVINCIA").asText().isBlank()) {
            return "(No se encontró la provincia)";
        } else if (!Utilidades.isProvinciaCV(node.get("PROVINCIA").asText())) {
            return "(Valor de provincia no válido : " + node.get("PROVINCIA").asText() + ")";
        } else {
            return "OK";
        }
    }

}
