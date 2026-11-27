package se.magnus.microservices.composite.product;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
// ★ 삭제: import static reactor.core.publisher.Mono.just;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;  // 교재와 다름 (Boot 4)  // ★ 변경 (전: import org.springframework.boot.webtestclient.autoconfigure.AutoCon...)
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
// ★ 변경 시작 (전 3줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import se.magnus.api.composite.product.ProductAggregate;
// ★ 전: import se.magnus.api.composite.product.RecommendationSummary;
// ★ 전: import se.magnus.api.composite.product.ReviewSummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
// ★ 변경 끝
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.composite.product.services.ProductCompositeIntegration;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient  // 교재와 다름 (Boot 4)  // ★ 변경 (전: @AutoConfigureWebTestClient  // Boot 4 (3장부터 유지))
class ProductCompositeServiceApplicationTests {

  private static final int PRODUCT_ID_OK = 1;
  private static final int PRODUCT_ID_NOT_FOUND = 2;
  private static final int PRODUCT_ID_INVALID = 3;

  // ★ 변경 시작 (전 1줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: @Autowired private WebTestClient client;
  @Autowired
  private WebTestClient client;
  // ★ 변경 끝

  // ★ 변경 시작 (전 1줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: @MockitoBean private ProductCompositeIntegration compositeIntegration;
  @MockitoBean
  private ProductCompositeIntegration compositeIntegration;
  // ★ 변경 끝

  @BeforeEach
  void setUp() {

    when(compositeIntegration.getProduct(PRODUCT_ID_OK))
      .thenReturn(Mono.just(new Product(PRODUCT_ID_OK, "name", 1, "mock-address")));  // ★ 변경 (전: .thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));)

    when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
      .thenReturn(Flux.fromIterable(singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address"))));  // ★ 변경 (전: .thenReturn(singletonList(new Recommendation(PRODUCT_ID_OK, 1, "aut...)

    when(compositeIntegration.getReviews(PRODUCT_ID_OK))
      .thenReturn(Flux.fromIterable(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address"))));  // ★ 변경 (전: .thenReturn(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "s...)

    // ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
    // ★ 전: .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));
    when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND)).thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));
    // ★ 변경 끝

    // ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
    // ★ 전: .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
    when(compositeIntegration.getProduct(PRODUCT_ID_INVALID)).thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
    // ★ 변경 끝
  }

  @Test
  // ★ 변경 시작 (전 21줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: void contextLoads() {}
  // ★ 전: @Test
  // ★ 전: void createCompositeProduct1() {
  // ★ 전: ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1, null, null, null);
  // ★ 전: postAndVerifyProduct(compositeProduct, OK);
  // ★ 전: }
  // ★ 전: @Test
  // ★ 전: void createCompositeProduct2() {
  // ★ 전: ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
  // ★ 전: singletonList(new RecommendationSummary(1, "a", 1, "c")),
  // ★ 전: singletonList(new ReviewSummary(1, "a", "s", "c")), null);
  // ★ 전: postAndVerifyProduct(compositeProduct, OK);
  // ★ 전: ... (9줄 더)
  void contextLoads() {
  // ★ 변경 끝
  }

  @Test
  void getProductById() {

    getAndVerifyProduct(PRODUCT_ID_OK, OK)
      .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
      .jsonPath("$.recommendations.length()").isEqualTo(1)
      .jsonPath("$.reviews.length()").isEqualTo(1);
  }

  @Test
  void getProductNotFound() {

    getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, NOT_FOUND)
      .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
      .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
  }

  @Test
  void getProductInvalidInput() {

    getAndVerifyProduct(PRODUCT_ID_INVALID, UNPROCESSABLE_CONTENT)  // ★ 참고: 교재와 다름 (교재: UNPROCESSABLE_ENTITY)
      .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
      .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
  }

  private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
    return client.get()
      .uri("/product-composite/" + productId)
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(expectedStatus)
      .expectHeader().contentType(APPLICATION_JSON)
      .expectBody();
  }
// ★ 삭제 시작 (6줄) — 아래 줄들을 지운다
// ★ 지움: private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus) {
// ★ 지움: client.post()
// ★ 지움: .uri("/product-composite")
// ★ 지움: .body(just(compositeProduct), ProductAggregate.class)
// ★ 지움: .exchange()
// ★ 지움: .expectStatus().isEqualTo(expectedStatus);
// ★ 삭제 끝
}
// ★ 삭제 시작 (7줄) — 아래 줄들을 지운다
// ★ 지움: private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
// ★ 지움: client.delete()
// ★ 지움: .uri("/product-composite/" + productId)
// ★ 지움: .exchange()
// ★ 지움: .expectStatus().isEqualTo(expectedStatus);
// ★ 지움: }
// ★ 지움: }
// ★ 삭제 끝