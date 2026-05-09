package com.tunhan.micsu.mapper;

import com.tunhan.micsu.dto.response.PlaylistResponse;
import com.tunhan.micsu.entity.Playlist;
import com.tunhan.micsu.entity.PlaylistSong;
import com.tunhan.micsu.repository.PlaylistSongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlaylistMapper {

    private final PlaylistSongRepository playlistSongRepository;
    private final SongMapper songMapper;

    public PlaylistResponse toPlaylistResponse(Playlist playlist) {
        if (playlist == null) return null;
        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .coverImage(playlist.getCoverImage())
                .visibility(playlist.getVisibility())
                .songCount(playlist.getSongCount())
                .createdBy(playlist.getCreatedBy())
                .createdAt(playlist.getCreatedAt() != null ? playlist.getCreatedAt().toString() : null)
                .build();
    }

    public PlaylistResponse toPlaylistResponseWithSongs(Playlist playlist) {
        if (playlist == null) return null;
        PlaylistResponse response = toPlaylistResponse(playlist);
        response.setSongs(playlistSongRepository.findWithSongByPlaylistIdOrderByPosition(playlist.getId())
                .stream()
                .map(PlaylistSong::getSong)
                .map(songMapper::toSongResponse)
                .toList());
        return response;
    }
}
