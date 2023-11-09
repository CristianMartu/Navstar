package com.myapplication.navstar;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private boolean permissionGranted, mapType;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Context mapContext;
    private Marker currentMarker;
    private List<Marker> markerList = new ArrayList<>();
    private com.myapplication.navstar.List_Detail detail = new com.myapplication.navstar.List_Detail();
    private List<Integer> notificationList = new ArrayList<>();
    private com.myapplication.navstar.DatabaseSupport databaseSupport;
    private Button savedPlaceButton;
    private EditText editText;
    private boolean isInfoWindowClosed = false;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location lastKnownLocation;
    private Location currentLocation;
    public int NOTIFICATION_ID = 0;
    private String chanelID = "CHANNEL_ID_NOTIFICATION";
    private static final String TAG = "Navstar";

    public MapFragment() {
    }

    public MapFragment(Context context) {
        mapContext = context;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mapContext);
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mapContext = context;

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mapContext = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mapContext);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().
                findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        editText = view.findViewById(R.id.editText);
        savedPlaceButton = view.findViewById(R.id.savedPlaceButton);
        Button searchButton = view.findViewById(R.id.searchButton);
        InputMethodManager keyboard = (InputMethodManager) mapContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        searchButton.setOnClickListener(v -> {
            String qeury = editText.getText().toString();
            if (qeury.isEmpty()) {
                Toast.makeText(mapContext, "Inserire una coordinata geografica o un indirizzo", Toast.LENGTH_SHORT).show();
            } else {
                searchPosition(qeury);
            }
            editText.setText(null);
            editText.clearFocus();
            keyboard.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String s = editText.getText().toString().trim();
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    editText.clearFocus();
                    keyboard.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        databaseSupport = ((com.myapplication.navstar.MainActivity) mapContext).getDatabaseSupport();

        notificationList.clear();
        List<com.myapplication.navstar.List_Detail> database = databaseSupport.getAll();
        for (com.myapplication.navstar.List_Detail rowList : database) {
            notificationList.add(0);
        }
        updateMap();
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        if(mapType){
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
        checkPermission();
        showEnableGPSDialog();
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        googleMap.setOnMyLocationButtonClickListener(() -> {
            if (isGPSEnabled()) {
                Toast.makeText(mapContext, "Abilitare GPS per centrare la posizione.", Toast.LENGTH_SHORT).show();
            }
            return false;
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                savedPlaceButton.setVisibility(View.VISIBLE);
                savedPlaceButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int positionPlace = databaseSupport.positionPlace(detail.getName());
                        if (detail.getName() != null && positionPlace == -1) {
                            Calendar calendar = Calendar.getInstance();
                            Date currentDate = calendar.getTime();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
                            String formattedDate = dateFormat.format(currentDate);

                            Toast.makeText(mapContext, formattedDate, Toast.LENGTH_SHORT).show();

                            detail.setDate(formattedDate);

                            Log.i(TAG, "Date: " + detail.getDate());

                            SavedPlaceFragment savedPlaceFragment = ((com.myapplication.navstar.MainActivity) mapContext).getSavedPlaceFragment();
                            if (savedPlaceFragment != null && savedPlaceFragment.getInitialized()) {
                                boolean addToDatabase = databaseSupport.addPlace(detail);
                                savedPlaceFragment.addItems(detail);
                                notificationList.add(0);
                            }
                            Snackbar.make(getView(), "Luogo: " + detail.getName() + " è stato salvato.", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mapContext, "Luogo già salvato.", Toast.LENGTH_SHORT).show();
                        }
                        detail = new com.myapplication.navstar.List_Detail();
                        savedPlaceButton.setVisibility(View.GONE);
                        currentMarker.hideInfoWindow();
                    }
                });
                isInfoWindowClosed = true;
            }
        });

        googleMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(@NonNull Marker marker) {
                if (isInfoWindowClosed) {
                    savedPlaceButton.setVisibility(View.GONE);
                }
                isInfoWindowClosed = false;
            }
        });


        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                return false;
            }
        });
    }

    private void checkPermission(){
        if (((com.myapplication.navstar.MainActivity) getActivity()).checkPermission()) {
            googleMap.setMyLocationEnabled(true);
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng currentLatLang = new LatLng(latitude, longitude);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLang, 15f));
                }else{
                    LatLng italyCenter = new LatLng(41.9028, 12.4964);
                    float zoomLevel = 5.5f;
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(italyCenter, zoomLevel));
                }
            });
            permissionGranted = true;
        }
    }

    @SuppressLint("MissingPermission")
    private void updateMap() {

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setWaitForAccurateLocation(false)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    currentLocation = new Location("currentLocation");
                    currentLocation.setLatitude(latitude);
                    currentLocation.setLongitude(longitude);
                    if (distance(lastKnownLocation, currentLocation, 50)) {
                        sendNotifications(location);
                        lastKnownLocation = new Location("lastKnownLocation");
                        lastKnownLocation.setLatitude(latitude);
                        lastKnownLocation.setLongitude(longitude);
                    }
                }
            }
        };
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private boolean distance(Location location1, Location location2, float minDistance) {
        if (location1 == null || location2 == null) {
            return true;
        }

        float distance = location1.distanceTo(location2);
        return distance >= minDistance;
    }

    @SuppressLint("MissingPermission")
    private void sendNotifications(Location location) {
        if(mapContext != null){
            Location location1 = new Location("My position");
            location1.setLatitude(location.getLatitude());
            location1.setLongitude(location.getLongitude());
            List<com.myapplication.navstar.List_Detail> database = databaseSupport.getAll();
            int pos = 0;
            for (com.myapplication.navstar.List_Detail rowList : database) {
                double lat = rowList.getLatitude();
                double lng = rowList.getLongitude();
                Location location2 = new Location(" ");
                location2.setLatitude(lat);
                location2.setLongitude(lng);
                float distance = location1.distanceTo(location2);

                Log.i(TAG, "PROVA: " + notificationList.get(pos) + " " + rowList.getName());

                if (distance < 1000 ) { //&& notificationList.get(pos) == 0) {

                    createNotificationChannel();

                    Intent intent = new Intent(mapContext, com.myapplication.navstar.MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(mapContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(mapContext, chanelID);
                    builder.setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentTitle(rowList.getName())
                            .setContentText(rowList.getDescription())
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(mapContext);
                    if(notificationList.get(pos) == 0) {
                        notificationList.set(pos, 1);
                        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
                    }
                    NOTIFICATION_ID++;
                    //Toast.makeText(mapContext, "Distanza: " + distance + "   luogo: " + rowList.getName(), Toast.LENGTH_SHORT).show();
                }
                pos++;
            }
        }
        //Log.i(TAG, "PROVA: " + notificationList.size());
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My notification";
            String description = "My description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(chanelID, name, importance);
            notificationChannel.setDescription(description);

            if (mapContext != null) {
                NotificationManager notificationManager = (NotificationManager) mapContext.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) mapContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager == null || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showEnableGPSDialog() {
        if (permissionGranted && isGPSEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mapContext);
            builder.setIcon(R.mipmap.ic_launcher_round);
            builder.setTitle("Abilita GPS");
            builder.setMessage("Per utilizzare al meglio questa app, abilita il GPS sul tuo dispositivo.\n");
            builder.show();
        }
    }

    public void searchPosition(String query) {
        Places.initialize(mapContext, getString(R.string.API_KEY));
        PlacesClient placesClient = Places.createClient(mapContext);
        Geocoder geocoder = new Geocoder(mapContext);
        try {
            List<Address> addressList;
            if (isCoordinates(query)) {
                String[] coordinates = query.split(",");
                double lat = Double.parseDouble(coordinates[0].trim());
                double lon = Double.parseDouble(coordinates[1].trim());
                addressList = geocoder.getFromLocation(lat, lon, 1);
                Address address = addressList.get(0);
                Toast.makeText(mapContext, "Reverse geocoding", Toast.LENGTH_SHORT).show();
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();
                String name = address.getAddressLine(0);
                LatLng latLng = new LatLng(latitude, longitude);
                placeInfoReverse(name, latLng, placesClient);
            } else if (!isCoordinates(query)) {
                addressList = geocoder.getFromLocationName(query, 1);
                Log.i(TAG, "Ricerca: " + addressList);
                if (addressList != null && !addressList.isEmpty()) {
                    placeInfo(query, placesClient);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mapContext);
                    builder.setIcon(R.mipmap.ic_launcher_round);
                    builder.setTitle("Errore di compilazione!");
                    builder.setMessage("Inserire un indirizzo esistente: \nVia delle Scienze Parma \n\no una coordinata valida : \n44.764531,10.312128 \n");
                    builder.show();
                }
            }
        } catch (IOException e) {
            Toast.makeText(mapContext, "Internet non disponibile!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(mapContext, "Errore! \n Inserire una coordinata valida", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
            if(googleMap == null){
                Toast.makeText(mapContext, "Mappa non pronta o non inizializzata", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isCoordinates(String query) {
        String[] coordinates = query.split(",");
        return coordinates.length == 2 && isValidCoordinate(coordinates[0].trim()) && isValidCoordinate(coordinates[1].trim());
    }

    private boolean isValidCoordinate(String coordinate) {
        try {
            Double.parseDouble(coordinate);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void setMarker(String name, String address, LatLng latLang, Uri website, String number, OpeningHours openingHours, String placeId, Bitmap bitmap){

        googleMap.setInfoWindowAdapter(new com.myapplication.navstar.CustomMarkerInfoWindowAdapter(mapContext));
        com.myapplication.navstar.CustomMarkerData markerData = new com.myapplication.navstar.CustomMarkerData(name, address, latLang.toString(), website, number, openingHours, placeId, bitmap);

        detail.setName(name);
        detail.setAddress(address);
        detail.setPlaceId(placeId);
        detail.setLatitude(latLang.latitude);
        detail.setLongitude(latLang.longitude);
        detail.setDescription("Luogo nelle vicinanze");


        MarkerOptions markerOptions = new MarkerOptions().position(latLang).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        currentMarker = googleMap.addMarker(markerOptions);
        currentMarker.setTag(markerData);
        markerList.add(currentMarker);
        currentMarker.showInfoWindow();
        if(markerList.size() == 1){
            double verticalOffset = 0.005;
            LatLng latLng = new LatLng(latLang.latitude + verticalOffset, latLang.longitude);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        } else{
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLang, 12f));
        }
        Log.i(TAG, "marker size: " + markerList.size());
    }

    private void placeInfo(String query, PlacesClient placesClient){
        for (Marker marker : markerList) {
            marker.remove();
        }
        markerList.clear();
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.WEBSITE_URI, Place.Field.PHONE_NUMBER,
                    Place.Field.OPENING_HOURS, Place.Field.PHOTO_METADATAS, Place.Field.TYPES, Place.Field.ICON_URL,
                    Place.Field.RATING, Place.Field.ADDRESS_COMPONENTS, Place.Field.USER_RATINGS_TOTAL);
            List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
            if (!predictions.isEmpty()) {
                AutocompletePrediction prediction = predictions.get(0);
                String placeId = prediction.getPlaceId();
                String placeName = prediction.getPrimaryText(null).toString();
                String address = prediction.getSecondaryText(null).toString();

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest)
                        .addOnSuccessListener(fetchPlaceResponse -> {
                            Place place = fetchPlaceResponse.getPlace();
                            LatLng latLang = place.getLatLng();
                            Uri web = place.getWebsiteUri();
                            String phoneNumber = place.getPhoneNumber();
                            OpeningHours openingHours = place.getOpeningHours();
                            List<PhotoMetadata> metadata = place.getPhotoMetadatas();
                            if (metadata != null && !metadata.isEmpty()){
                                PhotoMetadata photoMetadata = metadata.get(0);
                                String attributions = photoMetadata.getAttributions();
                                FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                        .setMaxWidth(900)
                                        .setMaxHeight(500)
                                        .build();
                                placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                                    Bitmap bitmap = fetchPhotoResponse.getBitmap();

                                    setMarker(placeName, address, latLang, web, phoneNumber, openingHours, placeId , bitmap);
                                });
                            } else {
                                Bitmap bitmap = null;
                                setMarker(placeName, address, latLang, web, phoneNumber, openingHours, placeId , bitmap);
                            }
                        });
            }
        });
    }

    private void placeInfoReverse(String query, LatLng latLang, PlacesClient placesClient){
        for (Marker marker : markerList) {
            marker.remove();
        }
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {

            List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
            if (!predictions.isEmpty()) {
                AutocompletePrediction prediction = predictions.get(0);

                String placeId = prediction.getPlaceId();
                String placeName = prediction.getPrimaryText(null).toString();
                String address = prediction.getSecondaryText(null).toString();

                List<Place.Field> placeFields = Collections.singletonList(Place.Field.WEBSITE_URI);
                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest)
                        .addOnSuccessListener(fetchPlaceResponse -> {
                            Place place = fetchPlaceResponse.getPlace();
                            Uri web = place.getWebsiteUri();
                            String phoneNumber = place.getPhoneNumber();
                            OpeningHours openingHours = place.getOpeningHours();
                            Bitmap bitmap = null;
                            setMarker(placeName, address, latLang, web, phoneNumber, openingHours, placeId, bitmap);
                        });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.toolbar_action_settings){
            AlertDialog.Builder builder = new AlertDialog.Builder(mapContext);
            builder.setMessage("Navstar! \n" + "La tua app per gestire  una lista di segnaposti geografici.");
            builder.show();
        } else if(id == R.id.toolbar_map){
            if(mapType){

                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mapType = false;
            } else {
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                mapType = true;
            }
            return true;
        } else if (id == R.id.toolbar_view) {
            for (Marker marker : markerList) {
                marker.remove();
            }
            markerList.clear();
            List<com.myapplication.navstar.List_Detail> database = databaseSupport.getAll();
            if(!database.isEmpty()){
                for (com.myapplication.navstar.List_Detail rowList : database) {
                    double lat = rowList.getLatitude();
                    double lng = rowList.getLongitude();
                    String name = rowList.getName();
                    String address = rowList.getAddress();
                    LatLng latLng = new LatLng(lat, lng);
                    setMarker(name, address, latLng, null, null, null, null , null);
                }
                Toast.makeText(mapContext, "Luoghi salvati", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}