package com.tunhan.micsu.dto.response;

import com.tunhan.micsu.entity.enums.TargetType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeResponse {
    private String id;
    private TargetType targetType;
    private String targetId;
    private String createdAt;
}
