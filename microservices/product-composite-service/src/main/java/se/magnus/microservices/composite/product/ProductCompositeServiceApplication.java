package se.magnus.microservices.composite.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;  // ★ 추가
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

// ★ 추가 시작 (5줄)
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
// ★ 추가 끝
import tools.jackson.databind.json.JsonMapper;

@ComponentScan("se.magnus")
@SpringBootApplication
public class ProductCompositeServiceApplication {

	// ★ 추가 시작 (11줄)
	@Value("${api.common.version}")         String apiVersion;
	@Value("${api.common.title}")           String apiTitle;
	@Value("${api.common.description}")     String apiDescription;
	@Value("${api.common.termsOfService}")  String apiTermsOfService;
	@Value("${api.common.license}")         String apiLicense;
	@Value("${api.common.licenseUrl}")      String apiLicenseUrl;
	@Value("${api.common.externalDocDesc}") String apiExternalDocDesc;
	@Value("${api.common.externalDocUrl}")  String apiExternalDocUrl;
	@Value("${api.common.contact.name}")    String apiContactName;
	@Value("${api.common.contact.url}")     String apiContactUrl;
	@Value("${api.common.contact.email}")   String apiContactEmail;
	// ★ 추가 끝

	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeServiceApplication.class, args);
	}

	@Bean
	RestTemplate restTemplate(JsonMapper jsonMapper) {
		return new RestTemplate(List.of(new JacksonJsonHttpMessageConverter(jsonMapper)));
	}

	// ★ 추가 시작 (17줄)
	@Bean
	public OpenAPI getOpenApiDocumentation() {
		return new OpenAPI()
			.info(new Info().title(apiTitle)
				.description(apiDescription)
				.version(apiVersion)
				.contact(new Contact()
					.name(apiContactName)
					.url(apiContactUrl)
					.email(apiContactEmail))
				.termsOfService(apiTermsOfService)
				.license(new License()
					.name(apiLicense)
					.url(apiLicenseUrl)))
			.externalDocs(new ExternalDocumentation()
				.description(apiExternalDocDesc)
				.url(apiExternalDocUrl));
	// ★ 추가 끝
	}
}  // ★ 추가