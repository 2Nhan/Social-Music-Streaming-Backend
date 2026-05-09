package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.request.SongUploadRequest;
import com.tunhan.micsu.dto.request.SongUpdateRequest;
import com.tunhan.micsu.dto.response.ApiResponse;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.service.song.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
            @Valid @ModelAttribute SongUploadRequest request,
            @AuthenticationPrincipal Jwt jwt) throws IOException {
        if (jwt != null) {
            request.setUploadedBy(jwt.getSubject());
        }

        songService.uploadSong(request);
        return ResponseEntity.ok(ApiResponse.success("Song uploaded successfully", null));
    }

    @PostMapping("/songs/uploadV2")
    public ResponseEntity<ApiResponse<Void>> uploadSongV2(
            @Valid @ModelAttribute SongUploadRequest request,
            @AuthenticationPrincipal Jwt jwt) throws IOException {
        if (jwt != null)
            request.setUploadedBy(jwt.getSubject());
        songService.uploadSongV2(request);
        return ResponseEntity.ok(ApiResponse.success("Song uploaded successfully", null));
    }

    @GetMapping("/songs")
    public ResponseEntity<ApiResponse<PageResponse<SongResponse>>> getAllSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        String requesterId = jwt != null ? jwt.getSubject() : null;
        return ResponseEntity.ok(ApiResponse.success(songService.getAllSongs(PageRequest.of(page, size), requesterId)));
    }

    @GetMapping("/songs/{id}")
    public ResponseEntity<ApiResponse<SongResponse>> getSongById(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        String requesterId = jwt != null ? jwt.getSubject() : null;
        return ResponseEntity.ok(ApiResponse.success(songService.getSongById(id, requesterId)));
    }

    @PutMapping("/songs/{id}")
    public ResponseEntity<ApiResponse<SongResponse>> updateSong(
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
