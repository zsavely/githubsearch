package com.szagurskii.githubsearch.callbacks;

/**
 * @author Savelii Zagurskii
 */
public interface SearchListener {
    /**
     * When server returned zero results.
     */
    void onNoResults();

    /**
     * When server returned non-zero results.
     *
     * @param query
     */
    void onDataSetChanged(String query);

    /**
     * When server returned 403 - Forbidden.
     */
    void onLimitExceeded();

    /**
     * When no internet connection is detected.
     */
    void onNoInternet();

    /**
     * When the request is being executed.
     */
    void inProgress(boolean inProgress);
}