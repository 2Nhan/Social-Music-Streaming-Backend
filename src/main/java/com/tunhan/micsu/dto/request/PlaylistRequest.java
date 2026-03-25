package com.tunhan.micsu.dto.request;

import com.tunhan.micsu.entity.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaylistRequest {

    @NotBlank(message = "Playlist name is required")
    private String name;

    private String description;
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;
}
