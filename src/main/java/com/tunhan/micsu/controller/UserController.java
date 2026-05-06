package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.request.UpdateProfileRequest;
import com.tunhan.micsu.dto.response.*;
import com.tunhan.micsu.service.follow.FollowService;
import com.tunhan.micsu.service.like.LikeService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final FollowService followService;
    private final LikeService likeService;
    private final RepostService repostService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @PathVariable String id,
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully",
                userService.updateProfile(id, request, jwt.getSubject())));
    }

    @GetMapping("/{id}/songs")
    public ResponseEntity<ApiResponse<PageResponse<SongResponse>>> getUserSongs(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getUserSongs(id, buildPageable(page, size, sort))));
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
