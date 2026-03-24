package com.tunhan.micsu.repository;

import com.tunhan.micsu.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, String> {

    /** Khi lấy following list → cần load User follower (người đang follow) */
    @EntityGraph(attributePaths = "follower")
    Page<Follow> findByFollowingId(String followingId, Pageable pageable);

    /** Khi lấy followers list → cần load User following (người được follow) */
    @EntityGraph(attributePaths = "following")
    Page<Follow> findByFollowerId(String followerId, Pageable pageable);

    Optional<Follow> findByFollowerIdAndFollowingId(String followerId, String followingId);

    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);
}
