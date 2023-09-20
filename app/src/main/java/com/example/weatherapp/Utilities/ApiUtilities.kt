package com.example.weatherapp.Utilities

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtilities {
    private var retrofit : Retrofit? = null
    var Base_URL="https://api.openweathermap.org/data/2.5/"
    fun getAPIInterface(): APIInterface? {
        if(retrofit==null){
            retrofit=Retrofit.Builder().baseUrl(Base_URL).addConverterFactory(GsonConverterFactory.create()).build()
        }
        return retrofit?.create(APIInterface::class.java)
    }
}