package com.tunhan.micsu.service.song;

import com.tunhan.micsu.dto.request.SongUpdateRequest;
import com.tunhan.micsu.dto.request.SongUploadRequest;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface SongService {

    void uploadSong(SongUploadRequest request) throws IOException;

    void uploadSongV2(SongUploadRequest request) throws IOException;

    PageResponse<SongResponse> getAllSongs(Pageable pageable);

    SongResponse getSongById(String id, String requesterId);

    String playSong(String id);

    SongResponse updateSong(String id, SongUpdateRequest request, String userId);

    void deleteSong(String id, String userId);
}
