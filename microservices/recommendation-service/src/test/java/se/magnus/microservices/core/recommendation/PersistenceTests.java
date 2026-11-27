package se.magnus.microservices.core.recommendation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;  // 교재와 다름 (6장부터)  // ★ 변경 (전: import org.springframework.boot.data.mongodb.test.autoconfigure.Dat...)
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import se.magnus.microservices.core.recommendation.persistence.RecommendationEntity;
import se.magnus.microservices.core.recommendation.persistence.RecommendationRepository;

@DataMongoTest
class PersistenceTests extends MongoDbTestBase {

  @Autowired
  private RecommendationRepository repository;

  private RecommendationEntity savedEntity;

  @BeforeEach
  void setupDb() {
    repository.deleteAll().block();  // ★ 변경 (전: repository.deleteAll();)

    RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");
    savedEntity = repository.save(entity).block();  // ★ 변경 (전: savedEntity = repository.save(entity);)

    assertEqualsRecommendation(entity, savedEntity);
  }

  @Test
  void create() {

    RecommendationEntity newEntity = new RecommendationEntity(1, 3, "a", 3, "c");
    repository.save(newEntity).block();  // ★ 변경 (전: repository.save(newEntity);)

    RecommendationEntity foundEntity = repository.findById(newEntity.getId()).block();  // ★ 변경 (전: RecommendationEntity foundEntity = repository.findById(newEntity.ge...)
    assertEqualsRecommendation(newEntity, foundEntity);

    assertEquals(2, (long)repository.count().block());  // ★ 변경 (전: assertEquals(2, repository.count());)
  }

  @Test
  void update() {
    savedEntity.setAuthor("a2");
    repository.save(savedEntity).block();  // ★ 변경 (전: repository.save(savedEntity);)

    RecommendationEntity foundEntity = repository.findById(savedEntity.getId()).block();  // ★ 변경 (전: RecommendationEntity foundEntity = repository.findById(savedEntity....)
    assertEquals(1, (long)foundEntity.getVersion());
    assertEquals("a2", foundEntity.getAuthor());
  }

  @Test
  void delete() {
    // ★ 변경 시작 (전 2줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: repository.delete(savedEntity);
    // ★ 전: assertFalse(repository.existsById(savedEntity.getId()));
    repository.delete(savedEntity).block();
    assertFalse(repository.existsById(savedEntity.getId()).block());
    // ★ 변경 끝
  }

  @Test
  void getByProductId() {
    List<RecommendationEntity> entityList = repository.findByProductId(savedEntity.getProductId()).collectList().block();  // ★ 변경 (전: List<RecommendationEntity> entityList = repository.findByProductId(...)

    assertThat(entityList, hasSize(1));
    assertEqualsRecommendation(savedEntity, entityList.get(0));
  }

  @Test
  void duplicateError() {
    assertThrows(DuplicateKeyException.class, () -> {
      RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");
      repository.save(entity).block();  // ★ 변경 (전: repository.save(entity);)
    });
  }

  @Test
  void optimisticLockError() {

    // ★ 변경 시작 (전 2줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: RecommendationEntity entity1 = repository.findById(savedEntity.getId()).get();
    // ★ 전: RecommendationEntity entity2 = repository.findById(savedEntity.getId()).get();
    RecommendationEntity entity1 = repository.findById(savedEntity.getId()).block();
    RecommendationEntity entity2 = repository.findById(savedEntity.getId()).block();
    // ★ 변경 끝

    entity1.setAuthor("a1");
    repository.save(entity1).block();  // ★ 변경 (전: repository.save(entity1);)

    assertThrows(OptimisticLockingFailureException.class, () -> {
      entity2.setAuthor("a2");
      repository.save(entity2).block();  // ★ 변경 (전: repository.save(entity2);)
    });

    RecommendationEntity updatedEntity = repository.findById(savedEntity.getId()).block();  // ★ 변경 (전: RecommendationEntity updatedEntity = repository.findById(savedEntit...)
    assertEquals(1, (int)updatedEntity.getVersion());
    assertEquals("a1", updatedEntity.getAuthor());
  }

  private void assertEqualsRecommendation(RecommendationEntity expectedEntity, RecommendationEntity actualEntity) {
    assertEquals(expectedEntity.getId(),               actualEntity.getId());
    assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
    assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
    assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
    assertEquals(expectedEntity.getAuthor(),           actualEntity.getAuthor());
    assertEquals(expectedEntity.getRating(),           actualEntity.getRating());
    assertEquals(expectedEntity.getContent(),          actualEntity.getContent());
  }
}