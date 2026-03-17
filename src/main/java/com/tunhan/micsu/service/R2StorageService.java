package com.tunhan.micsu.service;

import com.tunhan.micsu.utils.HlsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

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

    public CompletableFuture<String> uploadCoverAsync(MultipartFile file, String id) throws IOException {
        this.checkImageInput(file);

        String originalFilename = file.getOriginalFilename();
        String extension = ".jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID().toString() + extension;
        String key = "songs/" + id + "/" + coverAssets + "/" + uniqueFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(mBucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        byte[] fileBytes = file.getBytes();
        AsyncRequestBody requestBody = AsyncRequestBody.fromBytes(fileBytes);

        return s3AsyncClient.putObject(putObjectRequest, requestBody)
                .handle((res, ex) -> {
                    if (ex != null) {
                        log.error("[R2StorageService] Cover upload failed: {}", ex.getMessage());
                        throw new RuntimeException("Cover upload failed: " + ex.getMessage(), ex);
                    }
                    log.info("[R2StorageService] Cover uploaded successfully");
                    return publicDomain + "/" + key;
                });
    }

    private void checkImageInput(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file must not be empty.");
        }
        List<String> allowedExtensions = List.of("image/jpeg",
                "image/png",
                "image/gif");
        if (!allowedExtensions.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "Unsupported image format. Please upload .jpeg, .png, or .gif file.");
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
}
