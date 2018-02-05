package me.chrislane.accudrop;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

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
        int randSpeed = ThreadLocalRandom.current().nextInt(0, 10);
        int randDir = ThreadLocalRandom.current().nextInt(0, 360);
        new RouteTask(routeListener, new WindTask.WindTuple(randSpeed, randDir)).execute(target);
    }

    /**
     * Create a new a jump and add a route's positions to it.
     *
     * @param route The route containing jump positions.
     */
    private void addJump(List<Location> route) {
        List<Location> finalRoute = addIntermediaryPoints(route);

        CreateAndInsertJumpTask.Listener createListener = result -> jumpId = result;
        InsertJumpTask.Listener insertListener = () -> addPositions(jumpId, finalRoute);
        new CreateAndInsertJumpTask(this, createListener, insertListener).execute();
    }

    private List<Location> addIntermediaryPoints(List<Location> route) {
        List<Location> result = new ArrayList<>();

        for (int i = 0; i < route.size() - 1; i++) {
            Location loc1 = route.get(i);
            Location loc2 = route.get(i + 1);
            double totalDistance = loc1.distanceTo(loc2);
            double altitude = loc1.getAltitude();
            int split = (int) (totalDistance / 5);
            double altitudeDec = (altitude - loc2.getAltitude()) / split;

            result.add(loc1);
            LatLng prevPos = GnssViewModel.getLatLng(loc1);
            Location prevLoc = loc1;
            for (int j = 0; j <= split; j++) {
                double bearing = prevLoc.bearingTo(loc2);

                int randBear = ThreadLocalRandom.current().nextInt(-15, 15);
                prevLoc = new Location("");
                prevPos = RouteCalculator.getPosAfterMove(prevPos, 5, bearing + randBear);
                prevLoc.setLatitude(prevPos.latitude);
                prevLoc.setLongitude(prevPos.longitude);
                altitude -= altitudeDec;
                prevLoc.setAltitude(altitude);

                result.add(prevLoc);
            }
        }

        return result;
    }

    /**
     * Add positions in a route to a jump.
     *
     * @param jumpId The jump ID to add positions for.
     * @param route  The route containing positions.
     */
    private void addPositions(int jumpId, List<Location> route) {
        for (Location location : route) {
            Position pos = new Position();
            pos.latitude = location.getLatitude();
            pos.longitude = location.getLongitude();
            pos.altitude = (int) location.getAltitude();
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
            case R.id.nav_landing_pattern:
                fragmentClass = PlanFragment.class;
                break;
            case R.id.nav_replay:
                fragmentClass = ReplayFragment.class;
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
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
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