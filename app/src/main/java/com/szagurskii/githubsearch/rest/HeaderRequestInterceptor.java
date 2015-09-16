package com.szagurskii.githubsearch.rest;

import android.util.Base64;

import retrofit.RequestInterceptor;

/**
 * @author Savelii Zagurskii
 */
public class HeaderRequestInterceptor implements RequestInterceptor {
    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Accept", "application/vnd.github.v3+json");

        final String credentials = "zsavely" + ":" + "b3cccc15b40d1ba43049a99dc15180f8d482e340";
        String string = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        request.addHeader("Authorization", string);
    }
}