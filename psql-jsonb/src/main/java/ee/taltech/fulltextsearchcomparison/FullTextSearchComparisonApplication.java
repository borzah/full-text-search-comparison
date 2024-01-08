package ee.taltech.fulltextsearchcomparison;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FullTextSearchComparisonApplication {

	public static void main(String[] args) {
		SpringApplication.run(FullTextSearchComparisonApplication.class, args);
	}

}
