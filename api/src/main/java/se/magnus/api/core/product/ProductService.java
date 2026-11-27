package se.magnus.api.core.product;

// ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import org.springframework.web.bind.annotation.GetMapping;
// ★ 전: import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.*;
// ★ 변경 끝

public interface ProductService {

  // ★ 추가 시작 (5줄) — POST: 생성
  @PostMapping(
    value    = "/product",
    consumes = "application/json",
    produces = "application/json")
  Product createProduct(@RequestBody Product body);
  // ★ 추가 끝

  @GetMapping(
    value = "/product/{productId}",
    // ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: produces = "application/json"
    // ★ 전: )
    produces = "application/json")
    // ★ 변경 끝
  Product getProduct(@PathVariable int productId);

  // ★ 추가 시작 (2줄) — DELETE: 삭제
  @DeleteMapping(value = "/product/{productId}")
  void deleteProduct(@PathVariable int productId);
  // ★ 추가 끝
}