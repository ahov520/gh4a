package com.gh4a.translation;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.gh4a.R;
import com.gh4a.utils.ApiHelpers;
import com.gh4a.utils.StringUtils;
import com.gh4a.fragment.SettingsFragment;
import com.meisolsson.githubsdk.core.ServiceGenerator;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class TranslationClient {
    private static final String DEFAULT_TARGET_LANG = "zh-CN";
    private static final String DEFAULT_PATH = "/translate";
    private static final String DEFAULT_SOURCE_LANG = "auto";
    private static final String DEFAULT_FORMAT = "markdown";

    private final Context mContext;
    private final SharedPreferences mPrefs;

    public TranslationClient(Context context) {
        mContext = context.getApplicationContext();
        mPrefs = mContext.getSharedPreferences(SettingsFragment.PREF_NAME, Context.MODE_PRIVATE);
    }

    public Single<TranslationResult> translate(String sourceText) {
        final String normalizedText = normalizeText(sourceText);
        if (StringUtils.isBlank(normalizedText)) {
            return Single.error(new IllegalArgumentException(
                    mContext.getString(R.string.translation_empty_content)));
        }

        final Config config;
        try {
            config = loadConfig();
        } catch (RuntimeException e) {
            return Single.error(e);
        }

        final String cacheKey = String.format(Locale.US, "%s|%s",
                config.targetLang, normalizedText);

        TranslationResult cachedResult = TranslationCache.get().get(cacheKey);
        if (cachedResult != null) {
            return Single.just(cachedResult.withCacheFlag(true));
        }

        TranslationRequest request = new TranslationRequest(
                normalizedText, DEFAULT_SOURCE_LANG, config.targetLang, DEFAULT_FORMAT);

        return createService(config)
                .translate(config.endpointUrl, request)
                .map(ApiHelpers::throwOnFailure)
                .map(response -> {
                    if (response == null || StringUtils.isBlank(response.translatedText())) {
                        throw new IllegalStateException(mContext.getString(
                                R.string.translation_invalid_response));
                    }
                    return new TranslationResult(response.translatedText(),
                            response.detectedSourceLang(), false);
                })
                .doOnSuccess(result -> TranslationCache.get().put(cacheKey, result));
    }

    private Config loadConfig() {
        boolean enabled = mPrefs.getBoolean(SettingsFragment.KEY_TRANSLATION_ENABLED, true);
        if (!enabled) {
            throw new IllegalStateException(mContext.getString(R.string.translation_disabled));
        }

        String baseUrlInput = mPrefs.getString(SettingsFragment.KEY_TRANSLATION_GATEWAY_BASE_URL, "");
        if (StringUtils.isBlank(baseUrlInput)) {
            throw new IllegalStateException(mContext.getString(R.string.translation_missing_base_url));
        }

        String baseUrl = baseUrlInput.trim();
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "https://" + baseUrl;
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        HttpUrl baseHttpUrl = HttpUrl.parse(baseUrl);
        if (baseHttpUrl == null) {
            throw new IllegalStateException(mContext.getString(R.string.translation_invalid_base_url));
        }

        String path = mPrefs.getString(SettingsFragment.KEY_TRANSLATION_GATEWAY_PATH, DEFAULT_PATH);
        if (TextUtils.isEmpty(path)) {
            path = DEFAULT_PATH;
        }
        path = path.trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        HttpUrl endpointUrl = baseHttpUrl.newBuilder()
                .encodedPath(path)
                .build();

        String targetLang = mPrefs.getString(SettingsFragment.KEY_TRANSLATION_TARGET_LANG,
                DEFAULT_TARGET_LANG);
        if (StringUtils.isBlank(targetLang)) {
            targetLang = DEFAULT_TARGET_LANG;
        }

        int timeoutSec = mPrefs.getInt(SettingsFragment.KEY_TRANSLATION_TIMEOUT_SEC, 15);
        if (timeoutSec <= 0) {
            timeoutSec = 15;
        }

        String apiKey = mPrefs.getString(SettingsFragment.KEY_TRANSLATION_API_KEY, "");

        return new Config(baseHttpUrl.toString(), endpointUrl.toString(),
                targetLang, timeoutSec, apiKey);
    }

    private TranslationGatewayService createService(Config config) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(config.timeoutSec, TimeUnit.SECONDS)
                .readTimeout(config.timeoutSec, TimeUnit.SECONDS)
                .writeTimeout(config.timeoutSec, TimeUnit.SECONDS)
                .callTimeout(config.timeoutSec + 5L, TimeUnit.SECONDS);

        if (!StringUtils.isBlank(config.apiKey)) {
            clientBuilder.addInterceptor(chain -> {
                Request request = chain.request()
                        .newBuilder()
                        .addHeader("Authorization", "Bearer " + config.apiKey)
                        .addHeader("X-API-Key", config.apiKey)
                        .build();
                return chain.proceed(request);
            });
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(config.baseUrl)
                .client(clientBuilder.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(ServiceGenerator.moshi))
                .build();

        return retrofit.create(TranslationGatewayService.class);
    }

    private static String normalizeText(String sourceText) {
        String result = sourceText == null ? "" : sourceText.trim();
        // Clamp repeated empty lines to reduce gateway load.
        return result.replaceAll("\\n{3,}", "\\n\\n");
    }

    private static class Config {
        final String baseUrl;
        final String endpointUrl;
        final String targetLang;
        final int timeoutSec;
        final String apiKey;

        Config(String baseUrl, String endpointUrl, String targetLang,
                int timeoutSec, String apiKey) {
            this.baseUrl = baseUrl;
            this.endpointUrl = endpointUrl;
            this.targetLang = targetLang;
            this.timeoutSec = timeoutSec;
            this.apiKey = apiKey;
        }
    }
}
