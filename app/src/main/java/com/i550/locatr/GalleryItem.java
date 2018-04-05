package com.i550.locatr;


import android.net.Uri;

public class GalleryItem {          //сюда расшифровывается инфа, пришедшая с фликера
    private String mCaption;
    private String mId;
    private String mUrl;
    private String mOwner;

    public String getOwner() {return mOwner;}

    public void setOwner(String owner) {mOwner = owner;}

    public String getmCaption() {
        return mCaption;
    }

    public void setmCaption(String mCaption) {
        this.mCaption = mCaption;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    @Override
    public String toString() {
        return mCaption;
    }

    public Uri getPhotoPageUri(){       //так выглядит УРЛ страницы фотки
        return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(mOwner)
                .appendPath(mId)
                .build();
    }
}
