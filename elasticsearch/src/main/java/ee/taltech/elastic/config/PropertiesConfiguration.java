package ee.taltech.elastic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

public class PropertiesConfiguration {

    @ConfigurationProperties(prefix = "elastic")
    public record ElasticConfig(
            String serverUrl,
            String apiKey
    ) {}
}
