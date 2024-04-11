package com.example.tareita

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.tareita.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.MapStyleOptions
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var mGeocoder: Geocoder? = null
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mSensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var lightSensorListener: SensorEventListener

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(lightSensorListener, lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL)
    }
    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(lightSensorListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        initViews()

        binding.editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
            }
            val addressString = binding.editText.text.toString()
            //val addressString = "Universidad de la Sabana"
            if (addressString.isNotEmpty()) {
                try {
                    if (mMap != null && mGeocoder != null) {
                        val addresses = mGeocoder!!.getFromLocationName(addressString, 2)
                        if (addresses != null && addresses.isNotEmpty()) {
                            val addressResult = addresses[0]
                            val position = LatLng(addressResult.latitude, addressResult.longitude)

                            mMap!!.addMarker(
                                MarkerOptions().position(position)
                                    .title("Marker in busqueda")
                            )

                        }
                    } else {
                        Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT)
                            .show()
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this, "La dirección esta vacía", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    fun initViews (){
        lightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        lightSensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (mMap != null) {
                    if (event.values[0] < 5000) {
                        Log.i("MAPS", "DARK MAP " + event.values[0])
                        //mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@semana9_E3, R.raw.style_json))
                    } else {
                        Log.i("MAPS", "LIGHT MAP " + event.values[0])
                        //mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@semana9_E3, R.raw.style_json))
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mGeocoder = Geocoder(baseContext)


        mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.retro_map_style))

        // Add a marker in Sydney and move the camera
        val jave = LatLng(4.62852, -74.06469)
        val mkr1 = LatLng(4.52852, -74.06469)
        val mkr2 = LatLng(4.72852, -74.06469)
        val position = LatLng(4.63852, -74.07469)
        mMap!!.moveCamera(CameraUpdateFactory.zoomTo(15F))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(jave))
        mMap!!.addMarker(
            MarkerOptions().position(jave)
                .title("Marker in Sydney")
                .snippet("Alma Matter")
        )

        mMap!!.addMarker(
            MarkerOptions().position(mkr1)
                .title("Marker in mkr1")
                .snippet("Alma Matter")
        )

        mMap!!.addMarker(
            MarkerOptions().position(mkr2)
                .title("Marker in mkr2")
                .snippet("Alma Matter")
        )

    }
}