package com.tunhan.micsu.dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileRequest {
    private String bio;
    private MultipartFile avatarFile;
}
