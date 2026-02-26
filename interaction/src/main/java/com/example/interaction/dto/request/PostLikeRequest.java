package com.example.interaction.dto.request;

import com.example.domain.Post;
import com.example.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeRequest {
    private User user;
    private Post targetId;
}
