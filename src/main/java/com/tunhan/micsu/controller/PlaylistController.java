package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.request.AddSongToPlaylistRequest;
import com.tunhan.micsu.dto.request.PlaylistRequest;
import com.tunhan.micsu.dto.response.ApiResponse;
import com.tunhan.micsu.dto.response.PlaylistResponse;
import com.tunhan.micsu.service.playlist.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlaylistResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.getPlaylistById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PlaylistResponse>> create(
            @Valid @RequestBody PlaylistRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success("Playlist created successfully",
                playlistService.createPlaylist(request, jwt.getSubject())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PlaylistResponse>> update(
            @PathVariable String id,
            @RequestBody PlaylistRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success("Playlist updated successfully",
                playlistService.updatePlaylist(id, request, jwt.getSubject())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        playlistService.deletePlaylist(id, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success("Playlist deleted successfully", null));
    }

    @PostMapping("/{id}/songs")
    public ResponseEntity<ApiResponse<PlaylistResponse>> addSong(
            @PathVariable String id,
            @Valid @RequestBody AddSongToPlaylistRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success("Song added to playlist successfully",
                playlistService.addSong(id, request.getSongId(), jwt.getSubject())));
    }

    @DeleteMapping("/{id}/songs/{songId}")
    public ResponseEntity<ApiResponse<PlaylistResponse>> removeSong(
            @PathVariable String id,
            @PathVariable String songId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success("Song removed from playlist successfully",
                playlistService.removeSong(id, songId, jwt.getSubject())));
    }
}
