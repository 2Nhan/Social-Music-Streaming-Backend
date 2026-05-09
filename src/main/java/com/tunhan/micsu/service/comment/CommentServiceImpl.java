package com.tunhan.micsu.service.comment;

import com.tunhan.micsu.dto.request.CommentRequest;
import com.tunhan.micsu.dto.response.CommentResponse;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.entity.Comment;
import com.tunhan.micsu.exception.AccessDeniedException;
import com.tunhan.micsu.exception.ResourceNotFoundException;
import com.tunhan.micsu.repository.CommentRepository;
import com.tunhan.micsu.repository.SongRepository;
import com.tunhan.micsu.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final SongRepository songRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentResponse addComment(String songId, CommentRequest request, String userId) {
        if (!songRepository.existsById(songId)) {
            throw new ResourceNotFoundException("Song", songId);
        }
        Comment comment = Comment.builder()
                .songId(songId)
                .userId(userId)
                .content(request.getContent())
                .timestampInSong(request.getTimestampInSong())
                .build();
        commentRepository.save(comment);
        log.info("[CommentService] User {} commented on song {}", userId, songId);
        return commentMapper.toCommentResponse(comment);
    }

    @Override
    public PageResponse<CommentResponse> getSongComments(String songId, Pageable pageable) {
        Page<Comment> page = commentRepository.findBySongIdOrderByCreatedAtAsc(songId, pageable);
        return PageResponse.<CommentResponse>builder()
                .content(page.getContent().stream().map(commentMapper::toCommentResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }

    @Override
    public void deleteComment(String commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
        if (!comment.getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
        log.info("[CommentService] User {} deleted comment {}", userId, commentId);
    }

}
