package com.example.springfoxdemo.controllers;

public class GeoLocation {

  private long cityId;
  private double latitude;
  private double longitude;

  public GeoLocation(long cityId, double latitude, double longitude) {
    this.cityId = cityId;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public long getCityId() {
    return cityId;
  }
}
