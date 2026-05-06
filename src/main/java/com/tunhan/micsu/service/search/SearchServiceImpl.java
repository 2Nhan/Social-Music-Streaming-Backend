package com.tunhan.micsu.service.search;

import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.mapper.SongMapper;
import com.tunhan.micsu.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class SearchServiceImpl implements SearchService {
    private final SongRepository songRepository;
    private final SongMapper songMapper;

    @Override
    public List<String> autocompletSearch(String query) {
        List<String> autocomplete = songRepository.autocompleteSearch(query);
        log.info("[SearchService] Autocomplete search for query: '{}', found {} results", query, autocomplete.size());
        return autocomplete;
    }

    @Override
    public PageResponse<SongResponse> searchResults(String query, Pageable pageable) {
        var page = songRepository.searchResults(query, pageable);
        log.info("[SearchService] Search results for query: '{}', found {} results", query, page.getTotalElements());
        return PageResponse.<SongResponse>builder()
                .content(page.getContent().stream().map(songMapper::toSongResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }
}
