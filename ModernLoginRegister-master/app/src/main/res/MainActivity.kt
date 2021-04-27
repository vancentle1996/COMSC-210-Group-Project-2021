import android.Manifest.permission
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.amier.modernloginregister.model.DirectionsResponse
import com.amier.modernloginregister.model.PlacesPOJO
import com.amier.modernloginregister.model.AddressModel
import com.amier.modernloginregister.model.GeoCodeResponse
import com.amier.modernloginregister.network.NetworkHandler
import com.amier.modernloginregister.network.RetrofitApiClient
import com.amier.modernloginregister.ui.fragment.ResultFragment
import com.amier.modernloginregister.ui.fragment.SearchFragment
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity(), SearchFragment.onXploreClickListener,
    ResultFragment.MapResultSelectListener {
    private var permissionsToRequest: ArrayList<String>? = null
    private val permissionsRejected = ArrayList<String>()
    private val permissions = ArrayList<String>()
    var latLngString: String? = null
    var apiService: NetworkHandler? = null


    lateinit var ziplatitude: String
    lateinit var ziplongitude: String

    companion object {
        var keyDistanceResponse: MutableList<AddressModel>? = null
        var KEY_PLACES_RESPONSE: List<PlacesPOJO.CustomA>? = null
        var latLng: LatLng? = null
        var directionsResponse: DirectionsResponse? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handleData()
        setUpView()

    }

    private fun setUpView() {
        findViewById<BottomNavigationView>(R.id.navBottomBar).apply {
            setupWithNavController(findNavController(R.id.fragmentNavHost))
        }
    }

    private fun handleData() {
        apiService = RetrofitApiClient.getClient().create(NetworkHandler::class.java)

        permissions.add(permission.ACCESS_FINE_LOCATION)
        permissions.add(permission.ACCESS_COARSE_LOCATION)
        permissionsToRequest = findUnAskedPermissions(permissions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest!!.size > 0) requestPermissions(
                permissionsToRequest!!.toTypedArray(),
                ResultFragment.ALL_PERMISSIONS_RESULT
            ) else {
                fetchLocation()
            }
        } else {
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        SmartLocation.with(this).location()
            .oneFix()
            .start(object : OnLocationUpdatedListener {
                override fun onLocationUpdated(location: Location) {
                    latLngString = location.latitude.toString() + "," + location.longitude
                    latLng = LatLng(location.latitude, location.longitude)
                }
            })
    }

    private fun getLatLngFromZip(zipcode: String, args: Bundle) {
        val call = apiService!!.getGeoCoding(
            zipcode, RetrofitApiClient.GOOGLE_PLACE_API_KEY
        )
        call?.enqueue(object : Callback<GeoCodeResponse?> {
            override fun onResponse(
                call: Call<GeoCodeResponse?>,
                response: Response<GeoCodeResponse?>
            ) {
                val result = response.body()
                result?.apply {
                    if ("OK".equals(status, ignoreCase = true)) {
                        ziplatitude = results[0].geometry.locationA.lat
                        ziplongitude = results[0].geometry.locationA.lng

                        args.putString(
                            ResultFragment.BUNDLE_KEY_LAT_LONG,
                            ziplatitude + "," + ziplongitude
                        )
                        launchResultFragment(args)
                    }
                }
            }

            override fun onFailure(call: Call<GeoCodeResponse?>, t: Throwable) {
                call.cancel()
                launchResultFragment(args)
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
                return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
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
            ResultFragment.ALL_PERMISSIONS_RESULT -> {
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
                                            ResultFragment.ALL_PERMISSIONS_RESULT
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
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun clearValues() {
        keyDistanceResponse = null
        KEY_PLACES_RESPONSE = null
        directionsResponse = null
    }

    override fun onXploreClicked(type: String, zipcode: String, nearbyDistance: String) {
        hideKeyboard()
        clearValues()
        // First-time init; create fragment to embed in activity.
        val args = Bundle()
        args.putString(ResultFragment.BUNDLE_KEY_TYPE, type)
        args.putString(ResultFragment.BUNDLE_KEY_ZIPCODE, zipcode)
        args.putString(ResultFragment.BUNDLE_KEY_NEARBY_DISTANCE, nearbyDistance)
        args.putString(ResultFragment.BUNDLE_KEY_LAT_LONG, latLngString)

        if (zipcode.isNullOrEmpty()) {
            launchResultFragment(args)
        } else {
            getLatLngFromZip(zipcode, args)
        }
    }

    private fun launchResultFragment(args: Bundle) {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.search, true)
            .build()
        this.findNavController(R.id.fragmentNavHost)
            .navigate(R.id.action_searchFragment_to_resultFragment, args, navOptions)
    }

    override fun onResultClicked(place: PlacesPOJO.CustomA) {
        val args = Bundle()
        args.putString(ResultFragment.BUNDLE_KEY_DEST_LAT, place.geometry.locationA.lat)
        args.putString(ResultFragment.BUNDLE_KEY_DEST_LNG, place.geometry.locationA.lng)
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.result, true)
            .build()
    }

    fun hideKeyboard() {
        val imm: InputMethodManager =
            this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = this.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
    }
}