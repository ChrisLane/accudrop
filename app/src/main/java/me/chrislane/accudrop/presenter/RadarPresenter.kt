package me.chrislane.accudrop.presenter

import android.app.Application
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.location.Location
import android.os.AsyncTask
import android.support.v4.util.Pair
import android.util.Log
import me.chrislane.accudrop.BuildConfig
import me.chrislane.accudrop.MainActivity
import me.chrislane.accudrop.db.FallType
import me.chrislane.accudrop.fragment.RadarFragment
import me.chrislane.accudrop.task.FetchLastJumpIdTask
import me.chrislane.accudrop.task.FetchUsersAndTypePositionsTask
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import me.chrislane.accudrop.viewmodel.RadarViewModel
import java.util.*

class RadarPresenter(private val fragment: RadarFragment) {
    private val radarViewModel: RadarViewModel
    private var databaseViewModel: DatabaseViewModel
    private var knownJumpId = -1
    val maxHDistance = 500 // In metres
    val maxVDistance = 50 // In metres

    init {

        val main = fragment.requireActivity() as MainActivity
        radarViewModel = ViewModelProviders.of(fragment).get(RadarViewModel::class.java)
        databaseViewModel = ViewModelProviders.of(main).get(DatabaseViewModel::class.java)

        // Set the current user as the subject
        setOwnerAsSubject()

        val listener = { jumpId: Int? ->
            if (jumpId != null) {
                radarViewModel.setJumpId(jumpId)
                radarViewModel.getSubjectEntry()
            }
        }
        FetchLastJumpIdTask(listener, databaseViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        subscribeToSubject()
        subscribeToTime()
        subscribeToButtonData()
        subscribeToJumpId()
        subscribeToJumpRange()
    }

    private fun setOwnerAsSubject() {
        val settings = databaseViewModel.getApplication<Application>()
                .getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val stringUuid = settings.getString("userUUID", "")
        val uuid = UUID.fromString(stringUuid)
        radarViewModel.setSubject(uuid)
    }

    fun test() {
        radarViewModel.setGuestEntries(mutableListOf())
        val guestEntries = radarViewModel.getGuestEntries().value ?: return

        val locationList1 = mutableListOf<Location>()
        val location1 = Location("")
        location1.latitude = 38.696680
        location1.longitude = 1.448990
        location1.altitude = 1.0
        locationList1.add(location1)
        radarViewModel.setSubjectEntry(
                Pair(UUID.fromString("a4f4231a-1a1a-40be-8fa2-632943fb1411"), locationList1))

        val locationList2 = mutableListOf<Location>()
        val location2 = Location("")
        location2.latitude = 38.696249
        location2.longitude = 1.449623
        location2.altitude = 2.0
        locationList2.add(location2)
        guestEntries.add(Pair(UUID.fromString("6ad2769d-5031-4ac3-8b63-ce3d8e779c3b"), locationList2))

        val locationList3 = mutableListOf<Location>()
        val location3 = Location("")
        location3.latitude = 38.695948
        location3.longitude = 1.448914
        location3.altitude = 3.0
        locationList3.add(location3)
        guestEntries.add(Pair(UUID.fromString("c22284cf-cd7d-49f8-8c96-bcdc3dcd61b8"), locationList3))

        val locationList4 = mutableListOf<Location>()
        val location4 = Location("")
        location4.latitude = 38.696358
        location4.longitude = 1.448222
        location4.altitude = 4.0
        locationList4.add(location4)
        guestEntries.add(Pair(UUID.fromString("bc832712-894d-4be4-8440-18aa1c90a7a7"), locationList4))
    }

    private fun subscribeToJumpRange() {
        val firstJumpIdObserver = Observer<Int> { firstJumpId ->
            if (firstJumpId != null) {
                radarViewModel.setFirstJumpId(firstJumpId)
            }
        }
        databaseViewModel.findFirstJumpId().observe(fragment, firstJumpIdObserver)

        val lastJumpIdObserver = Observer<Int> { lastJumpId ->
            if (lastJumpId != null) {
                radarViewModel.setLastJumpId(lastJumpId)
            }
        }
        databaseViewModel.findLastJumpId().observe(fragment, lastJumpIdObserver)
    }

    private fun subscribeToJumpId() {
        val jumpIdObserver = Observer<Int> { jumpId ->
            if (jumpId != null) {
                fragment.resetSeekBar()
                setOwnerAsSubject()
            }
        }
        radarViewModel.getJumpId().observe(fragment, jumpIdObserver)
    }

    private fun subscribeToSubject() {
        val subjectObserver = Observer<UUID> { subject ->
            val jumpId = radarViewModel.getJumpId().value
            if (subject != null && jumpId != null) {
                if (jumpId != knownJumpId) {
                    // We've changed the jump
                    knownJumpId = jumpId
                    generateJumpPositions(jumpId, subject)
                } else {
                    // Only the subject changed
                    start(subject)
                }
            }
        }
        radarViewModel.getSubject().observe(fragment, subjectObserver)
    }

    fun start(subject: UUID?) {
        val subjectEntry = radarViewModel.getSubjectEntry().value
        var guestEntries = radarViewModel.getGuestEntries().value
        if (subjectEntry == null || guestEntries == null) {
            return
        }

        guestEntries.add(subjectEntry)
        guestEntries = separateEntries(subject, guestEntries)

        val time = radarViewModel.getSubjectTime().value
        val subjectLocs = subjectEntry.second
        if (subjectLocs != null && time == null) {
            val newTime = subjectLocs[0].time
            radarViewModel.setSubjectTime(newTime)
        }
        if (subjectLocs != null && time != null) {
            val guestLocations = getGuestLocations(guestEntries, time)
            radarViewModel.setGuestLocations(guestLocations)
            updateGuestRelatives(guestLocations, time)
        }
    }

    private fun separateEntries(subject: UUID?,
                                userEntries: MutableList<Pair<UUID, MutableList<Location>>>): MutableList<Pair<UUID, MutableList<Location>>> {
        for (i in userEntries.indices) {
            val userEntry = userEntries[i]
            if (userEntry.first != null && userEntry.first == subject) {
                radarViewModel.setSubjectEntry(userEntry)
                userEntries.removeAt(i)
                break
            }
        }

        val subjectEntry = radarViewModel.getSubjectEntry().value
        // None of the users were the subject.
        if (subjectEntry?.second == null) {
            Log.e(TAG, "Subject does not exist in jump data.", IllegalArgumentException(""))
        }

        return userEntries
    }

    private fun generateJumpPositions(jumpId: Int, subject: UUID?) {
        val listener = listener@{ userEntries: MutableList<Pair<UUID, MutableList<Location>>> ->
            val guestEntries = separateEntries(subject, userEntries)
            val subjectEntry = radarViewModel.getSubjectEntry().value
            guestEntries.let { radarViewModel.setGuestEntries(it) }
            if (subjectEntry != null) {
                val subjectLocs = subjectEntry.second
                if (subjectLocs != null && !subjectLocs.isEmpty()) {
                    val startTime = subjectLocs[0].time
                    val guestLocations = getGuestLocations(guestEntries, startTime)
                    radarViewModel.setGuestLocations(guestLocations)
                    updateGuestRelatives(guestLocations, startTime)
                }
            }
        }
        FetchUsersAndTypePositionsTask(listener, FallType.FREEFALL, databaseViewModel).execute(jumpId)
    }

    private fun getGuestLocations(guestLocs: MutableList<Pair<UUID, MutableList<Location>>>, time: Long): MutableList<Pair<UUID, Location>> {
        val result = mutableListOf<Pair<UUID, Location>>()
        for (userEntry in guestLocs) {
            val locations = userEntry.second
            if (locations != null && locations.size > 0) {
                val nearest = getLocationByTime(locations, time)

                result.add(Pair(userEntry.first, nearest))
            }
        }
        return result
    }

    fun updateTime(time: Long) {
        radarViewModel.setSubjectTime(time)
    }

    private fun subscribeToTime() {
        val timeObserver = Observer<Long> { time ->
            if (time != null) {
                val guestEntries = radarViewModel.getGuestEntries().value
                if (guestEntries != null) {
                    radarViewModel.setGuestLocations(getGuestLocations(guestEntries, time))
                    updateGuestRelatives(radarViewModel.getGuestLocations().value!!, time)
                }
            }
        }
        radarViewModel.getSubjectTime().observe(fragment, timeObserver)
    }

    private fun subscribeToButtonData() {
        val buttonDataObserver = Observer<Int> { ignored ->
            val jumpId = radarViewModel.getJumpId().value
            val firstJumpId = radarViewModel.getFirstJumpId().value
            val lastJumpId = radarViewModel.getLastJumpId().value

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Button data: $jumpId, $firstJumpId, $lastJumpId")
            }

            if (jumpId != null && firstJumpId != null && lastJumpId != null) {
                fragment.updateButtons(jumpId, firstJumpId, lastJumpId)
            }
        }
        radarViewModel.getJumpId().observe(fragment, buttonDataObserver)
        radarViewModel.getFirstJumpId().observe(fragment, buttonDataObserver)
        radarViewModel.getLastJumpId().observe(fragment, buttonDataObserver)
    }

