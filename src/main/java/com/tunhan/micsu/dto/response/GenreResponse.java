package com.tunhan.micsu.dto.response;

import com.tunhan.micsu.entity.enums.GenreName;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenreResponse {
    private String id;
    private GenreName name;
    private String image;
}
