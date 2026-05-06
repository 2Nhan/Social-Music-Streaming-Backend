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
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final SongFavoriteRepository songFavoriteRepository;
    private final SongMapper songMapper;

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
}
