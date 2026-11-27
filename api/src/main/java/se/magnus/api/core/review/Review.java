package se.magnus.api.core.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;  // ★ 추가

@Getter
@Setter  // ★ 추가 — 교재: setter 메서드를 직접 작성
@AllArgsConstructor
@NoArgsConstructor(force = true)

public class Review {
  // ★ 변경 시작 (전 6줄 → 6줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
  // ★ 전: private final int productId;
  // ★ 전: private final int reviewId;
  // ★ 전: private final String author;
  // ★ 전: private final String subject;
  // ★ 전: private final String content;
  // ★ 전: private final String serviceAddress;
  private int productId;
  private int reviewId;
  private String author;
  private String subject;
  private String content;
  private String serviceAddress;
  // ★ 변경 끝
}