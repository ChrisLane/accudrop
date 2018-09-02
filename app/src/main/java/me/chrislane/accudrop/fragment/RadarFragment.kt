package me.chrislane.accudrop.fragment

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.util.Pair
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.SeekBar
import me.chrislane.accudrop.BuildConfig
import me.chrislane.accudrop.R
import me.chrislane.accudrop.Util
import me.chrislane.accudrop.presenter.RadarPresenter
import me.chrislane.accudrop.viewmodel.RadarViewModel
import java.util.*

class RadarFragment : Fragment() {
    private lateinit var presenter: RadarPresenter
    private lateinit var radarViewModel: RadarViewModel
    private var radarView: RadarView? = null
    private var nextbtn: Button? = null
    private var prevButton: Button? = null
    private var seekBar: SeekBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = RadarPresenter(this)
        radarViewModel = ViewModelProviders.of(this).get(RadarViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_radar, container, false)
        val layout: FrameLayout? = view.findViewById(R.id.radar)
        radarView = context?.let { RadarView(it) }
        layout?.addView(radarView)

        prevButton = view.findViewById(R.id.radar_prev_button)
        prevButton?.setOnClickListener { presenter.prevJump() }

        nextbtn = view.findViewById(R.id.radar_next_button)
        nextbtn?.setOnClickListener { presenter.nextJump() }

        seekBar = view.findViewById(R.id.radar_seek_bar)
        seekBar?.setOnSeekBarChangeListener(SeekBarChangeListener())

