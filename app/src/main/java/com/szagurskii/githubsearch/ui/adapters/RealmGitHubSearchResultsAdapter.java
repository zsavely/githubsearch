package com.szagurskii.githubsearch.ui.adapters;

import android.content.Context;

import com.szagurskii.githubsearch.realm.models.ResultItem;
import com.szagurskii.githubsearch.ui.base.RealmModelAdapter;

import io.realm.RealmResults;

/**
 * @author Savelii Zagurskii
 */
public class RealmGitHubSearchResultsAdapter extends RealmModelAdapter<ResultItem> {
    public RealmGitHubSearchResultsAdapter(Context context, RealmResults<ResultItem> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }

    /**
     * Update current dataset to a new one.
     *
     * @param realmResults new dataset.
     */
    public void update(RealmResults<ResultItem> realmResults) {
        this.realmResults = realmResults;
        notifyDataSetChanged();
    }
}