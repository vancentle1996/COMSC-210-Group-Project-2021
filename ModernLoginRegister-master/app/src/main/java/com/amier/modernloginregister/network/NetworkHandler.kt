package com.amier.modernloginregister.network

import com.amier.modernloginregister.model.DirectionsResponse
import com.amier.modernloginregister.model.GeoCodeResponse
import com.amier.modernloginregister.model.PlacesPOJO
import com.amier.modernloginregister.model.ResultDistanceMatrix
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NetworkHandler {

    @GET("place/nearbysearch/json?")
    fun doPlaces(
        @Query(value = "type", encoded = true) type: String?,
        @Query(value = "location", encoded = true) location: String?,
        @Query(value = "name", encoded = true) name: String?,
        @Query(value = "opennow", encoded = true) opennow: Boolean,
        @Query(value = "rankby", encoded = true) rankby: String?,
//                 @Query(value = "radius", encoded = true) radius: Long,
        @Query(value = "key", encoded = true) key: String?
    ): Call<PlacesPOJO.Root?>?


    @GET("distancematrix/json")
    fun  // origins/destinations:  LatLng as string
            getDistance(
        @Query("key") key: String?,
        @Query("origins") origins: String?,
        @Query("destinations") destinations: String?
    ): Call<ResultDistanceMatrix?>?

    //    https://maps.googleapis.com/maps/api/directions/json?" +"origin=" + origin.latitude + "," + origin.longitude+ "&" +"destination=" + dest.latitude + "," + dest.longitude +"&sensor=false&units=metric&mode=driving" + "&" + key=YOUR_API_KEY
    @GET("directions/json")
    fun  // origins/directions:  LatLng as string
            getDirections(
        @Query("origin") origin: String?,
        @Query("destination") destination: String?,
        @Query("mode") mode: String?, @Query(value = "key", encoded = true) key: String?
    ): Call<DirectionsResponse?>?

    // http://maps.googleapis.com/maps/api/geocode/json?address=zipcode
    @GET("geocode/json")
    fun  // origins/directions:  LatLng as string
            getGeoCoding(
        @Query("address") address: String?,
        @Query("key") key: String?
    ): Call<GeoCodeResponse?>?
}