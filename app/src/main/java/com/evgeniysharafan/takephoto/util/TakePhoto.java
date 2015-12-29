package com.evgeniysharafan.takephoto.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.evgeniysharafan.takephoto.R;
import com.evgeniysharafan.utils.ExifHelper;
import com.evgeniysharafan.utils.IO;
import com.evgeniysharafan.utils.L;
import com.evgeniysharafan.utils.PrefUtils;
import com.evgeniysharafan.utils.Res;
import com.evgeniysharafan.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
public class TakePhoto {

    /**
     * Enable the Photo button when you get one of these callbacks.
     */
    public interface OnPhotoTakenListener {
        void onPhotoTaken(File photo);

        void onPhotoError();
    }

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final SimpleDateFormat PHOTO_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    private static final int REQUEST_CODE_SYSTEM_CHOOSER = 141;
    private static final int REQUEST_CODE_CAMERA = 142;
    private static final int REQUEST_CODE_GALLERY = 143;

    private static final TakePhoto instance = new TakePhoto();
    private File photoFile;
    private OnPhotoTakenListener photoTakenListener;
    // we use this file if we get the result between onStop() and onStart(), in this case listener is null.
    private File completedFile;
    // we use this flag if we get an error between onStop() and onStart(), in this case listener is null.
    private boolean hasError;
    private boolean isInProgress;
    private boolean isCancelled;
    private TargetImpl picassoTarget;

    private TakePhoto() {
    }

    public static TakePhoto getInstance() {
        return instance;
    }

    // Custom dialog with chooser
    public void showDialogChooser(Fragment fragment) {
        if (createPhotoFile()) {
            ChooseDialog dialog = ChooseDialog.newInstance(fragment);
            dialog.show(fragment.getFragmentManager(), "");
        }
    }

    public void showSystemChooser(Activity activity) {
        if (createPhotoFile()) {
            Intent intent = getSystemChooserIntent(activity.getPackageManager());
            activity.startActivityForResult(intent, REQUEST_CODE_SYSTEM_CHOOSER);
        }
    }

    public void showSystemChooser(Fragment fragment) {
        if (createPhotoFile()) {
            Intent intent = getSystemChooserIntent(fragment.getActivity().getPackageManager());
            fragment.startActivityForResult(intent, REQUEST_CODE_SYSTEM_CHOOSER);
        }
    }

    public void setPhotoTakenListenerIfNeeded(OnPhotoTakenListener listener) {
        if (listener != null && !isCancelled) {
            if (isInProgress()) {
                photoTakenListener = listener;
            } else if (completedFile != null) {
                photoTakenListener = listener;
                fireSuccess(completedFile);
            } else if (hasError) {
                photoTakenListener = listener;
                fireError();
            }
        } else {
            photoTakenListener = null;
        }
    }

    public boolean isInProgress() {
        return isInProgress;
    }

    public void cancelCurrentProcessingIfNeeded() {
        if (isInProgress()) {
            isCancelled = true;
            isInProgress = false;
        }
    }

    private boolean createPhotoFile() {
        photoFile = null;
        File albumDir = getAlbumDir();
        if (albumDir != null) {
            String imageFileName = JPEG_FILE_PREFIX + PHOTO_DATE_FORMAT.format(new Date());
            photoFile = new File(albumDir, imageFileName + JPEG_FILE_SUFFIX);
            PrefUtils.put(JPEG_FILE_PREFIX, photoFile.getAbsolutePath());
        } else {
            L.e("Storage is unmounted");
        }

        return photoFile != null;
    }

