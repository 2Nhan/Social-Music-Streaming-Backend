package com.tunhan.micsu.service.repost;

import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.RepostResponse;
import org.springframework.data.domain.Pageable;

public interface RepostService {
    RepostResponse repost(String songId, String userId);

    void unrepost(String songId, String userId);

    PageResponse<RepostResponse> getUserReposts(String userId, Pageable pageable);
}
