package com.codepath.apps.SimpleTwitter.activities;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.activeandroid.query.Select;
import com.codepath.apps.SimpleTwitter.R;
import com.codepath.apps.SimpleTwitter.TwitterApplication;
import com.codepath.apps.SimpleTwitter.TwitterClient;
import com.codepath.apps.SimpleTwitter.adapters.TweetsArrayAdapter;
import com.codepath.apps.SimpleTwitter.fragments.ComposeTweetFragment;
import com.codepath.apps.SimpleTwitter.helpers.EndlessScrollListener;
import com.codepath.apps.SimpleTwitter.helpers.Helper;
import com.codepath.apps.SimpleTwitter.models.Tweet;
import com.loopj.android.http.*;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class TimelineActivity extends AppCompatActivity
        implements Helper.OnReTweetListener, Helper.OnFavoriteListener, Helper.OnReplyListener,
        Helper.OnTweetListener {

    private TwitterClient client;
    TweetsArrayAdapter aTweets;
    ArrayList<Tweet> tweets;
    ListView lvTweets;
    static long maxID = 1;
    SwipeRefreshLayout swipeContainer;
    static final String COMPOSE_TWEET = "Compose Tweet Dialog";
    static final int FIRST_PAGE = 1;
    MenuItem miActionProgressItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        setupViews();
    }

    private void setupViews() {
        // get singleton client
        client = TwitterApplication.getRestClient();
        lvTweets = (ListView) findViewById(R.id.lvTweets);
        tweets = new ArrayList<>();

        lvTweets.setOnScrollListener(scrollChangedListener);
        aTweets = new TweetsArrayAdapter(this, client, tweets);
        lvTweets.setAdapter(aTweets);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTimeLine();
            }
        });

        // Display the logo
        getSupportActionBar().setLogo(getResources().getDrawable(R.mipmap.ic_launcher));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        Helper.getCurrentUser(client, this);

        populateTimeLine(FIRST_PAGE);
    }

    private void populateTimeLine(long max_id) {
        if (!Helper.isNetworkAvailable(this)) {
            fetchTweetsFromDB();
            return;
        }

        showProgressBar();
        client.getHomeTimeLine(max_id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, JSONArray response) {
                swipeContainer.setRefreshing(false);
                aTweets.addAll(Tweet.fromJSONArray(response));
                if (aTweets.getCount() > 0) {
                    Tweet mostRecentTweet = aTweets.getItem(aTweets.getCount() - 1);
                    maxID = mostRecentTweet.getTweetId();
                }
                hideProgressBar();
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, Throwable throwable, JSONArray errorResponse) {
                swipeContainer.setRefreshing(false);
                Log.v("Twitter", errorResponse.toString());
                hideProgressBar();
            }
        });
    }

    private AbsListView.OnScrollListener scrollChangedListener = new EndlessScrollListener() {
        @Override
        public boolean onLoadMore(int page, int totalItemsCount) {
            populateTimeLine(maxID);
            return true;
        }
    };

    private void refreshTimeLine() {
        aTweets.clear();
        populateTimeLine(FIRST_PAGE);
    }

    public void showProgressBar() {
        if (miActionProgressItem != null)
            miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        if (miActionProgressItem != null)
            miActionProgressItem.setVisible(false);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        // Extract the action-view from the menu item
        ProgressBar v = (ProgressBar) MenuItemCompat.getActionView(miActionProgressItem);
        // Return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            refreshTimeLine();
        } else if (id == R.id.action_compose) {

            FragmentManager fm = getSupportFragmentManager();
            ComposeTweetFragment tweetDialog = ComposeTweetFragment.newInstance(Helper.getCurrentUser(client, this), null);
            tweetDialog.show(fm, COMPOSE_TWEET);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReTweet(final Tweet tweet, Tweet newTweet) {
        // Get current tweet posiiton
        int position = aTweets.getPosition(tweet);
        aTweets.remove(tweet);

        // Update the tweet with the new info returned
        tweet.setRetweetCount(newTweet.getRetweetCount());
        tweet.setRetweeted(newTweet.isRetweeted());
        aTweets.insert(tweet, position);
    }

    @Override
    public void onFavorite(final Tweet tweet, Tweet newTweet) {
        int position = aTweets.getPosition(tweet);
        aTweets.remove(tweet);
        aTweets.insert(newTweet, position);
    }

    @Override
    public void onReply(Tweet tweet) {
        // Display the compose tweet dialog
        FragmentManager fm = getSupportFragmentManager();
        ComposeTweetFragment tweetDialog = ComposeTweetFragment.newInstance(Helper.getCurrentUser(client, this), tweet);
        tweetDialog.show(fm, COMPOSE_TWEET);
    }

    @Override
    public void onTweetSent(Tweet tweet) {
        // Add the newly added tweet to the top of the adapter
        aTweets.insert(tweet, 0);
        lvTweets.setSelectionAfterHeaderView();
    }

    private void fetchTweetsFromDB() {
        aTweets.clear();
        List<Tweet> savedTweets = new Select().from(Tweet.class)
                .orderBy("tweetId DESC")
                .execute();
        aTweets.addAll(savedTweets);
    }
}
