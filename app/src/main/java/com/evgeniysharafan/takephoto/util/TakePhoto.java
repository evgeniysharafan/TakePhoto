package com.evgeniysharafan.takephoto.util;

import android.app.Activity;
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
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.evgeniysharafan.takephoto.R;
import com.evgeniysharafan.utils.ExifHelper;
import com.evgeniysharafan.utils.IO;
import com.evgeniysharafan.utils.L;
import com.evgeniysharafan.utils.PrefUtils;
import com.evgeniysharafan.utils.Res;
import com.evgeniysharafan.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestCreator;
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

    public static final String DO_NOT_PROCESS_KEY = "do_not_process_key";

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final SimpleDateFormat PHOTO_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    private static final int REQUEST_CODE_SYSTEM_CHOOSER = 141;
    private static final int REQUEST_CODE_CAMERA = 142;
    private static final int REQUEST_CODE_GALLERY = 143;

    private static final String STATE_FILE_PATH = "state_file_path";

    private static final String STATE_HAS_PICASSO_REQUEST = "state_has_picasso_request";
    private static final String STATE_HAS_SIZE = "state_has_size";
    private static final String STATE_TARGET_WIDTH = "state_target_width";
    private static final String STATE_TARGET_HEIGHT = "state_target_height";
    private static final String STATE_CENTER_CROP = "state_center_crop";
    private static final String STATE_CENTER_INSIDE = "state_center_inside";
    private static final String STATE_ONLY_SCALE_DOWN = "state_only_scale_down";
    private static final String STATE_ROTATION_DEGREES = "state_rotation_degrees";
    private static final String STATE_HAS_ROTATION_PIVOT = "state_has_rotation_pivot";
    private static final String STATE_ROTATION_PIVOT_X = "state_rotation_pivot_x";
    private static final String STATE_ROTATION_PIVOT_Y = "state_rotation_pivot_y";

    private static final TakePhoto instance = new TakePhoto();

    private File photoFile;
    private Request picassoRequest;

    private OnPhotoTakenListener photoTakenListener;

    // we use this file if we get the result between onStop() and onStart(), in this case listener is null.
    private File completedFile;
    // we use this flag if we get an error between onStop() and onStart(), in this case listener is null.
    private boolean hasError;

    private boolean isProcessingInProgress;
    private boolean isProcessingCancelled;

    private TargetImpl picassoTarget;

    private TakePhoto() {
    }

    public static TakePhoto getInstance() {
        return instance;
    }

    public void showSystemChooser(Activity activity) {
        showSystemChooser(activity, null);
    }

    /**
     * @param picassoRequest Supported methods: resize, centerCrop, centerInside, onlyScaleDown, rotate (both).
     *                       Example: TakePhoto.getInstance().showSystemChooser(this, new Request.Builder(42)
     *                       .resize(400, 400).centerCrop().build());
     *                       <p>If you don't want to process a photo and get it as is with wrong orientation on Samsung devices,
     *                       add stableKey(TakePhoto.DO_NOT_PROCESS_KEY) to your Picasso request.</p>
     */
    public void showSystemChooser(Activity activity, @Nullable Request picassoRequest) {
        if (createPhotoFile(picassoRequest)) {
            Intent intent = getSystemChooserIntent(activity.getPackageManager());
            activity.startActivityForResult(intent, REQUEST_CODE_SYSTEM_CHOOSER);
        }
    }

    public void showSystemChooser(Fragment fragment) {
        showSystemChooser(fragment, null);
    }

    /**
     * @param picassoRequest Supported methods: resize, centerCrop, centerInside, onlyScaleDown, rotate (both).
     *                       Example: TakePhoto.getInstance().showSystemChooser(this, new Request.Builder(42)
     *                       .resize(400, 400).centerCrop().build());
     *                       <p>If you don't want to process a photo and get it as is with wrong orientation on Samsung devices,
     *                       add stableKey(TakePhoto.DO_NOT_PROCESS_KEY) to your Picasso request.</p>
     */
    public void showSystemChooser(Fragment fragment, @Nullable Request picassoRequest) {
        if (createPhotoFile(picassoRequest)) {
            Intent intent = getSystemChooserIntent(fragment.getActivity().getPackageManager());
            fragment.startActivityForResult(intent, REQUEST_CODE_SYSTEM_CHOOSER);
        }
    }

    // Custom dialog with chooser
    public void showDialogChooser(Fragment fragment) {
        showDialogChooser(fragment, null);
    }

    /**
     * @param picassoRequest Supported methods: resize, centerCrop, centerInside, onlyScaleDown, rotate (both).
     *                       Example: TakePhoto.getInstance().showDialogChooser(this, new Request.Builder(42)
     *                       .resize(400, 400).centerCrop().build());
     *                       <p>If you don't want to process a photo and get it as is with wrong orientation on Samsung devices,
     *                       add stableKey(TakePhoto.DO_NOT_PROCESS_KEY) to your Picasso request.</p>
     */
    // Custom dialog with chooser
    public void showDialogChooser(Fragment fragment, @Nullable Request picassoRequest) {
        if (createPhotoFile(picassoRequest)) {
            ChooseDialog dialog = ChooseDialog.newInstance(fragment);
            dialog.show(fragment.getFragmentManager(), "");
        }
    }

    public void showCamera(Fragment fragment) {
        showCamera(fragment, null);
    }

    /**
     * @param picassoRequest Supported methods: resize, centerCrop, centerInside, onlyScaleDown, rotate (both).
     *                       Example: TakePhoto.getInstance().showCamera(this, new Request.Builder(42)
     *                       .resize(400, 400).centerCrop().build());
     *                       <p>If you don't want to process a photo and get it as is with wrong orientation on Samsung devices,
     *                       add stableKey(TakePhoto.DO_NOT_PROCESS_KEY) to your Picasso request.</p>
     */
    public void showCamera(Fragment fragment, @Nullable Request picassoRequest) {
        if (createPhotoFile(picassoRequest)) {
            takePhoto(fragment);
        }
    }

    public void showGallery(Fragment fragment) {
        showGallery(fragment, null);
    }

    /**
     * @param picassoRequest Supported methods: resize, centerCrop, centerInside, onlyScaleDown, rotate (both).
     *                       Example: TakePhoto.getInstance().showGallery(this, new Request.Builder(42)
     *                       .resize(400, 400).centerCrop().build());
     *                       <p>If you don't want to process a photo and get it as is with wrong orientation on Samsung devices,
     *                       add stableKey(TakePhoto.DO_NOT_PROCESS_KEY) to your Picasso request.</p>
     */
    public void showGallery(Fragment fragment, @Nullable Request picassoRequest) {
        if (createPhotoFile(picassoRequest)) {
            launchGallery(fragment);
        }
    }

    public void setPhotoTakenListenerIfNeeded(OnPhotoTakenListener listener) {
        if (listener != null && !isProcessingCancelled) {
            if (isProcessingInProgress()) {
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

    public boolean isProcessingInProgress() {
        return isProcessingInProgress;
    }

    public void cancelCurrentProcessingIfInProgress() {
        if (isProcessingInProgress()) {
            isProcessingCancelled = true;
            isProcessingInProgress = false;
        }
    }

    private boolean createPhotoFile(@Nullable Request request) {
        photoFile = null;
        picassoRequest = null;

        File albumDir = getAlbumDir();
        if (albumDir != null) {
            String imageFileName = JPEG_FILE_PREFIX + PHOTO_DATE_FORMAT.format(new Date());
            photoFile = new File(albumDir, imageFileName + JPEG_FILE_SUFFIX);
            savePhotoFile();

            picassoRequest = request;
            savePicassoRequestIfExists();
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
        galleryIntent.setType("image/*");

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
        intent.setType("image/*");
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
                                 final Intent data, OnPhotoTakenListener listener) {
        if (!isPhotoRequestOk(requestCode, resultCode)) {
            throw new IllegalStateException("isPhotoRequestOk() should be called before onActivityResult");
        }

        photoTakenListener = listener;
        isProcessingInProgress = true;
        isProcessingCancelled = false;
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
            // it means our process has been killed while the camera was running
            restorePhotoFile();
            restorePicassoRequestIfExists();
        }

        // we need to create a copy because user can press the Photo button again when current process is
        // in progress (if developer forgot to disable this button until current processing is finished).
        File currentFile = new File(photoFile.getPath());

        // dialog chooser
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                processIfNeeded(currentFile);
                break;

            case REQUEST_CODE_GALLERY:
                if (data != null && data.getData() != null) {
                    try {
                        if (isMediaStorage(data.getData()) || isFile(data.getData())) {
                            String path = getPathFromContentUri(data.getData());
                            if (path != null) {
                                IO.copyFile(new File(path), currentFile);
                                processIfNeeded(currentFile);
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

                processIfNeeded(file);
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

    private void processIfNeeded(File file) {
        if (!hasDoNotProcessKey() && (hasPicassoRequest() || needRotate(file))) {
            process(file);
        } else {
            fireSuccess(file);
        }
    }

    private boolean hasDoNotProcessKey() {
        return hasPicassoRequest() && DO_NOT_PROCESS_KEY.equals(picassoRequest.stableKey);
    }

    private boolean needRotate(File file) {
        int orientation = getOrientationFromContentUri(Uri.fromFile(file));
        return (orientation % 360) != 0;
    }

    private void process(final File file) {
        Utils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // we need to have a strong reference to the target
                picassoTarget = new TargetImpl(file);
                RequestCreator requestCreator = Picasso.with(Utils.getApp()).load(file);

                if (hasPicassoRequest()) {
                    if (picassoRequest.hasSize()) {
                        requestCreator.resize(picassoRequest.targetWidth, picassoRequest.targetHeight);
                    }
                    if (picassoRequest.centerCrop) {
                        requestCreator.centerCrop();
                    }
                    if (picassoRequest.centerInside) {
                        requestCreator.centerInside();
                    }
                    if (picassoRequest.onlyScaleDown) {
                        requestCreator.onlyScaleDown();
                    }
                    if (picassoRequest.rotationDegrees != 0) {
                        if (picassoRequest.hasRotationPivot) {
                            requestCreator.rotate(picassoRequest.rotationDegrees,
                                    picassoRequest.rotationPivotX, picassoRequest.rotationPivotY);
                        } else {
                            requestCreator.rotate(picassoRequest.rotationDegrees);
                        }
                    }
                }

                requestCreator.into(picassoTarget);
            }
        });
    }

    private void fireSuccess(final File file) {
        Utils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (photoTakenListener != null && !isProcessingCancelled) {
                    photoTakenListener.onPhotoTaken(file);
                }

                completedFile = (photoTakenListener == null && !isProcessingCancelled) ? new File(file.getPath()) : null;
                setPhotoTakenListenerIfNeeded(null);
                hasError = false;
                isProcessingInProgress = false;
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
                if (photoTakenListener != null && !isProcessingCancelled) {
                    photoTakenListener.onPhotoError();
                }

                hasError = photoTakenListener == null && !isProcessingCancelled;
                setPhotoTakenListenerIfNeeded(null);
                completedFile = null;
                isProcessingInProgress = false;
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

    public void clearAlbumDir() {
        IO.deleteFilesInDir(getAlbumDir(), false);
    }

    public void clearAlbumDirRemainCount(int remainLatestPhotosCount) {
        IO.deleteFilesInDirRemainCount(getAlbumDir(), remainLatestPhotosCount, false);
    }

    public void clearAlbumDirRemainDays(int remainFilesForDays) {
        IO.deleteFilesInDirRemainDays(getAlbumDir(), remainFilesForDays, false);
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

    private void savePhotoFile() {
        PrefUtils.put(STATE_FILE_PATH, photoFile.getAbsolutePath());
    }

    private void restorePhotoFile() {
        photoFile = new File(PrefUtils.getString(STATE_FILE_PATH, null));
    }

    private boolean hasPicassoRequest() {
        return picassoRequest != null;
    }

    private void savePicassoRequestIfExists() {
        PrefUtils.put(STATE_HAS_PICASSO_REQUEST, hasPicassoRequest());
        if (hasPicassoRequest()) {
            PrefUtils.put(STATE_HAS_SIZE, picassoRequest.hasSize());
            PrefUtils.put(STATE_TARGET_WIDTH, picassoRequest.targetWidth);
            PrefUtils.put(STATE_TARGET_HEIGHT, picassoRequest.targetHeight);
            PrefUtils.put(STATE_CENTER_CROP, picassoRequest.centerCrop);
            PrefUtils.put(STATE_CENTER_INSIDE, picassoRequest.centerInside);
            PrefUtils.put(STATE_ONLY_SCALE_DOWN, picassoRequest.onlyScaleDown);
            PrefUtils.put(STATE_ROTATION_DEGREES, picassoRequest.rotationDegrees);
            PrefUtils.put(STATE_HAS_ROTATION_PIVOT, picassoRequest.hasRotationPivot);
            PrefUtils.put(STATE_ROTATION_PIVOT_X, picassoRequest.rotationPivotX);
            PrefUtils.put(STATE_ROTATION_PIVOT_Y, picassoRequest.rotationPivotY);
        }
    }

    private void restorePicassoRequestIfExists() {
        boolean hasPicassoRequest = PrefUtils.getBool(STATE_HAS_PICASSO_REQUEST, false);
        if (hasPicassoRequest) {
            Request.Builder builder = new Request.Builder(42);

            boolean hasSize = PrefUtils.getBool(STATE_HAS_SIZE, false);
            if (hasSize) {
                int targetWidth = PrefUtils.getInt(STATE_TARGET_WIDTH, 0);
                int targetHeight = PrefUtils.getInt(STATE_TARGET_HEIGHT, 0);
                builder.resize(targetWidth, targetHeight);
            }

            boolean centerCrop = PrefUtils.getBool(STATE_CENTER_CROP, false);
            if (centerCrop) {
                builder.centerCrop();
            }

            boolean centerInside = PrefUtils.getBool(STATE_CENTER_INSIDE, false);
            if (centerInside) {
                builder.centerInside();
            }

            boolean onlyScaleDown = PrefUtils.getBool(STATE_ONLY_SCALE_DOWN, false);
            if (onlyScaleDown) {
                builder.onlyScaleDown();
            }

            float rotationDegrees = PrefUtils.getFloat(STATE_ROTATION_DEGREES, 0);
            if (rotationDegrees != 0) {
                boolean hasRotationPivot = PrefUtils.getBool(STATE_HAS_ROTATION_PIVOT, false);
                if (hasRotationPivot) {
                    float rotationPivotX = PrefUtils.getFloat(STATE_ROTATION_PIVOT_X, 0);
                    float rotationPivotY = PrefUtils.getFloat(STATE_ROTATION_PIVOT_Y, 0);
                    builder.rotate(rotationDegrees, rotationPivotX, rotationPivotY);
                } else {
                    builder.rotate(rotationDegrees);
                }
            }

            picassoRequest = builder.build();
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
