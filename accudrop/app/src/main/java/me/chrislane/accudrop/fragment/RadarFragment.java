package me.chrislane.accudrop.fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import me.chrislane.accudrop.R;
import me.chrislane.accudrop.presenter.RadarPresenter;

public class RadarFragment extends Fragment {

    private RadarPresenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new RadarPresenter();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_radar, container, false);
        FrameLayout layout = view.findViewById(R.id.radar);
        layout.addView(new Radar(getContext()));
        return view;
    }

    public static class Radar extends View {
        Paint paint = new Paint();
        RectF oval = new RectF();

        public Radar(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float halfHeight = canvas.getHeight() / 2f;
            float quarterWidth = canvas.getWidth() / 4f;

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            oval.set(0, halfHeight - quarterWidth, canvas.getWidth(),
                    halfHeight + quarterWidth);
            canvas.drawOval(oval, paint);

            oval.inset(canvas.getWidth() * 0.5f, (canvas.getWidth() / 2f) * 0.5f);
            canvas.drawOval(oval, paint);
        }
    }
}