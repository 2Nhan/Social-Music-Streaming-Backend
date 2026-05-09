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
import com.tunhan.micsu.repository.SongFavoriteRepository;
import com.tunhan.micsu.repository.SongRepository;
import com.tunhan.micsu.repository.UserRepository;
import com.tunhan.micsu.mapper.UserMapper;
import com.tunhan.micsu.service.R2StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final SongFavoriteRepository songFavoriteRepository;
    private final SongMapper songMapper;
    private final UserMapper userMapper;
    private final R2StorageService r2StorageService;

    @Override
    public UserProfileResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return userMapper.toUserProfileResponse(user);
    }

    @Override
    public UserProfileResponse updateProfile(String id, UpdateProfileRequest request, String currentUserId) throws IOException {
        if (!id.equals(currentUserId)) {
            throw new AccessDeniedException("You can only edit your own profile");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (request.getBio() != null)
            user.setBio(request.getBio());
        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            user.setAvatarUrl(r2StorageService.uploadAvatar(request.getAvatarFile(), id));
        }

        userRepository.save(user);
        log.info("[UserService] Updated profile for user: {}", id);
        return userMapper.toUserProfileResponse(user);
    }

    @Override
    public PageResponse<SongResponse> getUserSongs(String userId, Pageable pageable, String requesterId) {
        Page<Song> page = songRepository.findByUploadedBy(userId, pageable);

        List<Song> songs = page.getContent();
        Set<String> likedSongIds = requesterId != null && !songs.isEmpty()
            ? songFavoriteRepository.findLikedSongIdsByUserIdAndSongIds(
                requesterId,
                songs.stream().map(Song::getId).toList())
            : Collections.emptySet();

        return PageResponse.<SongResponse>builder()
                .content(songs.stream()
                        .map(song -> songMapper.toSongResponse(song,
                                requesterId == null ? null : likedSongIds.contains(song.getId())))
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }



}
