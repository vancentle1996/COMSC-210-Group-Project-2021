package com.amier.modernloginregister.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GeoCodeResponse {

    @SerializedName("results")
    public List<Result> results = new ArrayList<>();
    @SerializedName("status")
    public String status;

    public class Result implements Serializable {
        public String formatted_address;
        public Geometry geometry;
        public List<AddressComponent> address_components;
    }

    public class AddressComponent implements Serializable {
        public String long_name;
        public String short_name;
        public List<String> types;
    }

    public class Geometry implements Serializable {

        @SerializedName("location")
        public LocationA locationA;

    }

    public class LocationA implements Serializable {

        @SerializedName("lat")
        public String lat;
        @SerializedName("lng")
        public String lng;


    }


}