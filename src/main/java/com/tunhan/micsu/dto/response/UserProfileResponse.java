package com.tunhan.micsu.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponse {
    private String id;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private Long followersCount;
    private Long followingCount;
    private Long songCount;
    private String createdAt;
}
