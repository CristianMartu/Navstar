package com.myapplication.navstar;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int POST_NOTIFICATION_REQUEST_CODE = 2;
    private boolean permissionGranted = false;
    private DrawerLayout drawerLayout;
    private MapFragment mapFragment;
    private SavedPlaceFragment savedPlaceFragment;
    private SavedPlaceDetail savedPlaceDetail;
    public DatabaseSupport databaseSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("NAVSTAR");
        //toolbar.setSubtitle("by Cristian Martucci");
        toolbar.inflateMenu(R.menu.toolbar_menu);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mapFragment = new MapFragment(MainActivity.this);
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    mapFragment).commit();
            navigationView.setCheckedItem(R.id.nav_map);
        }
        databaseSupport = new DatabaseSupport(MainActivity.this);
        savedPlaceFragment = new SavedPlaceFragment(MainActivity.this);
        //savedPlaceDetail = new SavedPlaceDetail(MainActivity.this);
        resultPermission();
    }

    public DatabaseSupport getDatabaseSupport() {
        return databaseSupport;
    }
    public SavedPlaceFragment getSavedPlaceFragment() {
        return savedPlaceFragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if(itemId == R.id.nav_map){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    mapFragment).commit();
        } else if (itemId == R.id.nav_savedPlace) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    savedPlaceFragment).commit();
        } else if (itemId == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Prova Navstar! Scaricala qui: [Inserire il link all'app]");
            sendIntent.setType("text/plain");

            startActivity(Intent.createChooser(sendIntent, "Condividi l'app con"));
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void resultPermission() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            POST_NOTIFICATION_REQUEST_CODE);
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
            case POST_NOTIFICATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    resultPermission();
                    if(permissionGranted) {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, "Abilitare i permessi per un corretto funzionamento!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}