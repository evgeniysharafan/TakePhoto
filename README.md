# TakePhoto

Taking photos on Android is pain:

* You need to rotate photos on some devices using they EXIF data.
* When you get a photo from a gallery app, the photo can be stored on some external resource and you need to open an input stream to get it. Now it is a very common case because of Google Photos.
* You need to handle orientation changes even if your app has only one orientation. Sometimes when you press the save button in the camera app, current orientation changes and your current activity instance will be destroyed.
* You need to handle your saved state. Your current activity instance can be destroyed because the camera app needs a lot of memory.

This project shows how to use the [TakePhoto class](https://github.com/evgeniysharafan/TakePhoto/blob/master/app/src/main/java/com/evgeniysharafan/takephoto/util/TakePhoto.java). It helps to take pictures easy and with all needed checks and handlers.

The TakePhoto class contains two choosers: system chooser (showSystemChooser) and custom dialog (showDialogChooser). 

It handles orientation changes properly and returns result (success or error) even if this result has been obtained between onStop() and onStart() methods when the listener has been null.

It stores all files to the getExternalCacheDir().

It uses [Picasso](https://github.com/square/picasso) for rotating images. I believe their implementation for rotating is better and has less issues than I can write on my own.
