package se.magnus.microservices.core.recommendation;

import static org.junit.jupiter.api.Assertions.*;  // ★ 변경 (전: import static org.junit.jupiter.api.Assertions.assertEquals;)
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
import se.magnus.api.core.recommendation.Recommendation;
// ★ 추가 시작 (2줄)
import se.magnus.api.event.Event;
import se.magnus.api.exceptions.InvalidInputException;
// ★ 추가 끝
import se.magnus.microservices.core.recommendation.persistence.RecommendationRepository;

@AutoConfigureWebTestClient  // 교재와 다름 (Boot 4)  // ★ 추가
@SpringBootTest(webEnvironment = RANDOM_PORT)
// ★ 삭제: @AutoConfigureWebTestClient
class RecommendationServiceApplicationTests extends MongoDbTestBase {

  @Autowired
  private WebTestClient client;

  @Autowired
  private RecommendationRepository repository;

  // ★ 추가 시작 (3줄)
  @Autowired
  @Qualifier("messageProcessor")  // ★ 참고: HTTP POST/DELETE 대신 이벤트를 직접 넣어 테스트
  private Consumer<Event<Integer, Recommendation>> messageProcessor;
  // ★ 추가 끝

  @BeforeEach
  void setupDb() {
    repository.deleteAll().block();  // ★ 변경 (전: repository.deleteAll();)
  }

  @Test
  void getRecommendationsByProductId() {

    int productId = 1;

    // ★ 변경 시작 (전 3줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: postAndVerifyRecommendation(productId, 1, OK);
    // ★ 전: postAndVerifyRecommendation(productId, 2, OK);
    // ★ 전: postAndVerifyRecommendation(productId, 3, OK);
    sendCreateRecommendationEvent(productId, 1);
    sendCreateRecommendationEvent(productId, 2);
    sendCreateRecommendationEvent(productId, 3);
    // ★ 변경 끝

    assertEquals(3, (long)repository.findByProductId(productId).count().block());  // ★ 변경 (전: assertEquals(3, repository.findByProductId(productId).size());)

    getAndVerifyRecommendationsByProductId(productId, OK)
      .jsonPath("$.length()").isEqualTo(3)
      .jsonPath("$[2].productId").isEqualTo(productId)
      .jsonPath("$[2].recommendationId").isEqualTo(3);
  }

  @Test
  void duplicateError() {

    int productId = 1;
    int recommendationId = 1;

    // ★ 변경 시작 (전 3줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: postAndVerifyRecommendation(productId, recommendationId, OK)
    // ★ 전: .jsonPath("$.productId").isEqualTo(productId)
    // ★ 전: .jsonPath("$.recommendationId").isEqualTo(recommendationId);
    sendCreateRecommendationEvent(productId, recommendationId);
    // ★ 변경 끝

    assertEquals(1, (long)repository.count().block());  // ★ 변경 (전: assertEquals(1, repository.count());)

    // ★ 변경 시작 (전 3줄 → 5줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: postAndVerifyRecommendation(productId, recommendationId, UNPROCESSABLE_CONTENT)
    // ★ 전: .jsonPath("$.path").isEqualTo("/recommendation")
    // ★ 전: .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Recommendation Id:1");
    InvalidInputException thrown = assertThrows(
      InvalidInputException.class,
      () -> sendCreateRecommendationEvent(productId, recommendationId),
      "Expected a InvalidInputException here!");
    assertEquals("Duplicate key, Product Id: 1, Recommendation Id:1", thrown.getMessage());
    // ★ 변경 끝

    assertEquals(1, (long)repository.count().block());  // ★ 변경 (전: assertEquals(1, repository.count());)
  }

  @Test
  void deleteRecommendations() {

    int productId = 1;
    int recommendationId = 1;

    // ★ 변경 시작 (전 2줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: postAndVerifyRecommendation(productId, recommendationId, OK);
    // ★ 전: assertEquals(1, repository.findByProductId(productId).size());
    sendCreateRecommendationEvent(productId, recommendationId);
    assertEquals(1, (long)repository.findByProductId(productId).count().block());
    // ★ 변경 끝

    // ★ 변경 시작 (전 2줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: deleteAndVerifyRecommendationsByProductId(productId, OK);
    // ★ 전: assertEquals(0, repository.findByProductId(productId).size());
    sendDeleteRecommendationEvent(productId);
    assertEquals(0, (long)repository.findByProductId(productId).count().block());
    // ★ 변경 끝

    sendDeleteRecommendationEvent(productId);  // ★ 변경 (전: deleteAndVerifyRecommendationsByProductId(productId, OK);)
  }

  @Test
  void getRecommendationsMissingParameter() {

    getAndVerifyRecommendationsByProductId("", BAD_REQUEST)
      .jsonPath("$.path").isEqualTo("/recommendation")
      .jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
  }

  @Test
  void getRecommendationsInvalidParameter() {

    getAndVerifyRecommendationsByProductId("?productId=no-integer", BAD_REQUEST)
      .jsonPath("$.path").isEqualTo("/recommendation")
      .jsonPath("$.message").isEqualTo("Type mismatch.");
  }

  @Test
  void getRecommendationsNotFound() {

    getAndVerifyRecommendationsByProductId("?productId=113", OK)
      .jsonPath("$.length()").isEqualTo(0);
  }

  @Test
  void getRecommendationsInvalidParameterNegativeValue() {

    int productIdInvalid = -1;

    getAndVerifyRecommendationsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_CONTENT)  // 교재와 다름 (교재: UNPROCESSABLE_ENTITY)  // ★ 변경 (전: getAndVerifyRecommendationsByProductId("?productId=" + productIdInv...)
      .jsonPath("$.path").isEqualTo("/recommendation")
      .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
  }

  private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
    return getAndVerifyRecommendationsByProductId("?productId=" + productId, expectedStatus);
  }

  private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery, HttpStatus expectedStatus) {
    return client.get()
      .uri("/recommendation" + productIdQuery)
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(expectedStatus)
      .expectHeader().contentType(APPLICATION_JSON)
      .expectBody();
  }

  private void sendCreateRecommendationEvent(int productId, int recommendationId) {  // ★ 변경 (전: private WebTestClient.BodyContentSpec postAndVerifyRecommendation(i...)
    Recommendation recommendation = new Recommendation(productId, recommendationId, "Author " + recommendationId, recommendationId, "Content " + recommendationId, "SA");
    // ★ 변경 시작 (전 8줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: return client.post()
    // ★ 전: .uri("/recommendation")
    // ★ 전: .body(just(recommendation), Recommendation.class)
    // ★ 전: .accept(APPLICATION_JSON)
    // ★ 전: .exchange()
    // ★ 전: .expectStatus().isEqualTo(expectedStatus)
    // ★ 전: .expectHeader().contentType(APPLICATION_JSON)
    // ★ 전: .expectBody();
    Event<Integer, Recommendation> event = new Event<>(CREATE, productId, recommendation);
    messageProcessor.accept(event);
    // ★ 변경 끝
  }

  // ★ 변경 시작 (전 7줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: private WebTestClient.BodyContentSpec deleteAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
  // ★ 전: return client.delete()
  // ★ 전: .uri("/recommendation?productId=" + productId)
  // ★ 전: .accept(APPLICATION_JSON)
  // ★ 전: .exchange()
  // ★ 전: .expectStatus().isEqualTo(expectedStatus)
  // ★ 전: .expectBody();
  private void sendDeleteRecommendationEvent(int productId) {
    Event<Integer, Recommendation> event = new Event<>(DELETE, productId, null);
    messageProcessor.accept(event);
  // ★ 변경 끝
  }
}