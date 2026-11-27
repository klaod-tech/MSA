package se.magnus.microservices.core.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
// ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import org.springframework.data.mongodb.core.MongoOperations;
// ★ 전: import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
// ★ 변경 끝
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;  // ★ 추가
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import se.magnus.microservices.core.product.persistence.ProductEntity;

@SpringBootApplication
@ComponentScan("se.magnus")
public class ProductServiceApplication {

  private static final Logger LOG = LoggerFactory.getLogger(ProductServiceApplication.class);

  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = SpringApplication.run(ProductServiceApplication.class, args);

    // ★ 변경 시작 (전 2줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: String mongodDbHost = ctx.getEnvironment().getProperty("spring.mongodb.host");
    // ★ 전: String mongodDbPort = ctx.getEnvironment().getProperty("spring.mongodb.port");
    String mongodDbHost = ctx.getEnvironment().getProperty("spring.mongodb.host");  // 교재와 다름 (교재: spring.data.mongodb.host, 6장부터)
    String mongodDbPort = ctx.getEnvironment().getProperty("spring.mongodb.port");  // 교재와 다름 (교재: spring.data.mongodb.port, 6장부터)
    // ★ 변경 끝
    LOG.info("Connected to MongoDb: " + mongodDbHost + ":" + mongodDbPort);
  }

  @Autowired
  ReactiveMongoOperations mongoTemplate;  // ★ 변경 (전: MongoOperations mongoTemplate;)

  @EventListener(ContextRefreshedEvent.class)
  public void initIndicesAfterStartup() {

    MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate.getConverter().getMappingContext();
    IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

    // ★ 변경 시작 (전 2줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: IndexOperations indexOps = mongoTemplate.indexOps(ProductEntity.class);
    // ★ 전: resolver.resolveIndexFor(ProductEntity.class).forEach(e -> indexOps.createIndex(e));
    ReactiveIndexOperations indexOps = mongoTemplate.indexOps(ProductEntity.class);
    resolver.resolveIndexFor(ProductEntity.class).forEach(e -> indexOps.createIndex(e).block());
    // ★ 변경 끝
  }
}