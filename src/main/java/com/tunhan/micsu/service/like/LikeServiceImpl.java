package com.tunhan.micsu.service.like;

import com.tunhan.micsu.dto.request.LikeRequest;
import com.tunhan.micsu.dto.response.LikeResponse;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.entity.Like;
import com.tunhan.micsu.exception.AccessDeniedException;
import com.tunhan.micsu.exception.DuplicateResourceException;
import com.tunhan.micsu.exception.ResourceNotFoundException;
import com.tunhan.micsu.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;

    @Override
    public LikeResponse like(LikeRequest request, String userId) {
        if (likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, request.getTargetType(),
                request.getTargetId())) {
            throw new DuplicateResourceException("Already liked this " + request.getTargetType().name().toLowerCase());
        }
        Like like = Like.builder()
                .userId(userId)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .build();
        likeRepository.save(like);
        log.info("[LikeService] User {} liked {} {}", userId, request.getTargetType(), request.getTargetId());
        return toResponse(like);
    }

    @Override
    public void unlike(String likeId, String userId) {
        Like like = likeRepository.findById(likeId)
                .orElseThrow(() -> new ResourceNotFoundException("Like", likeId));
        if (!like.getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only remove your own likes");
        }
        likeRepository.delete(like);
        log.info("[LikeService] User {} unliked {}", userId, likeId);
    }

    @Override
    public PageResponse<LikeResponse> getUserLikes(String userId, Pageable pageable) {
        Page<Like> page = likeRepository.findByUserId(userId, pageable);
        return PageResponse.<LikeResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }

    private LikeResponse toResponse(Like like) {
        return LikeResponse.builder()
                .id(like.getId())
                .targetType(like.getTargetType())
                .targetId(like.getTargetId())
                .createdAt(like.getCreatedAt() != null ? like.getCreatedAt().toString() : null)
                .build();
    }
}
