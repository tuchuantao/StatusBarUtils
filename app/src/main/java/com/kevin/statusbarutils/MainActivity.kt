package com.kevin.statusbarutils

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.contentView
import org.jetbrains.anko.onClick

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DisplayUtil.initDisplayOpinion(this)
        initView()
        bg_is_black.performClick()
    }

    private fun initView() {
        bg_is_black.onClick {
            DisplayUtil.translucentStatusBar(this, false)
            if (DisplayUtil.isTranslucent() && DisplayUtil.canControlStatusBarTextColor()) {
                contentView?.backgroundColor = Color.BLACK
            } else {
                contentView?.backgroundColor = DisplayUtil.calculateStatusColor(Color.WHITE, DisplayUtil.DEFAULT_STATUS_BAR_WHITE_ALPHA)
            }
        }

        bg_is_white.onClick {
            DisplayUtil.translucentStatusBar(this, true)
            if (DisplayUtil.isTranslucent() && DisplayUtil.canControlStatusBarTextColor()) {
                contentView?.backgroundColor = Color.WHITE
            } else {
                contentView?.backgroundColor = DisplayUtil.calculateStatusColor(Color.WHITE, DisplayUtil.DEFAULT_STATUS_BAR_WHITE_ALPHA)
            }
        }
    }
}
