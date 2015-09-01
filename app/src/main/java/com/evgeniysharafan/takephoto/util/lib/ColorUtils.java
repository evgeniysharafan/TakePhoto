package com.evgeniysharafan.takephoto.util.lib;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;

public final class ColorUtils {

    private ColorUtils() {
    }

    /**
     * Calculate whether a color is light or dark, based on a commonly known
     * brightness formula.
     *
     * @param brightnessThreshold 0...255. Use -1 for default value (130)
     * @see {@literal http://en.wikipedia.org/wiki/HSV_color_space%23Lightness}
     */
    public static boolean isColorDark(int color, int brightnessThreshold) {
        return ((30 * Color.red(color) +
                59 * Color.green(color) +
                11 * Color.blue(color)) / 100) <= (brightnessThreshold == -1 ? 130 : brightnessThreshold);
    }

    /**
     * @param alpha 0...255
     */
    public static int setColorAlpha(int color, float alpha) {
        return Color.argb(Math.min(Math.max((int) (alpha * 255.0f), 0), 255),
                Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * @param factor     0.8f
     * @param scaleAlpha false
     */
    public static int scaleColor(int color, float factor, boolean scaleAlpha) {
        return Color.argb(scaleAlpha ? (Math.round(Color.alpha(color) * factor)) : Color.alpha(color),
                Math.round(Color.red(color) * factor), Math.round(Color.green(color) * factor),
                Math.round(Color.blue(color) * factor));
    }

    /**
     * Desaturates and color-scrims the image
     *
     * @param scrimAlpha      0 = invisible, 1 = visible image. Use -1 for default value (0.25f)
     * @param scrimSaturation 0 = gray, 1 = color image. Use -1 for default value (0.2f)
     */
    public static ColorFilter makeImageScrimColorFilter(int color, float scrimAlpha, float scrimSaturation) {
        float a = (scrimAlpha == -1 ? 0.25f : scrimAlpha);
        float sat = (scrimSaturation == -1 ? 0.2f : scrimAlpha);
        return new ColorMatrixColorFilter(new float[]{
                ((1 - 0.213f) * sat + 0.213f) * a, ((0 - 0.715f) * sat + 0.715f) * a,
                ((0 - 0.072f) * sat + 0.072f) * a, 0, Color.red(color) * (1 - a),
                ((0 - 0.213f) * sat + 0.213f) * a, ((1 - 0.715f) * sat + 0.715f) * a,
                ((0 - 0.072f) * sat + 0.072f) * a, 0, Color.green(color) * (1 - a),
                ((0 - 0.213f) * sat + 0.213f) * a, ((0 - 0.715f) * sat + 0.715f) * a,
                ((1 - 0.072f) * sat + 0.072f) * a, 0, Color.blue(color) * (1 - a),
                0, 0, 0, 0, 255
        });
    }

}
