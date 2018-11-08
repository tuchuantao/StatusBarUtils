package com.kevin.statusbarutils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.support.v4.view.ViewCompat
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.kevin.statusbarutils.SystemUtil.Companion.isFlyme
import com.kevin.statusbarutils.SystemUtil.Companion.isMiui
import com.kevin.statusbarutils.SystemUtil.Companion.isOverMIUI9


object DisplayUtil {

    var screenWidthPx: Int = 0 //屏幕宽 px
    var screenHeightPx: Int = 0 //屏幕高 px
    var density: Float = 0.toFloat()//屏幕密度
    var densityDPI: Int = 0//屏幕密度
    var screenWidthDip: Float = 0.toFloat()//  dp单位
    var screenHeightDip: Float = 0.toFloat()//  dp单位
    var statusBarHeight: Int = 0

    const val DEFAULT_STATUS_BAR_WHITE_ALPHA = 60

    /**
     * 在Application中初始化设备的尺寸信息
     */
    fun initDisplayOpinion(mContext: Activity) {
        val dm = mContext.resources.displayMetrics
        density = dm.density
        densityDPI = dm.densityDpi
        screenWidthPx = dm.widthPixels
        screenHeightPx = dm.heightPixels

        screenWidthDip = px2dip(mContext.applicationContext, dm.widthPixels).toFloat()
        screenHeightDip = px2dip(mContext.applicationContext, dm.heightPixels).toFloat()
        statusBarHeight = getStatusBarHeight(mContext)
    }

    /**
     * 获取设备状态栏的高度
     */
    private fun getStatusBarHeight(context: Activity): Int {
        val frame = Rect()
        context.window.decorView.getWindowVisibleDisplayFrame(frame)
        var statusBarHeight = frame.top
        if (statusBarHeight == 0) {
            statusBarHeight = context.resources.getDimensionPixelOffset(R.dimen.status_bar_height)
        }
        return statusBarHeight
    }

    /**
     * 设置沉浸式并设置状态栏字体颜色
     */
    fun translucentStatusBar(activity: Activity, isFontColorDark: Boolean = true): Boolean {
        // 设置MIUI或FLYME系统的状态栏字体颜色
        if (isMiui()) {
            setMiuiStatusBarLightMode(activity, isFontColorDark)
        } else if (isFlyme()) {
            setFlymeStatusBarLightMode(activity, isFontColorDark)
        }

        // 设置沉浸式和状态栏字体颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            if (isFontColorDark) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }

            val contentView = window.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
            val childView = contentView.getChildAt(0)
            if (childView != null) {
                childView.fitsSystemWindows = false
                ViewCompat.requestApplyInsets(childView)
            }
            return true
        }
        return false
    }

    /**
     * 是否能开启沉浸式
     */
    fun isTranslucent(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

    /**
     * 是否能改变状态栏字体颜色
     */
    fun canControlStatusBarTextColor(): Boolean {
        return isMiui() || isFlyme() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    private fun setMiuiStatusBarLightMode(activity: Activity, isFontColorDark: Boolean) {
        try {
            val window = activity.window
            if (window != null) {
                if (!isOverMIUI9() || Build.VERSION.SDK_INT < 23) {
                    val clazz = window::class.java
                    try {
                        var darkModeFlag = 0
                        val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                        val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                        darkModeFlag = field.getInt(layoutParams)
                        val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                        if (isFontColorDark) {
                            extraFlagField.invoke(window, darkModeFlag, darkModeFlag)//状态栏透明且黑色字体
                        } else {
                            extraFlagField.invoke(window, 0, darkModeFlag)//清除黑色字体
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setFlymeStatusBarLightMode(activity: Activity, isFontColorDark: Boolean): Boolean {
        var result = false
        val window = activity.window
        if (window != null) {
            try {
                val lp = window.attributes
                val darkFlag = WindowManager.LayoutParams::class.java
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java
                        .getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(lp)
                value = if (isFontColorDark) {
                    value or bit
                } else {
                    value and bit.inv()
                }
                meizuFlags.setInt(lp, value)
                window.attributes = lp
                result = true
            } catch (e: Exception) {

            }
        }
        return result
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dip(context: Context, pxValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }


    /**
     * 计算状态栏颜色
     *
     * @param color color值
     * @param alpha alpha值
     * @return 最终的状态栏颜色
     */
    fun calculateStatusColor(color: Int, alpha: Int): Int {
        val a = 1 - alpha / 255f
        var red = color shr 16 and 0xff
        var green = color shr 8 and 0xff
        var blue = color and 0xff
        red = (red * a + 0.5).toInt()
        green = (green * a + 0.5).toInt()
        blue = (blue * a + 0.5).toInt()
        return 0xff shl 24 or (red shl 16) or (green shl 8) or blue
    }
}

