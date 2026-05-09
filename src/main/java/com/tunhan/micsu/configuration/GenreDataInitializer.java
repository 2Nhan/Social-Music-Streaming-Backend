package com.tunhan.micsu.configuration;

import com.tunhan.micsu.entity.Genre;
import com.tunhan.micsu.entity.enums.GenreName;
import com.tunhan.micsu.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GenreDataInitializer implements ApplicationRunner {

    private final GenreRepository genreRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (GenreName name : GenreName.values()) {
            if (!genreRepository.existsByName(name)) {
                genreRepository.save(Genre.builder()
                        .name(name)
                        .build());
            }
        }
    }
}
