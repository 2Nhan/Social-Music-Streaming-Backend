package com.tunhan.micsu.service.like;

import com.tunhan.micsu.dto.request.LikeRequest;
import com.tunhan.micsu.dto.response.LikeResponse;
import com.tunhan.micsu.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface LikeService {
    LikeResponse like(LikeRequest request, String userId);

    void unlike(String likeId, String userId);

    PageResponse<LikeResponse> getUserLikes(String userId, Pageable pageable);
}
