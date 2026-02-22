package com.gh4a.translation;

import com.squareup.moshi.Json;

public class TranslationRequest {
    @Json(name = "text")
    public final String text;
    @Json(name = "source_lang")
    public final String sourceLang;
    @Json(name = "target_lang")
    public final String targetLang;
    @Json(name = "format")
    public final String format;

    public TranslationRequest(String text, String sourceLang, String targetLang, String format) {
        this.text = text;
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
        this.format = format;
    }
}
