package io.github.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.sample.theme.AppTheme
import io.github.tbib.klocation.KLocationService
import kotlinx.coroutines.launch

@Composable
internal fun App() = AppTheme {
    var isInit by remember { mutableStateOf(false) }


    val viewModel by remember { mutableStateOf(LocationViewModel()) }

    val scope = rememberCoroutineScope()
    // LaunchedEffect for requesting permission
    LaunchedEffect(isInit) {
        if (!isInit) {
            viewModel.init()
            isInit = true

        }

    }
    val userLocation by remember { derivedStateOf { viewModel.userLocation } }
    val isGPSOpen by remember { derivedStateOf { viewModel.isGPSOpen } }

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userLocation) {
        isLoading = userLocation == null
    }
    if (viewModel.requestPermission) {
        KLocationService().EnableLocation()
        viewModel.requestPermission = false
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            Text(
                text = "Fetching location...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = "User Location:",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Latitude: ${userLocation?.latitude}\nLongitude: ${userLocation?.longitude}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )

            ElevatedButton(onClick = {
                scope.launch {
                    println("location ${viewModel.getLocation()}")
                }
            }) {
                Text("print location")
            }
        }

        TopToast(
            isVisible = !isGPSOpen,
            message = "GPS is not active. Tap to enable.",
            onClick = {
                viewModel.enableGPSAndLocation()
            }
        )
    }
}

@Composable
fun TopToast(
    message: String,
    isVisible: Boolean,
    onClick: () -> Unit
) {
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.error,
                    shape = MaterialTheme.shapes.medium
                )
                .clickable { onClick() }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onError,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
