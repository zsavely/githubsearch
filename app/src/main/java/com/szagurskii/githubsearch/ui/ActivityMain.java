package com.szagurskii.githubsearch.ui;

import android.animation.LayoutTransition;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.szagurskii.githubsearch.R;
import com.szagurskii.githubsearch.callbacks.SearchListener;
import com.szagurskii.githubsearch.realm.models.ResultItem;
import com.szagurskii.githubsearch.ui.adapters.GitHubSearchResultsAdapter;
import com.szagurskii.githubsearch.ui.adapters.RealmGitHubSearchResultsAdapter;
import com.szagurskii.githubsearch.utils.ServerUtils;

import io.realm.Realm;
import io.realm.RealmResults;

public class ActivityMain extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchListener, MenuItemCompat.OnActionExpandListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = ActivityMain.class.getSimpleName();

    private static final String BUNDLE_ICONIFIED = "bundle_iconified";
    private static final String BUNDLE_QUERY = "bundle_query";

    private Realm realm;

    private ImageView mImageView;
    private LinearLayout mLinearLayout;
    private TextView mTextView;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private GitHubSearchResultsAdapter mAdapter;
    private RealmGitHubSearchResultsAdapter realmAdapter;

    private SearchView mSearchView;
    private EditText mEditText;
    private Toast toast;

    private String mCurrentQuery;
    private boolean mIsIconified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeRealm();
        initializeViews();
        initializeVariables(savedInstanceState);
        initializeRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initializeAdapters();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_activity_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        MenuItemCompat.setOnActionExpandListener(searchItem, this);
        if (!mIsIconified)
            MenuItemCompat.expandActionView(searchItem);

        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(this);

            mEditText = (EditText) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            if (mEditText != null) {
                mEditText.setText(mCurrentQuery);
                mEditText.setSelection(mEditText.length());
            }

            // only for honeycomb and newer versions
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if (searchItem != null) {
                    LinearLayout searchBar = (LinearLayout) mSearchView.findViewById(R.id.search_bar);
                    searchBar.setLayoutTransition(new LayoutTransition());
                }
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(BUNDLE_ICONIFIED, isIconified());
        outState.putString(BUNDLE_QUERY, mCurrentQuery);
    }

    @Override
    public void onBackPressed() {
        if (!mSearchView.isIconified())
            mSearchView.setIconified(true);
        else super.onBackPressed();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(TAG, query);

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.i(getClass().getSimpleName(), newText);

        if (!newText.trim().isEmpty()) {
            mCurrentQuery = newText;
            ServerUtils.query(this, newText, this);
        }

        return true;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {

        if (mEditText != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mEditText.setText(mCurrentQuery);
                    mEditText.setSelection(mEditText.length());
                }
            }, 100);
        }

        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return true;
    }

    @Override
    public void onRefresh() {
        ServerUtils.query(this, mCurrentQuery, this);
    }

    @Override
    public void onNoResults() {
        if (mRecyclerView != null) {
            mRecyclerView.setVisibility(View.GONE);
            mLinearLayout.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.GONE);
            mTextView.setText(R.string.search_no_results);
        }
    }

    @Override
    public void onDataSetChanged(String query) {
        if (mRecyclerView != null) {
            mLinearLayout.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        bindData(query);
    }

    @Override
    public void onLimitExceeded() {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(this, R.string.error_limit_exceeded, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onNoInternet() {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(this, R.string.error_not_connected, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void inProgress(boolean inProgress) {
        if (inProgress)
            startUpdating();
        else stopUpdating();
    }

    private void initializeRealm() {
        realm = Realm.getDefaultInstance();
    }

    private void initializeRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new GitHubSearchResultsAdapter(this);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(getResources().getInteger(R.integer.columns), StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initializeViews() {
        mLinearLayout = (LinearLayout) findViewById(R.id.linearlayout);
        mImageView = (ImageView) findViewById(R.id.imageview);
        mTextView = (TextView) findViewById(R.id.textview);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    private void initializeAdapters() {
        // RealmResults that show nothing
        RealmResults<ResultItem> channelItems = realm.where(ResultItem.class)
                .equalTo(ResultItem.COLUMN_QUERY, mCurrentQuery)
                .findAll();
        realmAdapter = new RealmGitHubSearchResultsAdapter(this, channelItems, true);

        mAdapter.setRealmAdapter(realmAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void initializeVariables(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentQuery = savedInstanceState.getString(BUNDLE_QUERY);
            mIsIconified = savedInstanceState.getBoolean(BUNDLE_ICONIFIED);
        } else {
            mCurrentQuery = "";
            mIsIconified = true;
        }
    }

    private void bindData(String query) {
        if (realm != null) {
            RealmResults<ResultItem> channelItems = realm.where(ResultItem.class)
                    .equalTo(ResultItem.COLUMN_QUERY, query)
                    .findAll();

            realmAdapter.update(channelItems);
            mAdapter.notifyDataSetChanged();
        }
    }

    private boolean isIconified() {
        boolean iconified = true;
        if (mSearchView != null)
            iconified = mSearchView.isIconified();
        return iconified;
    }

    private void startUpdating() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }
    }

    private void stopUpdating() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }
}