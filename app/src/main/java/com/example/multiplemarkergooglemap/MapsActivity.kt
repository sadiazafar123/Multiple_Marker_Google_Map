package com.example.multiplemarkergooglemap

import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.multiplemarkergooglemap.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    lateinit var currentLocation: Location
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var permissionCode = 101

    var arrList = arrayListOf<LatLng>()
    var markerList = arrayListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        findViewById<FloatingActionButton>(R.id.btnLocation).setOnClickListener() {
            getCurrentLocationUser()
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //after this onMapReady function will call



    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionCode -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationUser()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val mOptions = MarkerOptions()

        mMap = googleMap

        getCurrentLocationUser()

        googleMap.setOnMapClickListener { point ->
            //we will add latitude and longitude of click place in this "arraylist" whose type is "latlng"
            if (arrList.size < 2) {
                //code of default marker
                //val marker = MarkerOptions().position(LatLng(point.latitude, point.longitude)).title("New Marker")
                // googleMap.addMarker(marker)
                arrList.add(LatLng(point.latitude, point.longitude))
     //everytime we are creating object of marker mean preious marker we dont have
                // thast why we make arraylist where we add marker so that later we can use

                val markerOptions = MarkerOptions()
                markerOptions.position(LatLng(point.latitude, point.longitude))
                val marker: Marker? = googleMap.addMarker(markerOptions)


                var bitmapDescriptor: BitmapDescriptor? = null


                if (arrList.size == 1) {
                    bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.young_man)
                }
                if (arrList.size == 2) {
                    bitmapDescriptor =
                        BitmapDescriptorFactory.fromResource(R.drawable.bussiness_man)
                }

                markerList.add(marker!!)
                marker.setIcon(bitmapDescriptor)


                if (markerList.size == 2) {
                    val polyline1 = mMap.addPolyline(
                        PolylineOptions().clickable(true)
                            .add(arrList[0], arrList[1])
                            .geodesic(true) // this will turn this line to curve
                    )
                    // To add styles in the line
                    stylePolyline(polyline1)
                 //   mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(arrList.get(0), 15.0f));
                    //markerlist where marker will move
                    //arrlist the address where marker reached
                    animateMarker( markerList[0], arrList[1], false)

                }



            } else {
                googleMap.clear()
                arrList.clear()
                markerList.clear()
                getCurrentLocationUser()
            }
        }


    }


    private fun stylePolyline(polyline: Polyline?) {
        polyline?.let {
            Log.v("stylePolyLine", "polyline")
            // it.color = ContextCompat.getColor(binding.mapPlaceholder.context, R.color.lineColor)
            // it.color= ContextCompat.getColor(binding., R.color.coolgrey));
            it.color = Color.GREEN

            it.pattern = mutableListOf(Dash(5f), Gap(5F))
            //it.pattern = mutableListOf()
            it.startCap = RoundCap()
            it.jointType = JointType.ROUND
        }
    }

    private fun getCurrentLocationUser() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), permissionCode
            )
            return
        }
        //The FusedLocationProviderClient provides several methods to retrieve device location information
        val getLocation =
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                    Toast.makeText(
                        this, currentLocation.latitude.toString() +
                                "" + currentLocation.longitude.toString(), Toast.LENGTH_SHORT
                    ).show()

                    /* val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                     mapFragment.getMapAsync(this)
     */
                    val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                    val markerOptions = MarkerOptions().position(latLng).title("current location")
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
                    // mMap.addMarker(markerOptions)
                    mMap.setMyLocationEnabled(true)

                }
            }

    }

    fun animateMarker(
        marker: Marker, toPosition: LatLng,
        hideMarker: Boolean
    ) {
        val handler = Handler()
        val start = SystemClock.uptimeMillis()
        val proj: Projection = mMap.getProjection()
        val startPoint: Point = proj.toScreenLocation(marker.position)
        val startLatLng = proj.fromScreenLocation(startPoint)
        val duration: Long = 10000
        val interpolator: Interpolator = LinearInterpolator()
        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - start
                val t = interpolator.getInterpolation(elapsed.toFloat() / duration)
                val lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude
                val lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude
                marker.position = LatLng(lat, lng)

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                }

/*
                else {
                    if (hideMarker) {
                        marker.isVisible = false
                    } else {
                        marker.isVisible = true
                    }
                }
*/
            }
        })
    }

}