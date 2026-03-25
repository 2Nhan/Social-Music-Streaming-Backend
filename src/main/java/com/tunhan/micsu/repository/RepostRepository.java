package com.tunhan.micsu.repository;

import com.tunhan.micsu.entity.Repost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepostRepository extends JpaRepository<Repost, String> {
    Page<Repost> findByUserId(String userId, Pageable pageable);

    Optional<Repost> findByUserIdAndSongId(String userId, String songId);

    boolean existsByUserIdAndSongId(String userId, String songId);
}
