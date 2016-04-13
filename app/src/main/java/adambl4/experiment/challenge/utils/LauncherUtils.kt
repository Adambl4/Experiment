package adambl4.experiment.challenge.utils

import adambl4.experiment.challenge.TheGame.GameConfig
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Browser
import android.provider.MediaStore
import android.provider.Settings
import org.jetbrains.anko.newTask

/**
 * Created by Adambl4 on 11.04.2016.
 */


fun getChromeApplicationInfo(context: Context = GameConfig.context): ApplicationInfo? =
        try {
            context.packageManager.getApplicationInfo("com.android.chrome", 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

fun getSettingsApplicationInfo(context: Context = GameConfig.context): ApplicationInfo? =
        try {
            context.packageManager.getApplicationInfo("com.android.settings", 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

fun getCameraApplicationInfo(context: Context = GameConfig.context): ApplicationInfo? {
    val resolveInfo: ResolveInfo? = context.packageManager.resolveActivity(getCameraIntent(), 0)
    try {
        return context.packageManager.getApplicationInfo(resolveInfo?.activityInfo?.packageName ?: null, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        return null
    }
}

fun getMusicApplicationInfo(context: Context = GameConfig.context): ApplicationInfo? {
    val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN,
            Intent.CATEGORY_APP_MUSIC)
    val resolveInfo: ResolveInfo? = context.packageManager.resolveActivity(intent, 0)
    try {
        return context.packageManager.getApplicationInfo(resolveInfo?.activityInfo?.packageName ?: null, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        return null
    }
}

fun getFileMangerAppInfo(context: Context = GameConfig.context): ApplicationInfo? {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = "*/*";
    val resolveInfo: ResolveInfo? = context.packageManager.resolveActivity(intent, 0)
    try {
        return context.packageManager.getApplicationInfo(resolveInfo?.activityInfo?.packageName ?: null, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        return null
    }
}

fun getPhotoViewerAppInfo(context: Context = GameConfig.context): ApplicationInfo? {
    val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN,
            Intent.CATEGORY_APP_GALLERY)
    val resolveInfo: ResolveInfo? = context.packageManager.resolveActivity(intent, 0)
    try {
        return context.packageManager.getApplicationInfo(resolveInfo?.activityInfo?.packageName ?: null, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        return null
    }
}

fun getChromeIntent(uri: Uri? = Uri.parse("http://www.google.com"), context: Context = GameConfig.context): Intent =
        Intent(Intent.ACTION_VIEW).setPackage("com.android.chrome").putExtra(Browser.EXTRA_APPLICATION_ID, context.packageName).setData(uri).newTask()
fun getSettingsIntent(): Intent = Intent(Settings.ACTION_SETTINGS)
fun getCameraIntent(uri: Uri? = null): Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, uri)
fun getMusicIntent(uri: Uri? = null): Intent = if(uri == null ) Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC) else Intent(Intent.ACTION_VIEW).setDataAndType(uri, "audio/*");
fun getFileManagerIntent(): Intent = Intent(Intent.ACTION_GET_CONTENT).setType("*/*")
fun getPhotoViewerIntent(): Intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_GALLERY)
