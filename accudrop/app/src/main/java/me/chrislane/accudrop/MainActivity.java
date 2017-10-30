package me.chrislane.accudrop;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.common.api.GoogleApiClient;
import me.chrislane.accudrop.fragments.MainFragment;
import me.chrislane.accudrop.fragments.MapFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private LocationViewModel locationViewModel;
    private String currentFragmentTag = "";
    private static final String MAIN_FRAGMENT_TAG = "main_fragment";
    private static final String MAP_FRAGMENT_TAG = "map_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
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

        // Set home fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;

        if (savedInstanceState == null) {
        }
            GoogleApiClient googleApiClient = new ApiClient(this).getGoogleApiClient();
            locationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);


        if (savedInstanceState == null) {
            fragment = new MainFragment();
            currentFragmentTag = MAIN_FRAGMENT_TAG;
        } else {
            currentFragmentTag = savedInstanceState.getString("currentFragmentTag");
            fragment = fragmentManager.findFragmentByTag(currentFragmentTag);
        }
            fragmentManager.beginTransaction().replace(R.id.frame, fragment, currentFragmentTag).commit();
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

    TextToSpeech tts;

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
                tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            tts.setLanguage(Locale.UK);
                            tts.speak("At 500 feet, turn upwind", TextToSpeech.QUEUE_FLUSH, null, null);

                        }
                    }
                });
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment;
        Class fragmentClass = MapFragment.class;

        switch (id) {
            case R.id.nav_jump:
                // Handle the jump action
                break;
            case R.id.nav_landing_pattern:
                fragmentClass = MapFragment.class;
                currentFragmentTag = MAP_FRAGMENT_TAG;
                break;
            case R.id.nav_share:
                break;
            default:
                break;
        }


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

    public LocationViewModel getLocationViewModel() {
        return locationViewModel;
    }
}
