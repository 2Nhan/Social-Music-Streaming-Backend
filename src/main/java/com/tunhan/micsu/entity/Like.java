package com.tunhan.micsu.entity;

import com.tunhan.micsu.entity.enums.TargetType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "target_type", "target_id" }))
public class Like extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** FK to users.id */
    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    /**
     * Polymorphic FK — either songs.id or playlists.id depending on targetType.
     * No @ManyToOne here since it references different tables.
     */
    @Column(name = "target_id", nullable = false)
    private String targetId;
}
