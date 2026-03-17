package com.tunhan.micsu.controller;

import com.tunhan.micsu.entity.Song;
import com.tunhan.micsu.exception.ResourceNotFoundException;
import com.tunhan.micsu.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/songs")
public class HlsController {

    private final SongRepository songRepository;

    @GetMapping("/{songId}/hls/master.m3u8")
    public ResponseEntity<Void> streamHls(@PathVariable String songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song", songId));

        if (song.getAudioUrl() == null || song.getAudioUrl().isBlank()) {
            log.warn("[HlsController] Song {} has no HLS URL", songId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        log.info("[HlsController] Redirecting HLS stream for song {}", songId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(song.getAudioUrl()))
                .build();
    }
}
