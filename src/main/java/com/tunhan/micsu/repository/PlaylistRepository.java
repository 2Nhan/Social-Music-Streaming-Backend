package com.tunhan.micsu.repository;

import com.tunhan.micsu.entity.Playlist;
import com.tunhan.micsu.entity.enums.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, String> {
    Page<Playlist> findByCreatedBy(String createdBy, Pageable pageable);

    Page<Playlist> findAllByVisibility(Visibility visibility, Pageable pageable);
}
