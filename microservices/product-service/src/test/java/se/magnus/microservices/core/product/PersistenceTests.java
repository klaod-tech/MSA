package se.magnus.microservices.core.product;

// ★ 삭제 시작 (6줄) — 아래 줄들을 지운다
// ★ 지움: import static java.util.stream.IntStream.rangeClosed;
// ★ 지움: import static org.junit.jupiter.api.Assertions.*;
// ★ 지움: import static org.springframework.data.domain.Sort.Direction.ASC;
// ★ 지움: import java.util.List;
// ★ 지움: import java.util.Optional;
// ★ 지움: import java.util.stream.Collectors;
// ★ 삭제 끝
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;  // 교재와 다름 (6장부터)  // ★ 변경 (전: import org.springframework.boot.data.mongodb.test.autoconfigure.Dat...)
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
// ★ 변경 시작 (전 3줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import org.springframework.data.domain.Page;
// ★ 전: import org.springframework.data.domain.PageRequest;
// ★ 전: import org.springframework.data.domain.Pageable;
import reactor.test.StepVerifier;
// ★ 변경 끝
import se.magnus.microservices.core.product.persistence.ProductEntity;
import se.magnus.microservices.core.product.persistence.ProductRepository;

@DataMongoTest
class PersistenceTests extends MongoDbTestBase {

  @Autowired
  private ProductRepository repository;

  private ProductEntity savedEntity;

  @BeforeEach
  void setupDb() {
    StepVerifier.create(repository.deleteAll()).verifyComplete();  // ★ 변경 (전: repository.deleteAll();)

    ProductEntity entity = new ProductEntity(1, "n", 1);
    // ★ 변경 시작 (전 2줄 → 6줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: savedEntity = repository.save(entity);
    // ★ 전: assertEqualsProduct(entity, savedEntity);
    StepVerifier.create(repository.save(entity))
      .expectNextMatches(createdEntity -> {
        savedEntity = createdEntity;
        return areProductEqual(entity, savedEntity);
      })
      .verifyComplete();
    // ★ 변경 끝
  }

  @Test
  void create() {
    ProductEntity newEntity = new ProductEntity(2, "n", 2);  // ★ 추가

    // ★ 변경 시작 (전 2줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: ProductEntity newEntity = new ProductEntity(2, "n", 2);
    // ★ 전: repository.save(newEntity);
    StepVerifier.create(repository.save(newEntity))
      .expectNextMatches(createdEntity -> newEntity.getProductId() == createdEntity.getProductId())
      .verifyComplete();
    // ★ 변경 끝

    // ★ 변경 시작 (전 2줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: ProductEntity foundEntity = repository.findById(newEntity.getId()).get();
    // ★ 전: assertEqualsProduct(newEntity, foundEntity);
    StepVerifier.create(repository.findById(newEntity.getId()))
      .expectNextMatches(foundEntity -> areProductEqual(newEntity, foundEntity))
      .verifyComplete();
    // ★ 변경 끝

    StepVerifier.create(repository.count()).expectNext(2L).verifyComplete();  // ★ 변경 (전: assertEquals(2, repository.count());)
  }

  @Test
  void update() {
    savedEntity.setName("n2");
    // ★ 변경 시작 (전 1줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: repository.save(savedEntity);
    StepVerifier.create(repository.save(savedEntity))
      .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("n2"))
      .verifyComplete();
    // ★ 변경 끝

    // ★ 변경 시작 (전 3줄 → 5줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: ProductEntity foundEntity = repository.findById(savedEntity.getId()).get();
    // ★ 전: assertEquals(1, (long)foundEntity.getVersion());
    // ★ 전: assertEquals("n2", foundEntity.getName());
    StepVerifier.create(repository.findById(savedEntity.getId()))
      .expectNextMatches(foundEntity ->
        foundEntity.getVersion() == 1
        && foundEntity.getName().equals("n2"))
      .verifyComplete();
    // ★ 변경 끝
  }

  @Test
  void delete() {
    // ★ 변경 시작 (전 2줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: repository.delete(savedEntity);
    // ★ 전: assertFalse(repository.existsById(savedEntity.getId()));
    StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
    StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
    // ★ 변경 끝
  }

  @Test
  void getByProductId() {
// ★ 삭제: Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId());

    // ★ 변경 시작 (전 2줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: assertTrue(entity.isPresent());
    // ★ 전: assertEqualsProduct(savedEntity, entity.get());
    StepVerifier.create(repository.findByProductId(savedEntity.getProductId()))
      .expectNextMatches(foundEntity -> areProductEqual(savedEntity, foundEntity))
      .verifyComplete();
    // ★ 변경 끝
  }

  @Test
  void duplicateError() {
    // ★ 삭제: assertThrows(DuplicateKeyException.class, () -> {
    ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
    // ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: repository.save(entity);
    // ★ 전: });
    StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify();
    // ★ 변경 끝
  }

  @Test
  void optimisticLockError() {

    // ★ 변경 시작 (전 2줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: ProductEntity entity1 = repository.findById(savedEntity.getId()).get();
    // ★ 전: ProductEntity entity2 = repository.findById(savedEntity.getId()).get();
    ProductEntity entity1 = repository.findById(savedEntity.getId()).block();
    ProductEntity entity2 = repository.findById(savedEntity.getId()).block();
    // ★ 변경 끝

    entity1.setName("n1");
    repository.save(entity1).block();  // ★ 변경 (전: repository.save(entity1);)

    // ★ 변경 시작 (전 4줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: assertThrows(OptimisticLockingFailureException.class, () -> {
    // ★ 전: entity2.setName("n2");
    // ★ 전: repository.save(entity2);
    // ★ 전: });
    StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException.class).verify();
    // ★ 변경 끝

    // ★ 변경 시작 (전 3줄 → 5줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: ProductEntity updatedEntity = repository.findById(savedEntity.getId()).get();
    // ★ 전: assertEquals(1, (int)updatedEntity.getVersion());
    // ★ 전: assertEquals("n1", updatedEntity.getName());
    StepVerifier.create(repository.findById(savedEntity.getId()))
      .expectNextMatches(foundEntity ->
        foundEntity.getVersion() == 1
        && foundEntity.getName().equals("n1"))
      .verifyComplete();
    // ★ 변경 끝
  }

  // ★ 변경 시작 (전 24줄 → 7줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: @Test
  // ★ 전: void paging() {
  // ★ 전: repository.deleteAll();
  // ★ 전: List<ProductEntity> newProducts = rangeClosed(1001, 1010)
  // ★ 전: .mapToObj(i -> new ProductEntity(i, "name " + i, i))
  // ★ 전: .collect(Collectors.toList());
  // ★ 전: repository.saveAll(newProducts);
  // ★ 전: Pageable nextPage = PageRequest.of(0, 4, ASC, "productId");
  // ★ 전: nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
  // ★ 전: nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
  // ★ 전: nextPage = testNextPage(nextPage, "[1009, 1010]", false);
  // ★ 전: }
  // ★ 전: ... (12줄 더)
  private boolean areProductEqual(ProductEntity expectedEntity, ProductEntity actualEntity) {
    return
      (expectedEntity.getId().equals(actualEntity.getId()))
      && (expectedEntity.getVersion() == actualEntity.getVersion())
      && (expectedEntity.getProductId() == actualEntity.getProductId())
      && (expectedEntity.getName().equals(actualEntity.getName()))
      && (expectedEntity.getWeight() == actualEntity.getWeight());
  // ★ 변경 끝
  }
}