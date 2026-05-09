package com.tunhan.micsu.mapper;

import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.entity.Song;
import com.tunhan.micsu.entity.User;
import com.tunhan.micsu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SongMapper {

    private final UserRepository userRepository;

    public SongResponse toSongResponse(Song song) {
        User uploader = song.getUploadedBy() != null
                ? userRepository.findById(song.getUploadedBy()).orElse(null)
                : null;

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
                .uploaderUsername(uploader != null ? uploader.getUsername() : null)
                .uploaderAvatarUrl(uploader != null ? uploader.getAvatarUrl() : null)
                .createdAt(song.getCreatedAt() != null ? song.getCreatedAt().toString() : null)
                .updatedAt(song.getUpdatedAt() != null ? song.getUpdatedAt().toString() : null)
                .build();
    }
}
