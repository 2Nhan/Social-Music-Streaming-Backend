package com.tunhan.micsu.dto.request;

import com.tunhan.micsu.entity.enums.GenreName;
import com.tunhan.micsu.entity.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SongUploadRequest {
    @NotBlank
    private String title;

    private String description;

    private MultipartFile imageFile;

    @NotNull
    private MultipartFile audioFile;

    /** Set programmatically from JWT — not from client body */
    private String uploadedBy;

    @NotNull(message = "Genre is required")
    private GenreName genre;

    /** Default PUBLIC if not specified */
    private Visibility visibility;
}
