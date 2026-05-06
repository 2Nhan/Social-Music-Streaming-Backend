package com.tunhan.micsu.mapper;

import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.entity.Song;
import org.springframework.stereotype.Component;

@Component
public class SongMapper {
    public SongResponse toSongResponse(Song song) {
        return SongResponse.builder()
                .id(song.getId())
                .title(song.getTitle())
                .description(song.getDescription())
                .coverUrl(song.getCoverUrl())
                .duration(song.getDuration())
                .lyricsData(song.getLyricsData())
                .favoriteCount(song.getFavoriteCount())
                .viewCount(song.getViewCount())
                .repostCount(song.getRepostCount())
                .visibility(song.getVisibility())
                .uploadedBy(song.getUploadedBy())
                .createdAt(song.getCreatedAt() != null ? song.getCreatedAt().toString() : null)
                .updatedAt(song.getUpdatedAt() != null ? song.getUpdatedAt().toString() : null)
                .build();
    }
}
