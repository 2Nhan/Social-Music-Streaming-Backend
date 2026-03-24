package com.tunhan.micsu.service.follow;

import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.UserProfileResponse;
import org.springframework.data.domain.Pageable;

public interface FollowService {
    void follow(String targetUserId, String currentUserId);

    void unfollow(String targetUserId, String currentUserId);

    PageResponse<UserProfileResponse> getFollowers(String userId, Pageable pageable);

    PageResponse<UserProfileResponse> getFollowing(String userId, Pageable pageable);
}
