package me.chrislane.accudrop.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.chrislane.accudrop.R;

public class ReplayFragment extends Fragment {
    private ReplayMapFragment replayMap;
    private ReplaySideViewFragment replaySideView;

    public ReplayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        replayMap = new ReplayMapFragment();
        replaySideView = new ReplaySideViewFragment();
        transaction.add(R.id.replay_map_fragment, replayMap);
        transaction.add(R.id.replay_side_view_fragment, replaySideView).commit();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_replay, container, false);
    }
}
