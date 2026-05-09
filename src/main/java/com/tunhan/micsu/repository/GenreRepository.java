package com.tunhan.micsu.repository;

import com.tunhan.micsu.entity.Genre;
import com.tunhan.micsu.entity.enums.GenreName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, String> {
    Optional<Genre> findByName(GenreName name);

    boolean existsByName(GenreName name);
}
