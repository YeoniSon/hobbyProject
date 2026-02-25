package com.example.domain;

import com.example.common.entity.BaseShowEntity;
import com.example.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment extends BaseShowEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    public void updateContent(String content) {
        this.content = content;
    }
}
