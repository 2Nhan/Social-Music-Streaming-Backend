package com.tunhan.micsu.service.song;

import com.tunhan.micsu.dto.request.SongUpdateRequest;
import com.tunhan.micsu.dto.request.SongUploadRequest;
import com.tunhan.micsu.dto.response.SongDetailResponse;

import java.io.IOException;

public interface SongService {

    void uploadSong(SongUploadRequest request) throws IOException;

    void uploadSongV2(SongUploadRequest request) throws IOException;

    SongDetailResponse getSongById(String id, String requesterId);

    SongDetailResponse updateSong(String id, SongUpdateRequest request, String userId);

    void deleteSong(String id, String userId);
}
