package com.amier.modernloginregister.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DirectionsResponse {
    @SerializedName("status")
    public String status;

    @SerializedName("geocoded_waypoints")
    public List<GeocodedWaypoint> geocoded_waypoints;

    public List<Route> routes;

    public class GeocodedWaypoint {
        @SerializedName("geocoder_status")
        public String geocoder_status;
        public String place_id;
        public List<String> types;
    }

    public class Route {
        public MapBounds bounds;
        @SerializedName("legs")
        public List<Legs> legs;

        public class MapBounds {
            public MapBound northeast;
            public MapBound southwest;
        }

        public class MapBound {
            public double lat;
            public double lng;
        }

        public class Legs {
            @SerializedName("duration")
            public ValueItem duration;
            @SerializedName("distance")
            public ValueItem distance;
            public String end_address;
            public MapBound end_location;
            public String start_address;
            public MapBound start_location;
        }

        public class ValueItem {
            @SerializedName("value")
            public long value;
            @SerializedName("text")
            public String text;
        }
    }
}