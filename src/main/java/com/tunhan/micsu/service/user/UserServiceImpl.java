package com.tunhan.micsu.service.user;

import com.tunhan.micsu.dto.request.UpdateProfileRequest;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.dto.response.UserProfileResponse;
import com.tunhan.micsu.entity.Song;
import com.tunhan.micsu.entity.User;
import com.tunhan.micsu.exception.AccessDeniedException;
import com.tunhan.micsu.exception.ResourceNotFoundException;
import com.tunhan.micsu.mapper.SongMapper;
import com.tunhan.micsu.repository.SongRepository;
import com.tunhan.micsu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final SongMapper songMapper;

    @Override
    public UserProfileResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toResponse(user);
    }

    @Override
    public UserProfileResponse updateProfile(String id, UpdateProfileRequest request, String currentUserId) {
        if (!id.equals(currentUserId)) {
            throw new AccessDeniedException("You can only edit your own profile");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (request.getBio() != null)
            user.setBio(request.getBio());
        if (request.getAvatarUrl() != null)
            user.setAvatarUrl(request.getAvatarUrl());

        userRepository.save(user);
        log.info("[UserService] Updated profile for user: {}", id);
        return toResponse(user);
    }

    @Override
    public PageResponse<SongResponse> getUserSongs(String userId, Pageable pageable) {
        Page<Song> page = songRepository.findByUploadedBy(userId, pageable);
        return PageResponse.<SongResponse>builder()
                .content(page.getContent().stream().map(songMapper::toSongResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }

    private UserProfileResponse toResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .songCount(user.getSongCount())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .build();
    }

}
