package com.tunhan.micsu.service.playlist;

import com.tunhan.micsu.dto.request.PlaylistRequest;
import com.tunhan.micsu.dto.response.PlaylistResponse;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.entity.enums.Visibility;
import org.springframework.data.domain.Pageable;

public interface PlaylistService {
    PageResponse<PlaylistResponse> getAllPublicPlaylists(Pageable pageable);

    PageResponse<PlaylistResponse> getAllPlaylists(String userId, Pageable pageable);

    PlaylistResponse createPlaylist(PlaylistRequest request, String userId);

    PlaylistResponse updatePlaylist(String id, PlaylistRequest request, String userId);

    PlaylistResponse updatePlaylistVisibility(String id, Visibility visibility, String userId);

    void deletePlaylist(String id, String userId);

    PlaylistResponse addSong(String playlistId, String songId, String userId);

    PlaylistResponse removeSong(String playlistId, String songId, String userId);

    PlaylistResponse getPlaylistById(String id, String userId);
}
