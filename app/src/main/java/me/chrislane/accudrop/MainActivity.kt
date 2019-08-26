package me.chrislane.accudrop

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import me.chrislane.accudrop.db.AccuDropDb
import me.chrislane.accudrop.fragment.*
import me.chrislane.accudrop.generator.JumpGenerator
import me.chrislane.accudrop.preference.SettingsActivity
import me.chrislane.accudrop.viewmodel.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var currentFragmentTag: String? = null
    lateinit var permissionManager: PermissionManager
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this)

        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString("currentFragmentTag")
        }

        // Check that the device has a barometer.
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        if (sensorManager != null) {
            if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null) {
                // No barometer, do not continue.
                // TODO: Change to a more appropriate notification and close app
                Toast.makeText(this, "No barometer in device.", Toast.LENGTH_SHORT).show()
                //return;
            }
        }

        // Initialise preferences
        initPreferences()

        // Create or get ViewModels
        ViewModelProviders.of(this).get<PressureViewModel>(PressureViewModel::class.java)
        ViewModelProviders.of(this).get<GnssViewModel>(GnssViewModel::class.java)
        ViewModelProviders.of(this).get<DatabaseViewModel>(DatabaseViewModel::class.java)
        ViewModelProviders.of(this).get<RouteViewModel>(RouteViewModel::class.java)
        ViewModelProviders.of(this).get<WindViewModel>(WindViewModel::class.java)

        // Set the fragment to be displayed
        setCurrentFragment()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentFragmentTag", currentFragmentTag)
    }

    /**
     * Handle the user pressing the back button.
     */
    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    /**
     * Handle options items selection.
     *
     * @param item The options item to handle.
     * @return Whether the item was handled or not.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        when (id) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.email_database_file -> {
                emailDatabaseFile()
                return true
            }
            R.id.generate_jump -> {
                JumpGenerator(this).generateJump(LatLng(51.52, 0.08), 0)
                return true
            }
            R.id.generate_guest_jump -> {
                JumpGenerator(this).generateJump(LatLng(51.52, 0.08), 9)
                return true
            }
            R.id.clear_database -> {
                AccuDropDb.clearDatabase(this)
                Toast.makeText(this, "Restart app to clear DB.", Toast.LENGTH_SHORT)
                        .show()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun emailDatabaseFile() {
        try {
            val data = Environment.getDataDirectory()

            val currentDBPath = ("//data//" + "me.chrislane.accudrop" + "//databases//" + "accudrop")
            val currentDB = File(data, currentDBPath)

            val u = FileProvider.getUriForFile(this, "me.chrislane.accudrop.DbFileProvider", currentDB)
            val i = Intent(Intent.ACTION_SEND)
            i.type = "application/x-sqlite3"
            i.putExtra(Intent.EXTRA_EMAIL, arrayOf("chris@chrislane.com"))
            i.putExtra(Intent.EXTRA_SUBJECT, "AccuDrop Database File")
            i.putExtra(Intent.EXTRA_TEXT, "Date: " + Date().toString())
            i.putExtra(Intent.EXTRA_STREAM, u)
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(i, "Email:"))
        } catch (e: Exception) {
            Log.e(TAG, "emailDatabaseFile: ", e)
        }

    }

    /**
     * Handle navigation bar selections.
     *
     * @param item The selected item.
     * @return Whether the menu item was handled.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        val fragmentClass: Class<*>

        fragmentClass = when (id) {
            R.id.nav_jump -> JumpFragment::class.java
            R.id.nav_jump_stats -> JumpStatsFragment::class.java
            R.id.nav_landing_pattern -> PlanFragment::class.java
            R.id.nav_replay -> ReplayFragment::class.java
            R.id.nav_radar -> RadarFragment::class.java
            R.id.nav_share -> return false
            else -> return false
        }

        currentFragmentTag = fragmentClass.simpleName

        val fragmentManager = supportFragmentManager
        var fragment = fragmentManager.findFragmentByTag(currentFragmentTag)
        if (fragment == null) {
            try {
                fragment = fragmentClass.newInstance() as Fragment
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        fragmentManager.beginTransaction().replace(R.id.frame, fragment!!, currentFragmentTag).commit()

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     *
     * Set the current fragment to be displayed based on app state.
     *
     * If the app does not have a previously saved fragment tag, the default fragment will be
     * created and displayed, otherwise, the saved fragment will be displayed.
     */
    private fun setCurrentFragment() {
        val fragmentManager = supportFragmentManager
        val fragment: Fragment

        if (currentFragmentTag == null) {
            fragment = MainFragment()
            currentFragmentTag = MainFragment::class.java.simpleName
        } else {
            fragment = fragmentManager.findFragmentByTag(currentFragmentTag)!!
        }
        fragmentManager.beginTransaction()
                .replace(R.id.frame, fragment, currentFragmentTag)
                .commit()
    }

    /**
     * Initialise the app preferences.
     */
    private fun initPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.pref_canopy, true)
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, true)
        PreferenceManager.setDefaultValues(this, R.xml.pref_guidance, true)
        PreferenceManager.setDefaultValues(this, R.xml.pref_landing_pattern, true)
        val settings = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        if (!settings.contains("userUUID")) {
            settings.edit().putString("userUUID", UUID.randomUUID().toString()).apply()
        }
    }

    /**
     *
     * Handle a permissions request response.
     *
     * This will simply pass data to a permissions manager that checks whether the app should
     * request permissions again.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}