    private File getAlbumDir() {
        File storageDir = null;
        if (IO.isMediaStorageMounted()) {
            storageDir = Utils.getApp().getExternalCacheDir();
            if (storageDir != null && !storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    L.e("Failed to create directory");
                }
            }
        } else {
            L.e("External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private Intent getSystemChooserIntent(PackageManager packageManager) {
        // Camera
        List<Intent> cameraIntents = new ArrayList<>();
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        List<ResolveInfo> listCameraActivities = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCameraActivities) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));

            cameraIntents.add(intent);
        }

        // Gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);

        Intent chooserIntent = Intent.createChooser(galleryIntent, Res.getString(R.string.photo_select_source));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(
                new Parcelable[cameraIntents.size()]));
        return chooserIntent;
    }

    private void takePhoto(Fragment fragment) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        fragment.startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA);
    }

    private void launchGallery(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        fragment.startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    public boolean isPhotoRequestOk(int requestCode, int resultCode) {
        return (requestCode == REQUEST_CODE_SYSTEM_CHOOSER || requestCode == REQUEST_CODE_CAMERA
                || requestCode == REQUEST_CODE_GALLERY) && resultCode == Activity.RESULT_OK;
    }

    /**
     * Call isPhotoRequestOk() before, if true disable the Photo button until you get OnPhotoTakenListener callback
     */
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data, final OnPhotoTakenListener listener) {
        if (!isPhotoRequestOk(requestCode, resultCode)) {
            throw new IllegalStateException("isPhotoRequestOk() should be called before onActivityResult");
        }

        photoTakenListener = listener;
        isInProgress = true;
        isCancelled = false;
        completedFile = null;
        hasError = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                getPhoto(requestCode, resultCode, data);
            }
        }).start();
    }

    // gets the image in background thread
    private void getPhoto(int requestCode, int resultCode, Intent data) {
        // system chooser
        if (requestCode == REQUEST_CODE_SYSTEM_CHOOSER) {
            boolean isCamera;

            if (data == null || data.getData() == null) {
                isCamera = true;
            } else {
                final String action = data.getAction();
                isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
            }

            requestCode = isCamera ? REQUEST_CODE_CAMERA : REQUEST_CODE_GALLERY;
        }

        if (photoFile == null) {
            photoFile = new File(PrefUtils.getString(JPEG_FILE_PREFIX, null));
        }

        // we need to create a copy because user can press the Photo button again when current process is
        // in progress (if developer forgot to disable this button until current processing is finished).
        File currentFile = new File(photoFile.getPath());

        // dialog chooser
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                rotateIfNeeded(currentFile);
                break;

            case REQUEST_CODE_GALLERY:
                if (data != null && data.getData() != null) {
                    try {
                        if (isMediaStorage(data.getData()) || isFile(data.getData())) {
                            String path = getPathFromContentUri(data.getData());
                            if (path != null) {
                                IO.copyFile(new File(path), currentFile);
                                rotateIfNeeded(currentFile);
                            } else {
                                fireError();
                            }
                        } else {
                            getImageFromExternalContentProvider(data.getData(), currentFile);
                        }
                    } catch (Exception e) {
                        L.e(e);
                        fireError();
                    }
                } else {
                    fireError();
                }

                break;

            default:
                throw new IllegalStateException("case for requestCode " + requestCode + " is not defined");
        }
    }

    private boolean isFile(Uri uri) {
        return ContentResolver.SCHEME_FILE.equals(uri.getScheme());
    }

    private boolean isMediaStorage(Uri uri) {
        return ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) &&
                MediaStore.AUTHORITY.equals(uri.getAuthority());
    }

    // Convert the image URI to the direct file system path of the image file
    private String getPathFromContentUri(Uri contentUri) {
        String path = null;

        if (isMediaStorage(contentUri)) {
            Cursor cursor = null;
            try {
                cursor = Utils.getApp().getContentResolver().query(contentUri,
                        new String[]{MediaStore.Images.Media.DATA}, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                }
            } catch (Exception e) {
                L.e(e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (isFile(contentUri)) {
            path = contentUri.getPath();
        }

        return path;
    }

    private void getImageFromExternalContentProvider(Uri uri, File file) throws IOException {
        InputStream is = null;
        OutputStream outStream = null;

        if (uri.getAuthority() != null) {
            try {
                is = Utils.getApp().getContentResolver().openInputStream(uri);
                outStream = new FileOutputStream(file);
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                if (is != null) {
                    while ((bytesRead = is.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }

                rotateIfNeeded(file);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }

                    if (outStream != null) {
                        outStream.close();
                    }
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private void rotateIfNeeded(final File file) {
        int orientation = getOrientationFromContentUri(Uri.fromFile(file));
        if ((orientation % 360) != 0) {
            Utils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // we need to have a strong reference to the target
                    picassoTarget = new TargetImpl(file);
                    Picasso.with(Utils.getApp()).load(file).into(picassoTarget);
                }
            });
        } else {
            fireSuccess(file);
        }
    }

    private void fireSuccess(final File file) {
        Utils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (photoTakenListener != null && !isCancelled) {
                    photoTakenListener.onPhotoTaken(file);
                }

                completedFile = (photoTakenListener == null && !isCancelled) ? new File(file.getPath()) : null;
                setPhotoTakenListenerIfNeeded(null);
                hasError = false;
                isInProgress = false;
            }
        });
    }

    private void fireError() {
        if (photoFile != null) {
            photoFile.delete();
            photoFile = null;
        }

        Utils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (photoTakenListener != null && !isCancelled) {
                    photoTakenListener.onPhotoError();
                }

                hasError = photoTakenListener == null && !isCancelled;
                setPhotoTakenListenerIfNeeded(null);
                completedFile = null;
                isInProgress = false;
            }
        });
    }

    private int getOrientationFromContentUri(Uri contentUri) {
        int orientation = 0;

        if (isMediaStorage(contentUri)) {
            Cursor cursor = null;
            try {
                cursor = Utils.getApp().getContentResolver().query(contentUri,
                        new String[]{MediaStore.Images.Media.ORIENTATION}, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    orientation = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION));
                }
            } catch (Exception e) {
                L.e(e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (isFile(contentUri)) {
            orientation = ExifHelper.getExifOrientation(contentUri.getPath());
        }

        return orientation;
    }

    private void saveRotatedBitmap(File file, Bitmap rotatedBitmap) throws IOException {
        FileOutputStream out = null;
        try {
            ExifHelper exifHelper = new ExifHelper();
            exifHelper.readExifData(file.getPath());

            out = new FileOutputStream(file);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            exifHelper.setOrientation(String.valueOf(ExifInterface.ORIENTATION_NORMAL));
            exifHelper.setImageWidth(String.valueOf(rotatedBitmap.getWidth()));
            exifHelper.setImageLength(String.valueOf(rotatedBitmap.getHeight()));
            exifHelper.writeExifData(file.getPath());
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (Exception e) {
                L.e(e);
            }
        }
    }

    public Date getDate(String imagePath) {
        try {
            String imageName = imagePath.substring(imagePath.indexOf(JPEG_FILE_PREFIX) + JPEG_FILE_PREFIX.length());
            return PHOTO_DATE_FORMAT.parse(imageName);
        } catch (ParseException e) {
            L.e(e);
            return new Date();
        }
    }

    // Custom dialog with chooser
    public static final class ChooseDialog extends DialogFragment {

        private static final int TAKE_PHOTO_POSITION = 0;
        private static final int CHOOSE_FROM_GALLERY_POSITION = 1;

        static ChooseDialog newInstance(Fragment fragment) {
            ChooseDialog dialog = new ChooseDialog();
            dialog.setTargetFragment(fragment, 0);

            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.photo_select_source)
                    .setItems(R.array.photo_choose_dialog, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case TAKE_PHOTO_POSITION:
                                    TakePhoto.getInstance().takePhoto(getTargetFragment());
                                    break;
                                case CHOOSE_FROM_GALLERY_POSITION:
                                    TakePhoto.getInstance().launchGallery(getTargetFragment());
                                    break;
                                default:
                                    throw new IllegalStateException("case for " + which + " is not defined");
                            }
                        }
                    })
                    .create();
        }
    }

    private class TargetImpl implements Target {

        private final File file;

        public TargetImpl(File file) {
            this.file = file;
        }

        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        saveRotatedBitmap(file, bitmap);
                        fireSuccess(file);
                    } catch (IOException e) {
                        L.e(e);
                        fireError();
                    }
                }
            }).start();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            L.e("onBitmapFailed");
            fireError();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    }

}
