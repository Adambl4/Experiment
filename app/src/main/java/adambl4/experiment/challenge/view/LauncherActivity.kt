package adambl4.experiment.challenge.view

import adambl4.experiment.challenge.GameService
import adambl4.experiment.challenge.R
import adambl4.experiment.challenge.TheGame
import adambl4.experiment.challenge.TheGame.GameConfig
import adambl4.experiment.challenge.TheGame.PASTEBIN_CAMERA_UNLOCKER_LINK
import adambl4.experiment.challenge.utils.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import org.jetbrains.anko.*


class LauncherActivity : Activity() {
    data class LauncherItemModel(val text: CharSequence = "123", val drawable: Drawable? = null, val intent: Intent? = null) {
        override fun toString(): String {
            return text.toString()
        }
    }

    var gridView: GridView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("tag", "ON CREATE LAUNMCHER")

        onNewIntent(intent)
    }

    private val CAMERA_REQUEST = 123123

    override fun onNewIntent(intent: Intent?) {
        if (intent?.action == ACTION_TAKE_PHOTO) {
            val cameraIntent = getCameraIntent(Uri.fromFile(TheGame.GameStats.cameraFile.value))
            startActivityForResult(cameraIntent, CAMERA_REQUEST)
            intent?.action = null
            Log.d("tag", "STAR ACTIVITY FOR RESULT")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("tag", "ON ACTIVITY RESULT")
        if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST) {
            TheGame.sendEvent(TheGame.GameEvent.PHOTO_TAKEN)
        }
    }

    override fun onResume() {
        super.onResume()

        val view = frameLayout {
            backgroundColor = resources.getColor(android.R.color.white)

            if (!TheGame.GameStats.isGameWin) {
                textView {
                    gravity = Gravity.CENTER
                    text = "00:00:00"
                    textSize = dip(20).toFloat()
                    fun update() {
                        text = (System.currentTimeMillis() - TheGame.GameStats.startTimeStamp).toTimeString();
                        postDelayed(::update, 1000)
                    }
                    update()
                }
            } else {
                td { "TheGame.GameStats.endTimestamp - TheGame.GameStats.startTimeStamp ${TheGame.GameStats.endTimestamp - TheGame.GameStats.startTimeStamp}" }
                td { "starttime ${TheGame.GameStats.startTimeStamp}" }
                td { "endTimestamp ${TheGame.GameStats.endTimestamp}" }
                textView {
                    text = "You win.\n Your time is " + (TheGame.GameStats.endTimestamp - TheGame.GameStats.startTimeStamp).toTimeString()
                    textSize = dip(15).toFloat()
                    gravity = Gravity.CENTER
                }
            }.lparams() {
                gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                topPadding = dip(25)
            }

            gridView = gridView() {
                numColumns = 3
                gravity = Gravity.CENTER_HORIZONTAL
                horizontalSpacing = dip(10)
                verticalSpacing = dip(10)
                setPadding(dip(10), 0, dip(10), 0)
            }.lparams() {
                gravity = Gravity.CENTER
            }

            if (!TheGame.GameStats.isGameWin) {
                button("pass") {
                    onClick {
                        alert("Really") {
                        positiveButton("Yes") {GameService.stopGame(this@LauncherActivity)}
                    }.show() }
                }
            } else {
                button("go back to the way it were.") {
                    onClick { GameService.stopGame(this@LauncherActivity) }
                }
            }.lparams {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomPadding = dip(20)

            }
        }

        setContentView(view)
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

        gridView?.adapter = adapterr;
    }

    override fun startActivity(intent: Intent?) {
        var fixedIntent: Intent? = null
        if (intent?.filterEquals(getMusicIntent()) ?: false) {
            if (TheGame.GameStats.tapeFile.isInitialized()) {
                fixedIntent = getMusicIntent(Uri.fromFile(TheGame.GameStats.tapeFile.value))
            }
            TheGame.sendEvent(TheGame.GameEvent.AUDIO_APP_OPENED)
        } else if (intent?.filterEquals(getChromeIntent()) ?: false) {
            TheGame.sendEvent(TheGame.GameEvent.CHROME_OPENED)
            if (TheGame.GameStats.isCameraChecked) {
                fixedIntent = getChromeIntent(Uri.parse(PASTEBIN_CAMERA_UNLOCKER_LINK))
            }
        } else if (intent?.filterEquals(getCameraIntent()) ?: false) {
            fixedIntent = getCameraIntent(Uri.fromFile(TheGame.GameStats.cameraFile.value))
            startActivityForResult(fixedIntent, CAMERA_REQUEST)
            return

        }
        super.startActivity(if (fixedIntent != null) fixedIntent else intent)
    }


}

val ACTION_TAKE_PHOTO = "take_photo"
fun TAKE_PHOTO_INTENT(context: Context = GameConfig.context) = Intent(context, LauncherActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        .setAction(ACTION_TAKE_PHOTO)
        .newTask()