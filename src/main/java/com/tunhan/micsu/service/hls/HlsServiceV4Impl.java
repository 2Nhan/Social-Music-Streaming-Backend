package com.tunhan.micsu.service.hls;

import com.tunhan.micsu.service.R2StorageService;
import com.tunhan.micsu.utils.HlsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * HlsServiceV4 - Streaming Upload (WatchService)
 *
 * <p>
 * Thay vì chờ FFmpeg phân mảnh xong 100% rồi mới upload (như V1, V2, V3),
 * V4 sử dụng {@link WatchService} để "giám sát" thư mục tạm trong thời gian
 * thực. Mỗi khi FFmpeg ghi xong 1 file mới (sự kiện ENTRY_CREATE), Java sẽ
 * ngay lập tức kick-off 1 CompletableFuture để upload file đó lên Cloudflare R2
 * song song, mà không cần chờ FFmpeg hoàn thành toàn bộ bài hát.
 *
 * <p>
 * <b>Tại sao lại lọc kỹ file .m3u8 và .key?</b><br>
 * Các file playlist (.m3u8) tham chiếu đến các segment đã được upload trước.
 * Nếu upload .m3u8 khi các .ts còn chưa lên xong sẽ gây lỗi phát nhạc ở
 * Frontend. Vì vậy: upload .ts ngay lập tức, còn .m3u8 và .key thì để
 * FFmpeg xong hẳn mới upload sau.
 */
@Slf4j
@RequiredArgsConstructor
@Service("hlsServiceV4")
public class HlsServiceV4Impl implements HlsService {

    private final R2StorageService r2;

    @Override
    public String processHls(Path path, String songId) throws IOException {
        Path outDir = Files.createTempDirectory("hls-out-");
        Process p = null;
        WatchService watcher = FileSystems.getDefault().newWatchService();

        // Danh sách toàn bộ Future đang chạy — dùng để cancel khi lỗi
        List<CompletableFuture<Void>> uploadFutures = new ArrayList<>();

        try {
            // 1. Đăng ký WatchService trước khi FFmpeg chạy để không bỏ sót event
            outDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            log.info("[HlsServiceV4] [songId={}] WatchService started on: {}", songId, outDir);

            // 2. Khởi động FFmpeg
            p = HlsUtil.getFfmpegProcess(outDir, path);

            // 3. Thread đọc log FFmpeg (tránh STDOUT buffer bị đầy làm treo process)
            Process finalP = p;
            Thread logReader = new Thread(() -> {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(finalP.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        log.debug("[HlsServiceV4] ffmpeg: {}", line);
                    }
                } catch (IOException ignored) {
                }
            });
            logReader.start();

            // 4. Thread giám sát thư mục: upload .ts ngay khi vừa được FFmpeg tạo ra
            Thread watchThread = new Thread(() -> {
                try {
                    while (true) {
                        WatchKey watchKey = watcher.poll(200, java.util.concurrent.TimeUnit.MILLISECONDS);

                        if (watchKey == null) {
                            if (!finalP.isAlive())
                                break;
                            continue;
                        }

                        for (WatchEvent<?> event : watchKey.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.OVERFLOW)
                                continue;

                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                            Path newFile = outDir.resolve(pathEvent.context());
                            String fileName = newFile.getFileName().toString();

                            // Chỉ upload .ts ngay lập tức:
                            // .m3u8 phải được upload SAU KHI toàn bộ .ts đã lên xong
                            if (fileName.endsWith(".ts")) {
                                log.info("[HlsServiceV4] [songId={}] New segment detected, uploading: {}", songId,
                                        fileName);
                                CompletableFuture<Void> future = r2.uploadSingleFileAsync(newFile, songId);
                                synchronized (uploadFutures) {
                                    uploadFutures.add(future);
                                }
                            }
                        }

                        if (!watchKey.reset())
                            break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            watchThread.setDaemon(true);
            watchThread.start();

            // 5. Chờ FFmpeg hoàn tất
            int exitCode = p.waitFor();
            logReader.join();
            watchThread.join(2000); // Cho watchThread thêm 2s để flush các event cuối cùng

            if (exitCode != 0) {
                throw new IOException("Audio processing (FFmpeg) failed with exit code: " + exitCode);
            }

            // 6. Chờ TẤT CẢ .ts upload hoàn tất trước khi upload .m3u8
            log.info("[HlsServiceV4] [songId={}] FFmpeg done. Waiting for {} segment upload(s)...", songId,
                    uploadFutures.size());
            CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0])).join();
            log.info("[HlsServiceV4] [songId={}] All segments uploaded.", songId);

            // 7. Upload toàn bộ .m3u8 sau cùng (khi tất cả .ts đã chắc chắn tồn tại trên
            // R2)
            List<CompletableFuture<Void>> playlistFutures;
            try (var stream = Files.list(outDir)) {
                playlistFutures = stream
                        .filter(Files::isRegularFile)
                        .filter(f -> {
                            String name = f.getFileName().toString();
                            return name.endsWith(".m3u8") || name.endsWith(".key") || name.endsWith(".txt");
                        })
                        .map(f -> r2.uploadSingleFileAsync(f, songId))
                        .toList();
            }
            CompletableFuture.allOf(playlistFutures.toArray(new CompletableFuture[0])).join();

            String masterUrl = r2.getPublicDomain() + "/songs/" + songId + "/hls/master.m3u8";
            log.info("[HlsServiceV4] [songId={}] Upload complete. masterUrl={}", songId, masterUrl);
            return masterUrl;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[HlsServiceV4] [songId={}] Interrupted during HLS processing.", songId, e);
            cancelAllFutures(uploadFutures);
            killFfmpeg(p);
            throw new IOException("HLS processing interrupted", e);

        } catch (Exception e) {
            // Bắt toàn bộ Exception còn lại: IOException từ FFmpeg exit code,
            // CompletionException từ upload .ts/.m3u8 thất bại, v.v.
            log.error("[HlsServiceV4] [songId={}] Error during HLS processing: {}: {}",
                    songId, e.getClass().getSimpleName(), e.getMessage(), e);
            cancelAllFutures(uploadFutures);
            killFfmpeg(p);
            // Ném ra để SongServiceImpl bắt rồi thực hiện rollback xóa trên R2
            throw new IOException("HLS streaming upload process failed", e);

        } finally {
            // Luôn luôn đóng WatchService và dọn dẹp thư mục tạm dù thành công hay thất bại
            try {
                watcher.close();
            } catch (IOException ignored) {
            }
            HlsUtil.cleanup(outDir);
        }
    }

    /**
     * Cancel toàn bộ các CompletableFuture đang chạy.
     * Lưu ý: cancel() với S3AsyncClient không đảm bảo dừng request đang bay
     * nhưng vẫn nên gọi để giải phóng các future chưa bắt đầu.
     */
    private void cancelAllFutures(List<CompletableFuture<Void>> futures) {
        if (futures == null || futures.isEmpty())
            return;
        synchronized (futures) {
            int count = futures.size();
            futures.forEach(f -> f.cancel(true));
            log.warn("[HlsServiceV4] Cancelled {} pending upload future(s).", count);
        }
    }

    /**
     * Kill tiến trình FFmpeg nếu vẫn còn sống.
     */
    private void killFfmpeg(Process p) {
        if (p != null && p.isAlive()) {
            p.destroyForcibly();
            log.warn("[HlsServiceV4] FFmpeg process forcibly terminated.");
        }
    }
}
