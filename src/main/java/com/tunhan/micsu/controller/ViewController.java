package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.response.ApiResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.service.viewcounter.ViewCounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/views")
public class ViewController {

    private final ViewCounterService viewCounterService;

    @PatchMapping("/songs/{songId}")
    public ResponseEntity<ApiResponse<SongResponse>> increaseView(@PathVariable String songId,
                                                                  @AuthenticationPrincipal Jwt jwt) {
        String requesterId = jwt != null ? jwt.getSubject() : null;
        return ResponseEntity.ok(ApiResponse.success("Song view increased successfully",
                viewCounterService.increaseViewCount(songId, requesterId)));
    }
}
