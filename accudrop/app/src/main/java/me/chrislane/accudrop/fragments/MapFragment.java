package me.chrislane.accudrop.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import me.chrislane.accudrop.LocationViewModel;
import me.chrislane.accudrop.R;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private LocationViewModel locationViewModel;
    private CameraPosition.Builder camPosBuilder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        camPosBuilder = new CameraPosition.Builder()
                .zoom(15.5f)
                .bearing(0)
                .tilt(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Set up the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Add this as a location listener
        if (savedInstanceState == null) {
            locationViewModel = ViewModelProviders.of(getActivity()).get(LocationViewModel.class);
            subscribe();
        }

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
        Log.d("MapFragment", "Map ready");
        map = googleMap;

        setupMap(map);
    }

    public void setupMap(GoogleMap map) {
        Location loc = locationViewModel.getLastLocation().getValue();
        CameraPosition camPos = camPosBuilder.target(locationViewModel.getLatLng(loc)).build();

        // Initial map setup
        map.setBuildingsEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));

        //TODO : Remove example line drawing
        Polyline downWind = map.addPolyline(new PolylineOptions()
                .add(new LatLng(51.5, -0.1), new LatLng(40.7, -74.0))
                .width(5)
                .color(Color.RED));
    }

    private void subscribe() {
        final Observer<Location> locationObserver = new Observer<Location>() {
            @Override
            public void onChanged(@Nullable final Location location) {
                if (map != null) {
                    CameraPosition camPos = camPosBuilder.target(locationViewModel.getLatLng(location)).build();
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));
                }
            }
        };

        locationViewModel.getLastLocation().observe(this, locationObserver);
    }
}
