package com.szagurskii.githubsearch;

import android.app.Application;

import com.szagurskii.githubsearch.realm.modules.GitHubModule;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * @author Savelii Zagurskii
 */
public class GitHubSearchApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        RealmConfiguration configuration = new RealmConfiguration.Builder(this)
                .deleteRealmIfMigrationNeeded()
                .setModules(new GitHubModule())
                .name("github.realm")
                .build();
        Realm.setDefaultConfiguration(configuration);
    }
}