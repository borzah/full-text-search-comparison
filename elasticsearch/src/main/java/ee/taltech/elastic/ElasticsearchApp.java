package ee.taltech.elastic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ElasticsearchApp extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(ElasticsearchApp.class, args);
	}

}
