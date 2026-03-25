package com.tunhan.micsu.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RepostResponse {
    private String id;
    private String songId;
    private String userId;
    private String createdAt;
}
