package com.tunhan.micsu.service.repost;

import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.RepostResponse;
import com.tunhan.micsu.entity.Repost;
import com.tunhan.micsu.entity.Song;
import com.tunhan.micsu.exception.DuplicateResourceException;
import com.tunhan.micsu.exception.ResourceNotFoundException;
import com.tunhan.micsu.repository.RepostRepository;
import com.tunhan.micsu.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepostServiceImpl implements RepostService {

    private final RepostRepository repostRepository;
    private final SongRepository songRepository;

    @Override
    @Transactional
    public RepostResponse repost(String songId, String userId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song", songId));

        if (repostRepository.existsByUserIdAndSongId(userId, songId)) {
            throw new DuplicateResourceException("Already reposted this song");
        }

        Repost repost = Repost.builder()
                .userId(userId)
                .songId(songId)
                .build();
        repostRepository.save(repost);

        song.setRepostCount(song.getRepostCount() + 1);
        songRepository.save(song);

        log.info("[RepostService] User {} reposted song {}", userId, songId);
        return toResponse(repost);
    }

    @Override
    @Transactional
    public void unrepost(String songId, String userId) {
        Repost repost = repostRepository.findByUserIdAndSongId(userId, songId)
                .orElseThrow(() -> new ResourceNotFoundException("Repost not found for song: " + songId));

        repostRepository.delete(repost);

        songRepository.findById(songId).ifPresent(song -> {
            song.setRepostCount(Math.max(0, song.getRepostCount() - 1));
            songRepository.save(song);
        });

        log.info("[RepostService] User {} un-reposted song {}", userId, songId);
    }

    @Override
    public PageResponse<RepostResponse> getUserReposts(String userId, Pageable pageable) {
        Page<Repost> page = repostRepository.findByUserId(userId, pageable);
        return PageResponse.<RepostResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }

    private RepostResponse toResponse(Repost repost) {
        return RepostResponse.builder()
                .id(repost.getId())
                .songId(repost.getSongId())
                .userId(repost.getUserId())
                .createdAt(repost.getCreatedAt() != null ? repost.getCreatedAt().toString() : null)
                .build();
    }
}
