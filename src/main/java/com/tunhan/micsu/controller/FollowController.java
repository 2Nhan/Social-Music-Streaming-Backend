package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.response.ApiResponse;
import com.tunhan.micsu.service.follow.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/follow")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> follow(
            @PathVariable String userId,
            @AuthenticationPrincipal Jwt jwt) {
        followService.follow(userId, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success("Followed user successfully", null));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @PathVariable String userId,
            @AuthenticationPrincipal Jwt jwt) {
        followService.unfollow(userId, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success("Unfollowed user successfully", null));
    }
}
