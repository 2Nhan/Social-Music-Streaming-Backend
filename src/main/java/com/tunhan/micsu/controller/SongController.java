package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.request.SongUploadRequest;
import com.tunhan.micsu.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import com.tunhan.micsu.dto.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/songs")
public class SongController {
    private final SongService songService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Void>> uploadSong(@ModelAttribute SongUploadRequest request) throws IOException {
        songService.uploadSong(request);
        return ResponseEntity.ok(ApiResponse.success("Upload song thành công", null));
    }
}
