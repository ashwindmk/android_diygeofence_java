# DIY Geofence Java

This Android library is developed to provide geofence enter and exit callbacks without using the native geofencing functionalities.


### Installation

app/build.gradle

```gradle
...
dependencies {
    ...
    implementation 'com.ashwin.android:diy-geofence-java:0.0.+'
}
```


### Initialization

```java
public class YourApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the SDK
        DiyGeofence.init(this);

        ...
    }
}
```


### Configurations

```java
public class YourApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ...

        // Set -1 for stop logging
        DiyGeofence.setLogLevel(Log.VERBOSE);

        // This will start periodic location updates
        DiyGeofence.setAutoLocationUpdates(this, true);

        // You will get geofence on-enter and on-exit callbacks in this instance
        DiyGeofence.setGeofenceListener(new YourGeofenceListener());
    }
}
```


### Usage

You will get these callbacks on Main thread.

```java
public class YourGeofenceListener implements DiyGeofenceListener {
    @Override
    public void onEnter(Context context, String id) {
        // Entered geofence: ${id}
    }

    @Override
    public void onExit(Context context, String id) {
        // Exited geofence: ${id}
    }
}
```

Make sure to register this class in your Application class:

```java
public class YourApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ...

        DiyGeofence.setGeofenceListener(new YourGeofenceListener());
    }
}
```
