package se.magnus.microservices.composite.product;

// ★ 삭제 시작 (8줄) — 아래 줄들을 지운다
// ★ 지움: import java.util.List;
// ★ 지움: import org.springframework.beans.factory.annotation.Value;
// ★ 지움: import org.springframework.boot.SpringApplication;
// ★ 지움: import org.springframework.boot.autoconfigure.SpringBootApplication;
// ★ 지움: import org.springframework.context.annotation.Bean;
// ★ 지움: import org.springframework.context.annotation.ComponentScan;
// ★ 지움: import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
// ★ 지움: import org.springframework.web.client.RestTemplate;
// ★ 삭제 끝
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
// ★ 변경 시작 (전 1줄 → 9줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
// ★ 변경 끝

@SpringBootApplication  // ★ 추가
@ComponentScan("se.magnus")
// ★ 삭제: @SpringBootApplication
public class ProductCompositeServiceApplication {

  private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceApplication.class);  // ★ 추가

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

  // ★ 삭제 시작 (7줄) — 아래 줄들을 지운다
  // ★ 지움: public static void main(String[] args) {
  // ★ 지움: SpringApplication.run(ProductCompositeServiceApplication.class, args);
  // ★ 지움: }
  // ★ 지움: @Bean
  // ★ 지움: RestTemplate restTemplate(JsonMapper jsonMapper) {
  // ★ 지움: return new RestTemplate(List.of(new JacksonJsonHttpMessageConverter(jsonMapper)));
  // ★ 지움: }
  // ★ 삭제 끝
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
  }

  // ★ 추가 시작 (8줄) — 생성자와 publishEventScheduler 빈: 이벤트 게시를 별도 스레드 풀에서 실행 / RestTemplate 빈 (WebClient로 바뀜
  private final Integer threadPoolSize;
  private final Integer taskQueueSize;

  public ProductCompositeServiceApplication(
    @Value("${app.threadPoolSize:10}") Integer threadPoolSize,
    @Value("${app.taskQueueSize:100}") Integer taskQueueSize
  ) {
    this.threadPoolSize = threadPoolSize;
    this.taskQueueSize = taskQueueSize;
  // ★ 추가 끝
  }

  // ★ 추가 시작 (9줄)
  @Bean
  public Scheduler publishEventScheduler() {
    LOG.info("Creates a messagingScheduler with connectionPoolSize = {}", threadPoolSize);
    return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "publish-pool");
  }

  public static void main(String[] args) {
    SpringApplication.run(ProductCompositeServiceApplication.class, args);
  }

}
  // ★ 추가 끝