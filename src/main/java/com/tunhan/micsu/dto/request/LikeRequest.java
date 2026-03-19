package com.tunhan.micsu.dto.request;

import com.tunhan.micsu.entity.enums.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeRequest {

    @NotNull(message = "Target type is required")
    private TargetType targetType;

    @NotBlank(message = "Target ID is required")
    private String targetId;
}
