package com.tunhan.micsu.repository;

import com.tunhan.micsu.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongRepository extends JpaRepository<Song, String> {
    Page<Song> findByUploadedBy(String uploadedBy, Pageable pageable);
}
