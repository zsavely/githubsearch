package com.szagurskii.githubsearch.utils;

import android.os.Handler;
import android.os.Looper;

import com.szagurskii.githubsearch.callbacks.SearchListener;
import com.szagurskii.githubsearch.realm.models.Item;
import com.szagurskii.githubsearch.realm.models.RepoRequest;
import com.szagurskii.githubsearch.realm.models.ResultItem;
import com.szagurskii.githubsearch.realm.models.User;
import com.szagurskii.githubsearch.realm.models.UserRequest;
import com.szagurskii.githubsearch.rest.RestClient;

import java.net.HttpURLConnection;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Savelii Zagurskii
 */
public class RunnableQuery implements Runnable {

    private Handler handler;
    private SearchListener listener;
    private String query;

    public RunnableQuery(String query, SearchListener listener) {
        this.listener = listener;
        this.query = query;
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void run() {
        dispatchInProgress(true);

        query = query.replaceAll("\\s+", " ").trim();

        if (query.isEmpty()) {
            dispatchInProgress(false);
            return;
        }

        Realm realm = Realm.getDefaultInstance();

        RealmResults<ResultItem> resultItems = realm.where(ResultItem.class)
                .equalTo(ResultItem.COLUMN_QUERY, query)
                .findAll();

        // If no items are cached, we need to execute a request
        if (resultItems.size() == 0) {
            RestClient restClient = RestClient.getInstance();

            RepoRequest repoRequest = null;
            UserRequest userRequest = null;
            try {
                repoRequest = restClient.queryRepos(query);
                userRequest = restClient.queryUsers(query);

                repoRequest.setId(UUID.randomUUID().toString());
                userRequest.setId(UUID.randomUUID().toString());

                realm.beginTransaction();

                realm.copyToRealmOrUpdate(repoRequest);
                realm.copyToRealmOrUpdate(userRequest);

                realm.commitTransaction();
            } catch (RetrofitError e) {
                e.printStackTrace();

                dispatchRetrofitError(e);
            } catch (Exception e) {
                e.printStackTrace();

                dispatchError();
            }

            if (repoRequest != null && userRequest != null) {

                int totalRepoCount = repoRequest.getItems().size();
                int totalUserCount = userRequest.getItems().size();

                if (totalRepoCount == 0 && totalUserCount == 0) {
                    dispatchError();
                    dispatchInProgress(false);
                    return;
                }

                int i = 0;
                int j = 0;

                while (i < totalRepoCount || j < totalUserCount) {
                    Item item = null;
                    User user = null;

                    if (i < totalRepoCount) {
                        item = repoRequest.getItems().get(i);
                    }

                    if (j < totalUserCount) {
                        user = userRequest.getItems().get(j);
                    }

                    realm.beginTransaction();

                    ResultItem resultItem = realm.createObject(ResultItem.class);

                    resultItem.setId(UUID.randomUUID().toString());

                    if (item != null) {
                        long id = item.getId();
                        Item newItem = realm.where(Item.class).equalTo(Item.COLUMN_ID, id).findFirst();
                        resultItem.setItem(newItem);
                    }

                    if (user != null) {
                        long id = user.getId();
                        User newUser = realm.where(User.class).equalTo(User.COLUMN_ID, id).findFirst();
                        resultItem.setUser(newUser);
                    }

                    resultItem.setQuery(query);

                    realm.commitTransaction();

                    i++;
                    j++;
                }

                dispatchUpdate();
            }
        } else {
            // if items are cached, just send an update
            dispatchUpdate();
        }

        realm.close();

        dispatchInProgress(false);
    }

    /**
     * Dispatch update to UI.
     */
    private void dispatchUpdate() {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onDataSetChanged(query);
                }
            });
        }
    }

    /**
     * Dispatch update to UI.
     */
    private void dispatchRetrofitError(RetrofitError error) {
        if (listener != null) {
            Response response = error.getResponse();
            if (response != null) {
                if (response.getStatus() == HttpURLConnection.HTTP_FORBIDDEN) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onLimitExceeded();
                        }
                    });
                    return;
                }
            }
            dispatchError();
        }
    }

    /**
     * Dispatch update to UI.
     */
    private void dispatchError() {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onNoResults();
                }
            });
        }
    }

    /**
     * Dispatch update to UI.
     */
    private void dispatchInProgress(final boolean inProgress) {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.inProgress(inProgress);
                }
            });
        }
    }
}