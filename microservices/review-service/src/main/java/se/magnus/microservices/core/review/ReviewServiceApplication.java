package se.magnus.microservices.core.review;

// ★ 추가 시작 (2줄)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// ★ 추가 끝
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;  // ★ 추가
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication  // ★ 추가
@ComponentScan("se.magnus")
// ★ 삭제: @SpringBootApplication
public class ReviewServiceApplication {

  private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceApplication.class);  // ★ 추가

  public static void main(String[] args) {
    // ★ 변경 시작 (전 1줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: SpringApplication.run(ReviewServiceApplication.class, args);
    ConfigurableApplicationContext ctx = SpringApplication.run(ReviewServiceApplication.class, args);

    String mysqlUri = ctx.getEnvironment().getProperty("spring.datasource.url");
    LOG.info("Connected to MySQL: " + mysqlUri);
    // ★ 변경 끝
  }
}