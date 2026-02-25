package com.example.dto.response;

import com.example.domain.Post;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostDataResponse {
    private Long postId;
    private String posterEmail;
    private String categoryName;
    private String title;
    private String content;
    private boolean show;

    public static PostDataResponse from(Post post) {
        return PostDataResponse.builder()
                .postId(post.getId())
                .posterEmail(post.getUserId().getEmail())
                .categoryName(post.getCategoryId().getName())
                .title(post.getTitle())
                .content(post.getContent())
                .show(post.isShow())
                .build();
    }
}
