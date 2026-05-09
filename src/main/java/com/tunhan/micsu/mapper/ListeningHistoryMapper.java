package com.tunhan.micsu.mapper;

import com.tunhan.micsu.dto.response.ListeningHistoryResponse;
import com.tunhan.micsu.entity.ListeningHistory;
import org.springframework.stereotype.Component;

@Component
public class ListeningHistoryMapper {

    public ListeningHistoryResponse toListeningHistoryResponse(ListeningHistory history) {
        if (history == null) return null;
        return ListeningHistoryResponse.builder()
                .id(history.getId())
                .songId(history.getSongId())
                .listenedAt(history.getListenedAt() != null ? history.getListenedAt().toString() : null)
                .build();
    }
}
