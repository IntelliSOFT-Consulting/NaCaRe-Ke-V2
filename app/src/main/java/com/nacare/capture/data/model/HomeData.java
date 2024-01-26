package com.nacare.capture.data.model;

import android.os.Parcel;

import java.io.Serializable;

public class HomeData implements Serializable {
    private String id;
    private String name;

    public HomeData(String id, String name) {
        this.name = name;
        this.id = id;
    }

    protected HomeData(Parcel in) {
        id = in.readString();
        name = in.readString();
    }



    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }


}
