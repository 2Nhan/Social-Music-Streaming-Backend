package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.request.SongUploadRequest;
import com.tunhan.micsu.dto.request.SongUpdateRequest;
import com.tunhan.micsu.dto.response.ApiResponse;
import com.tunhan.micsu.dto.response.SongDetailResponse;
import com.tunhan.micsu.service.song.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SongController {

    private final SongService songService;

    @PostMapping(value = "/songs/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Void>> uploadSong(
            @ModelAttribute SongUploadRequest request,
            @AuthenticationPrincipal Jwt jwt) throws IOException {
        if (jwt != null)
            request.setUploadedBy(jwt.getSubject());
        songService.uploadSong(request);
        return ResponseEntity.ok(ApiResponse.success("Song uploaded successfully", null));
    }

    @PostMapping("/songs/uploadV2")
    public ResponseEntity<ApiResponse<Void>> uploadSongV2(
            @ModelAttribute SongUploadRequest request,
            @AuthenticationPrincipal Jwt jwt) throws IOException {
        if (jwt != null)
            request.setUploadedBy(jwt.getSubject());
        songService.uploadSongV2(request);
        return ResponseEntity.ok(ApiResponse.success("Song uploaded successfully", null));
    }

    @GetMapping("/songs/{id}")
    public ResponseEntity<ApiResponse<SongDetailResponse>> getSongById(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        String requesterId = jwt != null ? jwt.getSubject() : null;
        return ResponseEntity.ok(ApiResponse.success(songService.getSongById(id, requesterId)));
    }

    @PutMapping("/songs/{id}")
    public ResponseEntity<ApiResponse<SongDetailResponse>> updateSong(
            @PathVariable String id,
            @RequestBody SongUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success("Song updated successfully",
                songService.updateSong(id, request, jwt.getSubject())));
    }

    @DeleteMapping("/songs/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSong(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        songService.deleteSong(id, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success("Song deleted", null));
    }
}
