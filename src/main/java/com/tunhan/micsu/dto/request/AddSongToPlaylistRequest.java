package com.tunhan.micsu.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddSongToPlaylistRequest {

    @NotBlank(message = "Song ID is required")
    private String songId;
}
