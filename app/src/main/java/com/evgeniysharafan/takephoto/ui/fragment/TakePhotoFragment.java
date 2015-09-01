package com.evgeniysharafan.takephoto.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.evgeniysharafan.takephoto.util.lib.OnBackPressedListener;
import com.evgeniysharafan.takephoto.util.lib.Toasts;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class TakePhotoFragment extends Fragment implements OnPhotoTakenListener, OnBackPressedListener {

    private static final String STATE_PHOTO = "state_photo";

    @InjectView(R.id.add_image)
    ImageButton addImage;
    @InjectView(R.id.image)
    ImageView image;

    public static TakePhotoFragment newInstance() {
        return new TakePhotoFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_take_photo, container, false);
        ButterKnife.inject(this, view);
        restoreState(savedInstanceState);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        TakePhoto.getInstance().setPhotoTakenListenerIfNeeded(this);
        setPhotoButtonEnabled(!TakePhoto.getInstance().isInProgress());
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
        TakePhoto.getInstance().showSystemChooser(this);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_PHOTO, (String) image.getTag());
    }

    @Override
    public boolean onBackPressed() {
        TakePhoto.getInstance().cancelCurrentProcessingIfNeeded();
        return false;
    }
}
