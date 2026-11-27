package se.magnus.microservices.core.recommendation.services;

// ★ 삭제: import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;  // ★ 추가
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.exceptions.InvalidInputException;
// ★ 추가 시작 (2줄)
import se.magnus.microservices.core.recommendation.persistence.RecommendationEntity;
import se.magnus.microservices.core.recommendation.persistence.RecommendationRepository;
// ★ 추가 끝
import se.magnus.util.http.ServiceUtil;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

  private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

  // ★ 추가 시작 (2줄)
  private final RecommendationRepository repository;

  private final RecommendationMapper mapper;
  // ★ 추가 끝

  private final ServiceUtil serviceUtil;

  // ★ 변경 시작 (전 1줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: public RecommendationServiceImpl(ServiceUtil serviceUtil) {
  public RecommendationServiceImpl(RecommendationRepository repository, RecommendationMapper mapper, ServiceUtil serviceUtil) {
    this.repository = repository;
    this.mapper = mapper;
  // ★ 변경 끝
    this.serviceUtil = serviceUtil;
  }

  // ★ 추가 시작 (11줄)
  @Override
  public Recommendation createRecommendation(Recommendation body) {
    try {
      RecommendationEntity entity = mapper.apiToEntity(body);
      RecommendationEntity newEntity = repository.save(entity);

      LOG.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());
      return mapper.entityToApi(newEntity);

    } catch (DuplicateKeyException dke) {
      throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId());
    }
  }
  // ★ 추가 끝

  @Override
  public List<Recommendation> getRecommendations(int productId) {

    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    // ★ 변경 시작 (전 4줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: if (productId == 113) {
    // ★ 전: LOG.debug("No recommendations found for productId: {}", productId);
    // ★ 전: return new ArrayList<>();
    // ★ 전: }
    List<RecommendationEntity> entityList = repository.findByProductId(productId);
    List<Recommendation> list = mapper.entityListToApiList(entityList);
    list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));
    // ★ 변경 끝

    // ★ 변경 시작 (전 5줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: List<Recommendation> list = new ArrayList<>();
    // ★ 전: list.add(new Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()));
    // ★ 전: list.add(new Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()));
    // ★ 전: list.add(new Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));
    // ★ 전: LOG.debug("/recommendation response size: {}", list.size());
    LOG.debug("getRecommendations: response size: {}", list.size());
    // ★ 변경 끝

    return list;
  }

  // ★ 추가 시작 (4줄)
  @Override
  public void deleteRecommendations(int productId) {
    LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
    repository.deleteAll(repository.findByProductId(productId));
  // ★ 추가 끝
  }
}  // ★ 추가