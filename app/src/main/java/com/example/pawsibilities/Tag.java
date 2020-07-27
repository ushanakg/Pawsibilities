package com.example.pawsibilities;

import android.text.format.DateUtils;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.text.DecimalFormat;

@ParseClassName("Tag")
public class Tag extends ParseObject {

    private static final String TAG = "Tag";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHOTO = "photo";
    public static final String KEY_UPDATED_AT = "updatedAt";
    public static final String KEY_DROPPED_BY = "droppedBy";
    public static final String KEY_DIRECTION = "direction";
    public static final String KEY_ACTIVE = "active";
    public static final String KEY_LOCATION = "location";

    public Tag() {
        //required default constructor
    }

    public String getName() {
        return getString(KEY_NAME);
    }

    public ParseFile getPhoto() {
        return getParseFile(KEY_PHOTO);
    }

    public ParseUser getDroppedBy() {
        return getParseUser(KEY_DROPPED_BY);
    }

    public String getDirection() {
        return getString(KEY_DIRECTION);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(KEY_LOCATION);
    }

    public Boolean getActive() {
        return getBoolean(KEY_ACTIVE);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public void setPhoto(ParseFile photo) {
        put(KEY_PHOTO, photo);
    }

    public void setDroppedBy(ParseUser user) {
        put(KEY_DROPPED_BY, user);
    }

    public void setDirection(String direction) {
        put(KEY_DIRECTION, direction);
    }

    public void setActive(Boolean status) {
        put(KEY_ACTIVE, status);
    }

    public void setLocation(ParseGeoPoint location) {
        put(KEY_LOCATION, location);
    }

    // Get how long ago the tag was updated (indicates accuracy to user)
    public String getRelativeTimeAgo() {
        long dateMillis = getUpdatedAt().getTime();

        return DateUtils.getRelativeTimeSpanString (dateMillis, System.currentTimeMillis(),0L).toString();
    }

    public String distanceFrom(ParseGeoPoint point) {
        double dist = point.distanceInMilesTo(getLocation());
        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(dist);
    }
}
