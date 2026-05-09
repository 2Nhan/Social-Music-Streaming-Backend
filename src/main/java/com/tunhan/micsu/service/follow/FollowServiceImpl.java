package com.tunhan.micsu.service.follow;

import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.UserProfileResponse;
import com.tunhan.micsu.entity.Follow;
import com.tunhan.micsu.entity.User;
import com.tunhan.micsu.exception.DuplicateResourceException;
import com.tunhan.micsu.exception.ResourceNotFoundException;
import com.tunhan.micsu.repository.FollowRepository;
import com.tunhan.micsu.repository.UserRepository;
import com.tunhan.micsu.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void follow(String targetUserId, String currentUserId) {
        if (targetUserId.equals(currentUserId)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));
        User follower = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUserId));

        if (followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId)) {
            throw new DuplicateResourceException("Already following this user");
        }

        Follow follow = Follow.builder()
                .followerId(currentUserId)
                .followingId(targetUserId)
                .build();
        followRepository.save(follow);

        target.setFollowersCount(target.getFollowersCount() + 1);
        follower.setFollowingCount(follower.getFollowingCount() + 1);
        userRepository.save(target);
        userRepository.save(follower);

        log.info("[FollowService] User {} followed user {}", currentUserId, targetUserId);
    }

    @Override
    @Transactional
    public void unfollow(String targetUserId, String currentUserId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(currentUserId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Not following user: " + targetUserId));

        followRepository.delete(follow);

        userRepository.findById(targetUserId).ifPresent(target -> {
            target.setFollowersCount(Math.max(0, target.getFollowersCount() - 1));
            userRepository.save(target);
        });
        userRepository.findById(currentUserId).ifPresent(curr -> {
            curr.setFollowingCount(Math.max(0, curr.getFollowingCount() - 1));
            userRepository.save(curr);
        });

        log.info("[FollowService] User {} unfollowed user {}", currentUserId, targetUserId);
    }

    @Override
    public PageResponse<UserProfileResponse> getFollowers(String userId, Pageable pageable) {
        Page<Follow> page = followRepository.findByFollowingId(userId, pageable);
        // ✅ Use JPA relationship navigation instead of N+1 userRepository.findById()
        return PageResponse.<UserProfileResponse>builder()
                .content(page.getContent().stream()
                        .map(follow -> userMapper.toUserProfileResponse(follow.getFollower()))
                        .filter(u -> u != null)
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }

    @Override
    public PageResponse<UserProfileResponse> getFollowing(String userId, Pageable pageable) {
        Page<Follow> page = followRepository.findByFollowerId(userId, pageable);
        // ✅ Use JPA relationship navigation instead of N+1 userRepository.findById()
        return PageResponse.<UserProfileResponse>builder()
                .content(page.getContent().stream()
                        .map(follow -> userMapper.toUserProfileResponse(follow.getFollowing()))
                        .filter(u -> u != null)
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }


}
