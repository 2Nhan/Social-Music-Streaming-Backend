package com.tunhan.micsu.dto.response;

import com.tunhan.micsu.entity.enums.Visibility;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaylistResponse {
    private String id;
    private String name;
    private String description;
    private String coverImage;
    private Visibility visibility;
    private Long songCount;
    private String createdBy;
    private String createdAt;
}
