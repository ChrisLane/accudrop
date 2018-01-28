package me.chrislane.accudrop.task;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import me.chrislane.accudrop.Point3D;
import me.chrislane.accudrop.db.Position;
import me.chrislane.accudrop.viewmodel.JumpViewModel;

public class FetchJumpTask extends AsyncTask<Integer, Void, List<Point3D>> {

    private static final String TAG = FetchJumpTask.class.getSimpleName();
    private final JumpViewModel jumpViewModel;
    private final FetchJumpListener listener;

    public FetchJumpTask(FetchJumpListener listener, JumpViewModel jumpViewModel) {
        this.listener = listener;
        this.jumpViewModel = jumpViewModel;
    }

    @Override
    protected List<Point3D> doInBackground(Integer... integers) {
        Integer jumpNumber;
        if (integers.length > 0) {
            jumpNumber = integers[0];
            Log.d(TAG, "Fetching jump " + jumpNumber);
        } else {
            jumpNumber = jumpViewModel.getLastJumpId();
            Log.d(TAG, "Fetching last jump (" + jumpNumber + ")");
        }

        List<Point3D> points = new ArrayList<>();
        if (jumpNumber != null) {
            List<Position> positions = jumpViewModel.getPositionsForJump(jumpNumber);
            for (Position position : positions) {
                LatLng latLng = new LatLng(position.latitude, position.longitude);
                Point3D point = new Point3D(latLng, position.altitude);
                points.add(point);
                Log.d(TAG, point.toString());
            }
        } else {
            Log.e(TAG, "No last jump id found.");
        }

        return points;
    }

    @Override
    protected void onPostExecute(List<Point3D> points) {
        super.onPostExecute(points);

        Log.d(TAG, "Finished getting jump data.");
        listener.onFinished(points);
    }

    public interface FetchJumpListener {
        void onFinished(List<Point3D> result);
    }
}
