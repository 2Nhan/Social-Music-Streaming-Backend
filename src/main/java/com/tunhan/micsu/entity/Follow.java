package com.tunhan.micsu.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "follows", uniqueConstraints = @UniqueConstraint(columnNames = { "follower_id", "following_id" }))
public class Follow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** FK to users.id — người đang follow */
    @Column(name = "follower_id", nullable = false)
    private String followerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", insertable = false, updatable = false)
    private User follower;

    /** FK to users.id — người được follow */
    @Column(name = "following_id", nullable = false)
    private String followingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", insertable = false, updatable = false)
    private User following;
}
