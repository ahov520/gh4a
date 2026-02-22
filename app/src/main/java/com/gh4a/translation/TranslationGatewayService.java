package com.gh4a.translation;

import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface TranslationGatewayService {
    @POST
    Single<Response<TranslationResponse>> translate(@Url String url,
            @Body TranslationRequest request);
}
