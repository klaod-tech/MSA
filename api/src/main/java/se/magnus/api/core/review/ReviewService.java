package se.magnus.api.core.review;

import java.util.List;
// ★ 변경 시작 (전 2줄 → 1줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
// ★ 전: import org.springframework.web.bind.annotation.GetMapping;
// ★ 전: import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;
// ★ 변경 끝

public interface ReviewService {

  // ★ 추가 시작 (5줄) — POST: 생성
  @PostMapping(
      value    = "/review",
      consumes = "application/json",
      produces = "application/json")
  Review createReview(@RequestBody Review body);
  // ★ 추가 끝

  @GetMapping(
    value = "/review",
    produces = "application/json")
  List<Review> getReviews(@RequestParam(value = "productId", required = true) int productId);

  // ★ 추가 시작 (2줄) — DELETE: 삭제
  @DeleteMapping(value = "/review")
  void deleteReviews(@RequestParam(value = "productId", required = true)  int productId);
  // ★ 추가 끝
}