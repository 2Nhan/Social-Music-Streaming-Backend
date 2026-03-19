package com.tunhan.micsu.repository;

import com.tunhan.micsu.entity.Like;
import com.tunhan.micsu.entity.enums.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, String> {
    Optional<Like> findByUserIdAndTargetTypeAndTargetId(String userId, TargetType targetType, String targetId);

    Page<Like> findByUserId(String userId, Pageable pageable);

    boolean existsByUserIdAndTargetTypeAndTargetId(String userId, TargetType targetType, String targetId);

    long countByTargetTypeAndTargetId(TargetType targetType, String targetId);
}
