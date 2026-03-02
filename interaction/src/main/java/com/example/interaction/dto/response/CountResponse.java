package com.example.interaction.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CountResponse {

    private int count;

    public static CountResponse from(int count) {
        return CountResponse.builder()
                .count(count)
                .build();
    }
}
