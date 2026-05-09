package com.tunhan.micsu.dto.response;

import com.tunhan.micsu.entity.enums.GenreName;
import com.tunhan.micsu.entity.enums.Visibility;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SongResponse {
    private String id;
    private String title;
    private String description;
    private String coverUrl;
    private Long duration;
    private String lyricsData;
    private Long favoriteCount;
    private Long viewCount;
    private Long repostCount;
    private Visibility visibility;
    private String genreId;
    private GenreName genreName;
    private String genreImage;
    private String uploadedBy;
    private String uploaderUsername;
    private String uploaderAvatarUrl;
    private String createdAt;
    private String updatedAt;
}
