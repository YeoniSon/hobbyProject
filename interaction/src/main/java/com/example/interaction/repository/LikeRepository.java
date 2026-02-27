package com.example.interaction.repository;

import com.example.common.enums.TargetType;
import com.example.interaction.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    /** targetId(Long) + user(User)의 id로 조회 */
    Optional<Like> findByTargetTypeAndTargetIdAndUser_Id(TargetType targetType, Long targetId, Long userId);

    /** targetId(Long) 별로 좋아요 수 */
    int countByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    /** user(User)의 id + targetId로 존재 여부 */
    boolean existsByTargetTypeAndUser_IdAndTargetId(TargetType targetType,Long userId, Long targetId);
}
