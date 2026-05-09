package com.tunhan.micsu.mapper;

import com.tunhan.micsu.dto.response.CommentResponse;
import com.tunhan.micsu.entity.Comment;
import com.tunhan.micsu.entity.User;
import com.tunhan.micsu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final UserRepository userRepository;

    public CommentResponse toCommentResponse(Comment comment) {
        if (comment == null) return null;
        
        User user = userRepository.findById(comment.getUserId()).orElse(null);

        return CommentResponse.builder()
                .id(comment.getId())
                .songId(comment.getSongId())
                .userId(comment.getUserId())
                .username(user != null ? user.getUsername() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .content(comment.getContent())
                .timestampInSong(comment.getTimestampInSong())
                .createdAt(comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : null)
                .build();
    }
}
