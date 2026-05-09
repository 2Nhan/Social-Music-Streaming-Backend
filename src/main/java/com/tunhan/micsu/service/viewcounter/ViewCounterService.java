package com.tunhan.micsu.service.viewcounter;

import com.tunhan.micsu.dto.response.SongResponse;

public interface ViewCounterService {
    SongResponse increaseViewCount(String songId, String requesterId);
}
