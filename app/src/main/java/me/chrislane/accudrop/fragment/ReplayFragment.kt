package me.chrislane.accudrop.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import me.chrislane.accudrop.R
import me.chrislane.accudrop.presenter.ReplayPresenter
import java.util.*

class ReplayFragment : Fragment() {

    /**
     * Get the replay map fragment.
     *
     * @return The replay map fragment.
     */
    var replayMap: ReplayMapFragment? = null
        private set
    /**
     * Get the replay side view fragment.
     *
     * @return The replay side view fragment.
     */
    var replaySideView: ReplaySideViewFragment? = null
        private set
    private var presenter: ReplayPresenter? = null
    private var prevButton: Button? = null
    private var nextbtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = ReplayPresenter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val transaction = childFragmentManager.beginTransaction()
        replayMap = ReplayMapFragment()
        replaySideView = ReplaySideViewFragment()
        transaction.add(R.id.replay_map_fragment, replayMap)
        transaction.add(R.id.replay_side_view_fragment, replaySideView).commit()

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_replay, container, false)

        prevButton = view.findViewById(R.id.replay_back_button)
        prevButton!!.setOnClickListener { v -> presenter!!.prevJump() }

        nextbtn = view.findViewById(R.id.replay_forward_button)
        nextbtn!!.setOnClickListener { v -> presenter!!.nextJump() }

        return view
    }

    fun updateButtons(jumpId: Int, firstJumpId: Int, lastJumpId: Int) {
        // Check limits for previous button
        if (jumpId <= firstJumpId) {
            prevButton!!.text = "❌"
            prevButton!!.isEnabled = false
        } else {
            prevButton!!.setText(String.format(Locale.ENGLISH, "❮ %d", jumpId - 1))
            prevButton!!.isEnabled = true
        }

        // Check limits for next button
        if (jumpId >= lastJumpId) {
            nextbtn!!.text = "❌"
            nextbtn!!.isEnabled = false
        } else {
            nextbtn!!.isEnabled = true
            nextbtn!!.setText(String.format(Locale.ENGLISH, "%d ❯", jumpId + 1))
        }
    }
}
