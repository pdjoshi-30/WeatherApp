package com.example.weatherapp.Activities
import retrofit2.Callback;
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.media.audiofx.Equalizer.Settings
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import androidx.databinding.DataBindingUtil
import com.example.weatherapp.Models.WeatherModel
import com.example.weatherapp.R
import com.example.weatherapp.Utilities.ApiUtilities
import com.example.weatherapp.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import org.joda.time.DateTime
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private val LOCATION_REQUEST_CODE = 101
    private val apiKey = "ec56acf7f61b626db75c9ea5eaa50df8"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)

       getCurrentLocation()
        binding.SearchCity.setOnEditorActionListener { textView, i, keyEvent ->
            if(i==EditorInfo.IME_ACTION_SEARCH){
                getCityWeather(binding.SearchCity.text.toString())
                val view = this.currentFocus
                if(view!=null){
                    val imm :InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken,0,)
                    binding.SearchCity.clearFocus()
                }
                return@setOnEditorActionListener true
            }
            else{
                return@setOnEditorActionListener false
            }
        }
        binding.CurrentLocation.setOnClickListener{
            getCurrentLocation()
        }

    }
    private fun getCityWeather(city:String){
        binding.ProgressBAR.visibility = View.VISIBLE
        ApiUtilities.getAPIInterface()?.getCurrWeatherCity(city,apiKey)?.enqueue(object : Callback<WeatherModel>{
            override fun onResponse(call: Call<WeatherModel>, response: Response<WeatherModel>) {
                if(response.isSuccessful){
                    binding.ProgressBAR.visibility= View.GONE
                    response.body()?.let{
                        setData(it)
                    }
                }
                else{
                    Toast.makeText(this@MainActivity, "No City Found", Toast.LENGTH_SHORT).show();
                    binding.ProgressBAR.visibility= View.GONE

                }
            }

            override fun onFailure(call: Call<WeatherModel>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })

    }
    private fun fetchCurrentWeather(latitude:String,longitude:String){
        ApiUtilities.getAPIInterface()?.getCurrWeatherData(latitude,longitude,apiKey)?.enqueue(object :Callback<WeatherModel>{
            override fun onResponse(call: Call<WeatherModel>, response: Response<WeatherModel>) {
                if(response.isSuccessful){
                    binding.ProgressBAR.visibility= View.GONE
                    response.body()?.let{
                        setData(it)
                    }
                }

            }

            override fun onFailure(call: Call<WeatherModel>, t: Throwable) {

            }

        })
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getCurrentLocation(){
        if(checkPermissions()){
            if(isLocationEnabled()){
                if(ActivityCompat.checkSelfPermission(
                        this,Manifest.permission.ACCESS_FINE_LOCATION
                )!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                )!=PackageManager.PERMISSION_GRANTED){
                    requestPermission()
                    return
                }
                fusedLocationProvider.lastLocation.addOnSuccessListener {
                    location->
                    if(location!=null){
                        currentLocation=location
                        binding.ProgressBAR.visibility = View.VISIBLE
                        fetchCurrentWeather(location.latitude.toString(),location.longitude.toString())
                    }
                }

            }
            else{
                val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }
        else{

            requestPermission()

        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),LOCATION_REQUEST_CODE


            )
    }
    private fun isLocationEnabled(): Boolean{
          val locationManager:LocationManager=getSystemService(Context.LOCATION_SERVICE)
        as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)|| locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    }
    private fun checkPermissions(): Boolean{
        if(ActivityCompat.checkSelfPermission(
                this,Manifest.permission.ACCESS_COARSE_LOCATION
        )==PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==LOCATION_REQUEST_CODE){
            if(grantResults.isNotEmpty()&& grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            }
            else{

            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setData(body:WeatherModel){
        binding.apply {
            val currentdate = SimpleDateFormat("dd/mm/yyyy hh:mm").format(Date())
            time.text = currentdate.toString()
            maxTemp.text = "Max "+kelvintoCelcius(body?.main?.temp_max!!)+"°"
            minTemp.text = "Min "+kelvintoCelcius(body?.main?.temp_min!!)+"°"
            FinalTemp.text = ""+kelvintoCelcius(body?.main?.temp!!)+"°"
            WeatherName.text = body.weather[0].main
            sunriseTime.text = timeconversion(body.sys.sunrise.toLong())
            sunsetTime.text  = timeconversion(body.sys.sunset.toLong())
            PressureVal.text = body.main.pressure.toString()
            humidVal.text = body.main.humidity.toString()+"%"
            farenVal.text = ""+kelvintoCelcius(body.main.temp).times(1.8).plus(32).roundToInt()+""
            SearchCity.setText(body.name)
            windVal.text = body.wind.speed.toString()+"m/s"
            groundVal.text = body.main.grnd_level.toString()
            seaVal.text = body.main.sea_level.toString()
            CountryName.text=body.sys.country


        }
        updateUI(body.weather[0].id)
    }



    private fun timeconversion(toLong: Long): String {
         val localTime=toLong.let {
             Instant.ofEpochSecond(it).atZone(ZoneId.systemDefault()).toLocalTime()
         }
        return localTime.toString()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun kelvintoCelcius(tempMax: Double): Double {
         var Ctemp = tempMax
        Ctemp = Ctemp.minus(273)
        return Ctemp.toBigDecimal().setScale(1,RoundingMode.UP).toDouble()
    }
    private fun updateUI(id: Int) {
        binding.apply {
            when(id){
                in 200..232->{
                    WeatherLogo.setImageResource(R.drawable.ic_storm_weather)
                    majorLayout.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.thunderstrom_bg)
                    CardOptions.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.thunderstrom_bg)

                }
                in 300..321->{
                    WeatherLogo.setImageResource(R.drawable.ic_few_clouds)
                    majorLayout.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.drizzle_bg)
                    CardOptions.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.drizzle_bg)
                }
                in 500..531->{
                    WeatherLogo.setImageResource(R.drawable.ic_rainy_weather)
                    majorLayout.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.rain_bg)
                    CardOptions.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.rain_bg)
                }
                in 700..781->{
                    WeatherLogo.setImageResource(R.drawable.ic_broken_clouds)
                    majorLayout.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.atmosphere_bg)
                    CardOptions.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.atmosphere_bg)
                }
                800->{
                    WeatherLogo.setImageResource(R.drawable.ic_clear_day)
                    majorLayout.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.clear_bg)
                    CardOptions.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.clear_bg)
                }
                in 801..804->{
                    WeatherLogo.setImageResource(R.drawable.ic_cloudy_weather)
                    majorLayout.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.clouds_bg)
                    CardOptions.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.clouds_bg)
                }
                else->{
                    WeatherLogo.setImageResource(R.drawable.ic_unknown)
                    majorLayout.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.unknown_bg)
                    CardOptions.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.unknown_bg)
                }
            }
        }
    }

}