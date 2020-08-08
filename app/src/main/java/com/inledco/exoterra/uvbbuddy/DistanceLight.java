package com.inledco.exoterra.uvbbuddy;

//@Entity
public class DistanceLight {
//    @PrimaryKey(autoGenerate = true)
    private int id;
    private String category;
    private String distance;
    private String light;

    public DistanceLight() {
    }

    public DistanceLight(String category, String distance, String light) {
        this.category = category;
        this.distance = distance;
        this.light = light;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getLight() {
        return light;
    }

    public void setLight(String light) {
        this.light = light;
    }
}
