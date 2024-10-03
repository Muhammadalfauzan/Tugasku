package com.example.ecommerce.utils

import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyA11yDelegate : View.AccessibilityDelegate(){

    override fun onPopulateAccessibilityEvent(host: View, event: AccessibilityEvent) {
        super.onPopulateAccessibilityEvent(host, event)
        event?.let {
            if (it.text.size >0){
                it.text.clear()
                it.text.add("Censored!!")
            }
        }
    }

    override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(host, info)
        info?.let {
            if (it.text?.isNotEmpty()== true) {
                it.text = "Cencored!!"
            }
        }
    }
}