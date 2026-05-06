package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.request.SongUploadRequest;
import com.tunhan.micsu.service.R2StorageService;
import com.tunhan.micsu.service.hls.HlsService;
import com.tunhan.micsu.service.like.LikeService;
import com.tunhan.micsu.utils.HlsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.tunhan.micsu.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@RestController
@RequestMapping("/api")
public class TestController {

    private final HlsService hlsServiceV1;
    private final HlsService hlsServiceV2;
    private final HlsService hlsServiceV3;
    private final R2StorageService r2StorageService;
    private final LikeService likeService;

    @Autowired
    public TestController(@Qualifier("hlsServiceV1") HlsService hlsServiceV1,
            @Qualifier("hlsServiceV2") HlsService hlsServiceV2,
            @Qualifier("hlsServiceV3") HlsService hlsServiceV3,
            R2StorageService r2StorageService,
                          LikeService likeService) {
        this.hlsServiceV1 = hlsServiceV1;
        this.hlsServiceV2 = hlsServiceV2;
        this.hlsServiceV3 = hlsServiceV3;
        this.r2StorageService = r2StorageService;
        this.likeService = likeService;
    }

    @PostMapping("/v1/hls")
    public ResponseEntity<ApiResponse<Void>> processHlsV1(@ModelAttribute SongUploadRequest request) throws Exception {
        processWithHls(hlsServiceV1, request, "testId001");
        return ResponseEntity.ok(ApiResponse.success("Process HLS V1 successfully", null));
    }

    @PostMapping("/v2/hls")
    public ResponseEntity<ApiResponse<Void>> processHlsV2(@ModelAttribute SongUploadRequest request) throws Exception {
        processWithHls(hlsServiceV2, request, "testId002");
        return ResponseEntity.ok(ApiResponse.success("Process HLS V2 successfully", null));
    }

    @PostMapping("/v3/hls")
    public ResponseEntity<ApiResponse<Void>> processHlsV3(@ModelAttribute SongUploadRequest request) throws Exception {
        processWithHls(hlsServiceV3, request, "testId003");
        return ResponseEntity.ok(ApiResponse.success("Process HLS V3 successfully", null));
    }

    private void processWithHls(HlsService service, SongUploadRequest request, String songId) throws IOException {
        HlsUtil.checkInputMp3(request.getAudioFile());
        Path tempMp3 = Files.createTempFile("hls_test_", ".mp3");
        try {
            request.getAudioFile().transferTo(tempMp3);
            service.processHls(tempMp3, songId);
        } finally {
            Files.deleteIfExists(tempMp3);
        }
    }

    @DeleteMapping("/clean-bucket")
    public ResponseEntity<ApiResponse<Void>> cleanBucket() {
        r2StorageService.deleteByPrefix("songs/");
        return ResponseEntity.ok(ApiResponse.success("Test data deleted from bucket", null));
    }

    @PostMapping("/test/atomic-update-likes/{songId}")
    public ResponseEntity<ApiResponse<Void>> testConcurrentLikes(@PathVariable String songId) {
        likeService.atomicUpdateLikeSong(songId);
        return ResponseEntity.ok(ApiResponse.success("Concurrent like test completed", null));
    }
}
