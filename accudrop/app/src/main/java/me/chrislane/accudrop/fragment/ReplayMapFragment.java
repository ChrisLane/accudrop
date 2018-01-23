package me.chrislane.accudrop.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.Point3D;
import me.chrislane.accudrop.R;
import me.chrislane.accudrop.presenter.ReplayMapPresenter;
import me.chrislane.accudrop.viewmodel.JumpViewModel;

public class ReplayMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = ReplayMapFragment.class.getSimpleName();
    private JumpViewModel jumpViewModel;
    private CameraPosition.Builder camPosBuilder;
    private GoogleMap map;
    private ReplayMapPresenter replayMapPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this as a location listener
        if (savedInstanceState == null) {
            MainActivity main = (MainActivity) getActivity();
            if (main != null) {
                jumpViewModel = ViewModelProviders.of(main).get(JumpViewModel.class);
            }
        }

        replayMapPresenter = new ReplayMapPresenter(this, jumpViewModel);

        camPosBuilder = new CameraPosition.Builder()
                .zoom(15.5f)
                .bearing(0)
                .tilt(0);

        Fragment parentFragment = getParentFragment();
        if (parentFragment != null) {
            SupportMapFragment mapFragment = (SupportMapFragment) parentFragment
                    .getChildFragmentManager().findFragmentById(R.id.replay_map);
            mapFragment.getMapAsync(this);
        }
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
    }

    /**
     * Set up the GoogleMap with initial settings and get last jump data.
     */
    private void setupMap() {
        // Set map settings
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setBuildingsEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Get target location and set as camera position
        replayMapPresenter.getLastJumpPoints();
    }


    /**
     * Place a jump route on the map.
     *
     * @param points Points visited during the jump.
     */
    public void setPoints(List<Point3D> points) {
        map.clear();

        if (points != null && points.size() > 0) {
            Log.d(TAG, "Setting points");
            LatLng lastPos = points.get(points.size() - 1).getLatLng();
            Log.d(TAG, lastPos.toString());
            CameraPosition camPos = camPosBuilder.target(lastPos).build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));

            for (int i = 0; i < points.size() - 1; i++) {
                Point3D point1 = points.get(i);
                Point3D point2 = points.get(i + 1);

                map.addPolyline(new PolylineOptions()
                        .add(point1.getLatLng(), point2.getLatLng())
                        .width(5)
                        .color(Color.RED));
            }
        } else {
            Log.e(TAG, "No points available for this jump.");
        }
    }
}
