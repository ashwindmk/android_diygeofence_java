# DIY Geofence Java




### Installation

app/build.gradle

```gradle
...
dependencies {
    ...
    implementation 'com.ashwin.android:diy-geofence-java:'
}
```


### Initialization

```java

```


### Configurations

```java

```

It is recommended to


### Usage

You will get these callbacks on Main thread.

```java
public class YourGeofenceListener implements DiyGeofenceListener {
    @Override
    public void onEnter(Context context, String id) {
        // Entered geofence with ${id}
    }

    @Override
    public void onExit(Context context, String id) {
        // Exited geofence with ${id}
    }
}
```
