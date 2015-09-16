package com.szagurskii.githubsearch.rest;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.szagurskii.githubsearch.BuildConfig;
import com.szagurskii.githubsearch.realm.models.RepoRequest;
import com.szagurskii.githubsearch.realm.models.UserRequest;

import io.realm.RealmObject;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * @author Savelii Zagurskii
 */
public class RestClient {
    private static final String BASE_URL = "https://api.github.com";

    private static volatile RestClient instance;

    private GitHubService apiService;

    private RestClient() {
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                }).create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .setEndpoint(BASE_URL)
                .setRequestInterceptor(new HeaderRequestInterceptor())
                .setConverter(new GsonConverter(gson))
                .build();

        apiService = restAdapter.create(GitHubService.class);
    }

    public static RestClient getInstance() {
        RestClient localInstance = instance;
        if (localInstance == null) {
            synchronized (RestClient.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new RestClient();
                }
            }
        }
        return localInstance;
    }

    /**
     * Query GitHub service with specified query on repos.
     *
     * @param q specified query.
     * @return returned result.
     */
    public RepoRequest queryRepos(String q) {
        return apiService.searchReposSync(q);
    }

    /**
     * Query GitHub service with specified query on repos.
     *
     * @param q specified query.
     * @return returned result.
     */
    public UserRequest queryUsers(String q) {
        return apiService.searchUsersSync(q);
    }
}