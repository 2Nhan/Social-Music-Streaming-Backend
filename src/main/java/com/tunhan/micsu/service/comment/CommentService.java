package com.tunhan.micsu.service.comment;

import com.tunhan.micsu.dto.request.CommentRequest;
import com.tunhan.micsu.dto.response.CommentResponse;
import com.tunhan.micsu.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentResponse addComment(String songId, CommentRequest request, String userId);

    PageResponse<CommentResponse> getSongComments(String songId, Pageable pageable);

    void deleteComment(String commentId, String userId);
}
