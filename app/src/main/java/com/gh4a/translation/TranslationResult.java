package com.gh4a.translation;

public class TranslationResult {
    private final String mTranslatedText;
    private final String mDetectedSourceLang;
    private final boolean mFromCache;

    public TranslationResult(String translatedText, String detectedSourceLang, boolean fromCache) {
        mTranslatedText = translatedText;
        mDetectedSourceLang = detectedSourceLang;
        mFromCache = fromCache;
    }

    public String translatedText() {
        return mTranslatedText;
    }

    public String detectedSourceLang() {
        return mDetectedSourceLang;
    }

    public boolean fromCache() {
        return mFromCache;
    }

    public TranslationResult withCacheFlag(boolean fromCache) {
        return new TranslationResult(mTranslatedText, mDetectedSourceLang, fromCache);
    }
}
