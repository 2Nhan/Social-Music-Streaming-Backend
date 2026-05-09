package com.tunhan.micsu.mapper;

import com.tunhan.micsu.dto.response.RepostResponse;
import com.tunhan.micsu.entity.Repost;
import org.springframework.stereotype.Component;

@Component
public class RepostMapper {

    public RepostResponse toRepostResponse(Repost repost) {
        if (repost == null) return null;
        return RepostResponse.builder()
                .id(repost.getId())
                .songId(repost.getSongId())
                .userId(repost.getUserId())
                .createdAt(repost.getCreatedAt() != null ? repost.getCreatedAt().toString() : null)
                .build();
    }
}
