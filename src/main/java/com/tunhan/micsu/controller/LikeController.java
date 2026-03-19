package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.request.LikeRequest;
import com.tunhan.micsu.dto.response.ApiResponse;
import com.tunhan.micsu.dto.response.LikeResponse;
import com.tunhan.micsu.service.like.LikeService;
import jakarta.validation.Valid;
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

    @PostMapping
    public ResponseEntity<ApiResponse<LikeResponse>> like(
            @Valid @RequestBody LikeRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success("Liked successfully",
                likeService.like(request, jwt.getSubject())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> unlike(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        likeService.unlike(id, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success("Unliked successfully", null));
    }
}
