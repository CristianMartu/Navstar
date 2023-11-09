package com.myapplication.navstar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

public class SavedPlaceDetail extends AppCompatActivity {
    private TextView detailAddress, detailName, detailOpeningHours, detailOpeningHoursText,
            detailRating, detailPhoneNumber, detailPhoneNumberText, detailUri,
            nameViewDescription, detailUsersRating, detailRatingText;
    private View lineDetailUriWeb, lineDetailPhoneNumber;
    private Button savedDetailSet;
    private EditText detailSet;
    private ImageView detailImage;
    private Context detailContext;
    private String placeId;
    private com.myapplication.navstar.DatabaseSupport databaseSupport;
    private int clearFocus = 0;

    public SavedPlaceDetail(){}

    public SavedPlaceDetail(Context context){
        detailContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_saved_place_detail);

        Places.initialize(this, getString(R.string.API_KEY));
        PlacesClient placesClient = Places.createClient(this);

        detailAddress = findViewById(R.id.detailAddress);
        detailName = findViewById(R.id.detailName);
        detailImage = findViewById(R.id.detailImage);
        detailOpeningHours = findViewById(R.id.detailOpeningHours);
        detailOpeningHoursText = findViewById(R.id.detailOpeningHoursText);
        detailPhoneNumber = findViewById(R.id.detailPhoneNumber);
        lineDetailPhoneNumber = findViewById(R.id.lineDetailPhoneNumber);
        detailPhoneNumberText = findViewById(R.id.detailPhoneNumberText);
        detailUri = findViewById(R.id.detailUriWeb);
        lineDetailUriWeb = findViewById(R.id.lineDetailUriWeb);
        detailSet = findViewById(R.id.detailSetDetail);
        savedDetailSet = findViewById(R.id.savedDetailSet);
        nameViewDescription = findViewById(R.id.nameViewDescription);
        detailRating = findViewById(R.id.detailRating);
        detailUsersRating = findViewById(R.id.detailUsersRating);
        detailRatingText = findViewById(R.id.detailRatingText);


        databaseSupport = com.myapplication.navstar.DatabaseSupportSingleton.getInstance(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            detailAddress.setText(bundle.getString("Address"));
            detailName.setText(bundle.getString("Name"));
            placeId = bundle.getString("placeId");

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {

                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.WEBSITE_URI, Place.Field.PHONE_NUMBER,
                        Place.Field.OPENING_HOURS, Place.Field.PHOTO_METADATAS, Place.Field.TYPES, Place.Field.ICON_URL,
                        Place.Field.RATING, Place.Field.ADDRESS_COMPONENTS, Place.Field.USER_RATINGS_TOTAL);
                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest)
                        .addOnSuccessListener(fetchPlaceResponse -> {
                            Place place = fetchPlaceResponse.getPlace();

                            Double rating = place.getRating();
                            if (rating != null) {
                                detailRating.setText(rating.toString());
                            } else {
                                detailRating.setVisibility(View.GONE);
                            }
                            Integer usersRating = place.getUserRatingsTotal();
                            if (usersRating != null) {
                                String u = String.valueOf(usersRating);
                                detailUsersRating.setText(u);
                            } else {
                                detailUsersRating.setVisibility(View.GONE);
                                detailRatingText.setVisibility(View.GONE);
                            }

                            Uri web = place.getWebsiteUri();
                            if (web != null) {
                                detailUri.setText(web.toString());
                                detailUri.setOnClickListener(v -> {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(web.toString()));
                                    startActivity(intent);
                                });
                            } else {
                                detailUri.setVisibility(View.GONE);
                                lineDetailUriWeb.setVisibility(View.GONE);
                            }

                            String phoneNumber = place.getPhoneNumber();
                            if (phoneNumber != null) {
                                detailPhoneNumber.setText(phoneNumber);
                                detailPhoneNumber.setOnClickListener(v -> {
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:" + phoneNumber));
                                    startActivity(intent);
                                });
                            } else {
                                detailPhoneNumber.setVisibility(View.GONE);
                                lineDetailPhoneNumber.setVisibility(View.GONE);
                                detailPhoneNumberText.setVisibility(View.GONE);
                            }

                            OpeningHours openingHours = place.getOpeningHours();

                            if (openingHours != null) {
                                List<String> weekdayText = openingHours.getWeekdayText();
                                StringBuilder openingHoursBuilder = new StringBuilder();
                                for (String dayOpeningHours : weekdayText) {
                                    openingHoursBuilder.append(dayOpeningHours).append("\n");
                                }
                                detailOpeningHours.setText(openingHoursBuilder.toString());
                            } else {
                                detailOpeningHours.setVisibility(View.GONE);
                                detailOpeningHoursText.setVisibility(View.GONE);
                            }

                            List<PhotoMetadata> metadata = place.getPhotoMetadatas();
                            if (metadata != null && !metadata.isEmpty()) {
                                PhotoMetadata photoMetadata = metadata.get(0);
                                String attributions = photoMetadata.getAttributions();
                                FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                        .setMaxWidth(900)
                                        .setMaxHeight(500)
                                        .build();
                                placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                                    detailImage.setImageBitmap(bitmap);
                                });
                            }
                        });
            } else{
                Toast.makeText(getApplicationContext(), "Internet non disponibile!", Toast.LENGTH_SHORT).show();
                detailOpeningHoursText.setVisibility(View.GONE);
                detailOpeningHours.setVisibility(View.GONE);
                detailPhoneNumberText.setVisibility(View.GONE);
                detailPhoneNumber.setVisibility(View.GONE);
                lineDetailPhoneNumber.setVisibility(View.GONE);
                detailRating.setVisibility(View.GONE);
                detailRatingText.setVisibility(View.GONE);
                detailUsersRating.setVisibility(View.GONE);
                detailUri.setVisibility(View.GONE);
                lineDetailUriWeb.setVisibility(View.GONE);
            }
        }

        int positionPlace = databaseSupport.positionPlace(detailName.getText().toString());
        if(positionPlace >= 0) {
            detailSet.setText(databaseSupport.getAll().get(positionPlace).getDescription());
        } else {
            Log.e("TAG", "Errore, position_place: " + positionPlace);
        }

        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        detailSet.setOnFocusChangeListener((v, hasFocus) -> {
            if(clearFocus == 0) {
                detailSet.setText(null);
                nameViewDescription.setVisibility(View.INVISIBLE);
                savedDetailSet.setVisibility(View.VISIBLE);
                manager.showSoftInput(detailSet, InputMethodManager.SHOW_IMPLICIT);
            }else{
                clearFocus = 0;
            }
        });

        savedDetailSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newDescription = detailSet.getText().toString();
                if(newDescription.equals("")){
                    newDescription = "Luogo nelle vicinanze";
                }
                databaseSupport.setDescription(placeId, newDescription);
                clearFocus = 1;
                detailSet.clearFocus();
                manager.hideSoftInputFromWindow(detailSet.getApplicationWindowToken(), 0);
                savedDetailSet.setVisibility(View.INVISIBLE);
                nameViewDescription.setVisibility(View.VISIBLE);
            }
        });
    }
}
