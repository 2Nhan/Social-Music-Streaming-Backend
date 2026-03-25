package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.response.ApiResponse;
import com.tunhan.micsu.dto.response.CommentResponse;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.request.CommentRequest;
import com.tunhan.micsu.service.comment.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/v1/songs/{songId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable String songId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success("Comment added successfully",
                commentService.addComment(songId, request, jwt.getSubject())));
    }

    @GetMapping("/api/v1/songs/{songId}/comments")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getSongComments(
            @PathVariable String songId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                commentService.getSongComments(songId, pageable)));
    }

    @DeleteMapping("/api/v1/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        commentService.deleteComment(id, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }
}
