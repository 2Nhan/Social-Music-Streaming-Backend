package com.tunhan.micsu.service.playlist;

import com.tunhan.micsu.dto.request.PlaylistRequest;
import com.tunhan.micsu.dto.response.PlaylistResponse;

public interface PlaylistService {
    PlaylistResponse createPlaylist(PlaylistRequest request, String userId);

    PlaylistResponse updatePlaylist(String id, PlaylistRequest request, String userId);

    void deletePlaylist(String id, String userId);

    PlaylistResponse addSong(String playlistId, String songId, String userId);

    PlaylistResponse removeSong(String playlistId, String songId, String userId);

    PlaylistResponse getPlaylistById(String id);
}
