package se.magnus.microservices.core.product;

// ★ 추가 시작 (3줄)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// ★ 추가 끝
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;  // ★ 추가
import org.springframework.context.annotation.ComponentScan;
// ★ 추가 시작 (10줄)
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import se.magnus.microservices.core.product.persistence.ProductEntity;
// ★ 추가 끝

@SpringBootApplication
@ComponentScan("se.magnus")
public class ProductServiceApplication {

  private static final Logger LOG = LoggerFactory.getLogger(ProductServiceApplication.class);  // ★ 추가

  public static void main(String[] args) {
    // ★ 변경 시작 (전 1줄 → 4줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: SpringApplication.run(ProductServiceApplication.class, args);
    ConfigurableApplicationContext ctx = SpringApplication.run(ProductServiceApplication.class, args);

    String mongodDbHost = ctx.getEnvironment().getProperty("spring.mongodb.host");  // ★ 참고: 교재: spring.data.mongodb.host
    String mongodDbPort = ctx.getEnvironment().getProperty("spring.mongodb.port");  // ★ 참고: 교재: spring.data.mongodb.port
    LOG.info("Connected to MongoDb: " + mongodDbHost + ":" + mongodDbPort);
    // ★ 변경 끝
  }

  // ★ 추가 시작 (8줄)
  @Autowired
  MongoOperations mongoTemplate;

  @EventListener(ContextRefreshedEvent.class)
  public void initIndicesAfterStartup() {

    MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate.getConverter().getMappingContext();
    IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

    IndexOperations indexOps = mongoTemplate.indexOps(ProductEntity.class);
    resolver.resolveIndexFor(ProductEntity.class).forEach(e -> indexOps.createIndex(e));
  // ★ 추가 끝
  }
}  // ★ 추가