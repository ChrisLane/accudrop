package me.chrislane.accudrop;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.chrislane.accudrop.db.Position;
import me.chrislane.accudrop.fragment.JumpFragment;
import me.chrislane.accudrop.fragment.MainFragment;
import me.chrislane.accudrop.fragment.PlanFragment;
import me.chrislane.accudrop.fragment.ReplayFragment;
import me.chrislane.accudrop.task.CreateAndInsertJumpTask;
import me.chrislane.accudrop.task.InsertJumpTask;
import me.chrislane.accudrop.task.RouteTask;
import me.chrislane.accudrop.task.WindTask;
import me.chrislane.accudrop.viewmodel.GnssViewModel;
import me.chrislane.accudrop.viewmodel.JumpViewModel;
import me.chrislane.accudrop.viewmodel.PressureViewModel;
import me.chrislane.accudrop.viewmodel.RouteViewModel;
import me.chrislane.accudrop.viewmodel.WindViewModel;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextToSpeech tts;
    private String currentFragmentTag = null;
    private PermissionManager permissionManager;
    private JumpViewModel jumpViewModel;
    private int jumpId;

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
        jumpViewModel = ViewModelProviders.of(this).get(JumpViewModel.class);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.text_to_speech_test:
                // TODO: Remove example code after demonstration
                tts = new TextToSpeech(this, status -> {
                    if (status == TextToSpeech.SUCCESS) {
                        tts.setLanguage(Locale.UK);
                        tts.speak("At 500 feet, turn upwind", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                });
                return true;
            case R.id.generate_jump:
                calcRoute(new LatLng(51.52, 0.08));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Calculate a route and add it as a jump.
     *
     * @param target The target landing coordinates.
     */
    private void calcRoute(LatLng target) {
        // Run a route calculation task with the updated wind
        RouteTask.RouteTaskListener routeListener = this::addJump;
        new RouteTask(routeListener, new WindTask.WindTuple(0, 0)).execute(target);
    }

    private void addJump(List<Point3D> route) {
        CreateAndInsertJumpTask.Listener createListener = result -> jumpId = result;
        InsertJumpTask.Listener insertListener = () -> addPositions(jumpId, route);
        new CreateAndInsertJumpTask(this, createListener, insertListener).execute();
    }

    private void addPositions(int jumpId, List<Point3D> route) {
        for (Point3D point : route) {
            Position pos = new Position();
            pos.latitude = point.getLatLng().latitude;
            pos.longitude = point.getLatLng().longitude;
            pos.altitude = (int) point.getAltitude();
            pos.time = new Date();
            pos.jumpId = jumpId;

            String msg = String.format(Locale.ENGLISH, "Inserting position:\n" +
                    "\tJump ID: %d" +
                    "\t(Lat, Long): (%f,%f)\n" +
                    "\tAltitude: %d\n" +
                    "\tTime: %s", pos.jumpId, pos.latitude, pos.longitude, pos.altitude, pos.time);
            Log.d(TAG, msg);

            AsyncTask.execute(() -> jumpViewModel.addPosition(pos));
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment;
        Class<?> fragmentClass = PlanFragment.class;

        switch (id) {
            case R.id.nav_jump:
                fragmentClass = JumpFragment.class;
                break;
            case R.id.nav_landing_pattern:
                fragmentClass = PlanFragment.class;
                break;
            case R.id.nav_replay:
                fragmentClass = ReplayFragment.class;
                break;
            case R.id.nav_share:
                break;
            default:
                break;
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
     * Set the current fragment to be displayed based on app state.
     * If the app does not have a previously saved fragment tag, the default fragment will be created and displayed,
     * otherwise, the saved fragment will be displayed.
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

    private void initPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}