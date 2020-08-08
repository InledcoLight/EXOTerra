package com.inledco.exoterra.uvbbuddy;

public class DistanceUvbLight {

    private String distance;
    private String[] uvbLights;

    public DistanceUvbLight() {
    }

    public DistanceUvbLight(String distance, String[] uvbLights) {
        this.distance = distance;
        this.uvbLights = uvbLights;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String[] getUvbLights() {
        return uvbLights;
    }

    public void setUvbLights(String[] uvbLights) {
        this.uvbLights = uvbLights;
    }
}
