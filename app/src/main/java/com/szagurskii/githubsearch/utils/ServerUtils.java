package com.szagurskii.githubsearch.utils;

import android.content.Context;

import com.szagurskii.githubsearch.callbacks.SearchListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Savelii Zagurskii
 */
public class ServerUtils {
    // Executor that runs queries in a queue
    private final static ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Execute search.
     *
     * @param query    user query.
     * @param listener a listener for callbacks.
     */
    public static void query(Context context, String query, SearchListener listener) {
        if (Connectivity.isConnected(context)) {
            RunnableQuery runnableQuery = new RunnableQuery(query, listener);
            executor.execute(runnableQuery);
        } else {
            if (listener != null)
                listener.onNoInternet();
        }
    }
}