package me.chrislane.accudrop.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import me.chrislane.accudrop.R;
import me.chrislane.accudrop.presenter.JumpStatsPresenter;

public class JumpStatsFragment extends Fragment {

    private JumpStatsPresenter presenter;
    private View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new JumpStatsPresenter(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_jump_stats, container, false);
        return view;
    }

    public void updateJumpId(int jumpId) {
        TextView number = view.findViewById(R.id.jump_number);
        number.setText(String.format(Locale.ENGLISH, "%d", jumpId));
    }

    public void updateTotalDuration(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = (millis / 1000) % 60;

        TextView time = view.findViewById(R.id.total_duration_value);
        time.setText(String.format(Locale.ENGLISH, "%d:%d", minutes, seconds));
    }

    public void updateCanopyDuration(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = (millis / 1000) % 60;

        TextView time = view.findViewById(R.id.canopy_duration_value);
        time.setText(String.format(Locale.ENGLISH, "%d:%d", minutes, seconds));
    }

    public void updateFreefallDuration(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = (millis / 1000) % 60;

        TextView time = view.findViewById(R.id.freefall_duration_value);
        time.setText(String.format(Locale.ENGLISH, "%d:%d", minutes, seconds));
    }

    public void updateCanopyVSpeed(double vSpeed) {
        int speed = (int) vSpeed;
        TextView textView = view.findViewById(R.id.canopy_max_vspeed_value);
        textView.setText(String.format(Locale.ENGLISH, "%d", speed));
    }

    public void updateCanopyHSpeed(float hSpeed) {
        int speed = (int) hSpeed;
        TextView textView = view.findViewById(R.id.canopy_max_hspeed_value);
        textView.setText(String.format(Locale.ENGLISH, "%d", speed));
    }

    public void updateFreefallVSpeed(double vSpeed) {
        int speed = (int) vSpeed;
        TextView textView = view.findViewById(R.id.freefall_max_vspeed_value);
        textView.setText(String.format(Locale.ENGLISH, "%d", speed));
    }

    public void updateFreefallHSpeed(float hSpeed) {
        int speed = (int) hSpeed;
        TextView textView = view.findViewById(R.id.freefall_max_hspeed_value);
        textView.setText(String.format(Locale.ENGLISH, "%d", speed));
    }
}
