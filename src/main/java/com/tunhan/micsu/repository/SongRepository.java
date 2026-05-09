package com.tunhan.micsu.repository;

import com.tunhan.micsu.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, String> {
    Page<Song> findByUploadedBy(String uploadedBy, Pageable pageable);

    Page<Song> findByGenreId(String genreId, Pageable pageable);

    @Modifying
    @Query("UPDATE Song s SET s.favoriteCount = s.favoriteCount + 1 WHERE s.id = :songId")
    void incrementFavoriteCount(@Param("songId") String songId);

    @Modifying
    @Query("UPDATE Song s SET s.favoriteCount = s.favoriteCount - 1 WHERE s.id = :songId AND s.favoriteCount > 0")
    void decrementFavoriteCount(@Param("songId") String songId);

    @Query(value = """
    SELECT title
    FROM songs
    WHERE LOWER(title) LIKE LOWER(CONCAT(:query, '%'))
    ORDER BY title ASC
    LIMIT 10
    """, nativeQuery = true)
    List<String> autocompleteSearch(@Param("query") String query);

    @Query("""
    SELECT s
    FROM Song s
    JOIN FETCH s.uploader u
    WHERE 
        LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    Page<Song> searchResults(@Param("query") String query, Pageable pageable);
}
