package com.evgeniysharafan.takephoto.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.evgeniysharafan.takephoto.R;
import com.evgeniysharafan.takephoto.util.AppUtils;
import com.evgeniysharafan.takephoto.util.TakePhoto;
import com.evgeniysharafan.takephoto.util.TakePhoto.OnPhotoTakenListener;
import com.evgeniysharafan.utils.OnBackPressedListener;
import com.evgeniysharafan.utils.Toasts;
import com.squareup.picasso.Request;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.evgeniysharafan.takephoto.util.PermissionUtil.PermissionRequestCode;
import static com.evgeniysharafan.takephoto.util.PermissionUtil.PermissionRequestCode.STORAGE;
import static com.evgeniysharafan.takephoto.util.PermissionUtil.PermissionRequestCode.STORAGE_CROPPED;
import static com.evgeniysharafan.takephoto.util.PermissionUtil.STORAGE_PERMISSIONS;
import static com.evgeniysharafan.takephoto.util.PermissionUtil.getDeniedPermissions;
import static com.evgeniysharafan.takephoto.util.PermissionUtil.hasAllPermissions;
import static com.evgeniysharafan.takephoto.util.PermissionUtil.hasPermissionsResult;
import static com.evgeniysharafan.takephoto.util.PermissionUtil.setPermissionsResult;
import static com.evgeniysharafan.takephoto.util.PermissionUtil.shouldShowRationale;
import static com.evgeniysharafan.takephoto.util.PermissionUtil.showSnackbar;
import static com.evgeniysharafan.takephoto.util.PermissionUtil.showSnackbarWithOpenDetails;

public class TakePhotoFragment extends Fragment implements OnPhotoTakenListener, OnBackPressedListener {

    private static final String STATE_PHOTO = "state_photo";

    @Bind(R.id.add_image)
    ImageButton addImage;
    @Bind(R.id.image)
    ImageView image;
    @Bind(R.id.snackbar_container)
    CoordinatorLayout snackbarContainer;

    private Snackbar snackbar;

    public static TakePhotoFragment newInstance() {
        return new TakePhotoFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_take_photo, container, false);
        ButterKnife.bind(this, view);
        restoreState(savedInstanceState);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        TakePhoto.getInstance().setPhotoTakenListenerIfNeeded(this);
        setPhotoButtonEnabled(!TakePhoto.getInstance().isProcessingInProgress());
    }

    @Override
    public void onStop() {
        TakePhoto.getInstance().setPhotoTakenListenerIfNeeded(null);
        super.onStop();
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String photoFilePath = savedInstanceState.getString(STATE_PHOTO);
            if (!TextUtils.isEmpty(photoFilePath)) {
                AppUtils.loadImage(new File(photoFilePath), image);
                image.setTag(photoFilePath);
            }
        }
    }

    @OnClick(R.id.add_image)
    void addImageClick() {
        // we need to ask this permission because some galleries (e.g. HTC devices) don't return images without it.
        if (!hasAllPermissions(STORAGE_PERMISSIONS)) {
            askForPermissionsIfNeeded(STORAGE, STORAGE_PERMISSIONS);
            return;
        }

        TakePhoto.getInstance().showSystemChooser(this);
    }

    @OnClick(R.id.add_image_cropped)
    void addCroppedImageClick() {
        // we need to ask this permission because some galleries (e.g. HTC devices) don't return images without it.
        if (!hasAllPermissions(STORAGE_PERMISSIONS)) {
            askForPermissionsIfNeeded(STORAGE_CROPPED, STORAGE_PERMISSIONS);
            return;
        }

        TakePhoto.getInstance().showSystemChooser(this, new Request.Builder(42).resize(400, 400).centerCrop().build());
    }

    private void askForPermissionsIfNeeded(@PermissionRequestCode int requestCode, String... permissions) {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }

        if (hasAllPermissions(permissions)) {
            if (requestCode == STORAGE) {
                addImageClick();
            } else if (requestCode == STORAGE_CROPPED) {
                addCroppedImageClick();
            }
            return;
        }

        final String[] deniedPermissions = getDeniedPermissions(permissions);
        if (shouldShowRationale(getActivity(), deniedPermissions)) {
            snackbar = showSnackbarWithRequestPermissions(requestCode, deniedPermissions);
        } else {
            if (hasPermissionsResult(requestCode)) {
                snackbar = showSnackbarWithOpenDetails(snackbarContainer,
                        R.string.storage_permissions_rationale_text);
            } else {
                askForPermissions(requestCode, deniedPermissions);
            }
        }
    }

    private Snackbar showSnackbarWithRequestPermissions(@PermissionRequestCode final int requestCode,
                                                        final String... deniedPermissions) {
        return showSnackbar(snackbarContainer, R.string.storage_permissions_rationale_text,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        askForPermissions(requestCode, deniedPermissions);
                    }
                });
    }

    private void askForPermissions(@PermissionRequestCode int requestCode, String... deniedPermissions) {
        requestPermissions(deniedPermissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull int[] grantResults) {
        setPermissionsResult(requestCode);
        askForPermissionsIfNeeded(requestCode, permissions);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (TakePhoto.getInstance().isPhotoRequestOk(requestCode, resultCode)) {
            setPhotoButtonEnabled(false);
            TakePhoto.getInstance().onActivityResult(requestCode, resultCode, data, this);
        }
    }

    @Override
    public void onPhotoTaken(File photo) {
        AppUtils.loadImage(photo, image);
        image.setTag(photo.getPath());
        setPhotoButtonEnabled(true);
    }

    @Override
    public void onPhotoError() {
        Toasts.showLong(R.string.photo_error);
        setPhotoButtonEnabled(true);
    }

    private void setPhotoButtonEnabled(boolean enabled) {
        addImage.setEnabled(enabled);
    }

    // Use one of these methods to clear the folder with photos
    @SuppressWarnings("unused")
    private void clearCacheFolder() {
        TakePhoto.getInstance().clearCacheFolder();
        TakePhoto.getInstance().clearCacheFolderRemainCount(5);
        TakePhoto.getInstance().clearCacheFolderRemainDays(10);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_PHOTO, (String) image.getTag());
    }

    @Override
    public boolean onBackPressed() {
        TakePhoto.getInstance().cancelCurrentProcessingIfInProgress();
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
