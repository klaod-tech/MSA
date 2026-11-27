package se.magnus.microservices.core.recommendation.persistence;

// ★ 변경 시작 (전 2줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import java.util.List;
// ★ 전: import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
// ★ 변경 끝

// ★ 변경 시작 (전 2줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: public interface RecommendationRepository extends CrudRepository<RecommendationEntity, String> {
// ★ 전: List<RecommendationEntity> findByProductId(int productId);
public interface RecommendationRepository extends ReactiveCrudRepository<RecommendationEntity, String> {
  Flux<RecommendationEntity> findByProductId(int productId);
// ★ 변경 끝
}