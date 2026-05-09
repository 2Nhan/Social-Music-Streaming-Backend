package com.tunhan.micsu.repository;

import com.tunhan.micsu.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, String> {
    List<PlaylistSong> findByPlaylistIdOrderByPosition(String playlistId);

    @Query("""
            SELECT ps
            FROM PlaylistSong ps
            JOIN FETCH ps.song
            WHERE ps.playlistId = :playlistId
            ORDER BY ps.position
            """)
    List<PlaylistSong> findWithSongByPlaylistIdOrderByPosition(@Param("playlistId") String playlistId);

    Optional<PlaylistSong> findByPlaylistIdAndSongId(String playlistId, String songId);

    void deleteByPlaylistIdAndSongId(String playlistId, String songId);

    long countByPlaylistId(String playlistId);
}
