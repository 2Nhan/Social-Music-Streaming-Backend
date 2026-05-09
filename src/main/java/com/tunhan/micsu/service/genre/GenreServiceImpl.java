package com.tunhan.micsu.service.genre;

import com.tunhan.micsu.dto.response.GenreResponse;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.entity.Genre;
import com.tunhan.micsu.entity.enums.GenreName;
import com.tunhan.micsu.exception.ResourceNotFoundException;
import com.tunhan.micsu.mapper.GenreMapper;
import com.tunhan.micsu.mapper.SongMapper;
import com.tunhan.micsu.repository.GenreRepository;
import com.tunhan.micsu.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final SongRepository songRepository;
    private final GenreMapper genreMapper;
    private final SongMapper songMapper;

    @Override
    @Transactional(readOnly = true)
    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAll().stream()
                .sorted(Comparator.comparing(genre -> genre.getName().name()))
                .map(genreMapper::toGenreResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SongResponse> getSongsByGenre(GenreName genreName, Pageable pageable) {
        Genre genre = genreRepository.findByName(genreName)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", genreName.name()));
        var page = songRepository.findByGenreName(genre.getName(), pageable);
        return PageResponse.<SongResponse>builder()
                .content(page.getContent().stream().map(songMapper::toSongResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }
}
