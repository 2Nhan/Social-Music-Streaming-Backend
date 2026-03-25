package com.tunhan.micsu.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentRequest {

    @NotBlank(message = "Content is required")
    private String content;

    /**
     * Position in audio (milliseconds). Optional.
     */
    private Long timestampInSong;
}
