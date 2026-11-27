package se.magnus.api.core.recommendation;

import java.util.List;
// ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import org.springframework.web.bind.annotation.GetMapping;
// ★ 전: import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;
// ★ 변경 끝

public interface RecommendationService {

  // ★ 추가 시작 (5줄) — POST: 생성
  @PostMapping(
    value    = "/recommendation",
    consumes = "application/json",
    produces = "application/json")
  Recommendation createRecommendation(@RequestBody Recommendation body);
  // ★ 추가 끝

  @GetMapping(
    value = "/recommendation",
    produces = "application/json")
  List<Recommendation> getRecommendations(
    @RequestParam(value = "productId", required = true) int productId);

  // ★ 추가 시작 (2줄) — DELETE: 삭제
  @DeleteMapping(value = "/recommendation")
  void deleteRecommendations(@RequestParam(value = "productId", required = true)  int productId);
  // ★ 추가 끝
}