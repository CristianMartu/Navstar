package com.myapplication.navstar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TimeOfWeek;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

public class CustomMarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private Context mContext;

    public CustomMarkerInfoWindowAdapter(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_marker_info_window, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        render(marker, mWindow);
        return mWindow;
    }

    private void render(Marker marker, View view) {
        com.myapplication.navstar.CustomMarkerData markerData = (com.myapplication.navstar.CustomMarkerData) marker.getTag();

        ImageView photoImageView = view.findViewById(R.id.photoImageView);
        Bitmap bitmap = markerData.getBitmap();
        if (bitmap != null) {
            int imageWidth = bitmap.getWidth(); // <!--
            int imageHeight = bitmap.getHeight();
            float cornerRadius = Math.min(imageWidth, imageHeight) * 0.1f;
            Bitmap roundedBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(roundedBitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            RectF rectF = new RectF(0, 0, imageWidth, imageHeight);
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);//  -->
            photoImageView.setImageBitmap(roundedBitmap);
        } else {
            photoImageView.setVisibility(View.GONE);
        }

        TextView titleTextView = view.findViewById(R.id.titleTextView);
        titleTextView.setText(markerData.getTitle());

        TextView snippetTextView = view.findViewById(R.id.addressTextView);
        snippetTextView.setText(markerData.getAddress());

        TextView phoneNumberTextView = view.findViewById(R.id.phoneNumberTextView);
        String phoneNumber = markerData.getNumber();
        if (phoneNumber != null) {
            phoneNumberTextView.setText("Telefono: "+phoneNumber);
        } else {
            phoneNumberTextView.setVisibility(View.GONE);
        }

        TextView websiteTextView = view.findViewById(R.id.websiteTextView);
        Uri website = markerData.getWebsite();
        if (website != null) {
            websiteTextView.setText(website.toString());
        } else {
            websiteTextView.setVisibility(View.GONE);
        }

    }
}
