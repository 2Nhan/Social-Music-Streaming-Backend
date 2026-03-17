package com.tunhan.micsu.service.hls;

import java.io.IOException;
import java.nio.file.Path;

public interface HlsService {
    String processHls(Path path, String songId) throws IOException;
    
}
