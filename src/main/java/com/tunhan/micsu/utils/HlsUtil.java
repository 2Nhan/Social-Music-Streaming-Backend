package com.tunhan.micsu.utils;

import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@UtilityClass
public class HlsUtil {
    public static Process getFfmpegProcess(Path outDir, Path inputMp3) throws IOException {
        // Định nghĩa các pattern cho Adaptive Bitrate
        // %v sẽ được FFmpeg tự động thay thế bằng tên luồng (64k, 128k, 320k)
        // %03d là số thứ tự segment (000, 001, 002...)
        String segmentPattern = outDir.resolve("seg_%v_%03d.ts").toString();
        String playlistPattern = outDir.resolve("playlist_%v.m3u8").toString();

        // Chuẩn bị các lệnh cần thực thi cho ABR
        List<String> cmd = List.of(
                "ffmpeg",
                "-y", // Tự động ghi đè
                "-i", inputMp3.toString(), // File MP3 đầu vào
                "-vn", // Loại bỏ luồng video/ảnh bìa

                // 1. Nhân bản luồng âm thanh (Tạo 3 luồng từ 1 file gốc)
                "-map", "0:a",
                "-map", "0:a",
                "-map", "0:a",

                // 2. Cấu hình Codec chung
                "-c:a", "aac",
                "-ar", "44100",

                // 3. Thiết lập Bitrate cho từng luồng (0, 1, 2 tương ứng với 3 lệnh map ở trên)
                "-b:a:0", "64k",
                "-b:a:1", "128k",
                "-b:a:2", "320k",

                // 4. Cấu hình HLS chung
                "-hls_time", "6",
                "-hls_playlist_type", "vod",
                "-f", "hls", // Bắt buộc khai báo format hls khi dùng đa luồng

                // 5. Cấu hình Master Playlist và gắn nhãn cho từng luồng
                // master_pl_name chỉ cần truyền tên file, FFmpeg sẽ tự đặt nó vào cùng thư mục
                // với playlistPattern
                "-master_pl_name", "master.m3u8",
                "-var_stream_map", "a:0,name:64k a:1,name:128k a:2,name:320k",

                // 6. Quy tắc đặt tên cho các file Segment (.ts)
                "-hls_segment_filename", segmentPattern,

                // 7. Tham số cuối cùng: Quy tắc đặt tên cho các Playlist con (.m3u8)
                playlistPattern);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true); // Gom chung Error stream và Standard stream

        // Khởi chạy tiến trình
        return pb.start();
    }

    public static void checkInputMp3(MultipartFile mp3) {
        if (mp3 == null || mp3.isEmpty()) {
            throw new IllegalArgumentException("Tệp tin âm thanh không được để trống.");
        }
        if (!Objects.requireNonNull(mp3.getOriginalFilename()).endsWith(".mp3")) {
            throw new IllegalArgumentException("Định dạng tệp không được hỗ trợ. Vui lòng tải lên tệp định dạng .mp3.");
        }
    }

    public static String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank())
            return "";
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }

    public static void cleanup(Path dir) {
        if (dir == null)
            return;
        // Khác với Files.list() chỉ liệt kê các folder/file con ở cấp độ liền kề
        // Files.walk() liệt kê toàn bộ các folder/file con ở tất cả các cấp
        try (Stream<Path> walk = Files.walk(dir)) {
            // Kết quả từ Files.walk() trả về có thể là các thứ tự bất kì
            // Nếu hệ thống xóa 1 thứ mục mà trong thư mục đó vẫn còn tồn tại các file con
            // Thì sẽ gây ra lỗi vì ko thể xóa thư mục khi tồn tại các file con bên trong đó
            // Sort theo reverse order đảm bảo xóa theo thứ tự từ file ở cấp thấp nhất
            // Đảm bảo việc xóa các file ko gặp lỗi
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    public static String getAudioContentType(String filename) {
        if (filename.endsWith(".m3u8"))
            return "application/vnd.apple.mpegurl";
        if (filename.endsWith(".ts"))
            return "video/MP2T";
        return "application/octet-stream";
    }
}
