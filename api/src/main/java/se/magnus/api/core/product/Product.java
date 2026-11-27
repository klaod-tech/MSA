package se.magnus.api.core.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;  // ★ 추가

@Getter
@Setter  // ★ 추가 — 교재: setter 메서드를 직접 작성
@AllArgsConstructor
@NoArgsConstructor(force = true)

public class Product {
    // ★ 변경 시작 (전 4줄 → 4줄) — "전:" 줄은 지우고 그 아래 줄로 바꾼다
    // ★ 전: private final int productId;
    // ★ 전: private final String name;
    // ★ 전: private final int weight;
    // ★ 전: private final String serviceAddress;
    private int productId;
    private String name;
    private int weight;
    private String serviceAddress;
    // ★ 변경 끝
}