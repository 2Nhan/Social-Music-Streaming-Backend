package com.tunhan.micsu.service.like;

import com.tunhan.micsu.dto.response.SongResponse;


public interface LikeService {


    SongResponse likeSong(String songId, String userId);

    SongResponse unlikeSong(String songId, String userId);

    void atomicUpdateLikeSong(String songId);

    void like(String songId, String userId);

    void unlike(String songId, String userId);

    long incrementLikeCount(String songId);
}
