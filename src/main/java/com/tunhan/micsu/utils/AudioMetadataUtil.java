package com.tunhan.micsu.utils;

import com.mpatric.mp3agic.Mp3File;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@UtilityClass
public class AudioMetadataUtil {

    public static long getDurationInSeconds(MultipartFile file) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("audio_", "_" + file.getOriginalFilename());
            file.transferTo(tempFile.toFile());

            Mp3File mp3 = new Mp3File(tempFile.toFile());
            return mp3.getLengthInSeconds();
        } catch (Exception e) {
            log.warn("[AudioMetadataUtil] getDuration thất bại: file={}: {}", file.getOriginalFilename(),
                    e.getMessage());
            return 0L;
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static long getDurationInSeconds(Path path) {
        try {
            Mp3File mp3 = new Mp3File(path.toFile());
            return mp3.getLengthInSeconds();
        } catch (Exception e) {
            log.warn("[AudioMetadataUtil] getDuration thất bại: path={}: {}", path.getFileName(), e.getMessage());
            return 0L;
        }
    }
}
