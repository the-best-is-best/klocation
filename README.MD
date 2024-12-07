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
implementation("io.github.the-best-is-best:klocation:1.0.2")
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
AndroidKLocationService.initialization(this, AccuracyPriority.BALANCED_POWER_ACCURACY)
```

### in commonMain

```kotlin
  
@Composable
internal fun App() = AppTheme {
    var isInit by remember { mutableStateOf(false) }

    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) {
        factory.createPermissionsController()
    }

    BindEffect(controller)

    val viewModel by remember { mutableStateOf(LocationViewModel(controller)) }

    // LaunchedEffect for requesting permission
    LaunchedEffect(isInit) {
        if(!isInit) {
            viewModel.init()
            isInit = true
        }

    }
    val userLocation by remember { derivedStateOf { viewModel.userLocation } }
    val isGPSNotOpen by remember { derivedStateOf { viewModel.isGPSNotOpen } }
    val isLocationGranted by remember { derivedStateOf { viewModel.permissionLocation } }

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userLocation) {
        isLoading = userLocation == null
    }

    LaunchedEffect(isLocationGranted){
        if(isLocationGranted){
            viewModel.addListenerLocation()
        }
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
        }

        TopToast(
            isVisible = !isGPSNotOpen,
            message = "GPS is not active. Tap to enable.",
            onClick = { viewModel.enableLocation() }
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



class LocationViewModel(private  val controller: PermissionsController): ViewModel() {
    private val locationService = KLocationService()

     var userLocation by  mutableStateOf<Location?>(null)
    var isGPSNotOpen by mutableStateOf(false)

     var permissionLocation by  mutableStateOf(false)

    fun init () {
        viewModelScope.launch {
            if (!permissionLocation) {
                controller.providePermission(Permission.LOCATION)
                permissionLocation = controller.isPermissionGranted(Permission.LOCATION)
            }
            // Request location permission if not already granted
            locationService.gpsStateFlow().collect { isGps ->
                isGPSNotOpen = isGps
            }

        }
//        viewModelScope.launch {
//
//            if(permissionLocation) {
//                locationService.startLocationUpdates().collect { newLocation ->
//                    userLocation = newLocation
//                }
//            }

        //        }
    }
        fun addListenerLocation() {
            viewModelScope.launch {
                if (controller.isPermissionGranted(Permission.LOCATION)) {
                    locationService.startLocationUpdates().collect { newLocation ->
                        userLocation = newLocation
                    }
                }
            }
        }


    fun enableLocation(){
        locationService.enableLocation()
    }
}

```

### Note can check GPS and enable it in v 1.0.2

```kotlin
  val locationService = KLocationService()
   location.gpsStateFlow() // listener for gps
   location.enableLocation()
   location.isLocationEnabled() //for check location is enabled or not one time
```

### Note can check for is location enabled in v 1.0.1

```kotlin
  val locationService = KLocationService()
val isEnabledLocation = locationService.isLocationEnabled()
```
