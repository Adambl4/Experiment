package adambl4.experiment.challenge.view

import adambl4.experiment.challenge.GameConfig
import adambl4.experiment.challenge.R
import adambl4.experiment.challenge.utils.*
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import org.jetbrains.anko.*
import java.util.concurrent.TimeUnit


class LauncherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        data class LauncherItemModel(val text: CharSequence = "123", val drawable: Drawable? = null, val intent: Intent? = null) {
            override fun toString(): String {
                return text.toString()
            }
        }

        val chromeInfo = getChromeApplicationInfo(this)
        val settignsInfo = getSettingsApplicationInfo(this)
        val cameraInfo = getCameraApplicationInfo(this)
        val musicInfo = getMusicApplicationInfo(this)
        val fileManager = getFileMangerAppInfo(this)
        val photoViewer = getPhotoViewerAppInfo(this)


        val itemList = listOf(
                LauncherItemModel(chromeInfo!!.loadLabel(packageManager),
                        chromeInfo.loadIcon(packageManager),
                        getChromeIntent()),
                LauncherItemModel(settignsInfo!!.loadLabel(packageManager),
                        settignsInfo.loadIcon(packageManager),
                        getSettingsIntent()),
                LauncherItemModel(cameraInfo!!.loadLabel(packageManager),
                        cameraInfo.loadIcon(packageManager),
                        getCameraIntent()),
                LauncherItemModel(musicInfo!!.loadLabel(packageManager),
                        musicInfo.loadIcon(packageManager),
                        getMusicIntent()),
                LauncherItemModel(fileManager!!.loadLabel(packageManager),
                        fileManager.loadIcon(packageManager),
                        getFileManagerIntent()),
                LauncherItemModel(photoViewer!!.loadLabel(packageManager),
                        photoViewer.loadIcon(packageManager),
                        getPhotoViewerIntent()))

        val adapterr = object : BaseAdapter() {
            override fun getItem(position: Int): LauncherItemModel = itemList[position]

            override fun getItemId(position: Int): Long = 0

            override fun getCount(): Int = itemList.size

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
                val view = View.inflate(this@LauncherActivity, R.layout.item, null)
                val imageView = view.findViewById(R.id.imageView) as ImageView
                val textView = view.findViewById(R.id.textView) as TextView
                imageView.setImageDrawable(getItem(position).drawable)
                textView.text = getItem(position).text
                view.setOnClickListener { startActivity(getItem(position).intent) }
                return view
            }
        }

        val view = frameLayout {
            backgroundColor = resources.getColor(android.R.color.white)

            textView {
                text = "00:00:00"
                textSize = dip(20).toFloat()
                fun update() {
                    text = (System.currentTimeMillis() - GameConfig.startTimeStamp).toTimeString();
                    postDelayed(::update, 1000)
                }
                update()
            }.lparams() {
                gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                topPadding = dip(25)
            }

            gridView() {
                numColumns = 3
                adapter = adapterr
                gravity = Gravity.CENTER_HORIZONTAL
                horizontalSpacing = dip(10)
                verticalSpacing = dip(10)
                setPadding(dip(10), 0, dip(10), 0)
            }.lparams() {
                gravity = Gravity.CENTER
            }

            button("pass") {
                onClick { }
            }.lparams {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomPadding = dip(20)
            }
        }

        setContentView(view)

    }
}
