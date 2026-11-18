package ec.udla.licencias.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.udla.licencias.dto.LicenciaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * CacheManager: "Consulta y almacena datos" en Redis Cache de consultas ANT.
 * Guarda el JSON de LicenciaInfo con la clave ant:licencia:{cedula}.
 * Si Redis falla, no rompe la consulta (devuelve vacio / no guarda).
 */
@Component("licenciasCacheManager")
public class CacheManager {

    private static final Logger log = LoggerFactory.getLogger(CacheManager.class);
    private static final String PREFIJO = "ant:licencia:";

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final Duration ttlFresco;
    private final Duration ttlMaximo;

    public CacheManager(StringRedisTemplate redis, ObjectMapper mapper,
                        @Value("${licencias.cache.ttl-fresco-minutos}") long ttlFrescoMin,
                        @Value("${licencias.cache.ttl-maximo-dias}") long ttlMaximoDias) {
        this.redis = redis;
        this.mapper = mapper;
        this.ttlFresco = Duration.ofMinutes(ttlFrescoMin);
        this.ttlMaximo = Duration.ofDays(ttlMaximoDias);
    }

    /** "Busca informacion en cache" */
    public Optional<LicenciaInfo> buscar(String cedula) {
        try {
            String json = redis.opsForValue().get(PREFIJO + cedula);
            return json == null ? Optional.empty() : Optional.of(mapper.readValue(json, LicenciaInfo.class));
        } catch (Exception e) {
            log.warn("No se pudo leer Redis: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** "Almacena respuesta valida" */
    public void guardar(LicenciaInfo info) {
        try {
            redis.opsForValue().set(PREFIJO + info.cedula(), mapper.writeValueAsString(info), ttlMaximo);
        } catch (Exception e) {
            log.warn("No se pudo escribir en Redis: {}", e.getMessage());
        }
    }

    public boolean esFresca(LicenciaInfo info) {
        return info.obtenidoEn() != null && info.obtenidoEn().plus(ttlFresco).isAfter(Instant.now());
    }
}
