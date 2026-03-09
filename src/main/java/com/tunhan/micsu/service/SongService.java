package com.tunhan.micsu.service;

import com.tunhan.micsu.dto.request.SongUploadRequest;
import com.tunhan.micsu.entity.Song;
import com.tunhan.micsu.repository.SongRepository;
import com.tunhan.micsu.service.hls.HlsService;
import com.tunhan.micsu.utils.AudioMetadataUtil;
import com.tunhan.micsu.utils.HlsUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SongService {

    private final SongRepository songRepository;
    private final R2StorageService r2StorageService;
    private final HlsService hlsService;

    public SongService(
            SongRepository songRepository,
            R2StorageService r2StorageService,
            @Qualifier("hlsServiceV3") HlsService hlsService) {
        this.songRepository = songRepository;
        this.r2StorageService = r2StorageService;
        this.hlsService = hlsService;
    }

    public void uploadSong(SongUploadRequest request) throws IOException {
        HlsUtil.checkInputMp3(request.getAudioFile());

        String id = UUID.randomUUID().toString();
        Path tempMp3 = null;
        CompletableFuture<String> coverFuture = null;

        try {
            log.info("[SongService] Bắt đầu xử lý upload bài hát. Tên bài hát: '{}', ID tạo mới: {}", request.getName(),
                    id);

            tempMp3 = Files.createTempFile("upload_", ".mp3");
            request.getAudioFile().transferTo(tempMp3);
            log.info("[SongService] [ID={}] Đã lưu file MP3 gốc tải lên vào thư mục tạm: {}", id,
                    tempMp3.toAbsolutePath());

            long duration = AudioMetadataUtil.getDurationInSeconds(tempMp3);
            log.info("[SongService] [ID={}] Đọc metadata thành công. Thời lượng: {} giây", id, duration);

            log.info("[SongService] [ID={}] Bắt đầu tiến trình upload ảnh bìa (async)...", id);
            coverFuture = r2StorageService.uploadCoverAsync(request.getImageFile());

            log.info("[SongService] [ID={}] Bắt đầu tiến trình chuyển đổi HLS và tải lên mảnh nhỏ (async)...", id);
            hlsService.processHls(tempMp3, id);
            log.info("[SongService] [ID={}] Hoàn tất tiến trình HLS", id);

            String coverUrl = coverFuture.join();
            log.info("[SongService] [ID={}] Hoàn tất upload ảnh bìa. URL: {}", id, coverUrl);

            Song song = new Song();
            song.setId(id);
            song.setName(request.getName());
            song.setDescription(request.getDescription());
            song.setCoverUrl(coverUrl);
            song.setDuration(duration);
            songRepository.save(song);
            log.info(
                    "[SongService] [ID={}] Lưu thông tin bài hát vào cơ sở dữ liệu thành công. Hoàn tất toàn bộ chu trình upload.",
                    id);

        } catch (Exception e) {
            log.error(
                    "[SongService] [ID={}] Đã xảy ra lỗi nguy hiểm trong quá trình upload ({}: {}), bắt đầu dọn dẹp (rollback)...",
                    id, e.getClass().getSimpleName(), e.getMessage(), e);

            if (coverFuture != null) {
                coverFuture.cancel(true);
            }
            log.info("[SongService] [ID={}] Yêu cầu dọn dẹp các mảnh HLS...", id);
            r2StorageService.deleteByPrefix(r2StorageService.getMBucket(), "songs/" + id + "/hls/");
            log.info("[SongService] [ID={}] Yêu cầu dọn dẹp ảnh bìa...", id);
            r2StorageService.deleteByPrefix(r2StorageService.getIBucket(), "songs/" + id + "/covers/");
            throw new RuntimeException("Quá trình tải lên bài hát thất bại. Vui lòng thử lại sau.", e);
        } finally {
            if (tempMp3 != null)
                Files.deleteIfExists(tempMp3);
        }
    }
}
