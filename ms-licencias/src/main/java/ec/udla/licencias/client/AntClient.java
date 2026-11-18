package ec.udla.licencias.client;

import ec.udla.licencias.exception.AntNoDisponibleException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * ANTClient: "Consulta puntos por cedula" en el Portal Web ANT.
 * Solo hace la llamada HTTP y devuelve el HTML; la validacion la hace ResponseValidator.
 */
@Component
public class AntClient {

    private final String url;
    private final int timeoutMs;

    public AntClient(@Value("${ant.consulta.url}") String url,
                     @Value("${ant.consulta.timeout-ms}") int timeoutMs) {
        this.url = url;
        this.timeoutMs = timeoutMs;
    }

    public String consultarPuntos(String cedula, String placa) {
        try {
            Connection.Response resp = Jsoup.connect(url)
                    .data("ps_tipo_identificacion", "CED")
                    .data("ps_identificacion", cedula)
                    .data("ps_placa", placa == null ? "" : placa)
                    .userAgent("Mozilla/5.0 (UDLA-ISWZ2202)")
                    .timeout(timeoutMs)
                    .ignoreHttpErrors(true)
                    .execute();
            if (resp.statusCode() != 200) {
                throw new AntNoDisponibleException("La ANT respondio HTTP " + resp.statusCode());
            }
            return resp.body();
        } catch (AntNoDisponibleException e) {
            throw e;
        } catch (Exception e) {
            throw new AntNoDisponibleException("La ANT no esta disponible: " + e.getMessage(), e);
        }
    }
}
