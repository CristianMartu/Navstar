package com.myapplication.navstar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatabaseSupport extends SQLiteOpenHelper {
    public static final String PLACE_INFO = "PLACE_INFO";
    public static final String PLACE_ID = "PLACE_ID";
    public static final String PLACE_NAME = "PLACE_NAME";
    public static final String PLACE_ADRRESS = "PLACE_ADDRESS";
    public String PLACE_DESCRIPTION = "PLACE_DESCRIPTION";
    public String PLACE_DATE = "PLACE_DATE";

    public DatabaseSupport(@Nullable Context context) {
        super(context, "place_database", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String command = "CREATE TABLE " + PLACE_INFO + " (" +
                PLACE_NAME + " TEXT, " +
                PLACE_ADRRESS + " TEXT, " +
                PLACE_ID + " TEXT, " +
                "latitude REAL, " +
                "longitude REAL, " +
                PLACE_DESCRIPTION + " TEXT, "+
                PLACE_DATE + " TEXT)";
        db.execSQL(command);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addPlace(List_Detail detail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(PLACE_NAME, detail.getName());
        cv.put(PLACE_ADRRESS, detail.getAddress());
        cv.put(PLACE_ID, detail.getPlaceId());
        cv.put("latitude", detail.getLatitude());
        cv.put("longitude", detail.getLongitude());
        cv.put(PLACE_DESCRIPTION, detail.getDescription());
        cv.put(PLACE_DATE, detail.getDate());

        long insert = db.insert(PLACE_INFO, null, cv);
        return insert != -1;
    }

    public boolean deletePlace(List_Detail detail){
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = PLACE_NAME + " = ?";
        String[] whereArgs = {detail.getName()};
        int rowsDeleted = db.delete(PLACE_INFO, whereClause, whereArgs);
        db.close();
        return rowsDeleted > 0;
    }

    public int positionPlace(String name){
        int pos = -1;
        String command = "SELECT * FROM " + PLACE_INFO;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(command, null);
        if(cursor.moveToFirst()) {
            do {
                String placeName = cursor.getString(0);
                if(name.equals(placeName)){
                    pos = cursor.getPosition();
                    break;
                }
            } while (cursor.moveToNext());}
        cursor.close();
        db.close();
        return pos;
    }

    public void setDescription(String placeId, String newDescription) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PLACE_DESCRIPTION, newDescription);
        database.update(PLACE_INFO, values, PLACE_ID + " = ?", new String[]{placeId});
        database.close();
    }
    public List<List_Detail> getAll() {
        List<List_Detail> placeList = new ArrayList<>();
        String command = "SELECT * FROM " + PLACE_INFO;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(command, null);
        if(cursor.moveToFirst()) {
            do {
                String placeName = cursor.getString(0);
                String placeAddress = cursor.getString(1);
                String placeId = cursor.getString(2);
                double placeLat = cursor.getDouble(3);
                double placeLng = cursor.getDouble(4);
                String placeDesc = cursor.getString(5);
                String plasceDate = cursor.getString(6);
                List_Detail detail = new List_Detail(placeName, placeAddress, placeId, placeLat, placeLng, placeDesc, plasceDate);
                placeList.add(detail);
            } while (cursor.moveToNext());}
        cursor.close();
        db.close();
        return placeList;
    }
}
