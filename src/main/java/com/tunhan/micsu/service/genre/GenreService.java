package com.tunhan.micsu.service.genre;

import com.tunhan.micsu.dto.response.GenreResponse;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.entity.enums.GenreName;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GenreService {
    List<GenreResponse> getAllGenres();

    PageResponse<SongResponse> getSongsByGenre(GenreName genreName, Pageable pageable);
}
