package me.chrislane.accudrop.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.location.Location
import java.util.*


class RadarViewModel : ViewModel() {
    private val subject = MutableLiveData<UUID>()
    private val subjectTime = MutableLiveData<Long>()
    private val jumpId = MutableLiveData<Int>()
    private val guestHeightDiffs = MutableLiveData<MutableList<Double>>()
    private val guestLocations = MutableLiveData<MutableList<Pair<UUID, Location>>>()
    private val relativeGuestPositions = MutableLiveData<MutableList<Pair<Float, Float>>>()
    private val subjectEntry = MutableLiveData<Pair<UUID, MutableList<Location>>>()
    private val guestEntries = MutableLiveData<MutableList<Pair<UUID, MutableList<Location>>>>()
    private val guestsInView = MutableLiveData<MutableList<UUID>>()
    private val firstJumpId = MutableLiveData<Int>()
    private val lastJumpId = MutableLiveData<Int>()

    init {
        this.guestHeightDiffs.value = mutableListOf()
        this.relativeGuestPositions.value = mutableListOf()
        this.guestsInView.value = mutableListOf()
    }

    fun getSubject(): LiveData<UUID> {
        return subject
    }

    fun setSubject(subject: UUID) {
        this.subject.value = subject
    }

    fun getSubjectTime(): LiveData<Long> {
        return subjectTime
    }

    fun setSubjectTime(subjectTime: Long) {
        this.subjectTime.value = subjectTime
    }

    fun getJumpId(): LiveData<Int> {
        return jumpId
    }

    fun setJumpId(jumpId: Int) {
        this.jumpId.value = jumpId
    }

    fun getGuestHeightDiffs(): LiveData<MutableList<Double>> {
        return guestHeightDiffs
    }

    fun setGuestHeightDiffs(guestHeightDiffs: MutableList<Double>) {
        this.guestHeightDiffs.value = guestHeightDiffs
    }

    fun getGuestLocations(): LiveData<MutableList<Pair<UUID, Location>>> {
        return guestLocations
    }

    fun setGuestLocations(guestLocations: MutableList<Pair<UUID, Location>>) {
        this.guestLocations.value = guestLocations
    }

    fun getRelativeGuestPositions(): LiveData<MutableList<Pair<Float, Float>>> {
        return relativeGuestPositions
    }

    fun setRelativeGuestPositions(relativeGuestPositions: MutableList<Pair<Float, Float>>) {
        this.relativeGuestPositions.value = relativeGuestPositions
    }

    fun getSubjectEntry(): LiveData<Pair<UUID, MutableList<Location>>> {
        return subjectEntry
    }

    fun setSubjectEntry(subjectEntry: Pair<UUID, MutableList<Location>>) {
        this.subjectEntry.value = subjectEntry
    }

    fun getGuestEntries(): LiveData<MutableList<Pair<UUID, MutableList<Location>>>> {
        return guestEntries
    }

    fun setGuestEntries(guestEntries: MutableList<Pair<UUID, MutableList<Location>>>) {
        this.guestEntries.value = guestEntries
    }

    fun getGuestsInView(): LiveData<MutableList<UUID>> {
        return guestsInView
    }

    fun setGuestsInView(guestsInView: MutableList<UUID>) {
        this.guestsInView.value = guestsInView
    }

    fun setFirstJumpId(firstJumpId: Int) {
        this.firstJumpId.value = firstJumpId
    }

    fun setLastJumpId(lastJumpId: Int) {
        this.lastJumpId.value = lastJumpId
    }

    fun getLastJumpId(): LiveData<Int> {
        return lastJumpId
    }

    fun getFirstJumpId(): LiveData<Int> {
        return firstJumpId
    }
}
