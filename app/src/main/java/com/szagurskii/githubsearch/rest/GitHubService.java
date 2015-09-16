package com.szagurskii.githubsearch.rest;

import com.szagurskii.githubsearch.realm.models.RepoRequest;
import com.szagurskii.githubsearch.realm.models.UserRequest;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * @author Savelii Zagurskii
 */
public interface GitHubService {
    @GET("/search/repositories")
    RepoRequest searchReposSync(@Query(value = "q", encodeValue = true) String query);

    @GET("/search/repositories")
    void searchReposAsync(@Query(value = "q", encodeValue = true) String query,
                          Callback<RepoRequest> callback);

    @GET("/search/users")
    UserRequest searchUsersSync(@Query(value = "q", encodeValue = true) String query);

    @GET("/search/users")
    void searchUsersAsync(@Query(value = "q", encodeValue = true) String query,
                          Callback<UserRequest> callback);
}