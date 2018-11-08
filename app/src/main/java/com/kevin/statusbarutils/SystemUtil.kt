package com.kevin.statusbarutils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.text.TextUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


/**
 * 各个操作系统如Miui，Flyme相关操作工具类
 *
 * 此类中的相关方法涉及到io操作，请不要在主线程中直接调用
 *
 */
class SystemUtil {
    companion object {

        val ROM_MIUI = "MIUI"
        val ROM_EMUI = "EMUI"
        val ROM_FLYME = "FLYME"
        val ROM_OPPO = "OPPO"
        val ROM_SMARTISAN = "SMARTISAN"
        val ROM_VIVO = "VIVO"
        val ROM_QIKU = "QIKU"

        private val KEY_VERSION_MIUI = "ro.miui.ui.version.tag"
        private val KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code"
        private val KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name"

        private val KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage"

        private val KEY_VERSION_EMUI = "ro.build.version.emui"
        private val KEY_VERSION_OPPO = "ro.build.version.opporom"
        private val KEY_VERSION_SMARTISAN = "ro.smartisan.version"
        private val KEY_VERSION_VIVO = "ro.vivo.os.version"

        private var sName: String? = null
        private var sVersion: String? = null

        fun isEmui(): Boolean {
            return check(ROM_EMUI)
        }

        fun isMiui(): Boolean {
            return check(ROM_MIUI)
        }

        fun isVivo(): Boolean {
            return check(ROM_VIVO)
        }

        fun isOppo(): Boolean {
            return check(ROM_OPPO)
        }

        fun isFlyme(): Boolean {
            return check(ROM_FLYME)
        }

        fun is360(): Boolean {
            return check(ROM_QIKU) || check("360")
        }

        fun isSmartisan(): Boolean {
            return check(ROM_SMARTISAN)
        }

        fun getName(): String {
            if (sName == null) {
                check("")
            }
            return sName!!
        }

        fun getVersion(): String {
            if (sVersion == null) {
                check("")
            }
            return sVersion!!
        }

        fun check(rom: String): Boolean {
            if (sName != null) {
                return sName == rom
            }
            if (!TextUtils.isEmpty(getProp(KEY_VERSION_MIUI)) ||
                    !TextUtils.isEmpty(getProp(KEY_MIUI_VERSION_NAME)) ||
                    !TextUtils.isEmpty(getProp(KEY_MIUI_INTERNAL_STORAGE)) ||
                    !TextUtils.isEmpty(getProp(KEY_MIUI_VERSION_CODE))) {
                sVersion = getProp(KEY_VERSION_MIUI)
                sName = ROM_MIUI
            } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_EMUI))) {
                sVersion = getProp(KEY_VERSION_EMUI)
                sName = ROM_EMUI
            } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_OPPO))) {
                sVersion = getProp(KEY_VERSION_OPPO)
                sName = ROM_OPPO
            } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_VIVO))) {
                sVersion = getProp(KEY_VERSION_VIVO)
                sName = ROM_VIVO
            } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_SMARTISAN))) {
                sVersion = getProp(KEY_VERSION_SMARTISAN)
                sName = ROM_SMARTISAN
            } else {
                sVersion = Build.DISPLAY
                if (sVersion!!.toUpperCase().contains(ROM_FLYME)) {
                    sName = ROM_FLYME
                } else {
                    sVersion = Build.UNKNOWN
                    sName = Build.MANUFACTURER.toUpperCase()
                }
            }
            return sName == rom
        }

        fun getProp(name: String): String? {
            var line: String? = null
            var input: BufferedReader? = null
            try {
                val p = Runtime.getRuntime().exec("getprop " + name)
                input = BufferedReader(InputStreamReader(p.inputStream), 1024)
                line = input!!.readLine()
                input!!.close()
            } catch (ex: IOException) {
                return null
            } finally {
                if (input != null) {
                    try {
                        input!!.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
            return line
        }

        fun getCurrentProcessName(context: Context): String {
            try {
                val pid = android.os.Process.myPid()
                val activityManager = context.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                for (appProcess in activityManager.runningAppProcesses) {
                    if (appProcess.pid == pid) {
                        if (appProcess.processName != null) {
                            return appProcess.processName
                        }
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            return ""
        }

        fun isOverMIUI9(): Boolean {
            var overMIUI9 = false
            try {
                val miuiVersion = getSystemProperties("ro.miui.ui.version.name")
                if (!TextUtils.isEmpty(miuiVersion) && miuiVersion.length >= 2) {
                    val version = miuiVersion.substring(1).toInt()
                    if (version >= 9) {
                        overMIUI9 = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return overMIUI9
        }

        private fun getSystemProperties(key: String): String {
            try {
                val osSystem = Class.forName("android.os.SystemProperties")
                val getInvoke = osSystem.getMethod("get", String::class.java)
                return getInvoke.invoke(osSystem, key) as String
            } catch (e1: Exception) {
                e1.printStackTrace()
            }

            return ""
        }
    }
}