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

    lateinit var replayMap: ReplayMapFragment
        private set
    lateinit var replaySideView: ReplaySideViewFragment
        private set
    private lateinit var presenter: ReplayPresenter
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
        prevButton?.setOnClickListener { presenter.prevJump() }

        nextbtn = view.findViewById(R.id.replay_forward_button)
        nextbtn?.setOnClickListener { presenter.nextJump() }

        return view
    }

    fun updateButtons(jumpId: Int, firstJumpId: Int, lastJumpId: Int) {
        // Check limits for previous button
        if (jumpId <= firstJumpId) {
            prevButton?.text = "❌"
            prevButton?.isEnabled = false
        } else {
            prevButton?.text = String.format(Locale.ENGLISH, "❮ %d", jumpId - 1)
            prevButton?.isEnabled = true
        }

        // Check limits for next button
        if (jumpId >= lastJumpId) {
            nextbtn?.text = "❌"
            nextbtn?.isEnabled = false
        } else {
            nextbtn?.isEnabled = true
            nextbtn?.text = String.format(Locale.ENGLISH, "%d ❯", jumpId + 1)
        }
    }

    companion object {
        private val TAG: String = ReplayFragment::class.java.simpleName
    }
}
