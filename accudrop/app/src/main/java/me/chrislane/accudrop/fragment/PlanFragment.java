package me.chrislane.accudrop.fragment;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.PermissionManager;
import me.chrislane.accudrop.Point3D;
import me.chrislane.accudrop.R;
import me.chrislane.accudrop.Util;
import me.chrislane.accudrop.presenter.PlanPresenter;
import me.chrislane.accudrop.viewmodel.GnssViewModel;
import me.chrislane.accudrop.viewmodel.RouteViewModel;

public class PlanFragment extends Fragment implements LifecycleOwner, OnMapReadyCallback {

    private static final String TAG = PlanFragment.class.getSimpleName();
    private GoogleMap map;
    private GnssViewModel gnssViewModel;
    private CameraPosition.Builder camPosBuilder;
    private RouteViewModel routeViewModel;
    private PlanPresenter planPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // Add this as a location listener
        if (savedInstanceState == null) {
            MainActivity main = (MainActivity) getActivity();
            if (main != null) {
                gnssViewModel = ViewModelProviders.of(main).get(GnssViewModel.class);
                routeViewModel = ViewModelProviders.of(main).get(RouteViewModel.class);
            }
        }

        camPosBuilder = new CameraPosition.Builder()
                .zoom(15.5f)
                .bearing(0)
                .tilt(0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plan, container, false);

        // Set up the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.plan_map_fragment);
        mapFragment.getMapAsync(this);

        return view;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map ready");
        map = googleMap;

        setupMap();
        subscribeToRoute();

        LatLng target = gnssViewModel.getLatLng(gnssViewModel.getLastLocation().getValue());
        if (target != null) {
            planPresenter = new PlanPresenter(this, target);
        } else {
            planPresenter = new PlanPresenter(this);
        }
    }

    /**
     * Set up the GoogleMap with initial settings and location.
     */
    private void setupMap() {
        MainActivity main = (MainActivity) getActivity();
        if (main != null) {
            PermissionManager permissionManager = main.getPermissionManager();

            // Initial map setup
            if (permissionManager.checkLocationPermission()) {
                map.setMyLocationEnabled(true);
            } else {
                permissionManager.requestLocationPermission("Location access is required to find your location.");
            }
        }

        Location loc = gnssViewModel.getLastLocation().getValue();
        if (loc != null) {
            CameraPosition camPos = camPosBuilder.target(gnssViewModel.getLatLng(loc)).build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));
        }
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setBuildingsEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.setOnMapLongClickListener(this::onMapLongClick);
    }

    private void onMapLongClick(LatLng latLng) {
        // Update map camera position
        CameraPosition camPos = camPosBuilder.target(latLng).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));

        // Update route
        planPresenter.calcRoute(latLng);
    }

    private void subscribeToRoute() {
        final Observer<List<Point3D>> routeObserver = route -> {
            if (route != null) {
                map.clear();

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                String unitString = sharedPref.getString("pref_unit", "");

                Util.Unit unit = Util.getUnit(unitString);
                for (int i = 0; i < route.size() - 1; i++) {
                    Point3D point1 = route.get(i);
                    Point3D point2 = route.get(i + 1);
                    map.addPolyline(new PolylineOptions()
                            .add(point1.getLatLng(), point2.getLatLng())
                            .width(5)
                            .color(Color.RED));
                    map.addMarker(new MarkerOptions()
                            .position(point1.getLatLng())
                            .title(Util.getAltitudeText(point1.getAltitude(), unit))
                    );
                }

                map.addMarker(new MarkerOptions()
                        .position(route.get(route.size() - 1).getLatLng())
                        .title("Landing"));
            }
        };

        routeViewModel.getRoute().observe(this, routeObserver);
    }

    public void setProgressBarVisibility(boolean showBar) {
        ProgressBar progressBar = getView().findViewById(R.id.progressbar);
        if (showBar) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
}
