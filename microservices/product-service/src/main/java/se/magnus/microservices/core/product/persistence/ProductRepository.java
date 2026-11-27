package se.magnus.microservices.core.product.persistence;

// ★ 변경 시작 (전 3줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import java.util.Optional;
// ★ 전: import org.springframework.data.repository.CrudRepository;
// ★ 전: import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
// ★ 변경 끝

// ★ 변경 시작 (전 2줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: public interface ProductRepository extends PagingAndSortingRepository<ProductEntity, String>, CrudRepository<ProductEntity, String> {
// ★ 전: Optional<ProductEntity> findByProductId(int productId);
public interface ProductRepository extends ReactiveCrudRepository<ProductEntity, String> {
  Mono<ProductEntity> findByProductId(int productId);
// ★ 변경 끝
}