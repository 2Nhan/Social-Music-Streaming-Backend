package com.tunhan.micsu.repository;

import com.tunhan.micsu.entity.SongFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongFavoriteRepository extends JpaRepository<SongFavorite, String> {

    boolean existsByUserIdAndSongId(String userId, String songId);
    
    /**
     * Find all users who liked a specific song.
     * Used for cache reload from database.
     */
    @Query("SELECT sf FROM SongFavorite sf WHERE sf.song.id = :songId")
    List<SongFavorite> findBySongId(@Param("songId") String songId);
}
