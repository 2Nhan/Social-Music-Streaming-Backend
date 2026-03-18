package com.tunhan.micsu.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "playlist_songs", uniqueConstraints = @UniqueConstraint(columnNames = { "playlist_id", "song_id" }))
public class PlaylistSong extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** FK to playlists.id */
    @Column(name = "playlist_id", nullable = false)
    private String playlistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", insertable = false, updatable = false)
    private Playlist playlist;

    /** FK to songs.id */
    @Column(name = "song_id", nullable = false)
    private String songId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", insertable = false, updatable = false)
    private Song song;

    @Builder.Default
    @Column(name = "position")
    private Integer position = 0;
}
