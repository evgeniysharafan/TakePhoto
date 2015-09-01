package com.evgeniysharafan.takephoto.util.lib;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

public class CircleTransformation implements Transformation {

    private final int borderColor;
    private final int borderSize;
    private String key;

    /**
     * @param borderColor use 0 if no border
     * @param borderSize  use 0 if no border
     */
    public CircleTransformation(int borderColor, int borderSize) {
        this.borderColor = borderColor;
        this.borderSize = borderSize;
        key = "circle(color=" + borderColor + ", size=" + borderSize + ")";
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap.Config config = source.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, config);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;

        if (borderSize > 0) {
            Paint bg = new Paint();
            bg.setColor(borderColor);
            bg.setAntiAlias(true);
            canvas.drawCircle(r, r, r, bg);
        }

        canvas.drawCircle(r, r, r - borderSize, paint);
        squaredBitmap.recycle();

        return bitmap;
    }

    @Override
    public String key() {
        return key;
    }

}
