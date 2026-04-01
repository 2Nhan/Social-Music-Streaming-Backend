package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.response.SongDetailResponse;
import com.tunhan.micsu.entity.Song;
import com.tunhan.micsu.exception.ResourceNotFoundException;
import com.tunhan.micsu.repository.SongRepository;
import com.tunhan.micsu.service.song.SongService;
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

    private final SongService songService;

    @GetMapping("/{songId}/stream/master.m3u8")
    public ResponseEntity<Void> streamHls(@PathVariable String songId) {
        String audioUrl = songService.playSong(songId);

        log.info("[HlsController] Redirecting HLS stream for song {}", songId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(audioUrl))
                .build();
    }
}
