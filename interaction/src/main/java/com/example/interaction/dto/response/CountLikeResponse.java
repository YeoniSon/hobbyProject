package com.example.interaction.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CountLikeResponse {

    private int count;

    public static CountLikeResponse from(int count) {
        return CountLikeResponse.builder()
                .count(count)
                .build();
    }
}
