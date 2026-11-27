package se.magnus.microservices.core.product;

// ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import static org.junit.jupiter.api.Assertions.assertFalse;
// ★ 전: import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
// ★ 변경 끝
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
// ★ 변경 시작 (전 1줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import static reactor.core.publisher.Mono.just;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;
// ★ 변경 끝

import java.util.function.Consumer;  // ★ 추가
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;  // ★ 추가
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;  // 교재와 다름 (Boot 4)  // ★ 변경 (전: import org.springframework.boot.webtestclient.autoconfigure.AutoCon...)
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.product.Product;
// ★ 추가 시작 (2줄)
import se.magnus.api.event.Event;
import se.magnus.api.exceptions.InvalidInputException;
// ★ 추가 끝
import se.magnus.microservices.core.product.persistence.ProductRepository;

@AutoConfigureWebTestClient  // 교재와 다름 (Boot 4)  // ★ 추가
@SpringBootTest(webEnvironment = RANDOM_PORT)
// ★ 삭제: @AutoConfigureWebTestClient
class ProductServiceApplicationTests extends MongoDbTestBase {

  // ★ 변경 시작 (전 1줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: @Autowired private WebTestClient client;
  @Autowired
  private WebTestClient client;
  // ★ 변경 끝

  // ★ 변경 시작 (전 1줄 → 5줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: @Autowired private ProductRepository repository;
  @Autowired
  private ProductRepository repository;

  @Autowired
  @Qualifier("messageProcessor")  // ★ 참고: HTTP POST/DELETE 대신 이벤트를 직접 넣어 테스트
  private Consumer<Event<Integer, Product>> messageProcessor;
  // ★ 변경 끝

  @BeforeEach
  void setupDb() {
    repository.deleteAll().block();  // ★ 변경 (전: repository.deleteAll();)
  }

  @Test
  void getProductById() {

    int productId = 1;

    // ★ 변경 시작 (전 1줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: postAndVerifyProduct(productId, OK);
    assertNull(repository.findByProductId(productId).block());
    assertEquals(0, (long)repository.count().block());
    // ★ 변경 끝

    sendCreateProductEvent(productId);  // ★ 변경 (전: assertTrue(repository.findByProductId(productId).isPresent());)

    // ★ 변경 시작 (전 1줄 → 4줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: getAndVerifyProduct(productId, OK).jsonPath("$.productId").isEqualTo(productId);
    assertNotNull(repository.findByProductId(productId).block());
    assertEquals(1, (long)repository.count().block());

    getAndVerifyProduct(productId, OK)
      .jsonPath("$.productId").isEqualTo(productId);
    // ★ 변경 끝
  }

  @Test
  void duplicateError() {

    int productId = 1;

    assertNull(repository.findByProductId(productId).block());  // ★ 변경 (전: postAndVerifyProduct(productId, OK);)

    sendCreateProductEvent(productId);  // ★ 변경 (전: assertTrue(repository.findByProductId(productId).isPresent());)

    // ★ 변경 시작 (전 3줄 → 6줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: postAndVerifyProduct(productId, UNPROCESSABLE_CONTENT)
    // ★ 전: .jsonPath("$.path").isEqualTo("/product")
    // ★ 전: .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: " + productId);
    assertNotNull(repository.findByProductId(productId).block());

    InvalidInputException thrown = assertThrows(
      InvalidInputException.class,
      () -> sendCreateProductEvent(productId),
      "Expected a InvalidInputException here!");
    assertEquals("Duplicate key, Product Id: " + productId, thrown.getMessage());
    // ★ 변경 끝
  }

  @Test
  void deleteProduct() {

    int productId = 1;

    // ★ 변경 시작 (전 2줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: postAndVerifyProduct(productId, OK);
    // ★ 전: assertTrue(repository.findByProductId(productId).isPresent());
    sendCreateProductEvent(productId);
    assertNotNull(repository.findByProductId(productId).block());
    // ★ 변경 끝

    // ★ 변경 시작 (전 2줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: deleteAndVerifyProduct(productId, OK);
    // ★ 전: assertFalse(repository.findByProductId(productId).isPresent());
    sendDeleteProductEvent(productId);
    assertNull(repository.findByProductId(productId).block());
    // ★ 변경 끝

    sendDeleteProductEvent(productId);  // ★ 변경 (전: deleteAndVerifyProduct(productId, OK);)
  }

  @Test
  void getProductInvalidParameterString() {

    getAndVerifyProduct("/no-integer", BAD_REQUEST)
      .jsonPath("$.path").isEqualTo("/product/no-integer")
      .jsonPath("$.message").isEqualTo("Type mismatch.");
  }

  @Test
  void getProductNotFound() {

    int productIdNotFound = 13;
    getAndVerifyProduct(productIdNotFound, NOT_FOUND)
      .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
      .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
  }

  @Test
  void getProductInvalidParameterNegativeValue() {

    int productIdInvalid = -1;

    getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_CONTENT)  // ★ 참고: 교재와 다름 (교재: UNPROCESSABLE_ENTITY)
      .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
      .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
  }

  private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
    return getAndVerifyProduct("/" + productId, expectedStatus);
  }

  private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
    return client.get()
      .uri("/product" + productIdPath)
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(expectedStatus)
      .expectHeader().contentType(APPLICATION_JSON)
      .expectBody();
  }

  private void sendCreateProductEvent(int productId) {  // ★ 변경 (전: private WebTestClient.BodyContentSpec postAndVerifyProduct(int prod...)
    Product product = new Product(productId, "Name " + productId, productId, "SA");
    // ★ 변경 시작 (전 8줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: return client.post()
    // ★ 전: .uri("/product")
    // ★ 전: .body(just(product), Product.class)
    // ★ 전: .accept(APPLICATION_JSON)
    // ★ 전: .exchange()
    // ★ 전: .expectStatus().isEqualTo(expectedStatus)
    // ★ 전: .expectHeader().contentType(APPLICATION_JSON)
    // ★ 전: .expectBody();
    Event<Integer, Product> event = new Event<>(CREATE, productId, product);
    messageProcessor.accept(event);
    // ★ 변경 끝
  }

  // ★ 변경 시작 (전 7줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
  // ★ 전: return client.delete()
  // ★ 전: .uri("/product/" + productId)
  // ★ 전: .accept(APPLICATION_JSON)
  // ★ 전: .exchange()
  // ★ 전: .expectStatus().isEqualTo(expectedStatus)
  // ★ 전: .expectBody();
  private void sendDeleteProductEvent(int productId) {
    Event<Integer, Product> event = new Event<>(DELETE, productId, null);
    messageProcessor.accept(event);
  // ★ 변경 끝
  }
}