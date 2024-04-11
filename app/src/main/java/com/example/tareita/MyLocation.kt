package com.example.tareita

import org.json.JSONException
import org.json.JSONObject

class MyLocation(var latitud: Double, var longitud: Double, var fecha: String) {
    fun toJSON(): JSONObject {
        val obj = JSONObject()
        try {
            obj.put("latitud", latitud)
            obj.put("longitud", longitud)
            obj.put("fecha", fecha)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return obj
    }
}
