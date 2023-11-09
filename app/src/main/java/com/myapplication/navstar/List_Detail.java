package com.myapplication.navstar;

import androidx.lifecycle.ViewModel;

public class List_Detail extends ViewModel {
    private String name, address, placeId, description, date;
    private Double latitude, longitude;

    public List_Detail(){}

    public List_Detail(String name){
        this.name = name;
    }

    public List_Detail(String name, String address, String id, String date) {
        this.name = name;
        this.address = address;
        this.placeId = id;
        this.date = date;
    }

    public List_Detail(String name, String address, String id, double lat, double lng, String desc, String date) {
        this.name = name;
        this.address = address;
        this.placeId = id;
        this.latitude = lat;
        this.longitude = lng;
        this.description = desc;
        this.date = date;
    }

    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    public String getPlaceId() {
        return placeId;
    }
    public Double getLatitude() {
        return latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public String getDescription() {
        return description;
    }
    public String getDate() {
        return date;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setPlaceId(String placeId){
        this.placeId = placeId;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setDate(String date) {
        this.date = date;
    }
}
