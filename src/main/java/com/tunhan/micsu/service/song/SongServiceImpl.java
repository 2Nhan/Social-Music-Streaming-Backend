package com.tunhan.micsu.service.song;

import com.tunhan.micsu.dto.request.SongUpdateRequest;
import com.tunhan.micsu.dto.request.SongUploadRequest;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.entity.Song;
import com.tunhan.micsu.entity.enums.Visibility;
import com.tunhan.micsu.exception.AccessDeniedException;
import com.tunhan.micsu.exception.ResourceNotFoundException;
import com.tunhan.micsu.mapper.SongMapper;
import com.tunhan.micsu.repository.SongRepository;
import com.tunhan.micsu.repository.UserRepository;
import com.tunhan.micsu.service.R2StorageService;
import com.tunhan.micsu.service.hls.HlsService;
import com.tunhan.micsu.utils.AudioMetadataUtil;
import com.tunhan.micsu.utils.HlsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final R2StorageService r2StorageService;
    private final HlsService hlsServiceV3;
    private final HlsService hlsServiceV2;
    private final SongMapper songMapper;

    public SongServiceImpl(
            SongRepository songRepository,
            UserRepository userRepository,
            R2StorageService r2StorageService,
            @Qualifier("hlsServiceV3") HlsService hlsServiceV3,
            @Qualifier("hlsServiceV2") HlsService hlsServiceV2,
            SongMapper songMapper) {
        this.songRepository = songRepository;
        this.userRepository = userRepository;
        this.r2StorageService = r2StorageService;
        this.hlsServiceV3 = hlsServiceV3;
        this.hlsServiceV2 = hlsServiceV2;
        this.songMapper = songMapper;
    }

    @Override
    @Transactional
    public void uploadSong(SongUploadRequest request) throws IOException {
        doUpload(request, hlsServiceV3);
    }

    @Override
    @Transactional
    public void uploadSongV2(SongUploadRequest request) throws IOException {
        doUpload(request, hlsServiceV2);
    }

    private void doUpload(SongUploadRequest request, HlsService hlsService) throws IOException {
        HlsUtil.checkInputMp3(request.getAudioFile());

        String id = UUID.randomUUID().toString();
        Path tempMp3 = null;
        CompletableFuture<String> coverFuture = null;

        try {
            log.info("[SongService] Begin processing song upload. Name: '{}', ID: {}", request.getTitle(), id);

            tempMp3 = Files.createTempFile("upload_", ".mp3");
            request.getAudioFile().transferTo(tempMp3);

            long duration = AudioMetadataUtil.getDurationInSeconds(tempMp3);
            log.info("[SongService] [ID={}] Duration: {} seconds", id, duration);

            coverFuture = r2StorageService.uploadCoverAsync(request.getImageFile(), id);

            String hlsUrl = hlsService.processHls(tempMp3, id);
            log.info("[SongService] [ID={}] HLS masterUrl={}", id, hlsUrl);

            String coverUrl = coverFuture.join();

            Song song = new Song();
            song.setId(id);
            song.setTitle(request.getTitle());
            song.setDescription(request.getDescription());
            song.setCoverUrl(coverUrl);
            song.setAudioUrl(hlsUrl);
            song.setDuration(duration);
            song.setUploadedBy(request.getUploadedBy());
            song.setVisibility(request.getVisibility() != null ? request.getVisibility() : Visibility.PUBLIC);
            songRepository.save(song);

            // Increment user song count
            if (request.getUploadedBy() != null) {
                userRepository.findById(request.getUploadedBy()).ifPresent(user -> {
                    user.setSongCount(user.getSongCount() + 1);
                    userRepository.save(user);
                });
            }

            log.info("[SongService] [ID={}] Upload complete.", id);

        } catch (Exception e) {
            log.error("[SongService] [ID={}] Upload error ({}: {}), rollback...", id, e.getClass().getSimpleName(),
                    e.getMessage(), e);
            if (coverFuture != null)
                coverFuture.cancel(true);
            r2StorageService.deleteByPrefix("songs/" + id);
            throw new RuntimeException("Song upload process failed. Please try again later.", e);
        } finally {
            if (tempMp3 != null)
                Files.deleteIfExists(tempMp3);
        }
    }

    @Override
    public PageResponse<SongResponse> getAllSongs(Pageable pageable) {
        var page = songRepository.findAll(pageable);
        return PageResponse.<SongResponse>builder()
                .content(page.getContent().stream().map(songMapper::toSongResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }

    @Override
    public SongResponse getSongById(String id, String requesterId) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song", id));
        checkVisibility(song, requesterId);
        return songMapper.toSongResponse(song);
    }

    @Override
    public String playSong(String id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song", id));
        if (song.getAudioUrl() == null || song.getAudioUrl().isBlank()) {
            log.warn("[HlsController] Song {} has no HLS URL", id);
            throw new ResourceNotFoundException("Empty audio file with song: 3s", id);
        }
        return song.getAudioUrl();
    }

    @Override
    public SongResponse updateSong(String id, SongUpdateRequest request, String userId) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song", id));

        if (!song.getUploadedBy().equals(userId)) {
            throw new AccessDeniedException("Only the uploader can edit this song");
        }

        if (request.getTitle() != null)
            song.setTitle(request.getTitle());
        if (request.getDescription() != null)
            song.setDescription(request.getDescription());
        if (request.getLyricsData() != null)
            song.setLyricsData(request.getLyricsData());
        if (request.getVisibility() != null)
            song.setVisibility(request.getVisibility());

        songRepository.save(song);
        log.info("[SongService] Updated song {}", id);
        return songMapper.toSongResponse(song);
    }

    @Override
    @Transactional
    public void deleteSong(String id, String userId) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song", id));

        if (!song.getUploadedBy().equals(userId)) {
            throw new AccessDeniedException("Only the uploader can delete this song");
        }

        r2StorageService.deleteByPrefix("songs/" + id);
        songRepository.delete(song);

        userRepository.findById(userId).ifPresent(user -> {
            user.setSongCount(Math.max(0, user.getSongCount() - 1));
            userRepository.save(user);
        });

        log.info("[SongService] Deleted song {} by user {}", id, userId);
    }

    private void checkVisibility(Song song, String requesterId) {
        if (song.getVisibility() == Visibility.PRIVATE) {
            if (requesterId == null || !requesterId.equals(song.getUploadedBy())) {
                throw new AccessDeniedException("This song is private");
            }
        }
    }

}
