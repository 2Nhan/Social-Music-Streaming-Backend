package com.tunhan.micsu.service.playlist;

import com.tunhan.micsu.dto.request.PlaylistRequest;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.PlaylistResponse;
import com.tunhan.micsu.entity.Playlist;
import com.tunhan.micsu.entity.PlaylistSong;
import com.tunhan.micsu.entity.enums.Visibility;
import com.tunhan.micsu.exception.AccessDeniedException;
import com.tunhan.micsu.exception.DuplicateResourceException;
import com.tunhan.micsu.exception.ResourceNotFoundException;
import com.tunhan.micsu.mapper.SongMapper;
import com.tunhan.micsu.repository.PlaylistRepository;
import com.tunhan.micsu.repository.PlaylistSongRepository;
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
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongRepository songRepository;
    private final SongMapper songMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PlaylistResponse> getAllPublicPlaylists(Pageable pageable) {
        Page<Playlist> page = playlistRepository.findAllByVisibility(Visibility.PUBLIC, pageable);
        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PlaylistResponse> getAllPlaylists(String userId, Pageable pageable) {
        Page<Playlist> page = playlistRepository.findByCreatedBy(userId, pageable);
        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PlaylistResponse> getPublicPlaylistsByUser(String userId, Pageable pageable) {
        Page<Playlist> page = playlistRepository.findByCreatedByAndVisibility(userId, Visibility.PUBLIC, pageable);
        return toPageResponse(page);
    }

    @Override
    public PlaylistResponse createPlaylist(PlaylistRequest request, String userId) {
        Playlist playlist = Playlist.builder()
                .name(request.getName())
                .description(request.getDescription())
                .visibility(request.getVisibility() != null ? request.getVisibility() : Visibility.PUBLIC)
                .createdBy(userId)
                .build();
        playlistRepository.save(playlist);
        log.info("[PlaylistService] User {} created playlist {}", userId, playlist.getId());
        return toResponse(playlist);
    }

    @Override
    public PlaylistResponse updatePlaylist(String id, PlaylistRequest request, String userId) {
        Playlist playlist = findAndCheckOwnership(id, userId);
        if (request.getName() != null)
            playlist.setName(request.getName());
        if (request.getDescription() != null)
            playlist.setDescription(request.getDescription());
        if (request.getVisibility() != null)
            playlist.setVisibility(request.getVisibility());
        playlistRepository.save(playlist);
        return toResponse(playlist);
    }

    @Override
    public PlaylistResponse updatePlaylistVisibility(String id, Visibility visibility, String userId) {
        Playlist playlist = findAndCheckOwnership(id, userId);
        playlist.setVisibility(visibility);
        playlistRepository.save(playlist);
        return toResponse(playlist);
    }

    @Override
    public void deletePlaylist(String id, String userId) {
        Playlist playlist = findAndCheckOwnership(id, userId);
        playlistRepository.delete(playlist);
        log.info("[PlaylistService] User {} deleted playlist {}", userId, id);
    }

    @Override
    @Transactional
    public PlaylistResponse addSong(String playlistId, String songId, String userId) {
        Playlist playlist = findAndCheckOwnership(playlistId, userId);

        if (!songRepository.existsById(songId)) {
            throw new ResourceNotFoundException("Song", songId);
        }
        if (playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId).isPresent()) {
            throw new DuplicateResourceException("Song already in this playlist");
        }

        int position = (int) playlistSongRepository.countByPlaylistId(playlistId);
        PlaylistSong entry = PlaylistSong.builder()
                .playlistId(playlistId)
                .songId(songId)
                .position(position)
                .build();
        playlistSongRepository.save(entry);

        playlist.setSongCount(playlist.getSongCount() + 1);
        playlistRepository.save(playlist);
        return toResponse(playlist);
    }

    @Override
    @Transactional
    public PlaylistResponse removeSong(String playlistId, String songId, String userId) {
        Playlist playlist = findAndCheckOwnership(playlistId, userId);
        playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found in playlist"));
        playlistSongRepository.deleteByPlaylistIdAndSongId(playlistId, songId);

        playlist.setSongCount(Math.max(0, playlist.getSongCount() - 1));
        playlistRepository.save(playlist);
        return toResponse(playlist);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaylistResponse getPlaylistById(String id, String userId) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist", id));
        if (playlist.getVisibility() == Visibility.PRIVATE && !playlist.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to view this playlist");
        }
        return toResponseWithSongs(playlist);
    }

    private Playlist findAndCheckOwnership(String id, String userId) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist", id));
        if (!playlist.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException("You can only manage your own playlists");
        }
        return playlist;
    }

    private PageResponse<PlaylistResponse> toPageResponse(Page<Playlist> page) {
        return PageResponse.<PlaylistResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }

    private PlaylistResponse toResponse(Playlist playlist) {
        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .coverImage(playlist.getCoverImage())
                .visibility(playlist.getVisibility())
                .songCount(playlist.getSongCount())
                .createdBy(playlist.getCreatedBy())
                .createdAt(playlist.getCreatedAt() != null ? playlist.getCreatedAt().toString() : null)
                .build();
    }

    private PlaylistResponse toResponseWithSongs(Playlist playlist) {
        PlaylistResponse response = toResponse(playlist);
        response.setSongs(playlistSongRepository.findWithSongByPlaylistIdOrderByPosition(playlist.getId())
                .stream()
                .map(PlaylistSong::getSong)
                .map(songMapper::toSongResponse)
                .toList());
        return response;
    }
}
