package se.magnus.api.core.review;

// ★ 삭제: import java.util.List;
import org.springframework.web.bind.annotation.*;
// ★ 추가 시작 (2줄)
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
// ★ 추가 끝

public interface ReviewService {

  // ★ 변경 시작 (전 5줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: @PostMapping(
  // ★ 전: value    = "/review",
  // ★ 전: consumes = "application/json",
  // ★ 전: produces = "application/json")
  // ★ 전: Review createReview(@RequestBody Review body);
  Mono<Review> createReview(Review body);
  // ★ 변경 끝

  @GetMapping(
    value = "/review",
    produces = "application/json")
  // ★ 변경 시작 (전 1줄 → 2줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: List<Review> getReviews(@RequestParam(value = "productId", required = true) int productId);
  Flux<Review> getReviews(
   @RequestParam(value = "productId", required = true) int productId);
  // ★ 변경 끝

  // ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: @DeleteMapping(value = "/review")
  // ★ 전: void deleteReviews(@RequestParam(value = "productId", required = true)  int productId);
  Mono<Void> deleteReviews(int productId);
  // ★ 변경 끝
}