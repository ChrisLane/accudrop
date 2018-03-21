package me.chrislane.accudrop.fragment;


import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import me.chrislane.accudrop.R;
import me.chrislane.accudrop.Util;
import me.chrislane.accudrop.presenter.JumpStatsPresenter;

public class JumpStatsFragment extends Fragment {

    private JumpStatsPresenter presenter;
    private View view;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private Util.Unit unit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new JumpStatsPresenter(this);

        // Get unit preference
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String unitString = sharedPref.getString("general_unit", "");
        unit = Util.getUnit(unitString);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_jump_stats, container, false);

        // Set button listeners
        prevButton = view.findViewById(R.id.stats_prev_button);
        prevButton.setOnClickListener(v -> presenter.prevJump());
        nextButton = view.findViewById(R.id.stats_next_button);
        nextButton.setOnClickListener(v -> presenter.nextJump());

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
        time.setText(String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds));
    }

    public void updateCanopyDuration(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = (millis / 1000) % 60;

        TextView time = view.findViewById(R.id.canopy_duration_value);
        time.setText(String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds));
    }

    public void updateFreefallDuration(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = (millis / 1000) % 60;

        TextView time = view.findViewById(R.id.freefall_duration_value);
        time.setText(String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds));
    }

    public void updateCanopyVSpeed(double vSpeed) {
        String formatted = Util.getSpeedText(Util.getSpeedInUnit(vSpeed, unit), unit);
        TextView textView = view.findViewById(R.id.canopy_max_vspeed_value);
        textView.setText(formatted);
    }

    public void updateCanopyHSpeed(float hSpeed) {
        String formatted = Util.getSpeedText(Util.getSpeedInUnit(hSpeed, unit), unit);
        TextView textView = view.findViewById(R.id.canopy_max_hspeed_value);
        textView.setText(formatted);
    }

    public void updateFreefallVSpeed(double vSpeed) {
        String formatted = Util.getSpeedText(Util.getSpeedInUnit(vSpeed, unit), unit);
        TextView textView = view.findViewById(R.id.freefall_max_vspeed_value);
        textView.setText(formatted);
    }

    public void updateFreefallHSpeed(float hSpeed) {
        String formatted = Util.getSpeedText(Util.getSpeedInUnit(hSpeed, unit), unit);
        TextView textView = view.findViewById(R.id.freefall_max_hspeed_value);
        textView.setText(formatted);
    }

    public void updateButtons(int jumpId, int firstJumpId, int lastJumpId) {
        Drawable disabled = ContextCompat.getDrawable(requireContext(), R.drawable.ic_button_disabled);
        Drawable leftArrow = ContextCompat.getDrawable(requireContext(), R.drawable.ic_button_left_arrow);
        Drawable rightArrow = ContextCompat.getDrawable(requireContext(), R.drawable.ic_button_right_arrow);

        // Check limits for previous button
        if (jumpId <= firstJumpId) {
            prevButton.setImageDrawable(disabled);
            prevButton.setEnabled(false);
        } else {
            prevButton.setImageDrawable(leftArrow);
            prevButton.setEnabled(true);
        }

        // Check limits for next button
        if (jumpId >= lastJumpId) {
            nextButton.setImageDrawable(disabled);
            nextButton.setEnabled(false);
        } else {
            nextButton.setImageDrawable(rightArrow);
            nextButton.setEnabled(true);
        }
    }
}
