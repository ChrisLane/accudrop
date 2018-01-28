package me.chrislane.accudrop.presenter;

import me.chrislane.accudrop.fragment.ReplayMapFragment;
import me.chrislane.accudrop.task.FetchJumpTask;
import me.chrislane.accudrop.viewmodel.JumpViewModel;

public class ReplayMapPresenter {

    private final JumpViewModel jumpViewModel;
    private final ReplayMapFragment replayMapFragment;

    public ReplayMapPresenter(ReplayMapFragment replayMapFragment, JumpViewModel jumpViewModel) {
        this.replayMapFragment = replayMapFragment;
        this.jumpViewModel = jumpViewModel;
    }

    public void getLastJumpPoints() {
        FetchJumpTask.FetchJumpListener listener = replayMapFragment::setPoints;
        new FetchJumpTask(listener, jumpViewModel).execute();
    }
}
