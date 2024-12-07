package io.github.tbib.klocation

import android.app.Activity
import java.lang.ref.WeakReference

object AndroidKLocationService {
    private var activity: WeakReference<Activity?> = WeakReference(null)

    internal fun getActivity(): Activity {
        return activity.get()!!

    }

    fun initialization(activity: Activity) {
        this.activity = WeakReference(activity)

    }
}