package com.amier.modernloginregister.ui.fragment

import android.Manifest.permission
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.amier.modernloginregister.MainActivity.Companion.keyDistanceResponse
import com.amier.modernloginregister.MainActivity.Companion.KEY_PLACES_RESPONSE
import com.amier.modernloginregister.MainActivity.Companion.directionsResponse
import com.amier.modernloginregister.R
import com.amier.modernloginregister.model.DirectionsResponse
import com.amier.modernloginregister.model.PlacesPOJO
import com.amier.modernloginregister.model.PlacesPOJO.CustomA
import com.amier.modernloginregister.model.ResultDistanceMatrix
import com.amier.modernloginregister.model.AddressModel
import com.amier.modernloginregister.network.RetrofitApiClient
import com.amier.modernloginregister.network.NetworkHandler
import com.amier.modernloginregister.ui.adapter.MapResultRvAdapter
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ResultFragment : Fragment() {
    private var permissionsToRequest: ArrayList<String>? = null
    private val permissionsRejected = ArrayList<String>()
    private val permissions = ArrayList<String>()
    var addressModels: MutableList<AddressModel>? = null
    var apiService: NetworkHandler? = null
    var latLngString: String? = null
    var latLng: LatLng? = null
    var resultRecyclerView: RecyclerView? = null
    var resultPb: ProgressBar? = null

    var results: List<CustomA>? = null

    var type: String? = null
    var zipcode: String? = null
    var nearbyDistance: String? = null

    interface MapResultSelectListener {
        fun onResultClicked(place: PlacesPOJO.CustomA)
    }

    private lateinit var mapResultSelectListener: MapResultSelectListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MapResultSelectListener) {
            mapResultSelectListener = context
        } else {
            throw ClassCastException(
                context.toString() + " must implement OnProductSelected."
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(
            R.layout.fragment_result, container,
            false
        )
        handleData()
        setupViews(view)
        return view
    }

    private fun handleData() {
        type = arguments?.getString(BUNDLE_KEY_TYPE)
        zipcode = arguments?.getString(BUNDLE_KEY_ZIPCODE)
        nearbyDistance = arguments?.getString(BUNDLE_KEY_NEARBY_DISTANCE)
        latLngString = arguments?.getString(BUNDLE_KEY_LAT_LONG)

        permissions.add(permission.ACCESS_FINE_LOCATION)
        permissions.add(permission.ACCESS_COARSE_LOCATION)
        permissionsToRequest = findUnAskedPermissions(permissions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest!!.size > 0) requestPermissions(
                permissionsToRequest!!.toTypedArray(),
                ALL_PERMISSIONS_RESULT
            ) else {
                fetchLocation()
            }
        } else {
            fetchLocation()
        }
        apiService = RetrofitApiClient.getClient().create(NetworkHandler::class.java)
    }

    private fun setupViews(view: View) {
        resultPb = view.findViewById<View>(R.id.resultPb) as ProgressBar
        resultRecyclerView = view.findViewById<View>(R.id.rvResult) as RecyclerView
        resultRecyclerView!!.isNestedScrollingEnabled = false
        resultRecyclerView!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        resultRecyclerView!!.layoutManager = layoutManager
        if (keyDistanceResponse.isNullOrEmpty() || KEY_PLACES_RESPONSE.isNullOrEmpty()) {

            type?.let { fetchStores(it, it) }
        } else {
            results = KEY_PLACES_RESPONSE
            addressModels = keyDistanceResponse
            setAdapter()
        }
    }

    private fun fetchStores(placeType: String, businessName: String) {
        resultPb?.visibility = View.VISIBLE
        val call = apiService!!.doPlaces(
            placeType,
            latLngString,
            businessName,
            true,
            "distance",
            RetrofitApiClient.GOOGLE_PLACE_API_KEY
        )
        call!!.enqueue(object : Callback<PlacesPOJO.Root?> {
            override fun onResponse(
                call: Call<PlacesPOJO.Root?>,
                response: Response<PlacesPOJO.Root?>
            ) {
                val root = response.body()
                if (response.isSuccessful) {
                    if (root!!.status == "OK") {
                        results = root.customA
                        addressModels = ArrayList()
                        results?.apply {
                            for (i in indices) {
                                if (i == 10) break
                                val info = get(i)
                                fetchDistance(info)
                            }
                        }
                    } else {
                        resultPb?.visibility = View.GONE
                        Toast.makeText(context, "No matches found near you", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else if (response.code() != 200) {
                    resultPb?.visibility = View.GONE
                    Toast.makeText(
                        context,
                        "Error " + response.code() + " found.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<PlacesPOJO.Root?>, t: Throwable) {
                // Log error here since request failed
                resultPb?.visibility = View.GONE
                call.cancel()
            }
        })
    }

    private fun findUnAskedPermissions(wanted: ArrayList<String>): ArrayList<String> {
        val result = ArrayList<String>()
        for (perm in wanted) {
            if (!hasPermission(perm)) {
                result.add(perm)
            }
        }
        return result
    }

    private fun hasPermission(permission: String): Boolean {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return context?.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            }
        }
        return true
    }

    private fun canMakeSmores(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            ALL_PERMISSIONS_RESULT -> {
                for (perms in permissionsToRequest!!) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms)
                    }
                }
                if (permissionsRejected.size > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected[0])) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                DialogInterface.OnClickListener { dialog, which ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(
                                            permissionsRejected.toTypedArray(),
                                            ALL_PERMISSIONS_RESULT
                                        )
                                    }
                                })
                            return
                        }
                    }
                } else {
                    fetchLocation()
                }
            }
        }
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun fetchLocation() {
        SmartLocation.with(context).location()
            .oneFix()
            .start(object : OnLocationUpdatedListener {
                override fun onLocationUpdated(location: Location) {
                    latLngString = location.latitude.toString() + "," + location.longitude
                    latLng = LatLng(location.latitude, location.longitude)
                }
            })
    }

    private fun fetchDistance(info: CustomA) {
        val call = apiService!!.getDistance(
            RetrofitApiClient.GOOGLE_PLACE_API_KEY,
            latLngString,
            info.geometry.locationA.lat + "," + info.geometry.locationA.lng
        )
        call!!.enqueue(object : Callback<ResultDistanceMatrix?> {
            override fun onResponse(
                call: Call<ResultDistanceMatrix?>,
                response: Response<ResultDistanceMatrix?>
            ) {
                resultPb?.visibility = View.GONE
                val resultDistance = response.body()
                if ("OK".equals(resultDistance!!.status, ignoreCase = true)) {
                    val infoDistanceMatrix = resultDistance.rows[0]
                    val distanceElement =
                        infoDistanceMatrix.elements[0] as ResultDistanceMatrix.InfoDistanceMatrix.DistanceElement
                    if ("OK".equals(distanceElement.status, ignoreCase = true)) {
                        val itemDuration = distanceElement.duration
                        val itemDistance = distanceElement.distance
                        val totalDistance = itemDistance.text.toString()
                        val totalDuration = itemDuration.text.toString()
                        addressModels?.add(
                            AddressModel(
                                info.name,
                                info.vicinity,
                                totalDistance,
                                totalDuration
                            )
                        )
                        if (addressModels?.size == 10 || addressModels?.size == results?.size) {

                            KEY_PLACES_RESPONSE = results
                            keyDistanceResponse = addressModels

                            setAdapter()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResultDistanceMatrix?>, t: Throwable) {
                resultPb?.visibility = View.GONE
                call.cancel()
            }
        })
    }

    private fun setAdapter() {
        val adapterStores =
            MapResultRvAdapter(
                results,
                addressModels,
                mapResultSelectListener
            )
        resultRecyclerView?.adapter = adapterStores
    }

    fun fetchDirections(info: CustomA) {
        resultPb?.visibility = View.VISIBLE
        val call = apiService!!.getDirections(
            latLngString, info.geometry.locationA.lat + "," + info.geometry.locationA.lng,
            "driving", RetrofitApiClient.GOOGLE_PLACE_API_KEY
        )
        call?.enqueue(object : Callback<DirectionsResponse?> {
            override fun onResponse(
                call: Call<DirectionsResponse?>,
                response: Response<DirectionsResponse?>
            ) {
                val resultDirections = response.body()
                if ("OK".equals(resultDirections?.status, ignoreCase = true)) {
                    directionsResponse = resultDirections
                }
                resultPb?.visibility = View.GONE
            }

            override fun onFailure(call: Call<DirectionsResponse?>, t: Throwable) {
                resultPb?.visibility = View.GONE
                call.cancel()
            }
        })
    }

    companion object {
        const val ALL_PERMISSIONS_RESULT = 101
        const val BUNDLE_KEY_TYPE = "type"
        const val BUNDLE_KEY_ZIPCODE = "zipcode"
        const val BUNDLE_KEY_NEARBY_DISTANCE = "nearby_distance"
        const val BUNDLE_KEY_LAT_LONG = "lat_long_string"
        const val BUNDLE_KEY_DEST_LAT = "bundle_key_dest_lat"
        const val BUNDLE_KEY_DEST_LNG = "bundle_key_dest_lng"
    }
}