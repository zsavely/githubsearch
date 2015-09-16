package com.szagurskii.githubsearch;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.robotium.solo.Solo;
import com.szagurskii.githubsearch.realm.models.ResultItem;
import com.szagurskii.githubsearch.ui.ActivityMain;

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * @author Savelii Zagurskii
 */
public class ApplicationTest extends ActivityInstrumentationTestCase2<ActivityMain> {

    private Solo solo;
    private EditText mEditText;
    private TextView mTextView;

    public ApplicationTest() {
        super(ActivityMain.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        solo = new Solo(getInstrumentation(), getActivity());
        mTextView = (TextView) solo.getView(R.id.textview);
    }

    @Override
    public void tearDown() throws Exception {
        // tearDown() is run after a test case has finished.
        // finishOpenedActivities() will finish all the activities that have been opened during the test execution.
        solo.finishOpenedActivities();
    }

    /**
     * A basic test that enters random string and watches for changes.
     *
     * @throws Exception
     */
    public void testBasic() throws Exception {
        // Test for 15 times
        for (int j = 0; j < 15; j++) {
            // Get RecyclerView
            RecyclerView recyclerView = getRecyclerView();

            // Expand search view and reset EditText
            resetEditText();

            // Enter random EditText
            solo.enterText(mEditText, RandomStringGenerator.generateRandomString(new Random().nextInt(7) + 1, RandomStringGenerator.Mode.ALPHANUMERIC));

            // Watch for changes
            waitForAnswer(recyclerView);
        }
    }

    /**
     * A test that chooses a random item in database and tries to follow the specified URL.
     *
     * @throws Exception
     */
    public void testClickingDialog() throws Exception {
        // Get random query that was already entered
        final String query = getStringQuery();

        // Get RecyclerView
        final RecyclerView recyclerView = getRecyclerView();

        // Expand search view and reset EditText
        resetEditText();

        // Enter query in the EditText
        solo.enterText(mEditText, query);

        // Wait for answer from database
        waitForAnswer(recyclerView);

        int index;

        // If no results came from database, throw an exception
        if (recyclerView.getChildCount() == 0) {
            fail("No results");
            return;
        } else {
            // Randomly select the index of the item
            index = new Random().nextInt(recyclerView.getChildCount());
        }
        // Randomly select: user or repo
        int repoOrUser = new Random().nextInt(1);

        View v = recyclerView.getChildAt(index);
        CardView card;

        // If REPO
        if (repoOrUser == 0) {
            card = (CardView) v.findViewById(R.id.cardview_repo);
            baseClickDialog(v, card, R.id.cardview_user);
        } else if (repoOrUser == 1) {
            // If USER
            card = (CardView) v.findViewById(R.id.cardview_user);
            baseClickDialog(v, card, R.id.cardview_repo);
        } else {
            fail("Wrong random number generated");
        }
    }

    /**
     * Waits for recyclerView to be filled. Timeout: 5000
     *
     * @param recyclerView
     */
    private void waitForAnswer(RecyclerView recyclerView) {
        int i = 0;
        while (recyclerView.getChildCount() == 0) {
            solo.sleep(1);
            i++;

            if (mTextView.getText().toString().equals(getActivity().getString(R.string.search_no_results)))
                break;

            if (i > 5000) {
                fail("Timeout of getting Results expired");
            }
        }
    }

    /**
     * Get random query from database
     *
     * @return random query
     */
    private String getStringQuery() {
        String query;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ResultItem> items = realm.where(ResultItem.class).findAll();

        if (items.size() == 0)
            fail("No items in database");

        ResultItem item = items.get(new Random().nextInt(items.size()));
        query = item.getQuery();
        realm.close();

        return query;
    }

    /**
     * Determine which cardView to click.
     *
     * @param v
     * @param card
     * @param cardview_repo
     */
    private void baseClickDialog(View v, CardView card, int cardview_repo) {
        if (card.getVisibility() == View.VISIBLE) {
            clickDialog(card);
        } else {
            CardView cardRepo = (CardView) v.findViewById(cardview_repo);
            clickDialog(cardRepo);
        }
    }

    /**
     * Clicks the dialog twice.
     *
     * @param cardRepo
     */
    private void clickDialog(CardView cardRepo) {
        solo.clickOnView(cardRepo);
        solo.sleep(100);
        solo.clickOnText(getActivity().getString(R.string.dialog_confirm_no));

        solo.clickOnView(cardRepo);
        solo.sleep(100);
        solo.clickOnText(getActivity().getString(R.string.dialog_confirm_yes));
    }

    /**
     * Expand SearchView and reset EditText
     */
    private void resetEditText() {
        solo.unlockScreen();
        solo.clickOnView(solo.getView(R.id.action_search));
        mEditText = (EditText) solo.getView(android.support.v7.appcompat.R.id.search_src_text);
        solo.enterText(mEditText, "");
    }

    /**
     * Get recycler view
     *
     * @return
     */
    private RecyclerView getRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerview);

        assertEquals(false, recyclerView == null);
        return recyclerView;
    }
}