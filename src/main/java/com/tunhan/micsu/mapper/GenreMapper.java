package com.tunhan.micsu.mapper;

import com.tunhan.micsu.dto.response.GenreResponse;
import com.tunhan.micsu.entity.Genre;
import org.springframework.stereotype.Component;

@Component
public class GenreMapper {
    public GenreResponse toGenreResponse(Genre genre) {
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .image(genre.getImage())
                .build();
    }
}
