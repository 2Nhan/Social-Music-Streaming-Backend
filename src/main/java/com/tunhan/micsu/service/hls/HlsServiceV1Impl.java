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
@Service("hlsServiceV1")
public class HlsServiceV1Impl implements HlsService {

    private final R2StorageService r2;

    @Override
    public String processHls(Path path, String songId) throws IOException {
        Path outDir = Files.createTempDirectory("hls-out-");
        Process p = null;

        try {
            p = HlsUtil.getFfmpegProcess(outDir, path);

            StringBuilder ffmpegLogs = new StringBuilder();
            Process finalP = p;
            Thread logReader = new Thread(() -> {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(finalP.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null)
                        ffmpegLogs.append(line).append('\n');
                } catch (IOException ignored) {
                }
            });
            logReader.start();

            int exitCode = p.waitFor();
            logReader.join();

            if (exitCode != 0)
                throw new IOException("Audio processing (FFmpeg) failed with exit code: " + exitCode
                        + "\nError log:\n" + ffmpegLogs);

            String masterUrl = r2.uploadFolderEager(outDir, "songs/" + songId + "/hls/");
            log.info("[HlsServiceV1] upload hls successfully: songId={}, masterUrl={}", songId, masterUrl);
            return masterUrl;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (p != null && p.isAlive()) {
                p.destroyForcibly();
            }
            throw new IOException("HLS processing randomly interrupted", e);
        } finally {
            HlsUtil.cleanup(outDir);
        }
    }
}
