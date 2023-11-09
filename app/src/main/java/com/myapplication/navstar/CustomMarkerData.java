package com.myapplication.navstar;

import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import com.google.android.libraries.places.api.model.OpeningHours;

public class CustomMarkerData {
    private String title;
    private String address;
    private String latLang;
    private Uri website;
    private String phoneNumber;
    private OpeningHours openingHours;
    private String placeId;
    private Bitmap bitmap;

    public CustomMarkerData(String title, String address, String latLang, Uri website, String phoneNumber, OpeningHours openingHours, String placeId, Bitmap bitmap) {
        this.title = title;
        this.address = address;
        this.latLang = latLang;
        this.website = website;
        this.phoneNumber = phoneNumber;
        this.openingHours = openingHours;
        this.placeId = placeId;
        this.bitmap = bitmap;
    }

    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }

    public String getLatLang() {
        return latLang;
    }

    public Uri getWebsite() {
        return website;
    }
    public String getNumber() {
        return phoneNumber;
    }
    public OpeningHours getOpeningHours() {
        return openingHours;
    }
    public String getPlaceId(){
        return placeId;
    }
    public Bitmap getBitmap() {
        return bitmap;
    }
}

