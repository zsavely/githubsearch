package com.szagurskii.githubsearch.ui.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.szagurskii.githubsearch.R;
import com.szagurskii.githubsearch.realm.models.ResultItem;
import com.szagurskii.githubsearch.ui.ConfirmAlertDialog;
import com.szagurskii.githubsearch.ui.base.RealmRecyclerViewAdapter;

/**
 * @author Savelii Zagurskii
 */
public class GitHubSearchResultsAdapter extends RealmRecyclerViewAdapter<ResultItem> {

    private ImageLoader imageLoader;
    private Context context;

    public GitHubSearchResultsAdapter(Context context) {
        this.context = context;
        initImageLoader();
    }

    private void initImageLoader() {
        if (imageLoader != null)
            imageLoader.destroy();
        imageLoader = ImageLoader.getInstance();

        if (!imageLoader.isInited()) {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .resetViewBeforeLoading(true)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                    .defaultDisplayImageOptions(options)
                    .build();
            imageLoader.init(config);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_preview, parent, false);

        CardView cardView = (CardView) v.findViewById(R.id.cardview_repo);
        cardView.setPreventCornerOverlap(true);

        cardView = (CardView) v.findViewById(R.id.cardview_user);
        cardView.setPreventCornerOverlap(true);

        return new GitHubSearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        GitHubSearchViewHolder ghsvh = (GitHubSearchViewHolder) viewHolder;
        ResultItem item = getItem(i);

        if (item.getItem() != null) {
            ghsvh.cardViewRepo.setVisibility(View.VISIBLE);

            ghsvh.repoName.setText(item.getItem().getName());
            ghsvh.repoDescription.setText(item.getItem().getDescription());
            ghsvh.repoForksCount.setText(String.valueOf(item.getItem().getForksCount()));
        } else {
            ghsvh.cardViewRepo.setVisibility(View.GONE);
        }

        if (item.getUser() != null) {
            ghsvh.cardViewUser.setVisibility(View.VISIBLE);

            ghsvh.userLogin.setText(item.getUser().getLogin());
            imageLoader.displayImage(item.getUser().getAvatarUrl(), ghsvh.userAvatar);
        } else {
            ghsvh.cardViewUser.setVisibility(View.GONE);
        }
    }

    /**
     * The inner RealmBaseAdapter
     * recyclerView count is applied here.
     * <p/>
     * {@link #getRealmAdapter()} is defined in {@link RealmRecyclerViewAdapter}.
     */
    @Override
    public int getItemCount() {
        if (getRealmAdapter() != null) {
            return getRealmAdapter().getCount();
        }
        return 0;
    }

    private class GitHubSearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private static final String GITHUB_URL = "https://github.com/%s";

        public TextView userLogin;
        public TextView repoName;
        public TextView repoDescription;
        public TextView repoForksCount;
        public ImageView userAvatar;

        public CardView cardViewRepo;
        public CardView cardViewUser;

        public GitHubSearchViewHolder(View view) {
            super(view);

            cardViewUser = (CardView) view.findViewById(R.id.cardview_user);
            cardViewRepo = (CardView) view.findViewById(R.id.cardview_repo);

            userLogin = (TextView) view.findViewById(R.id.textview_userlogin);
            userAvatar = (ImageView) view.findViewById(R.id.imageview_useravatar);

            repoName = (TextView) view.findViewById(R.id.textview_reponame);
            repoDescription = (TextView) view.findViewById(R.id.textview_repodescription);
            repoForksCount = (TextView) view.findViewById(R.id.textview_forks);

            cardViewRepo.setClickable(true);
            cardViewUser.setClickable(true);

            cardViewRepo.setOnClickListener(this);
            cardViewUser.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String url;
            ResultItem item = getRealmAdapter().getItem(getAdapterPosition());

            if (v.getId() == R.id.cardview_repo) {
                url = item.getItem().getFullName();
            } else if (v.getId() == R.id.cardview_user) {
                url = item.getUser().getLogin();
            } else {
                return;
            }

            // Generate required link
            final String link = String.format(GITHUB_URL, url);

            // Show dialog
            new ConfirmAlertDialog(v.getContext(), link).show();
        }
    }
}