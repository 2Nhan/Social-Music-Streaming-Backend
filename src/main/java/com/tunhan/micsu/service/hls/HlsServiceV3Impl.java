package com.tunhan.micsu.service.hls;

import com.tunhan.micsu.service.R2StorageService;
import com.tunhan.micsu.utils.HlsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
@Service("hlsServiceV3")
public class HlsServiceV3Impl implements HlsService {

    private final R2StorageService r2;

    @Override
    public void processHls(Path path, String songId) throws IOException {
        Path outDir = Files.createTempDirectory("hls-out-");
        Process p = null;

        try {
            p = HlsUtil.getFfmpegProcess(outDir, path);

            Process finalP = p;
            Thread logReader = new Thread(() -> {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(finalP.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        log.debug("[HlsServiceV3] ffmpeg: {}", line);
                    }
                } catch (IOException ignored) {
                }
            });
            logReader.start();

            int exitCode = p.waitFor();
            logReader.join();

            if (exitCode != 0) {
                throw new IOException("Quá trình xử lý âm thanh (FFmpeg) thất bại với mã lỗi: " + exitCode);
            }

            r2.uploadFolderFromPathAsync(outDir, songId);
            log.info("[HlsServiceV3] upload hls thành công: songId={}", songId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (p != null && p.isAlive()) {
                p.destroyForcibly();
            }
            throw new IOException("Tiến trình xử lý HLS bị gián đoạn ngẫu nhiên", e);
        } finally {
            HlsUtil.cleanup(outDir);
        }
    }
}
