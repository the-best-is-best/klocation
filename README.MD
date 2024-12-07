<h1 align="center">KLocation</h1><br>

<div align="center">
<a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
<a href="https://android-arsenal.com/api?level=21" rel="nofollow">
    <img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat" style="max-width: 100%;">
</a>
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg?logo=android" alt="Badge Android" />
  <img src="https://img.shields.io/badge/iOS-12%2B-blue.svg?logo=apple" alt="iOS 12+ Badge" />

<a href="https://github.com/the-best-is-best/"><img alt="Profile" src="https://img.shields.io/badge/github-%23181717.svg?&style=for-the-badge&logo=github&logoColor=white" height="20"/></a>
</div>

### KLocation is a Kotlin Multiplatform (KMP) or Compose Multiplatform (CMP) library designed to simplify retrieving the current location and handling location updates in Android and iOS. It provides a unified API for accessing location services across platforms, enabling developers to focus on building location-aware features without worrying about platform-specific implementations

<hr>

[![Maven Central](https://img.shields.io/maven-central/v/io.github.the-best-is-best/klocation)](https://central.sonatype.com/artifact/io.github.the-best-is-best/klocation)

KLocation is available on `mavenCentral()`.

## Install

```kotlin
implementation("io.github.the-best-is-best:klocation:1.0.1")
```

### First in iosMain

```kotlin
fun MainViewController(): UIViewController {
    IOSKLocationServices().requestPermission()
    return ComposeUIViewController { App() }
}

```

### Second in android Main

```kotlin
AndroidKLocationService.initialization(this)
```

### in commonMain

```kotlin
  val locationService = KLocationService()
var userLocation by remember { mutableStateOf<Location?>(null) }

var permissionLocation by remember { mutableStateOf(false) }
val coroutineScope = rememberCoroutineScope()

// for listener location
DisposableEffect(locationService) {
    val job = coroutineScope.launch {
        locationService.startLocationUpdates().collect { newLocation ->
            userLocation = newLocation
        }
    }
    onDispose {
        job.cancel()
    }
}

// for get current location
LaunchedEffect(permissionLocation) {
    if (userLocation == null) {
        userLocation = locationService.getCurrentLocation()
    }
}


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
```

### Note can check for is location enabled in v 1.0.1

```kotlin
  val locationService = KLocationService()
val isEnabledLocation = locationService.isLocationEnabled()
```
