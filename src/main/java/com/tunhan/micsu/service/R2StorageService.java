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

    @Value("${storage.r2.iBucket}")
    private String iBucket;

    @Value("${storage.r2.publicDomain}")
    private String publicDomain;

    public String getMBucket() {
        return mBucket;
    }

    public String getIBucket() {
        return iBucket;
    }

    public CompletableFuture<String> uploadCoverAsync(MultipartFile file) throws IOException {
        this.checkImageInput(file);

        String originalFilename = file.getOriginalFilename();
        String extension = ".jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID().toString() + extension;
        String key = "songs/" + coverAssets + "/" + uniqueFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(iBucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        byte[] fileBytes = file.getBytes();
        AsyncRequestBody requestBody = AsyncRequestBody.fromBytes(fileBytes);

        return s3AsyncClient.putObject(putObjectRequest, requestBody)
                .handle((res, ex) -> {
                    if (ex != null) {
                        log.error("[R2StorageService] Upload ảnh bìa thất bại: {}", ex.getMessage());
                        throw new RuntimeException("Tải lên ảnh bìa thất bại: " + ex.getMessage(), ex);
                    }
                    log.info("[R2StorageService] Upload ảnh bìa thành công");
                    return publicDomain + "/" + key;
                });
    }

    private void checkImageInput(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Tệp tin hình ảnh không được để trống.");
        }
        List<String> allowedExtensions = List.of("image/jpeg",
                "image/png",
                "image/gif");
        if (!allowedExtensions.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "Định dạng hình ảnh không được hỗ trợ. Vui lòng tải lên tệp .jpeg, .png hoặc .gif.");
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

    public void uploadFolderFromPathSync(Path path, String prefix) throws IOException {
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
    }

    public void uploadFolderFromPathAsync(Path path, String id) throws IOException {

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

            // Lấy giấy phép trước khi gửi request
            semaphore.acquireUninterruptibly();

            // Trả về trực tiếp Future của S3, KHÔNG bọc qua runAsync, KHÔNG dùng .join()
            // bên trong
            return s3AsyncClient.putObject(putOb, AsyncRequestBody.fromFile(filePath))
                    .whenComplete((res, ex) -> {
                        // Dù thành công hay thất bại (có Exception), luôn phải nhả giấy phép ra
                        semaphore.release();

                        if (ex != null) {
                            log.error("[R2StorageService] Upload thất bại cho tệp: {}: {}", fileName, ex.getMessage());
                            throw new CompletionException("Đã xảy ra lỗi khi tải tệp tin lên máy chủ: " + fileName, ex);
                        }
                    })
                    .thenAccept(res -> {
                    });
        }).toList();

        // Chờ tất cả tiến trình con hoàn tất. Nếu có bất kỳ exception nào trong
        // whenComplete,
        // .join() ở đây sẽ ném ra CompletionException để báo lỗi cho hàm gọi bên ngoài.
        CompletableFuture.allOf(uploadTasks.toArray(new CompletableFuture[0])).join();
    }

    public void uploadFolderEager(Path path, String prefix) throws IOException {
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
    }

    public void deleteByPrefix(String bucket, String prefix) {
        try {
            ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response listRes = s3Client.listObjectsV2(listReq);
            List<ObjectIdentifier> toDelete = listRes.contents().stream()
                    .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                    .toList();

            if (!toDelete.isEmpty()) {
                DeleteObjectsRequest deleteReq = DeleteObjectsRequest.builder()
                        .bucket(bucket)
                        .delete(Delete.builder().objects(toDelete).build())
                        .build();
                s3Client.deleteObjects(deleteReq);
                log.info("[R2StorageService] Đã dọn dẹp thành công {} tệp tin tại thư mục: {}", toDelete.size(),
                        prefix);
            }
        } catch (Exception e) {
            log.error("[R2StorageService] Không thể dọn dẹp các tệp tin cũ (thư mục: {}). Chi tiết lỗi: {}", prefix,
                    e.getMessage());
        }
    }
}
