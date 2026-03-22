package com.tunhan.micsu.service.history;

import com.tunhan.micsu.dto.response.ListeningHistoryResponse;
import com.tunhan.micsu.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ListeningHistoryService {
    void logListen(String songId, String userId);

    PageResponse<ListeningHistoryResponse> getUserHistory(String userId, Pageable pageable);
}
