package se.magnus.microservices.composite.product.services;

// ★ 변경 시작 (전 1줄 → 4줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import static org.springframework.http.HttpMethod.GET;
import static java.util.logging.Level.FINE;
import static reactor.core.publisher.Flux.empty;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;
// ★ 변경 끝

// ★ 변경 시작 (전 4줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import tools.jackson.core.JacksonException;
// ★ 전: import tools.jackson.databind.ObjectMapper;
// ★ 전: import java.util.ArrayList;
// ★ 전: import java.util.List;
import tools.jackson.core.JacksonException;  // 교재와 다름 (교재: java.io.IOException, 3장부터)
import tools.jackson.databind.ObjectMapper;  // 교재와 다름 (교재: com.fasterxml.jackson.databind.ObjectMapper, 3장부터)
// ★ 변경 끝
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;  // ★ 추가
import org.springframework.beans.factory.annotation.Value;
// ★ 변경 시작 (전 1줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import org.springframework.core.ParameterizedTypeReference;
import org.springframework.boot.health.contributor.Health;  // ★ 참고: 교재: org.springframework.boot.actuate.health.Health
import org.springframework.cloud.stream.function.StreamBridge;
// ★ 변경 끝
import org.springframework.http.HttpStatus;
// ★ 추가 시작 (2줄)
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
// ★ 추가 끝
import org.springframework.stereotype.Component;
// ★ 변경 시작 (전 2줄 → 5줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import org.springframework.web.client.HttpClientErrorException;
// ★ 전: import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
// ★ 변경 끝
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.event.Event;  // ★ 추가
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

  private final WebClient webClient;  // ★ 변경 (전: private final RestTemplate restTemplate;)
  private final ObjectMapper mapper;

  private final String productServiceUrl;
  private final String recommendationServiceUrl;
  private final String reviewServiceUrl;

  // ★ 추가 시작 (2줄)
  private final StreamBridge streamBridge;

  private final Scheduler publishEventScheduler;
  // ★ 추가 끝

  public ProductCompositeIntegration(
    // ★ 변경 시작 (전 1줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: RestTemplate restTemplate,
    @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,

    WebClient.Builder webClient,
    // ★ 변경 끝
    ObjectMapper mapper,
    StreamBridge streamBridge,  // ★ 추가

    @Value("${app.product-service.host}") String productServiceHost,
    @Value("${app.product-service.port}") int  productServicePort,  // ★ 변경 (전: @Value("${app.product-service.port}") int productServicePort,)

    @Value("${app.recommendation-service.host}") String recommendationServiceHost,
    @Value("${app.recommendation-service.port}") int  recommendationServicePort,  // ★ 변경 (전: @Value("${app.recommendation-service.port}") int recommendationServ...)

    @Value("${app.review-service.host}") String reviewServiceHost,
    // ★ 변경 시작 (전 1줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: @Value("${app.review-service.port}") int reviewServicePort) {
    @Value("${app.review-service.port}") int  reviewServicePort
  ) {
    // ★ 변경 끝

    // ★ 변경 시작 (전 1줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: this.restTemplate = restTemplate;
    this.publishEventScheduler = publishEventScheduler;
    this.webClient = webClient.build();
    // ★ 변경 끝
    this.mapper = mapper;
    this.streamBridge = streamBridge;  // ★ 추가

    // ★ 변경 시작 (전 3줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product";
    // ★ 전: recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
    // ★ 전: reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
    productServiceUrl        = "http://" + productServiceHost + ":" + productServicePort;
    recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort;
    reviewServiceUrl         = "http://" + reviewServiceHost + ":" + reviewServicePort;
    // ★ 변경 끝
  }

  @Override
  public Mono<Product> createProduct(Product body) {  // ★ 변경 (전: public Product createProduct(Product body) {)

    // ★ 변경 시작 (전 9줄 → 4줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: try {
    // ★ 전: String url = productServiceUrl;
    // ★ 전: LOG.debug("Will post a new product to URL: {}", url);
    // ★ 전: Product product = restTemplate.postForObject(url, body, Product.class);
    // ★ 전: LOG.debug("Created a product with id: {}", product.getProductId());
    // ★ 전: return product;
    // ★ 전: } catch (HttpClientErrorException ex) {
    // ★ 전: throw handleHttpClientException(ex);
    // ★ 전: }
    return Mono.fromCallable(() -> {
      sendMessage("products-out-0", new Event<>(CREATE, body.getProductId(), body));
      return body;
    }).subscribeOn(publishEventScheduler);
    // ★ 변경 끝
  }

  @Override
  // ★ 변경 시작 (전 3줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: public Product getProduct(int productId) {
  // ★ 전: try {
  // ★ 전: String url = productServiceUrl + "/" + productId;
  public Mono<Product> getProduct(int productId) {
    String url = productServiceUrl + "/product/" + productId;
  // ★ 변경 끝
    LOG.debug("Will call the getProduct API on URL: {}", url);

    // ★ 변경 시작 (전 6줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: Product product = restTemplate.getForObject(url, Product.class);
    // ★ 전: LOG.debug("Found a product with id: {}", product.getProductId());
    // ★ 전: return product;
    // ★ 전: } catch (HttpClientErrorException ex) {
    // ★ 전: throw handleHttpClientException(ex);
    // ★ 전: }
    return webClient.get().uri(url).retrieve().bodyToMono(Product.class).log(LOG.getName(), FINE).onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    // ★ 변경 끝
  }

  @Override
  // ★ 변경 시작 (전 4줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: public void deleteProduct(int productId) {
  // ★ 전: try {
  // ★ 전: String url = productServiceUrl + "/" + productId;
  // ★ 전: LOG.debug("Will call the deleteProduct API on URL: {}", url);
  public Mono<Void> deleteProduct(int productId) {
  // ★ 변경 끝

    // ★ 변경 시작 (전 4줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: restTemplate.delete(url);
    // ★ 전: } catch (HttpClientErrorException ex) {
    // ★ 전: throw handleHttpClientException(ex);
    // ★ 전: }
    return Mono.fromRunnable(() -> sendMessage("products-out-0", new Event<>(DELETE, productId, null)))
      .subscribeOn(publishEventScheduler).then();
    // ★ 변경 끝
  }

  @Override
  public Mono<Recommendation> createRecommendation(Recommendation body) {  // ★ 변경 (전: public Recommendation createRecommendation(Recommendation body) {)

    // ★ 변경 시작 (전 9줄 → 4줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: try {
    // ★ 전: String url = recommendationServiceUrl;
    // ★ 전: LOG.debug("Will post a new recommendation to URL: {}", url);
    // ★ 전: Recommendation recommendation = restTemplate.postForObject(url, body, Recommendation.class);
    // ★ 전: LOG.debug("Created a recommendation with id: {}", recommendation.getProductId());
    // ★ 전: return recommendation;
    // ★ 전: } catch (HttpClientErrorException ex) {
    // ★ 전: throw handleHttpClientException(ex);
    // ★ 전: }
    return Mono.fromCallable(() -> {
      sendMessage("recommendations-out-0", new Event<>(CREATE, body.getProductId(), body));
      return body;
    }).subscribeOn(publishEventScheduler);
    // ★ 변경 끝
  }

  @Override
  public Flux<Recommendation> getRecommendations(int productId) {  // ★ 변경 (전: public List<Recommendation> getRecommendations(int productId) {)

    // ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: try {
    // ★ 전: String url = recommendationServiceUrl + "?productId=" + productId;
    String url = recommendationServiceUrl + "/recommendation?productId=" + productId;
    // ★ 변경 끝

    LOG.debug("Will call the getRecommendations API on URL: {}", url);
// ★ 삭제 시작 (3줄) — 아래 줄들을 지운다
// ★ 지움: List<Recommendation> recommendations = restTemplate
// ★ 지움: .exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {})
// ★ 지움: .getBody();
// ★ 삭제 끝

    // ★ 변경 시작 (전 6줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);
    // ★ 전: return recommendations;
    // ★ 전: } catch (Exception ex) {
    // ★ 전: LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
    // ★ 전: return new ArrayList<>();
    // ★ 전: }
    return webClient.get().uri(url).retrieve().bodyToFlux(Recommendation.class).log(LOG.getName(), FINE).onErrorResume(error -> empty());
    // ★ 변경 끝
  }

  @Override
  // ★ 변경 시작 (전 4줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: public void deleteRecommendations(int productId) {
  // ★ 전: try {
  // ★ 전: String url = recommendationServiceUrl + "?productId=" + productId;
  // ★ 전: LOG.debug("Will call the deleteRecommendations API on URL: {}", url);
  public Mono<Void> deleteRecommendations(int productId) {
  // ★ 변경 끝

    // ★ 변경 시작 (전 4줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: restTemplate.delete(url);
    // ★ 전: } catch (HttpClientErrorException ex) {
    // ★ 전: throw handleHttpClientException(ex);
    // ★ 전: }
    return Mono.fromRunnable(() -> sendMessage("recommendations-out-0", new Event<>(DELETE, productId, null)))
      .subscribeOn(publishEventScheduler).then();
    // ★ 변경 끝
  }

  @Override
  public Mono<Review> createReview(Review body) {  // ★ 변경 (전: public Review createReview(Review body) {)

    // ★ 변경 시작 (전 9줄 → 4줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: try {
    // ★ 전: String url = reviewServiceUrl;
    // ★ 전: LOG.debug("Will post a new review to URL: {}", url);
    // ★ 전: Review review = restTemplate.postForObject(url, body, Review.class);
    // ★ 전: LOG.debug("Created a review with id: {}", review.getProductId());
    // ★ 전: return review;
    // ★ 전: } catch (HttpClientErrorException ex) {
    // ★ 전: throw handleHttpClientException(ex);
    // ★ 전: }
    return Mono.fromCallable(() -> {
      sendMessage("reviews-out-0", new Event<>(CREATE, body.getProductId(), body));
      return body;
    }).subscribeOn(publishEventScheduler);
    // ★ 변경 끝
  }

  @Override
  public Flux<Review> getReviews(int productId) {  // ★ 변경 (전: public List<Review> getReviews(int productId) {)

    // ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: try {
    // ★ 전: String url = reviewServiceUrl + "?productId=" + productId;
    String url = reviewServiceUrl + "/review?productId=" + productId;
    // ★ 변경 끝

    LOG.debug("Will call the getReviews API on URL: {}", url);
// ★ 삭제 시작 (3줄) — 아래 줄들을 지운다
// ★ 지움: List<Review> reviews = restTemplate
// ★ 지움: .exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {})
// ★ 지움: .getBody();
// ★ 삭제 끝

    // ★ 변경 시작 (전 6줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: LOG.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
    // ★ 전: return reviews;
    // ★ 전: } catch (Exception ex) {
    // ★ 전: LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
    // ★ 전: return new ArrayList<>();
    // ★ 전: }
    return webClient.get().uri(url).retrieve().bodyToFlux(Review.class).log(LOG.getName(), FINE).onErrorResume(error -> empty());
    // ★ 변경 끝
  }

  @Override
  // ★ 변경 시작 (전 4줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: public void deleteReviews(int productId) {
  // ★ 전: try {
  // ★ 전: String url = reviewServiceUrl + "?productId=" + productId;
  // ★ 전: LOG.debug("Will call the deleteReviews API on URL: {}", url);
  public Mono<Void> deleteReviews(int productId) {
  // ★ 변경 끝

    // ★ 변경 시작 (전 4줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: restTemplate.delete(url);
    // ★ 전: } catch (HttpClientErrorException ex) {
    // ★ 전: throw handleHttpClientException(ex);
    // ★ 전: }
    return Mono.fromRunnable(() -> sendMessage("reviews-out-0", new Event<>(DELETE, productId, null)))
      .subscribeOn(publishEventScheduler).then();
    // ★ 변경 끝
  }

  // ★ 변경 시작 (전 2줄 → 31줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
  // ★ 전: switch (HttpStatus.resolve(ex.getStatusCode().value())) {
  public Mono<Health> getProductHealth() {  // ★ 참고: 상태 점검: HealthCheckConfiguration이 사용
    return getHealth(productServiceUrl);
  }

  public Mono<Health> getRecommendationHealth() {
    return getHealth(recommendationServiceUrl);
  }

  public Mono<Health> getReviewHealth() {
    return getHealth(reviewServiceUrl);
  }

  private Mono<Health> getHealth(String url) {
    url += "/actuator/health";
    LOG.debug("Will call the Health API on URL: {}", url);
    return webClient.get().uri(url).retrieve().bodyToMono(String.class)
      .map(s -> new Health.Builder().up().build())
      .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
      .log(LOG.getName(), FINE);
  }

  private void sendMessage(String bindingName, Event event) {  // ★ 참고: 이벤트를 토픽으로 보내는 공통 메서드
    LOG.debug("Sending a {} message to {}", event.getEventType(), bindingName);
    Message message = MessageBuilder.withPayload(event)
      .setHeader("partitionKey", event.getKey())
      .build();
    streamBridge.send(bindingName, message);
  }

  private Throwable handleException(Throwable ex) {

    if (!(ex instanceof WebClientResponseException)) {
      LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
      return ex;
    }

    WebClientResponseException wcre = (WebClientResponseException)ex;

    switch (HttpStatus.resolve(wcre.getStatusCode().value())) {
  // ★ 변경 끝

      case NOT_FOUND:
        return new NotFoundException(getErrorMessage(wcre));  // ★ 변경 (전: return new NotFoundException(getErrorMessage(ex));)

      // ★ 변경 시작 (전 2줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
      // ★ 전: case UNPROCESSABLE_CONTENT:
      // ★ 전: return new InvalidInputException(getErrorMessage(ex));
      case UNPROCESSABLE_CONTENT:  // 교재와 다름 (교재: UNPROCESSABLE_ENTITY, 3장부터)
        return new InvalidInputException(getErrorMessage(wcre));
      // ★ 변경 끝

      default:
        // ★ 변경 시작 (전 2줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
        // ★ 전: LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
        // ★ 전: LOG.warn("Error body: {}", ex.getResponseBodyAsString());
        LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
        LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
        // ★ 변경 끝
        return ex;
    }
  }

  private String getErrorMessage(WebClientResponseException ex) {  // ★ 변경 (전: private String getErrorMessage(HttpClientErrorException ex) {)
    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (JacksonException ioex) {  // ★ 참고: 교재와 다름 (교재: IOException, 3장부터)
      return ex.getMessage();
    }
  }
}