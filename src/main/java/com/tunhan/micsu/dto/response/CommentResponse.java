package com.tunhan.micsu.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponse {
    private String id;
    private String songId;
    private String userId;
    private String username;
    private String avatarUrl;
    private String content;
    private Long timestampInSong;
    private String createdAt;
}
