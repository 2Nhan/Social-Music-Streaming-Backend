package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.request.UpdateProfileRequest;
import com.tunhan.micsu.dto.response.*;
import com.tunhan.micsu.exception.AccessDeniedException;
import com.tunhan.micsu.service.follow.FollowService;
import com.tunhan.micsu.service.like.LikeService;
import com.tunhan.micsu.service.playlist.PlaylistService;
import com.tunhan.micsu.service.repost.RepostService;
import com.tunhan.micsu.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final FollowService followService;
    private final LikeService likeService;
    private final RepostService repostService;
    private final PlaylistService playlistService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @PathVariable String id,
            @ModelAttribute UpdateProfileRequest request,
            @AuthenticationPrincipal Jwt jwt) throws IOException {
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully",
                userService.updateProfile(id, request, jwt.getSubject())));
    }

    @GetMapping("/{id}/songs")
    public ResponseEntity<ApiResponse<PageResponse<SongResponse>>> getUserSongs(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @AuthenticationPrincipal Jwt jwt) {
        String requesterId = jwt != null ? jwt.getSubject() : null;
        return ResponseEntity.ok(ApiResponse.success(
            userService.getUserSongs(id, buildPageable(page, size, sort), requesterId)));
    }

    @GetMapping("/me/playlists")
    public ResponseEntity<ApiResponse<PageResponse<PlaylistResponse>>> getMyPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(
                playlistService.getAllPlaylists(jwt.getSubject(), PageRequest.of(page, size))));
    }

    @GetMapping("/{id}/playlists")
    public ResponseEntity<ApiResponse<PageResponse<PlaylistResponse>>> getUserPlaylists(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {
        if (!id.equals(jwt.getSubject())) {
            throw new AccessDeniedException("You can only view your own playlists");
        }
        return ResponseEntity.ok(ApiResponse.success(
                playlistService.getAllPlaylists(id, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}/playlists/public")
    public ResponseEntity<ApiResponse<PageResponse<PlaylistResponse>>> getUserPublicPlaylists(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                playlistService.getPublicPlaylistsByUser(id, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<ApiResponse<PageResponse<UserProfileResponse>>> getFollowers(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                followService.getFollowers(id, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<ApiResponse<PageResponse<UserProfileResponse>>> getFollowing(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                followService.getFollowing(id, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}/reposts")
    public ResponseEntity<ApiResponse<PageResponse<RepostResponse>>> getUserReposts(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(repostService.getUserReposts(id, pageable)));
    }

    private Pageable buildPageable(int page, int size, String sort) {
        String[] parts = sort.split(",");
        Sort.Direction dir = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(dir, parts[0]));
    }
}
