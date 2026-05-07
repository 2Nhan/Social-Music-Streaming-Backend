package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.response.ApiResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/v1/like/songs/{songId}")
    public ResponseEntity<ApiResponse<SongResponse>> likeSong(
            @PathVariable String songId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("[LikeController] User {} is liking song {}", userId, songId);
        
        SongResponse response = likeService.likeSong(songId, userId);
        return ResponseEntity.ok(ApiResponse.success("Song liked successfully", response));
    }

    @PostMapping("/v2/like/songs/{songId}")
    public ResponseEntity<ApiResponse<SongResponse>> likeSongV2(
            @PathVariable String songId,
            @AuthenticationPrincipal Jwt jwt) {
        likeService.like(songId, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success("Song liked successfully", null));
    }

    @DeleteMapping("/v1/unlike/songs/{songId}")
    public ResponseEntity<ApiResponse<Void>> unlikeSong(
            @PathVariable String songId,
            @AuthenticationPrincipal Jwt jwt) {
         likeService.unlike(songId, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success("Song unliked successfully", null));
    }
}
