package me.chrislane.accudrop.fragment;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import java.util.List;

import me.chrislane.accudrop.R;
import me.chrislane.accudrop.presenter.ReplaySideViewPresenter;

public class ReplaySideViewFragment extends Fragment {

    private static final String TAG = ReplaySideViewFragment.class.getSimpleName();
    private ReplaySideViewPresenter presenter;
    private List<List<PointF>> screenPointsList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new ReplaySideViewPresenter(this);
    }

    /**
     * Redraw the side view lines.
     */
    public void updateDrawable(boolean generatePoints) {
        Activity activity = requireActivity();
        SideViewDrawable drawable = new SideViewDrawable(generatePoints);
        ImageView drawBox = activity.findViewById(R.id.replay_draw_area);
        drawBox.setImageDrawable(drawable);
    }

    public void setScreenPointsList(List<List<PointF>> screenPoints) {
        this.screenPointsList = screenPoints;
        updateDrawable(false);
    }

    /**
     * This drawable creates lines between points generates by the <code>ReplaySideViewPresenter</code>.
     */
    private class SideViewDrawable extends Drawable {

        private final boolean generatePoints;

        public SideViewDrawable(boolean generatePoints) {
            this.generatePoints = generatePoints;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            // Paint the ground
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            canvas.drawRect(0, (float) (canvas.getHeight() * 0.95), canvas.getWidth(), canvas.getHeight(), paint);

            if (generatePoints) {
                presenter.produceViewPositions(canvas.getWidth(), canvas.getHeight(), 20);
            }

            if (screenPointsList != null) {
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(10);

                Path path = new Path();
                for (List<PointF> screenPoints : screenPointsList) {
                    if (!screenPoints.isEmpty()) {
                        path.moveTo(screenPoints.get(0).x, canvas.getHeight() - screenPoints.get(0).y);
                        for (PointF point : screenPoints) {
                            path.lineTo(point.x, canvas.getHeight() - point.y);
                        }
                        canvas.drawPath(path, paint);
                    }
                }
            }
        }

        @Override
        public void setAlpha(int i) {

        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.UNKNOWN;
        }
    }
}
