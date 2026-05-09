package com.tunhan.micsu.service.search;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchService {
    List<String> autocompletSearch(String query);

    PageResponse<SongResponse> searchResults(String query, Pageable pageable, String requesterId);
}
