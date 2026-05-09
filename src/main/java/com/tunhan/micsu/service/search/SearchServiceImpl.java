package com.tunhan.micsu.service.search;

import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.entity.Song;
import com.tunhan.micsu.mapper.SongMapper;
import com.tunhan.micsu.repository.SongFavoriteRepository;
import com.tunhan.micsu.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class SearchServiceImpl implements SearchService {
    private final SongRepository songRepository;
    private final SongFavoriteRepository songFavoriteRepository;
    private final SongMapper songMapper;

    @Override
    public List<String> autocompletSearch(String query) {
        List<String> autocomplete = songRepository.autocompleteSearch(query);
        log.info("[SearchService] Autocomplete search for query: '{}', found {} results", query, autocomplete.size());
        return autocomplete;
    }

    @Override
    public PageResponse<SongResponse> searchResults(String query, Pageable pageable, String requesterId) {
        var page = songRepository.searchResults(query, pageable);
        log.info("[SearchService] Search results for query: '{}', found {} results", query, page.getTotalElements());

        List<Song> songs = page.getContent();
        Set<String> likedSongIds = requesterId != null && !songs.isEmpty()
            ? songFavoriteRepository.findLikedSongIdsByUserIdAndSongIds(
                requesterId,
                songs.stream().map(Song::getId).toList())
            : Collections.emptySet();

        return PageResponse.<SongResponse>builder()
                .content(songs.stream()
                        .map(song -> songMapper.toSongResponse(song,
                                requesterId == null ? null : likedSongIds.contains(song.getId())))
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }
}
