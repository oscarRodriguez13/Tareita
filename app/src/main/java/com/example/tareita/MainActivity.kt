package com.example.tareita

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tareita.databinding.ActivityMainBinding
import com.example.tareita.domain.Historial
import com.example.tareita.domain.HistorialAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.Writer
import java.util.Date
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback
    private val aeropuertoElDoradoLatitud = 4.7017
    private val aeropuertoElDoradoLongitud = -74.1469
    private val RADIUS_OF_EARTH_KM = 6371
    private val localizaciones = JSONArray()
    private var mHistorialAdapter: HistorialAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationRequest = createLocationRequest()

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                Log.i("LOCATION", "Location update in the callback: $location")
                if (location != null) {
                    updateLocationUI(location)
                }
            }
        }

        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                mFusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    updateLocationUI(location)
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Datos.MY_PERMISSION_REQUEST_FINE_LOCATION
                )
            }

            else -> {
                // You can directly ask for the permission.
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Datos.MY_PERMISSION_REQUEST_FINE_LOCATION
                )
            }
        }

        binding.button.setOnClickListener {
            println(binding.latitud.text.toString())
            writeJSONObject(binding.latitud.text.toString(), binding.longitud.text.toString())

            // Cargar JSON desde los assets
            val jsonArray = readJSONArrayFromFile("locations.json")

            // Parsear el JSON a una lista de objetos Jugador
            val historialList = parseJSON(jsonArray)

            // Inicialización del adaptador con la lista parseada
            historialList?.let {
                mHistorialAdapter = HistorialAdapter(this, it)
                binding.lista.adapter = mHistorialAdapter
            }
        }

        binding.button2.setOnClickListener {
            val intent = Intent(
                applicationContext,
                MapsActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mFusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            null
        )
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Datos.MY_PERMISSION_REQUEST_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    mFusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                        updateLocationUI(location)
                    }
                } else {
                    // Explain to the user that the feature is unavailable
                    Toast.makeText(this, "Funcionalidad Reducida", Toast.LENGTH_SHORT).show()
                }
                return
            }

            else -> {
                // Ignore all other requests.
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateLocationUI(location: android.location.Location?) {
        location?.let {
            binding.latitud.text = "Latitud: ${location.latitude}"
            binding.longitud.text = "Longitud: ${location.longitude}"
            binding.elevacion.text = "Altitud: ${location.altitude}"

            val distancia = distance(
                location.latitude, location.longitude,
                aeropuertoElDoradoLatitud, aeropuertoElDoradoLongitud
            )
            binding.distancia.text = "Distancia al Aeropuerto El Dorado: ${distancia} km"
        }
    }

    private fun distance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val latDistance = Math.toRadians(lat1 - lat2)
        val lngDistance = Math.toRadians(long1 - long2)
        val a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val result = RADIUS_OF_EARTH_KM * c
        return (result * 100.0).roundToInt() / 100.0
    }

    private fun createLocationRequest(): LocationRequest =
        // New builder
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).apply {
            setMinUpdateIntervalMillis(5000)
        }.build()

    private fun writeJSONObject(latitud: String, longitud: String) {
        val latArray = latitud.split(":")
        val longArray = longitud.split(":")
        val latitud = latArray.getOrNull(1)?.trim()?.toDoubleOrNull()
        if (latitud == null) {
            Log.e("LOCATION", "Error al convertir latitud a Double: $latitud")
            return
        }

        val longitud = longArray.getOrNull(1)?.trim()?.toDoubleOrNull()
        if (longitud == null) {
            Log.e("LOCATION", "Error al convertir longitud a Double: $longitud")
            return
        }

        localizaciones.put(
            MyLocation(
                latitud, longitud, Date(System.currentTimeMillis()).toString()
            ).toJSON()
        )
        var output: Writer?
        val filename = "locations.json"
        try {
            val file = File(baseContext.getExternalFilesDir(null), filename)
            Log.i("LOCATION", "Ubicacion de archivo: $file")
            output = BufferedWriter(FileWriter(file))
            output.write(localizaciones.toString())
            output.close()
            Toast.makeText(applicationContext, "Location saved", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("LOCATION", "Error al guardar la ubicación: ${e.message}", e)
        }
    }

    private fun readJSONArrayFromFile(fileName: String): JSONArray {
        val file = File(baseContext.getExternalFilesDir(null), fileName)
        if (!file.exists()) {
            Log.i("LOCATION", "Ubicacion de archivo: $file no encontrado")
            return JSONArray()
        }
        val jsonString = file.readText()
        return JSONArray(jsonString)
    }

    private fun parseJSON(jsonArray: JSONArray?): List<Historial>? {
        return jsonArray?.let {
            val gson = Gson()
            val type = object : TypeToken<List<Historial>>() {}.type
            gson.fromJson<List<Historial>>(it.toString(), type)
        }
    }

}