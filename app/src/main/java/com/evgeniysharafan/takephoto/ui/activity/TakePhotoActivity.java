package com.evgeniysharafan.takephoto.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.evgeniysharafan.takephoto.R;
import com.evgeniysharafan.takephoto.ui.fragment.TakePhotoFragment;
import com.evgeniysharafan.takephoto.util.lib.Fragments;
import com.evgeniysharafan.takephoto.util.lib.OnBackPressedListener;

public class TakePhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_fragment);

        if (savedInstanceState == null) {
            Fragments.replace(getSupportFragmentManager(), R.id.content, TakePhotoFragment.newInstance(), null);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = Fragments.getById(getSupportFragmentManager(), R.id.content);
        if (Fragments.isFragmentAdded(fragment) && OnBackPressedListener.class.isAssignableFrom(fragment.getClass())) {
            if (((OnBackPressedListener) fragment).onBackPressed()) {
                return;
            }
        }

        super.onBackPressed();
    }
}
