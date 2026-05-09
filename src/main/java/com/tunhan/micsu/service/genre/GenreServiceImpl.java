package com.tunhan.micsu.service.genre;

import com.tunhan.micsu.dto.response.GenreResponse;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.entity.Genre;
import com.tunhan.micsu.entity.Song;
import com.tunhan.micsu.exception.ResourceNotFoundException;
import com.tunhan.micsu.mapper.GenreMapper;
import com.tunhan.micsu.mapper.SongMapper;
import com.tunhan.micsu.repository.GenreRepository;
import com.tunhan.micsu.repository.SongFavoriteRepository;
import com.tunhan.micsu.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final SongRepository songRepository;
    private final SongFavoriteRepository songFavoriteRepository;
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
    public PageResponse<SongResponse> getSongsByGenre(String genreId, Pageable pageable, String requesterId) {
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", genreId));
        var page = songRepository.findByGenreId(genre.getId(), pageable);

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
