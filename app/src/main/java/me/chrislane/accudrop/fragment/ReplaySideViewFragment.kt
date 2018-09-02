package me.chrislane.accudrop.fragment

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.ImageView
import me.chrislane.accudrop.R
import me.chrislane.accudrop.presenter.ReplaySideViewPresenter

class ReplaySideViewFragment : Fragment() {
    private lateinit var presenter: ReplaySideViewPresenter
    private var screenPointsList: MutableList<MutableList<PointF>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = ReplaySideViewPresenter(this)
    }

    /**
     * Redraw the side view lines.
     */
    fun updateDrawable(generatePoints: Boolean) {
        val activity = requireActivity()
        val drawable = SideViewDrawable(generatePoints)
        val drawBox = activity.findViewById<ImageView>(R.id.replay_draw_area)
        drawBox.setImageDrawable(drawable)
    }

    fun setScreenPointsList(screenPoints: MutableList<MutableList<PointF>>) {
        this.screenPointsList = screenPoints
        updateDrawable(false)
    }

    /**
     * This drawable creates lines between points generates by the `ReplaySideViewPresenter`.
     */
    private inner class SideViewDrawable(private val generatePoints: Boolean) : Drawable() {

        override fun draw(canvas: Canvas) {
            // Paint the ground
            val paint = Paint()
            paint.color = Color.GREEN
            canvas.drawRect(0f, (canvas.height * 0.95).toFloat(), canvas.width.toFloat(), canvas.height.toFloat(), paint)

            if (generatePoints) {
                presenter.produceViewPositions(canvas.width, canvas.height, 20)
            }

            if (screenPointsList != null) {
                paint.color = Color.RED
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 10f

                val path = Path()
                for (screenPoints in screenPointsList!!) {
                    if (!screenPoints.isEmpty()) {
                        path.moveTo(screenPoints[0].x, canvas.height - screenPoints[0].y)
                        for (point in screenPoints) {
                            path.lineTo(point.x, canvas.height - point.y)
                        }
                        canvas.drawPath(path, paint)
                    }
                }
            }
        }

        override fun setAlpha(i: Int) {

        }

        override fun setColorFilter(colorFilter: ColorFilter?) {

        }

        override fun getOpacity(): Int {
            return PixelFormat.UNKNOWN
        }
    }

    companion object {
        private val TAG: String = ReplaySideViewFragment::class.java.simpleName
    }
}
