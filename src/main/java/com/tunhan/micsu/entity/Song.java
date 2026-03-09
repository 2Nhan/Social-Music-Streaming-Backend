package com.tunhan.micsu.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "songs")
public class Song extends BaseEntity {
    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "lyrics_data", columnDefinition = "TEXT")
    private String lyricsData;

    @Builder.Default
    @Column(name = "favorite_count")
    private Long favoriteCount = 0L;

    @Builder.Default
    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Builder.Default
    @Column(name = "repost_count")
    private Long repostCount = 0L;
}
