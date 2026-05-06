package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.response.ApiResponse;
import com.tunhan.micsu.dto.response.PageResponse;
import com.tunhan.micsu.dto.response.SongResponse;
import com.tunhan.micsu.service.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/search")
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<String>>> autocompleteSearch(@RequestParam String query) {
        List<String> response = searchService.autocompletSearch(query);
        return ResponseEntity.ok(ApiResponse.success("Autocomplete search successful", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SongResponse>>> searchResults(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var response = searchService.searchResults(query, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", response));
    }
}
