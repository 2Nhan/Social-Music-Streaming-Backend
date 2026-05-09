package com.tunhan.micsu.entity;

import com.tunhan.micsu.entity.enums.GenreName;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "genres")
public class Genre extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private GenreName name;

    @Column(name = "image")
    private String image;

    @OneToMany(mappedBy = "genre")
    private List<Song> songs;
}
