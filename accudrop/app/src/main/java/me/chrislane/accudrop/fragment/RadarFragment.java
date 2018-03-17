package me.chrislane.accudrop.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import me.chrislane.accudrop.BuildConfig;
import me.chrislane.accudrop.R;
import me.chrislane.accudrop.Util;
import me.chrislane.accudrop.presenter.RadarPresenter;
import me.chrislane.accudrop.viewmodel.RadarViewModel;

public class RadarFragment extends Fragment {

    private static final String TAG = RadarFragment.class.getSimpleName();
    private RadarPresenter presenter;
    private RadarView radarView;
    private RadarViewModel radarViewModel;
    private Button nextbtn;
    private Button prevButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new RadarPresenter(this);
        radarViewModel = ViewModelProviders.of(this).get(RadarViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_radar, container, false);
        FrameLayout layout = view.findViewById(R.id.radar);
        radarView = new RadarView(getContext());
        layout.addView(radarView);

        prevButton = view.findViewById(R.id.radar_prev_button);
        prevButton.setOnClickListener(v -> presenter.prevJump());

        nextbtn = view.findViewById(R.id.radar_next_button);
        nextbtn.setOnClickListener(v -> presenter.nextJump());

        SeekBar seekBar = view.findViewById(R.id.radar_seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBarChangeListener());

        return view;
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

    public void updateRadarPoints() {
        radarView.invalidate();
    }

    private List<PointF> getScaledPositions(int width, int height, float shrink) {
        int maxHDistance = presenter.getMaxHDistance();
        List<Pair<Float, Float>> relativePositions = radarViewModel.getRelativeGuestPositions().getValue(); // bearing, distance
        List<PointF> result = new ArrayList<>();

        if (relativePositions != null) {
            for (Pair<Float, Float> position : relativePositions) {
                if (position.first != null && position.second != null) {
                    // Scale distance to ellipse width
                    double scaledDistance = Util.getScaledValue(position.second, 0, maxHDistance, 0, width / 2f);
                    Log.v(TAG, "Scaled H Distance: " + scaledDistance);
                    // Calculate cartesian coordinate
                    PointF newPoint = calculateCartesian(position.first, scaledDistance);
                    Log.v(TAG, "After Cartesian: " + newPoint);
                    // Apply shrink to stay within ellipse
                    newPoint = applyShrink(newPoint, shrink);
                    Log.v(TAG, "After Shrink: " + newPoint);
                    // Move position to be relative to the centre of the screen
                    newPoint = calculateRelative(newPoint, new PointF(width / 2, height / 2));
                    Log.v(TAG, "After Relative: " + newPoint);

                    // Add to the result
                    result.add(newPoint);
                }
            }
        }

        return result;
    }

    private List<Double> getScaledAltitudes(int height) {
        int maxVDistance = presenter.getMaxVDistance();
        List<Double> guestHeightDiffs = radarViewModel.getGuestHeightDiffs().getValue();
        List<Double> result = new ArrayList<>();

        if (guestHeightDiffs != null) {
            for (Double heightDiff : guestHeightDiffs) {
                // Scale the vertical distance with the screen height
                double scaledDiff = Util.getScaledValue(heightDiff, -maxVDistance, maxVDistance,
                        -(height / 2f), height / 2f);
                Log.v(TAG, "Scaled V Distance: " + scaledDiff);
                result.add(scaledDiff);
            }
        }

        return result;
    }

    private static PointF applyShrink(PointF point, float shrink) {
        // TODO #50: Scale to ellipse, not just height change in ellipse.
        return new PointF(point.x, point.y * shrink);
    }

    private static PointF calculateCartesian(Float bearing, double scaledDistance) {
        double rBearing = Math.toRadians(bearing);
        float x = (float) (scaledDistance * Math.sin(rBearing));
        float y = (float) (scaledDistance * Math.cos(rBearing));
        return new PointF(x, y);
    }

