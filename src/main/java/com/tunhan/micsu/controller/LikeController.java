package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.response.ApiResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/likes")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/songs/{songId}")
    public ResponseEntity<ApiResponse<SongResponse>> likeSong(
            @PathVariable String songId,
            @AuthenticationPrincipal Jwt jwt) {
        SongResponse response = likeService.likeSong(songId, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success("Song liked successfully", response));
    }
}
