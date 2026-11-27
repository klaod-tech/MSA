package se.magnus.microservices.composite.product;

import java.util.List;  // ★ 추가 — Boot 4 대응

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// ★ 추가 시작 (4줄)
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;  // ★ 참고: Boot 4 대응
import org.springframework.web.client.RestTemplate;
// ★ 추가 끝

// ★ 추가 시작 (2줄) — Boot 4 대응
import tools.jackson.databind.json.JsonMapper;

@ComponentScan("se.magnus")
// ★ 추가 끝
@SpringBootApplication
public class ProductCompositeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeServiceApplication.class, args);
	}

	// ★ 추가 시작 (3줄)
	@Bean
	RestTemplate restTemplate(JsonMapper jsonMapper) {  // ★ 참고: 교재: restTemplate()
		return new RestTemplate(List.of(new JacksonJsonHttpMessageConverter(jsonMapper)));  // ★ 참고: 교재: return new RestTemplate();
	// ★ 추가 끝
	}
}  // ★ 추가