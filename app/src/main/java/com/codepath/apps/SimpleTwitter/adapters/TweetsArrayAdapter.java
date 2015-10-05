package com.codepath.apps.SimpleTwitter.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.SimpleTwitter.R;
import com.codepath.apps.SimpleTwitter.TwitterClient;
import com.codepath.apps.SimpleTwitter.activities.TweetDetailActivity;
import com.codepath.apps.SimpleTwitter.helpers.Helper;
import com.codepath.apps.SimpleTwitter.models.Tweet;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by SushmaNayak on 9/28/2015.
 */
public class TweetsArrayAdapter extends ArrayAdapter<Tweet> {

    private Context mContext;
    Tweet mTweet;
    TwitterClient mClient;

    final static class ViewHolder {
        ImageView iconRetweeted;
        ImageView ivProfileImg;
        TextView tvBody;
        TextView tvUserName;
        TextView tvScreenName;
        TextView tvTimeStamp;
        ImageView ivMedia;
        TextView tvRetweets;
        TextView tvLikes;
        ImageView ivReply;
        ImageView ivRetweet;
        ImageView ivLike;
        TextView tvRetweetUser;
    }

    public TweetsArrayAdapter(Context context, TwitterClient client, List<Tweet> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
        mContext = context;
        mClient = client;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mTweet = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_tweet, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.iconRetweeted = (ImageView) convertView.findViewById(R.id.iconRetweeted);
            viewHolder.tvRetweetUser = (TextView) convertView.findViewById(R.id.tvRetweetUser);
            viewHolder.ivProfileImg = (ImageView) convertView.findViewById(R.id.ivProfileImg);
            viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
            viewHolder.tvBody = (TextView) convertView.findViewById(R.id.tvBody);
            viewHolder.tvScreenName = (TextView) convertView.findViewById(R.id.tvScreenName);
            viewHolder.tvTimeStamp = (TextView) convertView.findViewById(R.id.tvTimeStamp);
            viewHolder.ivMedia = (ImageView) convertView.findViewById(R.id.ivMedia);
            viewHolder.tvRetweets = (TextView) convertView.findViewById(R.id.tvRetweets);
            viewHolder.tvLikes = (TextView) convertView.findViewById(R.id.tvLikes);
            viewHolder.ivReply = (ImageView) convertView.findViewById(R.id.ivReply);
            viewHolder.ivReply.setOnClickListener(replyListener);
            viewHolder.ivRetweet = (ImageView) convertView.findViewById(R.id.ivRetweet);
            viewHolder.ivRetweet.setOnClickListener(retweetListener);
            viewHolder.ivLike = (ImageView) convertView.findViewById(R.id.ivLike);
            viewHolder.ivLike.setOnClickListener(favoriteListener);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), TweetDetailActivity.class);
                    intent.putExtra("tweet", (Tweet) v.getTag(R.layout.item_tweet));
                    mContext.startActivity(intent);
                }
            });
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        convertView.setTag(R.layout.item_tweet, mTweet);
        viewHolder.tvRetweetUser.setVisibility(mTweet.wasRetweeted() ? View.VISIBLE : View.GONE);
        viewHolder.iconRetweeted.setVisibility(mTweet.wasRetweeted() ? View.VISIBLE : View.GONE);
        if (mTweet.wasRetweeted())
            viewHolder.tvRetweetUser.setText(mTweet.getReTweetUser() + " Retweeted");
        viewHolder.tvUserName.setText(mTweet.getUser().getName());
        viewHolder.tvBody.setText(mTweet.getTwitterURL() == null ? mTweet.getBody() : mTweet.getBody().replace(mTweet.getTwitterURL(), ""));
        viewHolder.ivProfileImg.setImageResource(android.R.color.transparent);
        viewHolder.ivProfileImg.setImageResource(android.R.color.transparent);
        viewHolder.tvScreenName.setText(" @" + mTweet.getUser().getScreenName());
        viewHolder.tvTimeStamp.setText(Helper.getRelativeTimeStamp(mTweet.getCreatedAt()));
        viewHolder.tvLikes.setText(Integer.toString(mTweet.getFavoriteCount()));
        viewHolder.ivLike.setTag(mTweet);
        viewHolder.ivLike.setImageResource(mTweet.isFavorited() ? R.drawable.ic_like_on : R.drawable.ic_like);
        viewHolder.tvRetweets.setText(Integer.toString(mTweet.getRetweetCount()));
        viewHolder.ivRetweet.setTag(mTweet);
        viewHolder.ivRetweet.setImageResource(mTweet.isRetweeted() ? R.drawable.ic_retweet_on : R.drawable.ic_retweet);
        viewHolder.ivReply.setTag(mTweet);

        Picasso.with(mContext).load(mTweet.getUser().getProfileImageUrl()).into(viewHolder.ivProfileImg);
        if (mTweet.getMediaURL() != null) {
            Picasso.with(getContext()).load(mTweet.getMediaURL()).error(R.drawable.image_unavailable).into(viewHolder.ivMedia);
            viewHolder.ivMedia.setVisibility(View.VISIBLE);
        } else
            viewHolder.ivMedia.setVisibility(View.GONE);
        return convertView;
    }

    View.OnClickListener replyListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Helper.onReply((Tweet) v.getTag(), (Helper.OnReplyListener) mContext);
        }
    };

    View.OnClickListener retweetListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Helper.isNetworkAvailable(mContext)) {
                Helper.updateButtonStatus((ImageView) v, true, R.drawable.ic_retweet_on);
                Helper.onReTweet(mClient, (Tweet) v.getTag(), (Helper.OnReTweetListener) mContext);
            }
        }
    };

    View.OnClickListener favoriteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Helper.isNetworkAvailable(mContext)) {
                Tweet tweet = (Tweet) v.getTag();
                if (tweet.isFavorited())
                    Helper.updateButtonStatus((ImageView) v, true, R.drawable.ic_like);
                else
                    Helper.updateButtonStatus((ImageView) v, true, R.drawable.ic_like_on);
                Helper.onFavorite(mClient, (Tweet) v.getTag(), (Helper.OnFavoriteListener) mContext);
            }
        }
    };
}
