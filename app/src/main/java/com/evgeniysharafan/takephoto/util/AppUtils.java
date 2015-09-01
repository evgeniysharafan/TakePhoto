package com.evgeniysharafan.takephoto.util;

import android.text.TextUtils;
import android.widget.ImageView;

import com.evgeniysharafan.takephoto.util.lib.CircleTransformation;
import com.evgeniysharafan.takephoto.util.lib.L;
import com.evgeniysharafan.takephoto.util.lib.Res;
import com.evgeniysharafan.takephoto.util.lib.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.File;

public class AppUtils {

    private static Transformation roundedTransformation = new CircleTransformation(0, 0);

    public static void loadImage(String image, ImageView view, boolean round) {
        if (!TextUtils.isEmpty(image)) {
            RequestCreator request = Picasso.with(Utils.getApp()).load(image)
                    .resize(Res.getDisplayMetrics().widthPixels, Res.getDisplayMetrics().heightPixels)
                    .centerInside()
                    .onlyScaleDown();

            if (round) {
                request.transform(roundedTransformation);
            }

            request.into(view);
        } else {
            L.e("image url is empty");
        }
    }

    public static void loadImage(File photo, ImageView view) {
        if (photo != null && !TextUtils.isEmpty(photo.getPath())) {
            RequestCreator request = Picasso.with(Utils.getApp()).load(photo)
                    .resize(Res.getDisplayMetrics().widthPixels, Res.getDisplayMetrics().heightPixels)
                    .centerInside()
                    .onlyScaleDown();
            request.into(view);
        } else {
            L.e("photo file is empty");
        }
    }

}
