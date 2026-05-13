package com.tunhan.micsu.service;

import com.tunhan.micsu.utils.HlsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Semaphore;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class R2StorageService {

    private static final int MAX_CONCURRENT_UPLOADS = 16;

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final S3AsyncClient s3AsyncClient;

    private String songAssets = "songs";
    private String hlsAssets = "hls";
    private String coverAssets = "covers";

    @Value("${storage.r2.mBucket}")
    private String mBucket;

    @Value("${storage.r2.publicDomain}")
    private String publicDomain;

    public String getMBucket() {
        return mBucket;
    }

    public String getPublicDomain() {
        return publicDomain;
    }

    public String uploadNormalAudio(MultipartFile file, String id) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = ".mp3";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID().toString() + extension;
        String key = "test/" + id + "/" + songAssets + "/" + uniqueFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(mBucket)
                .key(key)
                .contentType(file.getContentType() != null ? file.getContentType() : "audio/mpeg")
                .build();

        byte[] fileBytes = file.getBytes();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));

        log.info("[R2StorageService] Normal audio uploaded successfully: {}", key);
        return publicDomain + "/" + key;
    }

    public String uploadAvatar(MultipartFile file, String userId) throws IOException {
        this.checkImageInput(file);

        // Tự động đổi đuôi thành .webp cho sạch sẽ, vì data bên trong là webp
        String key = String.format("users/%s/avatars/%s.webp", userId, UUID.randomUUID());

        // Nén ảnh
        byte[] optimizedBytes = processImageToWebp(file);

        // Upload đồng bộ
        s3Client.putObject(buildPutRequest(key, "image/webp"),
                RequestBody.fromBytes(optimizedBytes));

        log.info("[R2StorageService] Avatar uploaded and optimized: {}", key);
        return publicDomain + "/" + key;
    }

    public CompletableFuture<String> uploadCoverAsync(MultipartFile file, String id) throws IOException {
        this.checkImageInput(file);

        String key = String.format("songs/%s/%s/%s.webp", id, coverAssets, UUID.randomUUID());

        // Chạy việc nén và upload trong luồng Async
        return CompletableFuture.supplyAsync(() -> {
            try {
                return processImageToWebp(file);
            } catch (IOException e) {
                throw new RuntimeException("Image compression failed", e);
            }
        }).thenCompose(optimizedBytes -> {
            AsyncRequestBody requestBody = AsyncRequestBody.fromBytes(optimizedBytes);

            return s3AsyncClient.putObject(buildPutRequest(key, "image/webp"), requestBody)
                    .thenApply(res -> {
                        log.info("[R2StorageService] Cover uploaded and optimized async: {}", key);
                        return publicDomain + "/" + key;
                    });
        }).exceptionally(ex -> {
            log.error("[R2StorageService] Async cover upload failed: {}", ex.getMessage());
            throw new RuntimeException("Upload failed", ex);
        });
    }

    private byte[] processImageToWebp(MultipartFile file) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .scale(1.0) // Giữ nguyên kích cỡ
                .outputFormat("webp")
                .outputQuality(0.75) // Nén chất lượng 75%
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }

    private PutObjectRequest buildPutRequest(String key, String contentType) {
        return PutObjectRequest.builder()
                .bucket(mBucket)
                .key(key)
                .contentType(contentType)
                .cacheControl("public, max-age=31536000") // Tối ưu cho CDN
                .build();
    }

    private void checkImageInput(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file must not be empty.");
        }
        List<String> allowedExtensions = List.of("image/jpeg",
                "image/png",
                "image/gif",
                "image/webp");
        if (!allowedExtensions.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "Unsupported image format. Please upload .jpeg, .png, .webp or .gif file.");
        }
    }

    public List<String> listKeys() {
        ListObjectsV2Request req = ListObjectsV2Request.builder()
                .bucket(mBucket)
                .prefix("songs/")
                .build();

        ListObjectsV2Response res = s3Client.listObjectsV2(req);
        return res.contents().stream().map(S3Object::key).toList();
    }

    public String getSignedStreamUrl(String key, Duration ttl) {
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(mBucket)
                .key(key)
                .build();

        GetObjectPresignRequest presign = GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(get)
                .build();

        PresignedGetObjectRequest signed = presigner.presignGetObject(presign);
        return signed.url().toString();
    }

    public String uploadFolderFromPathSync(Path path, String prefix) throws IOException {
        List<Path> files;
        try (Stream<Path> stream = Files.list(path)) {
            files = stream.filter(Files::isRegularFile).toList();
        }

        for (Path filePath : files) {
            String fileName = filePath.getFileName().toString();
            String key = HlsUtil.normalizePrefix(prefix) + fileName;

            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(mBucket)
                    .key(key)
                    .contentType(HlsUtil.getAudioContentType(fileName))
                    .build();

            s3Client.putObject(putOb, RequestBody.fromFile(filePath));
        }
        return publicDomain + "/" + HlsUtil.normalizePrefix(prefix) + "master.m3u8";
    }

    public CompletableFuture<String> uploadFolderFromPathAsync(Path path, String id) throws IOException {

        List<Path> files;
        try (Stream<Path> stream = Files.list(path)) {
            files = stream.filter(Files::isRegularFile).toList();
        }

        Semaphore semaphore = new Semaphore(MAX_CONCURRENT_UPLOADS);

        List<CompletableFuture<Void>> uploadTasks = files.stream().map(filePath -> {
            String fileName = filePath.getFileName().toString();
            String key = songAssets + "/" + id + "/" + hlsAssets + "/" + fileName;

            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(mBucket)
                    .key(key)
                    .contentType(HlsUtil.getAudioContentType(fileName))
                    .build();

            // Acquire permit before sending request
            semaphore.acquireUninterruptibly();

            // Return S3 Future directly, DO NOT wrap via runAsync, DO NOT use .join()
            // inside
            return s3AsyncClient.putObject(putOb, AsyncRequestBody.fromFile(filePath))
                    .whenComplete((res, ex) -> {
                        // Whether success or failure (Exception), must always release the permit
                        semaphore.release();

                        if (ex != null) {
                            log.error("[R2StorageService] Upload failed for file : {}: {}", fileName, ex.getMessage());
                            throw new CompletionException("Error occurred while uploading file to server: " + fileName,
                                    ex);
                        }
                    })
                    .thenAccept(res -> {
                    });
        }).toList();

        // Do not use .join() here anymore to avoid blocking main thread. Return
        // CompletableFuture.
        return CompletableFuture.allOf(uploadTasks.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    log.info("[R2StorageService] HLS folder uploaded successfully for song ID {}", id);
                    return publicDomain + "/" + songAssets + "/" + id + "/" + hlsAssets + "/master.m3u8";
                });
    }

    public CompletableFuture<Void> uploadSingleFileAsync(Path filePath, String songId) {
        String fileName = filePath.getFileName().toString();
        String key = songAssets + "/" + songId + "/" + hlsAssets + "/" + fileName;

        PutObjectRequest putOb = PutObjectRequest.builder()
                .bucket(mBucket)
                .key(key)
                .contentType(HlsUtil.getAudioContentType(fileName))
                .build();

        return s3AsyncClient.putObject(putOb, AsyncRequestBody.fromFile(filePath))
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.error("[R2StorageService] Upload failed for file '{}': {}", fileName, ex.getMessage());
                        throw new CompletionException("Error uploading file: " + fileName, ex);
                    }
                    log.debug("[R2StorageService] Uploaded single file: {}", key);
                })
                .thenAccept(res -> {
                });
    }

    public String uploadFolderEager(Path path, String prefix) throws IOException {
        List<Path> files;
        try (Stream<Path> stream = Files.list(path)) {
            files = stream.filter(Files::isRegularFile).toList();
        }

        for (Path filePath : files) {
            String fileName = filePath.getFileName().toString();
            String key = HlsUtil.normalizePrefix(prefix) + fileName;

            byte[] data = Files.readAllBytes(filePath);

            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(mBucket)
                    .key(key)
                    .contentType(HlsUtil.getAudioContentType(fileName))
                    .build();

            s3Client.putObject(putOb, RequestBody.fromBytes(data));
        }
        return publicDomain + "/" + HlsUtil.normalizePrefix(prefix) + "master.m3u8";
    }

    public void deleteByPrefix(String prefix) {
        try {
            ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                    .bucket(mBucket)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response listRes = s3Client.listObjectsV2(listReq);
            List<ObjectIdentifier> toDelete = listRes.contents().stream()
                    .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                    .toList();

            if (!toDelete.isEmpty()) {
                DeleteObjectsRequest deleteReq = DeleteObjectsRequest.builder()
                        .bucket(mBucket)
                        .delete(Delete.builder().objects(toDelete).build())
                        .build();
                s3Client.deleteObjects(deleteReq);
                log.info("[R2StorageService] Successfully cleaned up {} files in directory: {}", toDelete.size(),
                        prefix);
            }
        } catch (Exception e) {
            log.error("[R2StorageService] Cannot clean up old files (directory: {}). Error details: {}", prefix,
                    e.getMessage());
        }
    }

    private final List<String> TARGET_IMAGE_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".JPG", ".PNG");

    /**
     * Chạy tiến trình tối ưu hóa toàn bộ ảnh hiện có trên R2.
     */
    public void migrateAllImagesToWebp() {
        log.info("[R2StorageService] Starting global image optimization migration...");

        // Bạn có thể chỉ định các prefix cần quét để tránh quét nhầm các folder khác
        List<String> prefixes = List.of("genre-officcial/jpop.png");

        for (String prefix : prefixes) {
            log.info("[R2StorageService] Scanning prefix: {}", prefix);

            ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                    .bucket(mBucket)
                    .prefix(prefix)
                    .build();

            // Sử dụng Paginator để xử lý hàng triệu file mà không gây tràn RAM
            s3Client.listObjectsV2Paginator(listReq).contents().stream()
                    .filter(obj -> isImageFile(obj.key()))
                    .forEach(obj -> {
                        try {
                            this.optimizeAndOverwrite(obj.key());
                        } catch (Exception e) {
                            log.error("[R2StorageService] Failed to optimize {}: {}", obj.key(), e.getMessage());
                        }
                    });
        }
        log.info("[R2StorageService] Migration completed!");
    }

    private boolean isImageFile(String key) {
        return TARGET_IMAGE_EXTENSIONS.stream().anyMatch(key::endsWith);
    }

    private void optimizeAndOverwrite(String key) throws IOException {
        // 1. Kiểm tra Content-Type hiện tại
        HeadObjectRequest headReq = HeadObjectRequest.builder().bucket(mBucket).key(key).build();
        HeadObjectResponse headRes = s3Client.headObject(headReq);

        if ("image/webp".equals(headRes.contentType())) {
            log.debug("[R2StorageService] Skipping already optimized file: {}", key);
            return;
        }

        log.info("[R2StorageService] Optimizing: {} (Size: {} KB)", key, headRes.contentLength() / 1024);

        // 2. Download ảnh về Memory
        GetObjectRequest getReq = GetObjectRequest.builder().bucket(mBucket).key(key).build();
        ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(getReq);

        // 3. Xử lý nén và chuyển đổi sang WebP bằng Thumbnailator
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();

        // scale(1.0) giữ nguyên kích thước, outputQuality(0.75) nén dung lượng
        Thumbnails.of(s3Stream)
                .scale(1.0)
                .outputFormat("webp")
                .outputQuality(0.75)
                .toOutputStream(outputStream);

        byte[] optimizedData = outputStream.toByteArray();

        // 4. Ghi đè lên chính Key cũ (Giữ nguyên đường dẫn)
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(mBucket)
                .key(key)
                .contentType("image/webp") // Ép kiểu WebP để trình duyệt hiểu
                .cacheControl("public, max-age=31536000") // Cache 1 năm cho Client
                .build();

        s3Client.putObject(putReq, RequestBody.fromBytes(optimizedData));

        log.info("[R2StorageService] Overwritten success: {} (New Size: {} KB)", key, optimizedData.length / 1024);

        s3Stream.close();
        outputStream.close();
    }
}