    private static PointF calculateRelative(PointF point, PointF subject) {
        float x = subject.x - point.x;
        float y = subject.y + point.y;
        return new PointF(x, y);
    }

    public class RadarView extends View {
        private final String TAG = RadarView.class.getSimpleName();
        Paint paint = new Paint();
        RectF oval = new RectF();
        List<Pair<UUID, PointF>> points = new ArrayList<>();

        public RadarView(Context context) {
            super(context);
        }

        @Override
        public boolean performClick() {
            return super.performClick();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);

            if (event.getAction() != MotionEvent.ACTION_DOWN) {
                return false;
            }
            // We're supposed to call performClick for accessibility reasons
            performClick();

            float xArea = 25;
            float yArea = 25;

            Pair<UUID, PointF> closestPoint = null;
            float bestScore = xArea + yArea;
            for (Pair<UUID, PointF> userPoint : points) {
                PointF point = userPoint.second;

                if (point != null) {
                    float xDistance = Math.abs(point.x - event.getX());
                    float yDistance = Math.abs(point.y - event.getY());
                    Log.v(TAG, "Distances: " + xDistance + ", " + yDistance);
                    if (xDistance < xArea && yDistance < yArea) {
                        float score = xDistance + yDistance;
                        if (score < bestScore) {
                            bestScore = score;
                            closestPoint = userPoint;
                        }
                    }
                }
            }
            if (closestPoint != null) {
                Log.v(TAG, "Setting subject to " + closestPoint.first);
                radarViewModel.setSubject(closestPoint.first);
                return true;
            }

            return false;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            float halfHeight = canvas.getHeight() / 2f;
            float halfWidth = width / 2f;
            float shrink = 0.25f;
            float heightMove = halfWidth * shrink;

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            oval.set(0, halfHeight - heightMove, width, halfHeight + heightMove);
            canvas.drawOval(oval, paint);

            oval.inset(halfWidth / 2, heightMove / 2);
            canvas.drawOval(oval, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLUE);
            canvas.drawCircle(halfWidth, halfHeight, (width + height) * 0.003f, paint);

            List<PointF> positions = getScaledPositions(width, height, shrink);
            List<Double> heightDiffs = getScaledAltitudes(height);
            List<UUID> guestsInView = radarViewModel.getGuestsInView().getValue();
            if (guestsInView == null) {
                return;
            }
            paint.setColor(Color.RED);

            points.clear();
            for (int i = 0; i < positions.size() && i < heightDiffs.size() && i < guestsInView.size(); i++) {
                PointF position = positions.get(i);

                if (BuildConfig.DEBUG) {
                    Log.v(TAG, "Width: " + width + ", Height: " + height);
                    Log.v(TAG, "Adding Pos: " + position);
                }

                // Draw line from original x,y to height adjusted x,y
                float newY = (float) (position.y + heightDiffs.get(i));
                paint.setColor(Color.BLACK);
                canvas.drawLine(width - position.x, height - position.y,
                        width - position.x, height - newY, paint);

                // Draw circle
                paint.setColor(Color.RED);
                float pointX = width - position.x;
                float pointY = height - newY;
                canvas.drawCircle(pointX, pointY, (width + height) * 0.003f, paint);

                // Store user and circle position
                points.add(Pair.create(guestsInView.get(i), new PointF(pointX, pointY)));
            }
        }
    }

    class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Pair<UUID, List<Location>> subjectEntry = radarViewModel.getSubjectEntry().getValue();
            if (subjectEntry == null) {
                return;
            }
            List<Location> subjectLocs = subjectEntry.second;

            if (subjectLocs != null && !subjectLocs.isEmpty()) {
                int newIndex = (int) Util.getScaledValue(progress, 0, 100,
                        0, subjectLocs.size() - 1);

                long newTime = subjectLocs.get(newIndex).getTime();
                presenter.updateTime(newTime);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
}