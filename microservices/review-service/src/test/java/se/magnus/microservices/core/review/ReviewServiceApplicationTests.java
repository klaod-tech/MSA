package se.magnus.microservices.core.review;

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
import se.magnus.api.core.review.Review;
// ★ 추가 시작 (2줄)
import se.magnus.api.event.Event;
import se.magnus.api.exceptions.InvalidInputException;
// ★ 추가 끝
import se.magnus.microservices.core.review.persistence.ReviewRepository;

// ★ 변경 시작 (전 2줄 → 4줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: @SpringBootTest(webEnvironment = RANDOM_PORT)
// ★ 전: @AutoConfigureWebTestClient
@AutoConfigureWebTestClient  // 교재와 다름 (Boot 4)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
  "spring.cloud.stream.defaultBinder=rabbit",
  "logging.level.se.magnus=DEBUG"})
// ★ 변경 끝
class ReviewServiceApplicationTests extends MySqlTestBase {

  @Autowired
  private WebTestClient client;

  @Autowired
  private ReviewRepository repository;

  // ★ 추가 시작 (3줄)
  @Autowired
  @Qualifier("messageProcessor")  // ★ 참고: HTTP POST/DELETE 대신 이벤트를 직접 넣어 테스트
  private Consumer<Event<Integer, Review>> messageProcessor;
  // ★ 추가 끝

  @BeforeEach
  void setupDb() {
    repository.deleteAll();
  }

  @Test
  void getReviewsByProductId() {

    int productId = 1;

    assertEquals(0, repository.findByProductId(productId).size());

    // ★ 변경 시작 (전 3줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: postAndVerifyReview(productId, 1, OK);
    // ★ 전: postAndVerifyReview(productId, 2, OK);
    // ★ 전: postAndVerifyReview(productId, 3, OK);
    sendCreateReviewEvent(productId, 1);
    sendCreateReviewEvent(productId, 2);
    sendCreateReviewEvent(productId, 3);
    // ★ 변경 끝

    assertEquals(3, repository.findByProductId(productId).size());

    getAndVerifyReviewsByProductId(productId, OK)
      .jsonPath("$.length()").isEqualTo(3)
      .jsonPath("$[2].productId").isEqualTo(productId)
      .jsonPath("$[2].reviewId").isEqualTo(3);
  }

  @Test
  void duplicateError() {

    int productId = 1;
    int reviewId = 1;

    assertEquals(0, repository.count());

    // ★ 변경 시작 (전 3줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: postAndVerifyReview(productId, reviewId, OK)
    // ★ 전: .jsonPath("$.productId").isEqualTo(productId)
    // ★ 전: .jsonPath("$.reviewId").isEqualTo(reviewId);
    sendCreateReviewEvent(productId, reviewId);
    // ★ 변경 끝

    assertEquals(1, repository.count());

    // ★ 변경 시작 (전 3줄 → 5줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: postAndVerifyReview(productId, reviewId, UNPROCESSABLE_CONTENT)
    // ★ 전: .jsonPath("$.path").isEqualTo("/review")
    // ★ 전: .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Review Id:1");
    InvalidInputException thrown = assertThrows(
      InvalidInputException.class,
      () -> sendCreateReviewEvent(productId, reviewId),
      "Expected a InvalidInputException here!");
    assertEquals("Duplicate key, Product Id: 1, Review Id:1", thrown.getMessage());
    // ★ 변경 끝

    assertEquals(1, repository.count());
  }

  @Test
  void deleteReviews() {

    int productId = 1;
    int reviewId = 1;

    sendCreateReviewEvent(productId, reviewId);  // ★ 변경 (전: postAndVerifyReview(productId, reviewId, OK);)
    assertEquals(1, repository.findByProductId(productId).size());

    sendDeleteReviewEvent(productId);  // ★ 변경 (전: deleteAndVerifyReviewsByProductId(productId, OK);)
    assertEquals(0, repository.findByProductId(productId).size());

    sendDeleteReviewEvent(productId);  // ★ 변경 (전: deleteAndVerifyReviewsByProductId(productId, OK);)
  }

  @Test
  void getReviewsMissingParameter() {

    getAndVerifyReviewsByProductId("", BAD_REQUEST)
      .jsonPath("$.path").isEqualTo("/review")
      .jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
  }

  @Test
  void getReviewsInvalidParameter() {

    getAndVerifyReviewsByProductId("?productId=no-integer", BAD_REQUEST)
      .jsonPath("$.path").isEqualTo("/review")
      .jsonPath("$.message").isEqualTo("Type mismatch.");
  }

  @Test
  void getReviewsNotFound() {

    getAndVerifyReviewsByProductId("?productId=213", OK)
      .jsonPath("$.length()").isEqualTo(0);
  }

  @Test
  void getReviewsInvalidParameterNegativeValue() {

    int productIdInvalid = -1;

    getAndVerifyReviewsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_CONTENT)  // 교재와 다름 (교재: UNPROCESSABLE_ENTITY)  // ★ 변경 (전: getAndVerifyReviewsByProductId("?productId=" + productIdInvalid, UN...)
      .jsonPath("$.path").isEqualTo("/review")
      .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
  }

  private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
    return getAndVerifyReviewsByProductId("?productId=" + productId, expectedStatus);
  }

  private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productIdQuery, HttpStatus expectedStatus) {
    return client.get()
      .uri("/review" + productIdQuery)
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(expectedStatus)
      .expectHeader().contentType(APPLICATION_JSON)
      .expectBody();
  }

  private void sendCreateReviewEvent(int productId, int reviewId) {  // ★ 변경 (전: private WebTestClient.BodyContentSpec postAndVerifyReview(int produ...)
    Review review = new Review(productId, reviewId, "Author " + reviewId, "Subject " + reviewId, "Content " + reviewId, "SA");
    // ★ 변경 시작 (전 8줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: return client.post()
    // ★ 전: .uri("/review")
    // ★ 전: .body(just(review), Review.class)
    // ★ 전: .accept(APPLICATION_JSON)
    // ★ 전: .exchange()
    // ★ 전: .expectStatus().isEqualTo(expectedStatus)
    // ★ 전: .expectHeader().contentType(APPLICATION_JSON)
    // ★ 전: .expectBody();
    Event<Integer, Review> event = new Event<>(CREATE, productId, review);
    messageProcessor.accept(event);
    // ★ 변경 끝
  }

  // ★ 변경 시작 (전 7줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
  // ★ 전: return client.delete()
  // ★ 전: .uri("/review?productId=" + productId)
  // ★ 전: .accept(APPLICATION_JSON)
  // ★ 전: .exchange()
  // ★ 전: .expectStatus().isEqualTo(expectedStatus)
  // ★ 전: .expectBody();
  private void sendDeleteReviewEvent(int productId) {
    Event<Integer, Review> event = new Event<>(DELETE, productId, null);
    messageProcessor.accept(event);
  // ★ 변경 끝
  }
}