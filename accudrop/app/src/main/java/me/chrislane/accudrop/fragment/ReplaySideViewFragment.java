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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new ReplaySideViewPresenter(this);
    }

    /**
     * Redraw the side view lines.
     */
    public void updateDrawable() {
        Activity activity = getActivity();
        if (activity != null) {
            SideViewDrawable drawable = new SideViewDrawable();
            ImageView drawBox = activity.findViewById(R.id.replay_draw_area);
            drawBox.setImageDrawable(drawable);
        }
    }

    /**
     * This drawable creates lines between points generates by the <code>ReplaySideViewPresenter</code>.
     */
    private class SideViewDrawable extends Drawable {

        @Override
        public void draw(@NonNull Canvas canvas) {
            List<PointF> points = presenter.produceViewPositions(canvas.getWidth(), canvas.getHeight(), 20);

            Paint ground = new Paint();
            ground.setColor(Color.GREEN);
            canvas.drawRect(0, (float) (canvas.getHeight() * 0.95), canvas.getWidth(), canvas.getHeight(), ground);

            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10);

            Path path = new Path();
            if (!points.isEmpty()) {
                path.moveTo(points.get(0).x, points.get(0).y);
                for (PointF point : points) {
                    path.lineTo(point.x, point.y);
                }
                canvas.drawPath(path, paint);
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
