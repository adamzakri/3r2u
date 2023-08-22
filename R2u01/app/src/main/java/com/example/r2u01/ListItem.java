package com.example.r2u01;

import android.net.Uri;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ListItem implements Parcelable {
    private String title;
    private String description;
    private Uri imageUri;
    private Location location;

    public ListItem(String title, String description, Uri imageUri) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
    }

    public ListItem(String title, String description, Uri imageUri, Location location) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.location = location;
    }

    protected ListItem(Parcel in) {
        title = in.readString();
        description = in.readString();
        imageUri = in.readParcelable(Uri.class.getClassLoader());
        location = in.readParcelable(Location.class.getClassLoader());
    }

    public static final Creator<ListItem> CREATOR = new Creator<ListItem>() {
        @Override
        public ListItem createFromParcel(Parcel in) {
            return new ListItem(in);
        }

        @Override
        public ListItem[] newArray(int size) {
            return new ListItem[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeParcelable(imageUri, flags);
        dest.writeParcelable(location, flags);
    }
}
