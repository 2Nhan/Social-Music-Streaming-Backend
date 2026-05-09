package com.tunhan.micsu.service.viewcounter;

import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.entity.Song;
import com.tunhan.micsu.exception.ResourceNotFoundException;
import com.tunhan.micsu.mapper.SongMapper;
import com.tunhan.micsu.repository.SongFavoriteRepository;
import com.tunhan.micsu.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewCounterServiceImpl implements ViewCounterService {
    private final SongRepository songRepository;
    private final SongFavoriteRepository songFavoriteRepository;
    private final SongMapper songMapper;

    @Override
    public SongResponse increaseViewCount(String songId, String requesterId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song", songId));

        song.setViewCount((song.getViewCount() != null ? song.getViewCount() : 0L) + 1);
        songRepository.save(song);
        log.info("[ViewCounterService] Incremented view count for song {}: new count = {}", songId, song.getViewCount());
        Boolean isFavorited = requesterId == null
            ? null
            : songFavoriteRepository.existsByUserIdAndSongId(requesterId, song.getId());
        return songMapper.toSongResponse(song, isFavorited);
    }
}
