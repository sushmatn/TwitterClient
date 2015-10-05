package com.codepath.apps.SimpleTwitter.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

/**
 * Created by SushmaNayak on 9/28/2015.
 */
@Table(name = "tweets")
public class Tweet extends Model implements Parcelable {

    @Column(name = "body")
    private String body;

    @Column(name = "tweetId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long tweetId;

    @Column(name = "user", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public User user;

    @Column(name = "createdAt")
    private String createdAt;

    @Column(name = "twitterURL")
    private String twitterURL;

    @Column(name = "mediaURL")
    private String mediaURL;

    @Column(name = "wasRetweeted")
    private boolean wasRetweeted;

    @Column(name = "retweetCount")
    private int retweetCount;

    @Column(name = "favoriteCount")
    private int favoriteCount;

    @Column(name = "favorited")
    private boolean favorited;

    @Column(name = "retweeted")
    private boolean retweeted;

    @Column(name = "reTweetUser")
    private String reTweetUser;

    public boolean wasRetweeted() {
        return wasRetweeted;
    }

    public String getReTweetUser() {
        return reTweetUser;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public String getBody() {
        return body;
    }

    public long getTweetId() {
        return tweetId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public String getTwitterURL() {
        return twitterURL;
    }

    public String getMediaURL() {
        return mediaURL;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public void setRetweetCount(int retweetCount) {
        this.retweetCount = retweetCount;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public static Tweet fromJSONObject(JSONObject jsonObject) {
        Tweet tweet = null;
        try {

            long tweetId = jsonObject.getLong("id");
            tweet = new Select()
                    .from(Tweet.class)
                    .where("tweetId = ?", tweetId)
                    .executeSingle();

            if (tweet == null) {
                tweet = new Tweet();
                tweet.body = jsonObject.getString("text");
                tweet.tweetId = jsonObject.getLong("id");
                tweet.createdAt = jsonObject.getString("created_at");
                if (jsonObject.has("retweeted_status")) {
                    tweet.user = User.fromJSON(jsonObject.getJSONObject("retweeted_status").getJSONObject("user"));
                    tweet.reTweetUser = User.fromJSON(jsonObject.getJSONObject("user")).getName();
                    tweet.wasRetweeted = true;
                    tweet.tweetId = jsonObject.getJSONObject("retweeted_status").getLong("id");
                } else {
                    tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
                    tweet.wasRetweeted = false;
                    tweet.reTweetUser = null;
                }
                if (jsonObject.has("extended_entities")) {
                    if (jsonObject.getJSONObject("extended_entities").getJSONArray("media").length() > 0) {
                        tweet.twitterURL = jsonObject.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).getString("url");
                        tweet.mediaURL = jsonObject.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).getString("media_url");
                    }
                }
            }
            tweet.retweetCount = jsonObject.getInt("retweet_count");
            tweet.favoriteCount = jsonObject.getInt("favorite_count");
            tweet.favorited = jsonObject.getBoolean("favorited");
            tweet.retweeted = jsonObject.getBoolean("retweeted");
            tweet.save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tweet;
    }

    public static ArrayList<Tweet> fromJSONArray(JSONArray tweetArray) {
        ArrayList<Tweet> tweets = new ArrayList<>();
        ActiveAndroid.beginTransaction();
        try {
            for (int i = 0; i < tweetArray.length(); i++) {
                try {
                    JSONObject tweetObject = tweetArray.getJSONObject(i);
                    tweets.add(Tweet.fromJSONObject(tweetObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
        return tweets;
    }

    public Tweet() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.body);
        dest.writeLong(this.tweetId);
        dest.writeParcelable(this.user, 0);
        dest.writeString(this.createdAt);
        dest.writeString(this.twitterURL);
        dest.writeString(this.mediaURL);
        dest.writeInt(this.retweetCount);
        dest.writeInt(this.favoriteCount);
        dest.writeByte(favorited ? (byte) 1 : (byte) 0);
        dest.writeByte(retweeted ? (byte) 1 : (byte) 0);
        dest.writeByte(wasRetweeted ? (byte) 1 : (byte) 0);
        dest.writeString(this.reTweetUser);
    }

    private Tweet(Parcel in) {
        this.body = in.readString();
        this.tweetId = in.readLong();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.createdAt = in.readString();
        this.twitterURL = in.readString();
        this.mediaURL = in.readString();
        this.retweetCount = in.readInt();
        this.favoriteCount = in.readInt();
        this.favorited = in.readByte() != 0;
        this.retweeted = in.readByte() != 0;
        this.wasRetweeted = in.readByte() != 0;
        this.reTweetUser = in.readString();
    }

    public static final Creator<Tweet> CREATOR = new Creator<Tweet>() {
        public Tweet createFromParcel(Parcel source) {
            return new Tweet(source);
        }

        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };

    public static List<Tweet> getAll() {
        return new Select().from(Tweet.class)
                .orderBy("tweetId DESC")
                .execute();
    }

    public static void DeleteAll() {
        new Delete().from(Tweet.class).execute();
    }
}
