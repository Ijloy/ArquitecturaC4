package ec.udla.licencias.validator;

import ec.udla.licencias.cache.CacheManager;
import ec.udla.licencias.dto.LicenciaInfo;
import ec.udla.licencias.exception.AntNoDisponibleException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ResponseValidator: recibe el HTML del ANTClient, verifica que sea una respuesta adecuada,
 * extrae puntos y citaciones, "Almacena respuesta valida" en cache y "Entrega datos validados".
 * Una respuesta invalida NUNCA llega al cache.
 */
@Component
public class ResponseValidator {

    private static final Pattern PUNTOS =
            Pattern.compile("(?i)puntos?\\s*(?:de\\s*licencia)?\\s*:?\\s*(\\d{1,2})");

    private final CacheManager cacheManager;

    public ResponseValidator(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public LicenciaInfo validarYAlmacenar(String cedula, String html) {
        LicenciaInfo info = validar(cedula, html);
        cacheManager.guardar(info);
        return info;
    }

    public LicenciaInfo validar(String cedula, String html) {
        if (html == null || html.isBlank()) {
            throw new AntNoDisponibleException("La ANT devolvio una respuesta vacia");
        }
        Document doc = Jsoup.parse(html);
        String texto = doc.text();
        Elements tablas = doc.select("table");

        if (texto.toLowerCase().contains("error") && tablas.isEmpty()) {
            throw new AntNoDisponibleException("La ANT devolvio una pagina de error");
        }
        if (tablas.isEmpty() && !texto.toLowerCase().contains("punto")) {
            throw new AntNoDisponibleException("La pagina de la ANT no contiene datos de licencia");
        }

        Integer puntos = extraerPuntos(texto);
        if (puntos != null && (puntos < 0 || puntos > 30)) {
            throw new AntNoDisponibleException("Puntos fuera de rango: " + puntos);
        }
        return new LicenciaInfo(cedula, puntos, extraerCitaciones(tablas), Instant.now());
    }

    private Integer extraerPuntos(String texto) {
        Matcher m = PUNTOS.matcher(texto);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }

    /** Toma la tabla con mas filas y la convierte en lista de mapas encabezado -> valor. */
    private List<Map<String, String>> extraerCitaciones(Elements tablas) {
        Element tabla = null;
        for (Element t : tablas) {
            if (tabla == null || t.select("tr").size() > tabla.select("tr").size()) tabla = t;
        }
        if (tabla == null) return List.of();

        Elements filas = tabla.select("tr");
        if (filas.size() < 2) return List.of();

        List<String> encabezados = filas.get(0).select("th, td").eachText();
        List<Map<String, String>> resultado = new ArrayList<>();
        for (int f = 1; f < filas.size(); f++) {
            List<String> celdas = filas.get(f).select("td").eachText();
            if (celdas.isEmpty()) continue;
            Map<String, String> registro = new LinkedHashMap<>();
            for (int i = 0; i < celdas.size(); i++) {
                String columna = i < encabezados.size() && !encabezados.get(i).isBlank()
                        ? encabezados.get(i) : "col" + (i + 1);
                registro.put(columna, celdas.get(i));
            }
            resultado.add(registro);
        }
        return resultado;
    }
}
