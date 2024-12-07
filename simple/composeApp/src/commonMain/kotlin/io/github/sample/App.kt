package io.github.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import io.github.sample.theme.AppTheme
import io.github.tbib.klocation.KLocationService
import io.github.tbib.klocation.Location

@Composable
internal fun App() = AppTheme {
    val locationService = KLocationService()
    var userLocation by remember { mutableStateOf<Location?>(null) }

    var permissionLocation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(permissionLocation) {
        if (userLocation == null) {
            userLocation = locationService.getCurrentLocation()
        }
    }
//
//    DisposableEffect(locationService) {
//        val job = coroutineScope.launch {
//            locationService.startLocationUpdates().collect { newLocation ->
//                userLocation = newLocation
//            }
//        }
//        onDispose {
//            job.cancel()
//        }
//    }


    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) {
        factory.createPermissionsController()
    }

    BindEffect(controller)
    LaunchedEffect(permissionLocation) {
        if (!permissionLocation) {
            controller.providePermission(Permission.LOCATION)
            permissionLocation = controller.isPermissionGranted(Permission.LOCATION)

        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "UserLocation  lat ${userLocation?.latitude} - lon ${userLocation?.longitude}",
//            fontFamily = FontFamily(Font(Res.font.IndieFlower_Regular)),
            style = MaterialTheme.typography.displayLarge
        )

    }
}
