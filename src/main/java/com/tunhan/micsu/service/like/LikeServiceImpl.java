package com.tunhan.micsu.service.like;

import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.entity.Song;
import com.tunhan.micsu.entity.SongFavorite;
import com.tunhan.micsu.entity.User;
import com.tunhan.micsu.exception.DuplicateResourceException;
import com.tunhan.micsu.exception.ResourceNotFoundException;
import com.tunhan.micsu.mapper.SongMapper;
import com.tunhan.micsu.repository.SongFavoriteRepository;
import com.tunhan.micsu.repository.SongRepository;
import com.tunhan.micsu.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final SongFavoriteRepository songFavoriteRepository;
    private final SongMapper songMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public SongResponse likeSong(String songId, String userId) {
        if(songFavoriteRepository.existsByUserIdAndSongId(userId, songId)) {
            throw new DuplicateResourceException("User has already liked this song");
        }
        User proxyUser = userRepository.getReferenceById(userId);
        Song song = songRepository.findById(songId).orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + songId));

        SongFavorite songFavorite = SongFavorite.builder()
                .user(proxyUser)
                .song(song)
                .build();

        songFavoriteRepository.save(songFavorite);

        songRepository.incrementFavoriteCount(songId);

        return songMapper.toSongResponse(song);
    }

    @Override
    @Transactional
    public void atomicUpdateLikeSong(String songId) {
        songRepository.incrementFavoriteCount(songId);
    }

    @Override
    public void like(String songId, String userId) {
        if(!redisLike(songId, userId)) {
            throw new DuplicateResourceException("User has already liked this song");
        }
    }

    @Override
    public void unlike(String songId, String userId) {
        if(!redisUnlike(songId, userId)) {
            throw new ResourceNotFoundException("User has not liked this song");
        }
    }

    private boolean redisLike(String songId, String userId) {
        String key = "likes:" + songId;
        Long added = stringRedisTemplate.opsForSet().add(key, userId);
        return added != null && added == 1L;
    }

    private boolean redisUnlike(String songId, String userId) {
        String key = "likes:" + songId;
        Long removed = stringRedisTemplate.opsForSet().remove(key, userId);
        return removed != null && removed == 1L;
    }

    private long getLikeCount(String songId) {
        String key = "likes:" + songId;
        Long count = stringRedisTemplate.opsForSet().size(key);
        return count != null ? count : 0L;
    }

    public boolean hasliked(String songId, String userId) {
        String key = "likes:" + songId;
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId);
        return Boolean.TRUE.equals(isMember);
    }

    @Override
    public long incrementLikeCount(String songId) {
        String key = "song_like_count:" + songId;
        Long currentCount = stringRedisTemplate.opsForValue().increment(key);

        return currentCount != null ? currentCount : 0L;
    }
}
