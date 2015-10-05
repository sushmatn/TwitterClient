package com.codepath.apps.SimpleTwitter.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codepath.apps.SimpleTwitter.R;
import com.codepath.apps.SimpleTwitter.TwitterApplication;
import com.codepath.apps.SimpleTwitter.TwitterClient;
import com.codepath.apps.SimpleTwitter.helpers.Helper;
import com.codepath.apps.SimpleTwitter.models.Tweet;
import com.codepath.apps.SimpleTwitter.models.User;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class ComposeTweetFragment extends DialogFragment {

    ImageView ivDismiss;
    ImageView ivProfileImg;
    TextView tvTweetLength;
    Button btnTweet;
    EditText etTweet;
    LinearLayout replyContainer;
    int textColor;
    User currentUser;
    TextView tvName;
    TextView tvScreenName;
    TextView tvReplyToTweet;
    Tweet replyToTweet;

    final static int TWEET_LENGTH = 140;
    final static String CURRENT_USER = "CurrentUser";
    final static String REPLY_TO_TWEET = "ReplyToTweet";

    public ComposeTweetFragment() {
        // Required empty public constructor
    }

    public static ComposeTweetFragment newInstance(User currentUser, Tweet replyToTweet) {
        ComposeTweetFragment fragment = new ComposeTweetFragment();
        Bundle args = new Bundle();
        args.putParcelable(CURRENT_USER, currentUser);
        args.putParcelable(REPLY_TO_TWEET, replyToTweet);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUI(view);

        if (!Helper.isNetworkAvailable(getActivity())) {
            return;
        }

        currentUser = getArguments().getParcelable(CURRENT_USER);
        replyToTweet = getArguments().getParcelable(REPLY_TO_TWEET);
        tvName.setText(currentUser.getName());
        tvScreenName.setText("@" + currentUser.getScreenName());
        Picasso.with(getActivity()).load(currentUser.getProfileImageUrl()).into(ivProfileImg);

        replyContainer.setVisibility(replyToTweet == null ? View.GONE : View.VISIBLE);
        if (replyToTweet != null) {
            tvReplyToTweet.setText(getResources().getString(R.string.replyTo) + " " + replyToTweet.getUser().getName());
            etTweet.setText("@" + replyToTweet.getUser().getScreenName() + " ");
            etTweet.setSelection(etTweet.getText().length());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    private void setupUI(View view) {
        ivDismiss = (ImageView) view.findViewById(R.id.ivDismiss);
        ivProfileImg = (ImageView) view.findViewById(R.id.ivProfileImg);
        tvTweetLength = (TextView) view.findViewById(R.id.tvTweetLength);
        btnTweet = (Button) view.findViewById(R.id.btnTweet);
        etTweet = (EditText) view.findViewById(R.id.etTweet);
        textColor = tvTweetLength.getCurrentTextColor();
        ivDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        tvName = (TextView) view.findViewById(R.id.tvName);
        tvScreenName = (TextView) view.findViewById(R.id.tvScreenName);

        etTweet.addTextChangedListener(textWatcher);

        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                composeTweet(etTweet.getText().toString());
            }
        });
        replyContainer = (LinearLayout) view.findViewById(R.id.replyContainer);
        tvReplyToTweet = (TextView) view.findViewById(R.id.tvReplyToTweet);
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
                btnTweet.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                tvTweetLength.setTextColor(textColor);
            } else {
                btnTweet.setEnabled(false);
                btnTweet.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                if (charsRemaining < 0)
                    tvTweetLength.setTextColor(getResources().getColor(R.color.colorRed));
            }
        }
    };

    public void composeTweet(String tweet) {
        if (Helper.isNetworkAvailable(getActivity())) {
            TwitterClient client = TwitterApplication.getRestClient();
            if (replyToTweet == null)
                Helper.onTweet(client, null, tweet, (Helper.OnTweetListener) getActivity());
            else
                Helper.onTweet(client, Long.toString(replyToTweet.getTweetId()), tweet, (Helper.OnTweetListener) getActivity());
        }
        dismiss();
    }

    /**
     * Set the width and height of the dialog
     */
    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }
}