    private fun getLocationByTime(locations: MutableList<Location>, time: Long): Location {
        // Time is less than the first time in the sorted list
        if (time < locations[0].time) {
            return locations[0]
        }
        // Time is greater than the last element in the sorted list
        if (time > locations[locations.size - 1].time) {
            return locations[locations.size - 1]
        }

        var low = 0
        var high = locations.size - 1

        while (low <= high) {
            val mid = (high + low) / 2

            when {
                time < locations[mid].time -> high = mid - 1
                time > locations[mid].time -> low = mid + 1
                else -> return locations[mid]
            }
        }

        return if (locations[low].time - time < time - locations[high].time)
            locations[low]
        else
            locations[high]
    }

    private fun updateGuestRelatives(guestLocations: MutableList<Pair<UUID, Location>>, time: Long) {
        val guestHeightDiffs = radarViewModel.getGuestHeightDiffs().value
        val relativeGuestPositions = radarViewModel.getRelativeGuestPositions().value
        val subjectEntry = radarViewModel.getSubjectEntry().value
        val guestsInView = radarViewModel.getGuestsInView().value

        if (guestHeightDiffs == null || relativeGuestPositions == null
                || subjectEntry == null || guestsInView == null) {
            return
        }

        guestsInView.clear()
        relativeGuestPositions.clear()
        guestHeightDiffs.clear()

        val subjectLocs = subjectEntry.second

        if (subjectLocs == null || subjectLocs.isEmpty() || guestLocations.isEmpty()) {
            fragment.updateRadarPoints()
            return
        }

        var subjectLoc: Location? = null
        for (loc in subjectLocs) {
            if (loc.time == time) {
                subjectLoc = loc
                break
            }
        }

        // Check we got a value for the subject location
        if (subjectLoc == null) {
            Log.e(TAG, "No subject location matching timestamp")
            return
        }


        // Loop over user position arrays
        for (guest in guestLocations) {
            if (guest.first != null && guest.second != null) {
                val hDistanceTo = subjectLoc.distanceTo(guest.second)
                val vDistanceTo = guest.second!!.altitude - subjectLoc.altitude
                Log.v(TAG, "Horizontal Distance: $hDistanceTo")
                Log.v(TAG, "Vertical Distance: $vDistanceTo")

                // Check if distance from subject further than maxHDistance
                if (maxHDistance >= hDistanceTo && maxVDistance >= vDistanceTo) {
                    // Add to list of positions to draw
                    val bearingTo = subjectLoc.bearingTo(guest.second)
                    Log.v(TAG, "Bearing: $bearingTo")
                    val first = guest.first
                    first?.let { guestsInView.add(it) }
                    relativeGuestPositions.add(Pair(bearingTo, hDistanceTo))
                    guestHeightDiffs.add(vDistanceTo)
                }
            }
        }

        fragment.updateRadarPoints()
    }

    fun prevJump() {
        val jumpId = radarViewModel.getJumpId().value
        val firstJumpId = radarViewModel.getFirstJumpId().value

        if (jumpId != null && firstJumpId != null) {
            if (jumpId > firstJumpId) {
                Log.d(TAG, "Setting jump ID to " + (jumpId - 1))
                radarViewModel.setJumpId(jumpId - 1)
            }
        }
    }

    fun nextJump() {
        val jumpId = radarViewModel.getJumpId().value
        val lastJumpId = radarViewModel.getLastJumpId().value

        if (jumpId != null && lastJumpId != null) {
            if (jumpId < lastJumpId) {
                Log.d(TAG, "Setting jump ID to " + (jumpId + 1))
                radarViewModel.setJumpId(jumpId + 1)
            }
        }
    }

    companion object {
        private val TAG = RadarPresenter::class.java.simpleName
    }
}
