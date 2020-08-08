package com.inledco.exoterra.uvbbuddy;

import android.support.annotation.IntDef;

import java.io.Serializable;

//@Entity
public class Animal implements Serializable {

    public static final int RATE0 = 0;
    public static final int RATE1 = 1;
    public static final int RATE2 = 2;
    public static final int RATE3 = 3;
    public static final int RATE4 = 4;
    public static final int RATE5 = 5;
    @IntDef ({RATE0, RATE1, RATE2, RATE3, RATE4, RATE5})
    public @interface RATE {}

    private String name;
    private String latin_name;
    private String icon;
    @RATE
    private int rate;

    public Animal(String name, String latin_name) {
        this.name = name;
        this.latin_name = latin_name;
    }

    public Animal(String name, String latin_name, int rate) {
        this.name = name;
        this.latin_name = latin_name;
        this.rate = rate;
    }

    public Animal(String name, String latin_name, String icon, int rate) {
        this.name = name;
        this.latin_name = latin_name;
        this.icon = icon;
        this.rate = rate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatin_name() {
        return latin_name;
    }

    public void setLatin_name(String latin_name) {
        this.latin_name = latin_name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
}
