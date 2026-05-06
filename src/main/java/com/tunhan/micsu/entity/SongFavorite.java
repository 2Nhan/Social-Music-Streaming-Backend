package com.tunhan.micsu.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "song_favorites",
        uniqueConstraints = {
                // Rất quan trọng: Đảm bảo 1 user chỉ favorite 1 bài hát 1 lần duy nhất
                @UniqueConstraint(
                        name = "uk_user_song_favorite",
                        columnNames = {"user_id", "song_id"}
                )
        }
)
public class SongFavorite extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Liên kết tới người thực hiện hành động Favorite (Like)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Liên kết tới bài hát được Favorite (Like)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

}