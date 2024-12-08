package io.github.tbib.klocation

import android.app.Activity
import java.lang.ref.WeakReference

enum class AccuracyPriority(val value: Int) {
    HIGH_ACCURACY(100),
    BALANCED_POWER_ACCURACY(102),
    LOW_POWER(104),
    PASSIVE(105);
}

object AndroidKLocationService {
    private var activity: WeakReference<Activity?> = WeakReference(null)
    private var accuracyPriority: AccuracyPriority? = null

    internal fun getActivity(): Activity {
        return activity.get()!!
    }

    internal fun getAccuracyPriority(): AccuracyPriority {
        return accuracyPriority!!
    }

    fun initialization(activity: Activity, priority: AccuracyPriority) {
        this.activity = WeakReference(activity)
        this.accuracyPriority = priority

    }
}