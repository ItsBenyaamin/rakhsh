# Rakhsh DownloadManager Library
Rakhsh is a simple and elegant file DownloadManager library for Android written in Kotlin. 

<img src="/assets/screenshot_1.jpg" width="200" title="Rakhsh Download Manager library sample app" alt="Rakhsh Download Manager library sample app"/>

## Features
- Simple and easy to use
- Pause/Stop/Resume/Remove downloads
- Manage downloads by id or your choice of tag
- Provide a flow to get the latest list of downloads
- Provide a flow to get downloads progress info(overall, specific)
- Provide a flow to get latest downloads state changes
- Set group identifier for items
- Download all items with order(asc, desc)
- more on the way...

### Warning:
This library only download files to `Context.filesDir`.

You can specify path when enqueue an item for download but you should implement storing downloaded files in outside of app storage path yourself.

## Adding to your app
Add this line to your `repositories` block on setting.gradle:
```kotlin
maven { url = uri("https://jitpack.io") }
```
Then add this line to your build.gradle:
```kotlin
implementation("com.github.ItsBenyaamin:rakhsh:v1.1.0")
```

# Usage
It's very simple
```kotlin
// for default settings
downloadManager = Rakhsh.build(context)

// for more customization
downloadManager = Rakhsh.build(context) {
    debug() // log more info
    setTag("RDM") // default is RakhshDownloadManager
    setConnectionNum(4) // default if 6
    setDownloadChunk(1024 * 1024) // 1MB, default is 500KB
    setClient() // not any other client implemented yet, for now It uses HttpsUrlConnection
    setCoroutineScope() // set scope for library's internal coroutine launches
}
```
For enqueue a url for download:
```kotlin
viewModelScope.launch {
    // simple download request
    val id = downloadManager.enqueue(url, path, tag, group)
    
    // more advance
    val request = DownloadRequest(
        url = action.url,
        path = null,
        tag = null,
        group = null,
        headers = listOf(
            Pair("Header key", "Header value"),
            Pair("Header key1", "Header value2"),
        )
    )
    val id = downloadManager.enqueue(request)

    // you can call `prepare` on enqueue. This fetches file info from url and store it
    // this only fetches the info and not start the downlad process
    downloadManager.prepare(id)

    // or call `start` to fetch info then start it
    // if you already called the `prepare`, this will just start the download
    downloadManager.start(id)
}
```


For more advanced usage, check out the sample app

## Requirements
As we download from internet, don't forget to add internet permission to Manifest of your app:
```xml
<uses-permission android:name="android.permission.INTERNET"/>
```
