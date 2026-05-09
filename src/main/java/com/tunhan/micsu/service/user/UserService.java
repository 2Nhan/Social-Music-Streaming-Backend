package com.tunhan.micsu.service.user;

import com.tunhan.micsu.dto.request.UpdateProfileRequest;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.dto.response.UserProfileResponse;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface UserService {
    UserProfileResponse getUserById(String id);

    UserProfileResponse updateProfile(String id, UpdateProfileRequest request, String currentUserId) throws IOException;

    PageResponse<SongResponse> getUserSongs(String userId, Pageable pageable);
}
