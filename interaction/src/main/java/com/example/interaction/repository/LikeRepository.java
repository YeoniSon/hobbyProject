package com.example.interaction.repository;

import com.example.interaction.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    /** targetId(Long) + user(User)의 id로 조회 */
    Optional<Like> findByTargetIdAndUser_Id(Long targetId, Long userId);

    /** user(User)의 id + targetId로 존재 여부 */
    boolean existsByUser_IdAndTargetId(Long userId, Long targetId);
}
