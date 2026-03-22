package com.tunhan.micsu.service.history;

import com.tunhan.micsu.dto.response.ListeningHistoryResponse;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.entity.ListeningHistory;
import com.tunhan.micsu.exception.ResourceNotFoundException;
import com.tunhan.micsu.repository.ListeningHistoryRepository;
import com.tunhan.micsu.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListeningHistoryServiceImpl implements ListeningHistoryService {

    private final ListeningHistoryRepository historyRepository;
    private final SongRepository songRepository;

    @Override
    public void logListen(String songId, String userId) {
        if (!songRepository.existsById(songId)) {
            throw new ResourceNotFoundException("Song", songId);
        }
        ListeningHistory entry = ListeningHistory.builder()
                .userId(userId)
                .songId(songId)
                .listenedAt(LocalDateTime.now())
                .build();
        historyRepository.save(entry);
        log.info("[ListeningHistoryService] Logged listen for user {} on song {}", userId, songId);
    }

    @Override
    public PageResponse<ListeningHistoryResponse> getUserHistory(String userId, Pageable pageable) {
        Page<ListeningHistory> page = historyRepository.findByUserIdOrderByListenedAtDesc(userId, pageable);
        return PageResponse.<ListeningHistoryResponse>builder()
                .content(page.getContent().stream().map(this::toHistoryResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }

    private ListeningHistoryResponse toHistoryResponse(ListeningHistory h) {
        return ListeningHistoryResponse.builder()
                .id(h.getId())
                .songId(h.getSongId())
                .listenedAt(h.getListenedAt() != null ? h.getListenedAt().toString() : null)
                .build();
    }
}
