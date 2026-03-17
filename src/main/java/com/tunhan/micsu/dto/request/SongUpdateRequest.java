package com.tunhan.micsu.dto.request;

import com.tunhan.micsu.entity.enums.Visibility;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SongUpdateRequest {
    private String title;
    private String description;
    private String lyricsData;
    private Visibility visibility;
}
