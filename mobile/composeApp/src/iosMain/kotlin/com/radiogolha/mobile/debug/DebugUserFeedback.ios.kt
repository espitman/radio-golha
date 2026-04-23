package com.radiogolha.mobile.debug

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSTimer
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake

private const val TOAST_TAG: Long = 942531L

@OptIn(ExperimentalForeignApi::class)
actual fun showDebugToast(message: String) {
    NSOperationQueue.mainQueue.addOperationWithBlock {
        val window = UIApplication.sharedApplication.keyWindow ?: return@addOperationWithBlock
        window.viewWithTag(TOAST_TAG)?.removeFromSuperview()

        val toast = UIView()
        toast.tag = TOAST_TAG
        toast.backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.82)
        toast.layer.cornerRadius = 12.0
        toast.layer.masksToBounds = true

        val label = UILabel()
        label.text = message
        label.textColor = UIColor.whiteColor
        label.numberOfLines = 2
        label.textAlignment = NSTextAlignmentCenter
        label.font = UIFont.fontWithName("Vazirmatn-Regular", 12.0)
            ?: UIFont.fontWithName("Vazir", 12.0)
            ?: UIFont.systemFontOfSize(12.0)

        val windowWidth = window.bounds.useContents { size.width }
        val windowHeight = window.bounds.useContents { size.height }
        val maxToastWidth = windowWidth - 28.0

        val labelSize = label.sizeThatFits(CGSizeMake(maxToastWidth - 20.0, 80.0)).useContents {
            width to height
        }
        val toastWidth = minOf(maxToastWidth, maxOf(120.0, labelSize.first + 20.0))
        val toastHeight = maxOf(32.0, labelSize.second + 12.0)
        val toastX = (windowWidth - toastWidth) / 2.0
        val toastY = windowHeight - toastHeight - 28.0

        toast.setFrame(CGRectMake(toastX, toastY, toastWidth, toastHeight))
        label.setFrame(CGRectMake(10.0, 6.0, toastWidth - 20.0, toastHeight - 12.0))
        toast.addSubview(label)
        window.addSubview(toast)

        NSTimer.scheduledTimerWithTimeInterval(
            interval = 1.4,
            repeats = false
        ) { _: NSTimer? ->
            toast.removeFromSuperview()
        }
    }
}
