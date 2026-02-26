package com.example.interaction.domain;

import com.example.common.entity.BaseEntity;
import com.example.common.enums.TargetType;
import com.example.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "like")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Like extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @Column(name = "target_type",nullable = false)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;


}
