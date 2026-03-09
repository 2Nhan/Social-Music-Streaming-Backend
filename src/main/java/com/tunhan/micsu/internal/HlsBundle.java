package com.tunhan.micsu.internal;

import java.util.Map;

public class HlsBundle {
    private final Map<String, byte[]> files;

    public HlsBundle(Map<String, byte[]> files) {
        this.files = files;
    }

    public Map<String, byte[]> getFiles() {
        return files;
    }
}
