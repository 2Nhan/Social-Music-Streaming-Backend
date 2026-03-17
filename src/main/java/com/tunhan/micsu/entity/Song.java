package com.tunhan.micsu.entity;

import com.tunhan.micsu.entity.enums.Visibility;
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

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "audio_url")
    private String audioUrl;

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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 10)
    private Visibility visibility = Visibility.PUBLIC;

    /** FK to users.id */
    @Column(name = "uploaded_by")
    private String uploadedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", insertable = false, updatable = false)
    private User uploader;
}
