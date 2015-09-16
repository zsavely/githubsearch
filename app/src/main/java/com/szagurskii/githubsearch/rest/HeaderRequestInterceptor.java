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

        final String credentials = "zsavely" + ":" + "c3260016bf76b4de6a6d24a5bdd66777bbf5eafe";
        String string = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        request.addHeader("Authorization", string);
    }
}