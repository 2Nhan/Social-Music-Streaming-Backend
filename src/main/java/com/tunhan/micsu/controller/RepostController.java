package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.response.ApiResponse;
import com.tunhan.micsu.dto.response.RepostResponse;
import com.tunhan.micsu.service.repost.RepostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/songs/{songId}/repost")
public class RepostController {

    private final RepostService repostService;

    @PostMapping
    public ResponseEntity<ApiResponse<RepostResponse>> repost(
            @PathVariable String songId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success("Reposted successfully",
                repostService.repost(songId, jwt.getSubject())));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> unrepost(
            @PathVariable String songId,
            @AuthenticationPrincipal Jwt jwt) {
        repostService.unrepost(songId, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success("Unreposted successfully", null));
    }
}
