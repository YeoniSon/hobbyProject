package com.example.interaction.dto.response;


import com.example.common.enums.TargetType;
import com.example.domain.Comment;
import com.example.domain.Post;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikeDataResponse {

    private Long targetId;
    private TargetType targetType;
    private String content; // Comment 일 경우 사용
    private String title; // Post 일 경우 사용

    public static LikeDataResponse fromPost(Post post) {
        return LikeDataResponse.builder()
                .targetId(post.getId())
                .targetType(TargetType.POST)
                .title(post.getTitle())
                .build();
    }

    public static LikeDataResponse fromComment(Comment comment) {
        return LikeDataResponse.builder()
                .targetId(comment.getId())
                .targetType(TargetType.COMMENT)
                .content(comment.getContent())
                .build();
    }
}
