package com.example.hydrowatch

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hydrowatch.databinding.ActivityMainBinding
import com.example.hydrowatch.ui.home.HomeFragment
import com.example.hydrowatch.ui.waterCounter.WaterCounterViewModel
import com.google.android.material.navigation.NavigationView
import org.json.JSONArray
import java.lang.Double.max
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var dataUpdate: DataUpdate
    private lateinit var sharedModel: WaterCounterViewModel

    private val BASE_TEMPERATURE = 14.0
    private val C_WATER = 4.2
    fun processData(response: JSONArray) {

        val flow = max(response.getJSONObject(0).optDouble("value", 0.0), 0.0)
        val temperature = response.getJSONObject(1).optDouble("value", BASE_TEMPERATURE)
        val energy = C_WATER * flow * DataUpdate.DataUpdateConstants.UPDATE_FREQUENCY.toDouble() * max(temperature - BASE_TEMPERATURE, 0.0)

        DatabaseManager.getInstance(this).saveToDB(flow, energy)

        val calendar: Calendar = Calendar.getInstance()
        val cursor = DatabaseManager.getInstance(this).readFromDB(DatabaseManager.dateFormat.format(calendar.time))
        if (cursor.count == 0)
        {
            Log.e ("processData", "Error: cursor empty")
        } else {
            cursor.moveToFirst()
            Log.d("flow",
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Usage.COLUMN_FLOW))
                    .toString()
            )
            Log.d("energy",
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Usage.COLUMN_ENERGY))
                    .toString()
            )
        }
        val home = HomeFragment.getInstance()
        if (home != null) {
            home.updateHomeView(deltaWater = flow, deltaEnergy = energy)
        } else {
            Log.e("processData", "Home fragment is null")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        DatabaseManager.getInstance(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        sharedModel = ViewModelProvider(this)[WaterCounterViewModel::class.java]
        dataUpdate = DataUpdate(this)
        dataUpdate.startUpdates()

//        binding.appBarMain.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Wil you stop wasting water?", Snackbar.LENGTH_LONG)
//                .setAction("Pinky promise") { sharedModel.resetCounter() }.show()
//        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_statistics, R.id.nav_friends, R.id.nav_medals
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    fun setActionBarTitle(title:String) {
        supportActionBar!!.title = title
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


}