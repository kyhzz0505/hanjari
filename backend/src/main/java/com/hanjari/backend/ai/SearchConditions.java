package com.hanjari.backend.ai;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 문장에서 뽑은 검색 조건 (docs/api-spec.md 3번의 conditions)
public record SearchConditions(String district, Integer people, boolean room) {

    private static final int MAX_PEOPLE = 200;
    private static final Pattern PEOPLE_PATTERN = Pattern.compile("(\\d{1,3})\\s*(명|인)");
    // 마포구 안의 동네 이름으로 말해도 마포구로 본다
    private static final List<String> MAPO_WORDS = List.of("마포", "홍대", "합정", "연남", "망원", "상수", "서교", "동교");

    // AI가 준 값을 정리한다: "마포"·"홍대" → "마포구", 말이 안 되는 인원 → null
    public SearchConditions normalized() {
        String d = (district == null || district.isBlank()) ? null : district.trim();
        if (d != null && MAPO_WORDS.stream().anyMatch(d::contains)) {
            d = "마포구";
        }
        Integer p = (people == null || people < 1 || people > MAX_PEOPLE) ? null : people;
        return new SearchConditions(d, p, room);
    }

    // AI 호출이 실패했을 때 쓰는 간단한 규칙 기반 추출 ("10명", "룸", "홍대" 같은 단어만 본다)
    public static SearchConditions fromText(String text) {
        Matcher m = PEOPLE_PATTERN.matcher(text);
        Integer people = m.find() ? Integer.valueOf(m.group(1)) : null;
        String district = MAPO_WORDS.stream().anyMatch(text::contains) ? "마포구" : null;
        boolean room = text.contains("룸");
        return new SearchConditions(district, people, room).normalized();
    }
}
