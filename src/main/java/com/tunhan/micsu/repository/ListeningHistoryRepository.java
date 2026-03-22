package com.tunhan.micsu.repository;

import com.tunhan.micsu.entity.ListeningHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, String> {
    Page<ListeningHistory> findByUserIdOrderByListenedAtDesc(String userId, Pageable pageable);
}
