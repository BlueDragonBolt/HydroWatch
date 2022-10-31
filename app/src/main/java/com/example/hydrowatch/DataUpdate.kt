package com.example.hydrowatch

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.ui.AppBarConfiguration
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.hydrowatch.databinding.ActivityMainBinding
import com.example.hydrowatch.ui.waterCounter.WaterCounterViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DataUpdate(activity: MainActivity) : ViewModelStoreOwner {
    private var mainActivity: MainActivity = activity
    private var appViewModelStore: ViewModelStore = ViewModelStore()
    private val scope = MainScope()
    private var job: Job? = null
    private var queue = Volley.newRequestQueue(mainActivity.applicationContext)
    private val url = "http://192.168.47.168/data" //"http://192.168.0.145:8080"
    object DataUpdateConstants {
        const val UPDATE_FREQUENCY: Long = 2
    }
//    private val sharedModel = ViewModelProvider(this)[WaterCounterViewModel::class.java]
    fun startUpdates() {
        stopUpdates()
        job = scope.launch {
            while(true) {
                Log.d("DataUpdate", "Get data called")
                getData() // the function that should be ran every second
                delay(DataUpdateConstants.UPDATE_FREQUENCY * 1000)
            }
        }
    }
    private fun getData() {
        val jsonObjectRequest= JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
//            textView.text = "Response: %s".format(response.toString())
                mainActivity.processData(response)
                Log.d("DataUpdate", "Request responded")
            },
            { error ->
                Log.e("getData", "Error:$error")
            }
        )
        queue.add(jsonObjectRequest)
    }
    fun stopUpdates() {
        job?.cancel()
        job = null
    }

    override fun getViewModelStore(): ViewModelStore {
        return appViewModelStore
    }

}