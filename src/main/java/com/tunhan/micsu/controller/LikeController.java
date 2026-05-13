package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.response.ApiResponse;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.service.like.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Likes", description = "Like songs.")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/v1/likes/songs/{songId}")
    public ResponseEntity<ApiResponse<SongResponse>> likeSong(
            @PathVariable String songId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("[LikeController] User {} is liking song {}", userId, songId);

        SongResponse response = likeService.likeSong(songId, userId);
        return ResponseEntity.ok(ApiResponse.success("Song liked successfully", response));
    }

    @DeleteMapping("/v1/likes/songs/{songId}")
    public ResponseEntity<ApiResponse<SongResponse>> unlikeSong(
            @PathVariable String songId,
            @AuthenticationPrincipal Jwt jwt) {
        SongResponse response = likeService.unlikeSong(songId, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success("Song unliked successfully", response));
    }

    @GetMapping("/v1/likes/songs")
    public ResponseEntity<ApiResponse<PageResponse<SongResponse>>> getUserLikedSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("[LikeController] Fetching liked songs for user {}", userId);
        PageResponse<SongResponse> response = likeService.getUserLikedSongs(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Liked songs retrieved successfully", response));
    }
}
