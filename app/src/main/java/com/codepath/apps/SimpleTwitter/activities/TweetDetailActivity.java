package com.codepath.apps.SimpleTwitter.activities;

import android.content.Context;
import android.content.Intent;
import android.preference.EditTextPreference;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.SimpleTwitter.R;
import com.codepath.apps.SimpleTwitter.TwitterApplication;
import com.codepath.apps.SimpleTwitter.TwitterClient;
import com.codepath.apps.SimpleTwitter.fragments.ComposeTweetFragment;
import com.codepath.apps.SimpleTwitter.helpers.Helper;
import com.codepath.apps.SimpleTwitter.models.Tweet;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class TweetDetailActivity extends AppCompatActivity
        implements Helper.OnFavoriteListener, Helper.OnReTweetListener, Helper.OnReplyListener, Helper.OnTweetListener {

    TextView tvUserName;
    TextView tvScreenName;
    ImageView ivProfileImg;
    TextView tvBody;
    ImageView ivMediaURL;
    TextView tvTimestamp;
    TextView tvRetweetCount;
    TextView tvFavoritesCount;
    ImageView ivReply;
    ImageView ivRetweet;
    ImageView ivLike;
    ImageView ivShare;
    Tweet mTweet;
    Helper mHelper;
    TwitterClient mClient;
    EditText etReplyTweet;
    RelativeLayout replyTweetContainer;
    Button btnTweet;
    TextView tvTweetLength;
    int textColor;

    static final String COMPOSE_TWEET = "Compose Tweet Dialog";
    final int TWEET_LENGTH = 140;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);
        mClient = TwitterApplication.getRestClient();
        mHelper = new Helper();
        mTweet = getIntent().getParcelableExtra("tweet");
        setupView();
        updateView(false);
    }

    private void setupView() {
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(getResources().getDrawable(R.mipmap.ic_launcher));
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvScreenName = (TextView) findViewById(R.id.tvScreenName);
        ivProfileImg = (ImageView) findViewById(R.id.ivProfileImg);
        tvBody = (TextView) findViewById(R.id.tvBody);
        ivMediaURL = (ImageView) findViewById(R.id.ivMediaURL);
        tvTimestamp = (TextView) findViewById(R.id.tvTimestamp);
        tvRetweetCount = (TextView) findViewById(R.id.tvRetweetCount);
        tvFavoritesCount = (TextView) findViewById(R.id.tvFavoritesCount);
        ivReply = (ImageView) findViewById(R.id.ivReply);
        ivRetweet = (ImageView) findViewById(R.id.ivRetweet);
        ivLike = (ImageView) findViewById(R.id.ivLike);
        ivShare = (ImageView) findViewById(R.id.ivShare);
        etReplyTweet = (EditText) findViewById(R.id.etReplyTweet);

        replyTweetContainer = (RelativeLayout) findViewById(R.id.replyTweetContainer);
        btnTweet = (Button) findViewById(R.id.btnTweet);
        tvTweetLength = (TextView) findViewById(R.id.tvTweetLength);
        textColor = tvTweetLength.getCurrentTextColor();

        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Helper.isNetworkAvailable(TweetDetailActivity.this)) {
                    Helper.onTweet(mClient, Long.toString(mTweet.getTweetId()), etReplyTweet.getText().toString(), TweetDetailActivity.this);
                }
            }
        });

        ivReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.onReply(mTweet, TweetDetailActivity.this);
            }
        });

        ivRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Helper.isNetworkAvailable(TweetDetailActivity.this)) {
                    Helper.updateButtonStatus((ImageView) v, true, R.drawable.ic_retweet_on);
                    Helper.onReTweet(mClient, mTweet, TweetDetailActivity.this);
                }
            }
        });

        ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Helper.isNetworkAvailable(TweetDetailActivity.this)) {
                    if (mTweet.isFavorited())
                        Helper.updateButtonStatus((ImageView) v, true, R.drawable.ic_like_gray);
                    else
                        Helper.updateButtonStatus((ImageView) v, true, R.drawable.ic_like_on);
                    Helper.onFavorite(mClient, mTweet, TweetDetailActivity.this);
                }
            }
        });

        etReplyTweet.addTextChangedListener(textWatcher);

        etReplyTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etReplyTweet.hasFocus() && etReplyTweet.getText().length() == 0) {
                    replyTweetContainer.setVisibility(View.VISIBLE);
                    etReplyTweet.setText("@" + mTweet.getUser().getScreenName() + " ");
                    etReplyTweet.setSelection(etReplyTweet.getText().length());
                }
            }
        });

        ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareTweet();
            }
        });
    }

    private void shareTweet() {
        String tweetString = mTweet.getBody() + "\n";
        if(mTweet.getTwitterURL() != null)
            tweetString += mTweet.getTwitterURL();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, tweetString);
        startActivity(shareIntent);
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            int charsRemaining = TWEET_LENGTH - s.length();
            tvTweetLength.setText(Integer.toString(charsRemaining));

            if (charsRemaining >= 0 && charsRemaining < TWEET_LENGTH) {
                btnTweet.setEnabled(true);
                btnTweet.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvTweetLength.setTextColor(textColor);
            } else {
                btnTweet.setEnabled(false);
                btnTweet.setTextColor(getResources().getColor(R.color.lightGray));
                if (charsRemaining < 0)
                    tvTweetLength.setTextColor(getResources().getColor(R.color.colorRed));
            }
        }
    };

    private void updateView(boolean bRefreshView) {

        tvRetweetCount.setText(Integer.toString(mTweet.getRetweetCount()));
        tvFavoritesCount.setText(Integer.toString(mTweet.getFavoriteCount()));
        ivRetweet.setImageResource(mTweet.isRetweeted() ? R.drawable.ic_retweet_on : R.drawable.ic_retweet_gray);
        ivLike.setImageResource(mTweet.isFavorited() ? R.drawable.ic_like_on : R.drawable.ic_like_gray);

        if (bRefreshView)
            return;

        tvUserName.setText(mTweet.getUser().getName());
        tvScreenName.setText("@" + mTweet.getUser().getScreenName());
        Picasso.with(this).load(mTweet.getUser().getProfileImageUrl()).into(ivProfileImg);
        mHelper.SetText(this, mTweet.getBody(), tvBody);

        if (mTweet.getMediaURL() == null)
            ivMediaURL.setVisibility(View.GONE);
        else
            Picasso.with(this).load(mTweet.getMediaURL()).into(ivMediaURL);

        tvTimestamp.setText(DateFormat.getDateTimeInstance().format(new Date(mTweet.getCreatedAt())));
        etReplyTweet.setHint("Reply to " + mTweet.getUser().getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tweet_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Override
    public void onFavorite(Tweet tweet, Tweet newTweet) {
        mTweet = newTweet;
        updateView(true);
    }

    @Override
    public void onReTweet(Tweet tweet, Tweet newTweet) {
        mTweet.setRetweetCount(newTweet.getRetweetCount());
        mTweet.setRetweeted(newTweet.isRetweeted());
        updateView(true);
    }

    @Override
    public void onReply(Tweet tweet) {
        FragmentManager fm = getSupportFragmentManager();
        ComposeTweetFragment tweetDialog = ComposeTweetFragment.newInstance(Helper.getCurrentUser(mClient, this), tweet);
        tweetDialog.show(fm, COMPOSE_TWEET);
    }

    @Override
    public void onTweetSent(Tweet tweet) {

        //Hide the keyboard
        etReplyTweet.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etReplyTweet.getWindowToken(), 0);

        // Hide the tweet button and character count
        replyTweetContainer.setVisibility(View.GONE);

        // Display a custom toast message
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.toast_layout, null);
        Toast toast = new Toast(this);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }
}
