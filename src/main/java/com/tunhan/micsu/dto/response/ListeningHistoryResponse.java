package com.tunhan.micsu.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListeningHistoryResponse {
    private String id;
    private String songId;
    private String listenedAt;
}