        return view
    }

    fun resetSeekBar() {
        seekBar?.progress = 0
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

    fun updateRadarPoints() {
        radarView?.invalidate()
    }

    private fun getScaledPositions(width: Int, height: Int, shrink: Float): MutableList<PointF> {
        val maxHDistance = presenter.maxHDistance
        val relativePositions = radarViewModel.getRelativeGuestPositions().value // bearing, distance
        val result = mutableListOf<PointF>()

        if (relativePositions != null) {
            for (position in relativePositions) {
                if (position.first != null && position.second != null) {
                    // Scale distance to ellipse width
                    val scaledDistance = Util.getScaledValue(position.second!!.toDouble(), 0.0, maxHDistance.toDouble(), 0.0, (width / 2f).toDouble())
                    Log.v(TAG, "Scaled H Distance: $scaledDistance")
                    // Calculate cartesian coordinate
                    var newPoint = calculateCartesian(position.first, scaledDistance)
                    Log.v(TAG, "After Cartesian: $newPoint")
                    // Apply shrink to stay within ellipse
                    newPoint = applyShrink(newPoint, shrink)
                    Log.v(TAG, "After Shrink: $newPoint")
                    // Move position to be relative to the centre of the screen
                    newPoint = calculateRelative(newPoint, PointF((width / 2).toFloat(), (height / 2).toFloat()))
                    Log.v(TAG, "After Relative: $newPoint")

                    // Add to the result
                    result.add(newPoint)
                }
            }
        }

        return result
    }

    private fun getScaledAltitudes(height: Int): MutableList<Double> {
        val maxVDistance = presenter.maxVDistance
        val guestHeightDiffs = radarViewModel.getGuestHeightDiffs().value
        val result = mutableListOf<Double>()

        if (guestHeightDiffs != null) {
            for (heightDiff in guestHeightDiffs) {
                // Scale the vertical distance with the screen height
                val scaledDiff = Util.getScaledValue(heightDiff, (-maxVDistance).toDouble(), maxVDistance.toDouble(),
                        (-(height / 2f)).toDouble(), (height / 2f).toDouble())
                Log.v(TAG, "Scaled V Distance: $scaledDiff")
                result.add(scaledDiff)
            }
        }

        return result
    }

    inner class RadarView(context: Context) : View(context) {
        val TAG: String = RadarView::class.java.simpleName
        private var paint = Paint()
        private var textPaint = Paint()
        private var oval = RectF()
        private var points: MutableList<Pair<UUID, PointF>> = mutableListOf()
        private lateinit var event: MotionEvent

        override fun onTouchEvent(event: MotionEvent): Boolean {
            super.onTouchEvent(event)
            this.event = event

            if (event.action != MotionEvent.ACTION_DOWN) {
                return false
            }
            // We're supposed to call performClick for accessibility reasons
            return performClick()
        }

        override fun performClick(): Boolean {
            super.performClick()

            val xArea = 25f
            val yArea = 25f

            var closestPoint: Pair<UUID, PointF>? = null
            var bestScore = xArea + yArea
            for (userPoint in points) {
                val point = userPoint.second

                if (point != null) {
                    val xDistance = Math.abs(point.x - event.x)
                    val yDistance = Math.abs(point.y - event.y)
                    Log.v(TAG, "Distances: $xDistance, $yDistance")
                    if (xDistance < xArea && yDistance < yArea) {
                        val score = xDistance + yDistance
                        if (score < bestScore) {
                            bestScore = score
                            closestPoint = userPoint
                        }
                    }
                }
            }
            if (closestPoint != null) {
                Log.v(TAG, "Setting subject to " + closestPoint.first!!)
                radarViewModel.setSubject(closestPoint.first as UUID)
                return true
            }
            return false
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val width = canvas.width
            val height = canvas.height
            val halfHeight = canvas.height / 2f
            val halfWidth = width / 2f
            val shrink = 0.25f
            val heightMove = halfWidth * shrink

            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5f
            textPaint.textSize = 20f
            oval.set(0f, halfHeight - heightMove, width.toFloat(), halfHeight + heightMove)
            canvas.drawOval(oval, paint)

            oval.inset(halfWidth / 2, heightMove / 2)
            canvas.drawOval(oval, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.BLUE
            canvas.drawCircle(halfWidth, halfHeight, (width + height) * 0.003f, paint)

            val positions = getScaledPositions(width, height, shrink)
            val heightDiffs = getScaledAltitudes(height)
            val guestsInView = radarViewModel.getGuestsInView().value ?: return
            paint.color = Color.RED

            points.clear()
            var i = 0
            while (i < positions.size && i < heightDiffs.size && i < guestsInView.size) {
                val position = positions[i]

                if (BuildConfig.DEBUG) {
                    Log.v(TAG, "Width: $width, Height: $height")
                    Log.v(TAG, "Adding Pos: $position")
                }

                // Draw line from original x,y to height adjusted x,y
                val newY = (position.y + heightDiffs[i]).toFloat()
                paint.color = Color.BLACK
                canvas.drawLine(width - position.x, height - position.y,
                        width - position.x, height - newY, paint)

                // Draw circle
                paint.color = Color.RED
                val pointX = width - position.x
                val pointY = height - newY
                canvas.drawCircle(pointX, pointY, (width + height) * 0.003f, paint)
                canvas.drawText("Name", pointX, pointY, textPaint)

                // Store user and circle position
                points.add(Pair.create(guestsInView[i], PointF(pointX, pointY)))
                i++
            }
        }
    }

    internal inner class SeekBarChangeListener : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            val subjectEntry = radarViewModel.getSubjectEntry().value ?: return
            val subjectLocs = subjectEntry.second

            if (subjectLocs != null && !subjectLocs.isEmpty()) {
                val newIndex = Util.getScaledValue(progress.toDouble(), 0.0, 100.0,
                        0.0, (subjectLocs.size - 1).toDouble()).toInt()

                val newTime = subjectLocs[newIndex].time
                presenter.updateTime(newTime)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {

        }
    }

    companion object {

        private val TAG = RadarFragment::class.java.simpleName

        private fun applyShrink(point: PointF, shrink: Float): PointF {
            // TODO #50: Scale to ellipse, not just height change in ellipse.
            return PointF(point.x, point.y * shrink)
        }

        private fun calculateCartesian(bearing: Float?, scaledDistance: Double): PointF {
            val rBearing = Math.toRadians(bearing!!.toDouble())
            val x = (scaledDistance * Math.sin(rBearing)).toFloat()
            val y = (scaledDistance * Math.cos(rBearing)).toFloat()
            return PointF(x, y)
        }

        private fun calculateRelative(point: PointF, subject: PointF): PointF {
            val x = subject.x - point.x
            val y = subject.y + point.y
            return PointF(x, y)
        }
    }
}