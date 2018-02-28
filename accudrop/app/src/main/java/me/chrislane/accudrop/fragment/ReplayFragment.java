package me.chrislane.accudrop.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Locale;

import me.chrislane.accudrop.R;
import me.chrislane.accudrop.presenter.ReplayPresenter;

public class ReplayFragment extends Fragment {

    private ReplayMapFragment replayMap;
    private ReplaySideViewFragment replaySideView;
    private ReplayPresenter presenter;
    private Button prevButton;
    private Button nextbtn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new ReplayPresenter(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        replayMap = new ReplayMapFragment();
        replaySideView = new ReplaySideViewFragment();
        transaction.add(R.id.replay_map_fragment, replayMap);
        transaction.add(R.id.replay_side_view_fragment, replaySideView).commit();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_replay, container, false);

        prevButton = view.findViewById(R.id.replay_back_button);
        prevButton.setOnClickListener(v -> presenter.prevJump());

        nextbtn = view.findViewById(R.id.replay_forward_button);
        nextbtn.setOnClickListener(v -> presenter.nextJump());

        return view;
    }

    /**
     * Get the replay map fragment.
     *
     * @return The replay map fragment.
     */
    public ReplayMapFragment getReplayMap() {
        return replayMap;
    }

    /**
     * Get the replay side view fragment.
     *
     * @return The replay side view fragment.
     */
    public ReplaySideViewFragment getReplaySideView() {
        return replaySideView;
    }

    public void updateButtons(int jumpId, int firstJumpId, int lastJumpId) {
        // Check limits for previous button
        if (jumpId <= firstJumpId) {
            prevButton.setText("❌");
            prevButton.setEnabled(false);
        } else {
            prevButton.setText(String.format(Locale.ENGLISH, "❮ %d", jumpId - 1));
            prevButton.setEnabled(true);
        }

        // Check limits for next button
        if (jumpId >= lastJumpId) {
            nextbtn.setText("❌");
            nextbtn.setEnabled(false);
        } else {
            nextbtn.setEnabled(true);
            nextbtn.setText(String.format(Locale.ENGLISH, "%d ❯", jumpId + 1));
        }
    }
}
