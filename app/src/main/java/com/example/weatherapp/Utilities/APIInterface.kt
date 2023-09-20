package com.example.weatherapp.Utilities

import android.telecom.Call
import com.example.weatherapp.Models.Weather
import com.example.weatherapp.Models.WeatherModel
import retrofit2.http.GET
import retrofit2.http.Query

interface APIInterface {
    @GET("weather")
    fun getCurrWeatherData(
        @Query("lat")lat:String,
        @Query("Lan")lan:String,
        @Query("APPID")appid:String
    ):retrofit2.Call<WeatherModel>
    @GET("weather")
    fun getCurrWeatherCity(
        @Query("q")q:String,
        @Query("APPID")appid:String
    ):retrofit2.Call<WeatherModel>

}