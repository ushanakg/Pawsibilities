package com.example.pawsibilities;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Tag")
public class Tag extends ParseObject {

    private static final String TAG = "Tag";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHOTO = "photo";
    public static final String KEY_CREATED_AT = "createdAt";
    public static final String KEY_DROPPED_BY = "droppedBy";
    public static final String KEY_DIRECTION = "direction";
    public static final String KEY_ACTIVE = "active";

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

}
