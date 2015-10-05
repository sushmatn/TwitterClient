package com.codepath.apps.SimpleTwitter.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

/**
 * Created by SushmaNayak on 9/28/2015.
 */

@Table(name = "users")
public class User extends Model implements Parcelable {

    @Column(name = "name")
    private String name;

    @Column(name = "userId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long userId;

    @Column(name = "screenName")
    private String screenName;

    @Column(name = "profileImgURL")
    private String profileImageUrl;

    // Used to return items from another table based on the foreign key
    public List<User> items() {
        return getMany(User.class, "Category");
    }

    public String getName() {
        return name;
    }

    public long getUserId() {
        return userId;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public static User fromJSON(JSONObject jsonObject) {
        User user = null;
        try {
            long userId = jsonObject.getLong("id");
            user = new Select().from(User.class).where("userId = ?", userId).executeSingle();

            if (user == null) {
                user = new User();
                user.name = jsonObject.getString("name");
                user.userId = jsonObject.getLong("id");
                user.screenName = jsonObject.getString("screen_name");
                user.profileImageUrl = jsonObject.getString("profile_image_url");
                user.save();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeLong(this.userId);
        dest.writeString(this.screenName);
        dest.writeString(this.profileImageUrl);
    }

    public User() {
    }

    private User(Parcel in) {
        this.name = in.readString();
        this.userId = in.readLong();
        this.screenName = in.readString();
        this.profileImageUrl = in.readString();
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
