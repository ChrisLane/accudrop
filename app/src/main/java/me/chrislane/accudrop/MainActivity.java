package me.chrislane.accudrop;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import me.chrislane.accudrop.db.AccudropDb;
import me.chrislane.accudrop.fragment.JumpFragment;
import me.chrislane.accudrop.fragment.JumpStatsFragment;
import me.chrislane.accudrop.fragment.MainFragment;
import me.chrislane.accudrop.fragment.PlanFragment;
import me.chrislane.accudrop.fragment.RadarFragment;
import me.chrislane.accudrop.fragment.ReplayFragment;
import me.chrislane.accudrop.generator.JumpGenerator;
import me.chrislane.accudrop.preference.SettingsActivity;
import me.chrislane.accudrop.viewmodel.DatabaseViewModel;
import me.chrislane.accudrop.viewmodel.GnssViewModel;
import me.chrislane.accudrop.viewmodel.PressureViewModel;
import me.chrislane.accudrop.viewmodel.RouteViewModel;
import me.chrislane.accudrop.viewmodel.WindViewModel;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextToSpeech tts;
    private String currentFragmentTag = null;
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissionManager = new PermissionManager(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString("currentFragmentTag");
        }

        // Check that the device has a barometer.
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null) {
                // No barometer, do not continue.
                // TODO: Change to a more appropriate notification and close app
                Toast.makeText(this, "No barometer in device.", Toast.LENGTH_SHORT).show();
                //return;
            }
        }

        // Initialise preferences
        initPreferences();

        // Create or get ViewModels
        ViewModelProviders.of(this).get(PressureViewModel.class);
        ViewModelProviders.of(this).get(GnssViewModel.class);
        ViewModelProviders.of(this).get(DatabaseViewModel.class);
        ViewModelProviders.of(this).get(RouteViewModel.class);
        ViewModelProviders.of(this).get(WindViewModel.class);

        // Set the fragment to be displayed
        setCurrentFragment();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentFragmentTag", currentFragmentTag);
    }

    /**
     * Handle the user pressing the back button.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Handle options items selection.
     *
     * @param item The options item to handle.
     * @return Whether the item was handled or not.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.email_database_file:
                emailDatabaseFile();
                return true;
            case R.id.generate_jump:
                new JumpGenerator(this).generateJump(new LatLng(51.52, 0.08), 0);
                return true;
            case R.id.generate_guest_jump:
                new JumpGenerator(this).generateJump(new LatLng(51.52, 0.08), 9);
                return true;
            case R.id.clear_database:
                AccudropDb.clearDatabase(this);
                Toast.makeText(this, "Restart app to clear DB.", Toast.LENGTH_SHORT)
                        .show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void emailDatabaseFile() {
        try {
            File data = Environment.getDataDirectory();

            String currentDBPath = "//data//" + "me.chrislane.accudrop"
                    + "//databases//" + "accudrop";
            File currentDB = new File(data, currentDBPath);

            Uri U = FileProvider.getUriForFile(this,
                    "me.chrislane.accudrop.DbFileProvider", currentDB);
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("application/x-sqlite3");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{"chris@chrislane.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, "AccuDrop Database File");
            i.putExtra(Intent.EXTRA_TEXT, "Date: " + new Date().toString());
            i.putExtra(Intent.EXTRA_STREAM, U);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(i, "Email:"));
        } catch (Exception e) {
            Log.e(TAG, "emailDatabaseFile: ", e);
        }
    }

    /**
     * Handle navigation bar selections.
     *
     * @param item The selected item.
     * @return Whether the menu item was handled.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment;
        Class<?> fragmentClass;

        switch (id) {
            case R.id.nav_jump:
                fragmentClass = JumpFragment.class;
                break;
            case R.id.nav_jump_stats:
                fragmentClass = JumpStatsFragment.class;
                break;
            case R.id.nav_landing_pattern:
                fragmentClass = PlanFragment.class;
                break;
            case R.id.nav_replay:
                fragmentClass = ReplayFragment.class;
                break;
            case R.id.nav_radar:
                fragmentClass = RadarFragment.class;
                break;
            case R.id.nav_share:
                return false;
            default:
                return false;
        }

        currentFragmentTag = fragmentClass.getSimpleName();

        FragmentManager fragmentManager = getSupportFragmentManager();
        if ((fragment = fragmentManager.findFragmentByTag(currentFragmentTag)) == null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        fragmentManager.beginTransaction().replace(R.id.frame, fragment, currentFragmentTag).commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * <p>Set the current fragment to be displayed based on app state.</p>
     * <p>If the app does not have a previously saved fragment tag, the default fragment will be
     * created and displayed, otherwise, the saved fragment will be displayed.</p>
     */
    private void setCurrentFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;

        if (currentFragmentTag == null) {
            fragment = new MainFragment();
            currentFragmentTag = MainFragment.TAG;
        } else {
            fragment = fragmentManager.findFragmentByTag(currentFragmentTag);
        }
        fragmentManager.beginTransaction()
                .replace(R.id.frame, fragment, currentFragmentTag)
                .commit();
    }

    /**
     * Initialise the app preferences.
     */
    private void initPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.pref_canopy, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_guidance, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_landing_pattern, true);
        SharedPreferences settings = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        if (!settings.contains("userUUID")) {
            settings.edit().putString("userUUID", UUID.randomUUID().toString()).apply();
        }
    }

    /**
     * Get the permission manager.
     *
     * @return Permission manager.
     */
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    /**
     * <p>Handle a permissions request response.</p>
     * <p>This will simply pass data to a permissions manager that checks whether the app should
     * request permissions again.</p>
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}