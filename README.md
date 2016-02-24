# TakePhoto

Taking photos on Android is pain:

* You need to rotate photos on some devices using they EXIF data.
* When you get a photo from a gallery app, the photo can be stored on some external resource and you need to open an input stream to get it. Now it is a very common case because of Google Photos.
* You need to handle orientation changes even if your app has only one orientation. Sometimes when you press the save button in the camera app, current orientation changes and your current activity instance will be destroyed.
* You need to handle your saved state. Your current activity instance can be destroyed because the camera app needs a lot of memory.

This project [shows](app/src/main/java/com/evgeniysharafan/takephoto/ui/fragment/TakePhotoFragment.java)  how to use the [TakePhoto](app/src/main/java/com/evgeniysharafan/takephoto/util/TakePhoto.java) class. It helps to take pictures easy and with all needed checks and handlers.

The TakePhoto class contains two choosers: system chooser (showSystemChooser) and custom dialog (showDialogChooser). Or you can use showCamera and showGallery methods to launch camera or gallery without a chooser. Each method has a version with Request picassoRequest argument which can be used to modify returned image.

It handles orientation changes properly and returns result (success or error) even if this result was obtained between onStop() and onStart() methods when the listener was null.

It stores all files to the getExternalCacheDir() by default.

It uses [Picasso](https://github.com/square/picasso) for rotating and modifying images.

There are 3 methods to clear the photos directory:  
TakePhoto.getInstance().clearAlbumDir(); // clear all files  
TakePhoto.getInstance().clearAlbumDirRemainCount(5); // remain n latest files  
TakePhoto.getInstance().clearAlbumDirRemainDays(10); // remain files for n latest days
