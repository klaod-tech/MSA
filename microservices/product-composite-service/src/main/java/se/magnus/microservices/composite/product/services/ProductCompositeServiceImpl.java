package se.magnus.microservices.composite.product.services;

// ★ 추가 시작 (2줄)
import static java.util.logging.Level.FINE;

import java.util.ArrayList;
// ★ 추가 끝
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;  // ★ 추가
import se.magnus.api.composite.product.*;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
// ★ 삭제: import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

  private final ServiceUtil serviceUtil;
  private final ProductCompositeIntegration integration;  // ★ 변경 (전: private ProductCompositeIntegration integration;)

  // ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: public ProductCompositeServiceImpl(
  // ★ 전: ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
  public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
  // ★ 변경 끝
    this.serviceUtil = serviceUtil;
    this.integration = integration;
  }

  @Override
  public Mono<Void> createProduct(ProductAggregate body) {  // ★ 변경 (전: public void createProduct(ProductAggregate body) {)

    try {

      // ★ 변경 시작 (전 1줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
      // ★ 전: LOG.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());
      List<Mono> monoList = new ArrayList<>();

      LOG.info("Will create a new composite entity for product.id: {}", body.getProductId());
      // ★ 변경 끝

      Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
      monoList.add(integration.createProduct(product));  // ★ 변경 (전: integration.createProduct(product);)

      if (body.getRecommendations() != null) {
        body.getRecommendations().forEach(r -> {
          Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
          monoList.add(integration.createRecommendation(recommendation));  // ★ 변경 (전: integration.createRecommendation(recommendation);)
        });
      }

      if (body.getReviews() != null) {
        body.getReviews().forEach(r -> {
          Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
          monoList.add(integration.createReview(review));  // ★ 변경 (전: integration.createReview(review);)
        });
      }

      LOG.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

      // ★ 추가 시작 (3줄)
      return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
        .doOnError(ex -> LOG.warn("createCompositeProduct failed: {}", ex.toString()))
        .then();
      // ★ 추가 끝

    } catch (RuntimeException re) {
      LOG.warn("createCompositeProduct failed: {}", re.toString());  // ★ 변경 (전: LOG.warn("createCompositeProduct failed", re);)
      throw re;
    }
  }

  @Override
  // ★ 변경 시작 (전 1줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: public ProductAggregate getProduct(int productId) {
  @SuppressWarnings("unchecked")
  public Mono<ProductAggregate> getProduct(int productId) {
  // ★ 변경 끝

    // ★ 변경 시작 (전 9줄 → 8줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: LOG.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);
    // ★ 전: Product product = integration.getProduct(productId);
    // ★ 전: if (product == null) {
    // ★ 전: throw new NotFoundException("No product found for productId: " + productId);
    // ★ 전: }
    // ★ 전: List<Recommendation> recommendations = integration.getRecommendations(productId);
    // ★ 전: List<Review> reviews = integration.getReviews(productId);
    // ★ 전: LOG.debug("getCompositeProduct: aggregate entity found for productId: {}", productId);
    // ★ 전: return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    LOG.info("Will get composite product info for product.id={}", productId);
    return Mono.zip(
      values -> createProductAggregate((Product) values[0], (List<Recommendation>) values[1], (List<Review>) values[2], serviceUtil.getServiceAddress()),
      integration.getProduct(productId),
      integration.getRecommendations(productId).collectList(),
      integration.getReviews(productId).collectList())
      .doOnError(ex -> LOG.warn("getCompositeProduct failed: {}", ex.toString()))
      .log(LOG.getName(), FINE);
    // ★ 변경 끝
  }

  @Override
  public Mono<Void> deleteProduct(int productId) {  // ★ 변경 (전: public void deleteProduct(int productId) {)

    try {  // ★ 변경 (전: LOG.debug("deleteCompositeProduct: Deletes a product aggregate for ...)

      LOG.info("Will delete a product aggregate for product.id: {}", productId);  // ★ 변경 (전: integration.deleteProduct(productId);)

      // ★ 변경 시작 (전 1줄 → 7줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
      // ★ 전: integration.deleteRecommendations(productId);
      return Mono.zip(
        r -> "",
        integration.deleteProduct(productId),
        integration.deleteRecommendations(productId),
        integration.deleteReviews(productId))
        .doOnError(ex -> LOG.warn("delete failed: {}", ex.toString()))
        .log(LOG.getName(), FINE).then();
      // ★ 변경 끝

    // ★ 변경 시작 (전 2줄 → 4줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: integration.deleteReviews(productId);
    // ★ 전: LOG.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);
    } catch (RuntimeException re) {
      LOG.warn("deleteCompositeProduct failed: {}", re.toString());
      throw re;
    }
    // ★ 변경 끝
  }

  // ★ 변경 시작 (전 5줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: private ProductAggregate createProductAggregate(
  // ★ 전: Product product,
  // ★ 전: List<Recommendation> recommendations,
  // ★ 전: List<Review> reviews,
  // ★ 전: String serviceAddress) {
  private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {
  // ★ 변경 끝

    int productId = product.getProductId();
    String name = product.getName();
    int weight = product.getWeight();

    List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
       recommendations.stream()
        .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
        .collect(Collectors.toList());

    List<ReviewSummary> reviewSummaries = (reviews == null)  ? null :  // ★ 변경 (전: List<ReviewSummary> reviewSummaries = (reviews == null) ? null :)
      reviews.stream()
        .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
        .collect(Collectors.toList());

    String productAddress = product.getServiceAddress();
    String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
    String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
    ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

    return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
  }
}