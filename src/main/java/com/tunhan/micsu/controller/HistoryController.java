package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.response.ApiResponse;
import com.tunhan.micsu.dto.response.ListeningHistoryResponse;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.service.history.ListeningHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
public class HistoryController {

    private final ListeningHistoryService historyService;

    @PostMapping("/history/{songId}")
    public ResponseEntity<ApiResponse<Void>> logListen(
            @PathVariable String songId,
            @AuthenticationPrincipal Jwt jwt) {
        historyService.logListen(songId, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success("Listening history recorded", null));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<ListeningHistoryResponse>>> getHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                historyService.getUserHistory(jwt.getSubject(), pageable)));
    }
}
