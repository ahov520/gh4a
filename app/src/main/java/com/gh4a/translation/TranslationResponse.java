package com.gh4a.translation;

import com.squareup.moshi.Json;

public class TranslationResponse {
    @Json(name = "translated_text")
    private String mTranslatedText;
    @Json(name = "detected_source_lang")
    private String mDetectedSourceLang;

    public String translatedText() {
        return mTranslatedText;
    }

    public String detectedSourceLang() {
        return mDetectedSourceLang;
    }
}
