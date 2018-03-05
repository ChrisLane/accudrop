package me.chrislane.accudrop.fragment;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

import me.chrislane.accudrop.R;
import me.chrislane.accudrop.Util;
import me.chrislane.accudrop.presenter.RadarPresenter;

public class RadarFragment extends Fragment {

    private static final String TAG = RadarFragment.class.getSimpleName();
    private RadarPresenter presenter;
    private Radar radar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new RadarPresenter(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_radar, container, false);
        FrameLayout layout = view.findViewById(R.id.radar);
        radar = new Radar(getContext());
        layout.addView(radar);

        SeekBar seekBar = view.findViewById(R.id.radar_seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBarChangeListener());

        return view;
    }

    public void updateRadarPoints() {
        radar.invalidate();
    }

    private List<PointF> getScaledPositions(int width, int height, float shrink) {
        int maxHDistance = presenter.getMaxHDistance();
        List<Pair<Float, Float>> positions = presenter.getPositions(); // bearing, distance
        List<PointF> result = new ArrayList<>();

        for (Pair<Float, Float> position : positions) {
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

        return result;
    }

    private List<Double> getScaledAltitudes(int height) {
        int maxVDistance = presenter.getMaxVDistance();
        List<Double> heightDiffs = presenter.getHeightDiffs();
        List<Double> result = new ArrayList<>();

        for (Double heightDiff : heightDiffs) {
            // Scale the vertical distance with the screen height
            double scaledDiff = Util.getScaledValue(heightDiff, 0, maxVDistance, 0, height / 2f);
            Log.v(TAG, "Scaled V Distance: " + scaledDiff);
            result.add(scaledDiff);
        }

        return result;
    }

    private static PointF applyShrink(PointF point, float shrink) {
        // TODO: Scale to ellipse, not just height change in ellipse.
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

    public class Radar extends View {
        private final String TAG = Radar.class.getSimpleName();
        Paint paint = new Paint();
        RectF oval = new RectF();

        public Radar(Context context) {
            super(context);
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
            paint.setColor(Color.RED);
            for (int i = 0; i < positions.size(); i++) {
                PointF position = positions.get(i);
                Log.v(TAG, "Width: " + width + ", Height: " + height);
                Log.v(TAG, "Adding Pos: " + position);

                // Draw line from original x,y to height adjusted x,y
                float newY = (float) (position.y + heightDiffs.get(i));
                paint.setColor(Color.BLACK);
                canvas.drawLine(width - position.x, height - position.y,
                        width - position.x, height - newY, paint);

                // Draw circle
                paint.setColor(Color.RED);
                canvas.drawCircle(width - position.x, height - newY,
                        (width + height) * 0.003f, paint);
            }
        }
    }

    class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            List<Location> subjectLocs = presenter.getSubjectLocations();

            if (subjectLocs != null) {
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