package com.tunhan.micsu.service.viewcounter;

import com.tunhan.micsu.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewCounterServiceImpl implements ViewCounterService {
    private final SongRepository songRepository;

    @Override
    public void increaseViewCount(String songId) {
        songRepository.findById(songId).ifPresent(song -> {
            song.setViewCount(song.getViewCount() + 1);
            songRepository.save(song);
            log.info("[ViewCounterService] Incremented view count for song {}: new count = {}", songId, song.getViewCount());
        });
    }
}
