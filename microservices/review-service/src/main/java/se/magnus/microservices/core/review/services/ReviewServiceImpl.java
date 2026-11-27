package se.magnus.microservices.core.review.services;

// ★ 삭제: import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;  // ★ 추가
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.exceptions.InvalidInputException;
// ★ 추가 시작 (2줄)
import se.magnus.microservices.core.review.persistence.ReviewEntity;
import se.magnus.microservices.core.review.persistence.ReviewRepository;
// ★ 추가 끝
import se.magnus.util.http.ServiceUtil;

@RestController
public class ReviewServiceImpl implements ReviewService {

  private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

  // ★ 추가 시작 (2줄)
  private final ReviewRepository repository;

  private final ReviewMapper mapper;
  // ★ 추가 끝

  private final ServiceUtil serviceUtil;

  // ★ 변경 시작 (전 1줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: public ReviewServiceImpl(ServiceUtil serviceUtil) {
  public ReviewServiceImpl(ReviewRepository repository, ReviewMapper mapper, ServiceUtil serviceUtil) {
    this.repository = repository;
    this.mapper = mapper;
  // ★ 변경 끝
    this.serviceUtil = serviceUtil;
  }

  // ★ 추가 시작 (11줄)
  @Override
  public Review createReview(Review body) {
    try {
      ReviewEntity entity = mapper.apiToEntity(body);
      ReviewEntity newEntity = repository.save(entity);

      LOG.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
      return mapper.entityToApi(newEntity);

    } catch (DataIntegrityViolationException dive) {
      throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Review Id:" + body.getReviewId());
    }
  }
  // ★ 추가 끝

  @Override
  public List<Review> getReviews(int productId) {

    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    // ★ 변경 시작 (전 4줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: if (productId == 213) {
    // ★ 전: LOG.debug("No reviews found for productId: {}", productId);
    // ★ 전: return new ArrayList<>();
    // ★ 전: }
    List<ReviewEntity> entityList = repository.findByProductId(productId);
    List<Review> list = mapper.entityListToApiList(entityList);
    list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));
    // ★ 변경 끝

    // ★ 변경 시작 (전 5줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: List<Review> list = new ArrayList<>();
    // ★ 전: list.add(new Review(productId, 1, "Author 1", "Subject 1", "Content 1", serviceUtil.getServiceAddress()));
    // ★ 전: list.add(new Review(productId, 2, "Author 2", "Subject 2", "Content 2", serviceUtil.getServiceAddress()));
    // ★ 전: list.add(new Review(productId, 3, "Author 3", "Subject 3", "Content 3", serviceUtil.getServiceAddress()));
    // ★ 전: LOG.debug("/reviews response size: {}", list.size());
    LOG.debug("getReviews: response size: {}", list.size());
    // ★ 변경 끝

    return list;
  }

  // ★ 추가 시작 (4줄)
  @Override
  public void deleteReviews(int productId) {
    LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
    repository.deleteAll(repository.findByProductId(productId));
  // ★ 추가 끝
  }
}  // ★ 추가