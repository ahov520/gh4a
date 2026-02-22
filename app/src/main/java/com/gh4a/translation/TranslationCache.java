package com.gh4a.translation;

import java.util.LinkedHashMap;
import java.util.Map;

public class TranslationCache {
    private static final int MAX_ENTRIES = 200;
    private static final TranslationCache INSTANCE = new TranslationCache();

    private final Map<String, TranslationResult> mMap = new LinkedHashMap<>(MAX_ENTRIES, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, TranslationResult> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    public static TranslationCache get() {
        return INSTANCE;
    }

    public synchronized TranslationResult get(String key) {
        return mMap.get(key);
    }

    public synchronized void put(String key, TranslationResult value) {
        mMap.put(key, value);
    }
}
