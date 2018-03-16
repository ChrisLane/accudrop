package me.chrislane.accudrop.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class RadarViewModel extends ViewModel {
    private MutableLiveData<UUID> subject = new MutableLiveData<>();
    private MutableLiveData<Long> subjectTime = new MutableLiveData<>();
    private MutableLiveData<Integer> jumpId = new MutableLiveData<>();
    private MutableLiveData<List<Double>> guestHeightDiffs = new MutableLiveData<>();
    private MutableLiveData<List<Pair<UUID, Location>>> guestLocations = new MutableLiveData<>();
    private MutableLiveData<List<Pair<Float, Float>>> relativeGuestPositions = new MutableLiveData<>();
    private MutableLiveData<Pair<UUID, List<Location>>> subjectEntry = new MutableLiveData<>();
    private MutableLiveData<List<Pair<UUID, List<Location>>>> guestEntries = new MutableLiveData<>();
    private MutableLiveData<List<UUID>> guestsInView = new MutableLiveData<>();
    private MutableLiveData<Integer> firstJumpId = new MutableLiveData<>();
    private MutableLiveData<Integer> lastJumpId = new MutableLiveData<>();

    public RadarViewModel() {
        this.guestHeightDiffs.setValue(new ArrayList<>());
        this.relativeGuestPositions.setValue(new ArrayList<>());
        this.guestsInView.setValue(new ArrayList<>());
    }

    public LiveData<UUID> getSubject() {
        return subject;
    }

    public void setSubject(UUID subject) {
        this.subject.setValue(subject);
    }

    public LiveData<Long> getSubjectTime() {
        return subjectTime;
    }

    public void setSubjectTime(long subjectTime) {
        this.subjectTime.setValue(subjectTime);
    }

    public LiveData<Integer> getJumpId() {
        return jumpId;
    }

    public void setJumpId(int jumpId) {
        this.jumpId.setValue(jumpId);
    }

    public LiveData<List<Double>> getGuestHeightDiffs() {
        return guestHeightDiffs;
    }

    public void setGuestHeightDiffs(List<Double> guestHeightDiffs) {
        this.guestHeightDiffs.setValue(guestHeightDiffs);
    }

    public LiveData<List<Pair<UUID, Location>>> getGuestLocations() {
        return guestLocations;
    }

    public void setGuestLocations(List<Pair<UUID, Location>> guestLocations) {
        this.guestLocations.setValue(guestLocations);
    }

    public LiveData<List<Pair<Float, Float>>> getRelativeGuestPositions() {
        return relativeGuestPositions;
    }

    public void setRelativeGuestPositions(List<Pair<Float, Float>> relativeGuestPositions) {
        this.relativeGuestPositions.setValue(relativeGuestPositions);
    }

    public LiveData<Pair<UUID, List<Location>>> getSubjectEntry() {
        return subjectEntry;
    }

    public void setSubjectEntry(Pair<UUID, List<Location>> subjectEntry) {
        this.subjectEntry.setValue(subjectEntry);
    }

    public LiveData<List<Pair<UUID, List<Location>>>> getGuestEntries() {
        return guestEntries;
    }

    public void setGuestEntries(List<Pair<UUID, List<Location>>> guestEntries) {
        this.guestEntries.setValue(guestEntries);
    }

    public LiveData<List<UUID>> getGuestsInView() {
        return guestsInView;
    }

    public void setGuestsInView(List<UUID> guestsInView) {
        this.guestsInView.setValue(guestsInView);
    }

    public void setFirstJumpId(int firstJumpId) {
        this.firstJumpId.setValue(firstJumpId);
    }

    public void setLastJumpId(int lastJumpId) {
        this.lastJumpId.setValue(lastJumpId);
    }

    public LiveData<Integer> getLastJumpId() {
        return lastJumpId;
    }

    public LiveData<Integer> getFirstJumpId() {
        return firstJumpId;
    }
}
