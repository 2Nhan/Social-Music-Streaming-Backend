package com.tunhan.micsu.repository;

import com.tunhan.micsu.entity.SongFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SongFavoriteRepository extends JpaRepository<SongFavorite, String> {

    boolean existsByUserIdAndSongId(String userId, String songId);

    Optional<SongFavorite> findByUserIdAndSongId(String userId, String songId);
    
    /**
     * Find all users who liked a specific song.
     * Used for cache reload from database.
     */
    @Query("SELECT sf FROM SongFavorite sf WHERE sf.song.id = :songId")
    List<SongFavorite> findBySongId(@Param("songId") String songId);

    @Query("""
            SELECT sf.song.id
            FROM SongFavorite sf
            WHERE sf.user.id = :userId AND sf.song.id IN :songIds
            """)
    Set<String> findLikedSongIdsByUserIdAndSongIds(@Param("userId") String userId,
                                                   @Param("songIds") List<String> songIds);

    /**
     * Find all songs liked by a specific user with pagination.
     */
    @Query("SELECT sf FROM SongFavorite sf WHERE sf.user.id = :userId ORDER BY sf.createdAt DESC")
    Page<SongFavorite> findByUserId(@Param("userId") String userId, Pageable pageable);
}
