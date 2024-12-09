package io.github.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.tbib.klocation.AccuracyPriority
import io.github.tbib.klocation.AndroidKLocationService
import io.github.tbib.klocation.KLocationService

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AndroidKLocationService.initialization(this, AccuracyPriority.BALANCED_POWER_ACCURACY)
        setContent {
            KLocationService().ListenerToPermission()
            App()
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
