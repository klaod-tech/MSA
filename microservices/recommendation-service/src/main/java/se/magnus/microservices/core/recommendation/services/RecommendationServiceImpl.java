package se.magnus.microservices.core.recommendation.services;

import static java.util.logging.Level.FINE;  // ★ 변경 (전: import java.util.List;)

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
// ★ 추가 시작 (2줄)
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
// ★ 추가 끝
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.microservices.core.recommendation.persistence.RecommendationEntity;
import se.magnus.microservices.core.recommendation.persistence.RecommendationRepository;
import se.magnus.util.http.ServiceUtil;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

  private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

  private final RecommendationRepository repository;

  private final RecommendationMapper mapper;

  private final ServiceUtil serviceUtil;

  public RecommendationServiceImpl(RecommendationRepository repository, RecommendationMapper mapper, ServiceUtil serviceUtil) {
    this.repository = repository;
    this.mapper = mapper;
    this.serviceUtil = serviceUtil;
  }

  @Override
  // ★ 변경 시작 (전 2줄 → 4줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: public Recommendation createRecommendation(Recommendation body) {
  // ★ 전: try {
  public Mono<Recommendation> createRecommendation(Recommendation body) {

    if (body.getProductId() < 1) {
      throw new InvalidInputException("Invalid productId: " + body.getProductId());
    }
  // ★ 변경 끝

    RecommendationEntity entity = mapper.apiToEntity(body);
    // ★ 변경 시작 (전 1줄 → 6줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: RecommendationEntity newEntity = repository.save(entity);
    Mono<Recommendation> newEntity = repository.save(entity)
      .log(LOG.getName(), FINE)
      .onErrorMap(
        DuplicateKeyException.class,
        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId()))
      .map(e -> mapper.entityToApi(e));
    // ★ 변경 끝

    // ★ 변경 시작 (전 5줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: LOG.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());
    // ★ 전: return mapper.entityToApi(newEntity);
    // ★ 전: } catch (DuplicateKeyException dke) {
    // ★ 전: throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId());
    // ★ 전: }
    return newEntity;
    // ★ 변경 끝
  }

  @Override
  public Flux<Recommendation> getRecommendations(int productId) {  // ★ 변경 (전: public List<Recommendation> getRecommendations(int productId) {)

    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    // ★ 변경 시작 (전 3줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: List<RecommendationEntity> entityList = repository.findByProductId(productId);
    // ★ 전: List<Recommendation> list = mapper.entityListToApiList(entityList);
    // ★ 전: list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));
    LOG.info("Will get recommendations for product with id={}", productId);
    // ★ 변경 끝

    // ★ 변경 시작 (전 2줄 → 4줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: LOG.debug("getRecommendations: response size: {}", list.size());
    // ★ 전: return list;
    return repository.findByProductId(productId)
      .log(LOG.getName(), FINE)
      .map(e -> mapper.entityToApi(e))
      .map(e -> setServiceAddress(e));
    // ★ 변경 끝
  }

  @Override
  // ★ 변경 시작 (전 1줄 → 4줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: public void deleteRecommendations(int productId) {
  public Mono<Void> deleteRecommendations(int productId) {

    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }
  // ★ 변경 끝

    LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
    // ★ 변경 시작 (전 1줄 → 5줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: repository.deleteAll(repository.findByProductId(productId));
    return repository.deleteAll(repository.findByProductId(productId));
  }

  private Recommendation setServiceAddress(Recommendation e) {
    e.setServiceAddress(serviceUtil.getServiceAddress());
    return e;
    // ★ 변경 끝
  }
}