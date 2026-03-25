package com.tunhan.micsu.repository;

import com.tunhan.micsu.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    Page<Comment> findBySongIdOrderByCreatedAtAsc(String songId, Pageable pageable);
}
