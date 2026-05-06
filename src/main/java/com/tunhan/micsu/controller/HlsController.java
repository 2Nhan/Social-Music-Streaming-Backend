package com.tunhan.micsu.controller;

import com.tunhan.micsu.service.song.SongService;
import com.tunhan.micsu.service.viewcounter.ViewCounterService;
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
    private final ViewCounterService viewCounterService;

    @GetMapping("{songId}/stream/master.m3u8")
    public ResponseEntity<Void> streamHls(@PathVariable String songId) {
        String audioUrl = songService.playSong(songId);

        log.info("[HlsController] Redirecting HLS stream for song {}", songId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(audioUrl))
                .build();
    }

    @PatchMapping("/{songId}/updateView")
    public ResponseEntity<Void> incrementViewCount(@PathVariable String songId) {
        viewCounterService.increaseViewCount(songId);
        return ResponseEntity.ok().build();
    }
}
