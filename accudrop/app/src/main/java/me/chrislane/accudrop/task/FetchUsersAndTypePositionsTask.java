package me.chrislane.accudrop.task;

        import android.location.Location;
        import android.os.AsyncTask;
        import android.support.v4.util.Pair;
        import android.util.Log;

        import java.util.ArrayList;
        import java.util.List;
        import java.util.UUID;

        import me.chrislane.accudrop.db.FallType;
        import me.chrislane.accudrop.viewmodel.DatabaseViewModel;

public class FetchUsersAndTypePositionsTask extends AsyncTask<Integer, Void, List<Pair<UUID, List<Location>>>> {

    private static final String TAG = FetchUsersAndPositionsTask.class.getSimpleName();
    private final DatabaseViewModel databaseViewModel;
    private final FetchUsersAndTypePositionsTask.Listener listener;
    private final FallType fallType;

    public FetchUsersAndTypePositionsTask(FetchUsersAndTypePositionsTask.Listener listener,
                                      FallType fallType, DatabaseViewModel databaseViewModel) {
        this.listener = listener;
        this.fallType = fallType;
        this.databaseViewModel = databaseViewModel;
    }

    @Override
    protected List<Pair<UUID, List<Location>>> doInBackground(Integer... integers) {
        List<Pair<UUID, List<Location>>> result = new ArrayList<>();
        Integer jumpNumber;
        if (integers.length > 0) {
            jumpNumber = integers[0];
            Log.d(TAG, "Fetching jump " + jumpNumber);
        } else {
            jumpNumber = databaseViewModel.getLastJumpId();
            Log.d(TAG, "Fetching last jump (" + jumpNumber + ")");
        }

        if (jumpNumber != null) {
            if (fallType.equals(FallType.CANOPY)) {
                result = databaseViewModel.getUsersAndTypePositionsForJump(FallType.CANOPY, jumpNumber);
            } else {
                result = databaseViewModel.getUsersAndTypePositionsForJump(FallType.FREEFALL, jumpNumber);
            }
        } else {
            Log.e(TAG, "No last jump id found.");
        }

        return result;
    }

    @Override
    protected void onPostExecute(List<Pair<UUID, List<Location>>> result) {
        super.onPostExecute(result);

        Log.d(TAG, "Finished getting jump data.");
        listener.onFinished(result);
    }

    public interface Listener {
        void onFinished(List<Pair<UUID, List<Location>>> result);
    }
}
