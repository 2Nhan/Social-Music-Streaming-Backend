package com.tunhan.micsu.dto.request;

import com.tunhan.micsu.entity.enums.Visibility;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaylistVisibilityRequest {

    @NotNull(message = "Playlist visibility is required")
    private Visibility visibility;
}
