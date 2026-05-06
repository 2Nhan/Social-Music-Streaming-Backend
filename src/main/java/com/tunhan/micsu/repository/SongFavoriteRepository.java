package com.tunhan.micsu.repository;

import com.tunhan.micsu.entity.SongFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongFavoriteRepository extends JpaRepository<SongFavorite, String> {

    boolean existsByUserIdAndSongId(String userId, String songId);
}
