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
@Table(name = "playlists")
public class Playlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image")
    private String coverImage;

    /** FK to users.id */
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User creator;

    @Builder.Default
    @Column(name = "song_count")
    private Long songCount = 0L;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 10)
    private Visibility visibility = Visibility.PUBLIC;
}
