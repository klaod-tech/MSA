package se.magnus.api.core.product;

// ★ 변경 시작 (전 1줄 → 3줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;
// ★ 변경 끝

public interface ProductService {

  // ★ 변경 시작 (전 5줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: @PostMapping(
  // ★ 전: value    = "/product",
  // ★ 전: consumes = "application/json",
  // ★ 전: produces = "application/json")
  // ★ 전: Product createProduct(@RequestBody Product body);
  Mono<Product> createProduct(Product body);
  // ★ 변경 끝

  @GetMapping(
    value = "/product/{productId}",
    produces = "application/json")
  Mono<Product> getProduct(@PathVariable int productId);  // ★ 변경 (전: Product getProduct(@PathVariable int productId);)

  // ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: @DeleteMapping(value = "/product/{productId}")
  // ★ 전: void deleteProduct(@PathVariable int productId);
  Mono<Void> deleteProduct(int productId);
  // ★ 변경 끝
}