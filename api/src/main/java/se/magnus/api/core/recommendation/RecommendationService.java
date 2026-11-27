package se.magnus.api.core.recommendation;

// ★ 삭제: import java.util.List;
import org.springframework.web.bind.annotation.*;
// ★ 추가 시작 (2줄)
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
// ★ 추가 끝

public interface RecommendationService {

  // ★ 변경 시작 (전 5줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: @PostMapping(
  // ★ 전: value    = "/recommendation",
  // ★ 전: consumes = "application/json",
  // ★ 전: produces = "application/json")
  // ★ 전: Recommendation createRecommendation(@RequestBody Recommendation body);
  Mono<Recommendation> createRecommendation(Recommendation body);
  // ★ 변경 끝

  @GetMapping(
    value = "/recommendation",
    produces = "application/json")
  Flux<Recommendation> getRecommendations(  // ★ 변경 (전: List<Recommendation> getRecommendations()
    @RequestParam(value = "productId", required = true) int productId);

  // ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: @DeleteMapping(value = "/recommendation")
  // ★ 전: void deleteRecommendations(@RequestParam(value = "productId", required = true)  int productId);
  Mono<Void> deleteRecommendations(int productId);
  // ★ 변경 끝
}