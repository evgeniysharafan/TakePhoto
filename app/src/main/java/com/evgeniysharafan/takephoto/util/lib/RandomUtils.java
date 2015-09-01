package com.evgeniysharafan.takephoto.util.lib;

import android.text.format.DateUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.Random;

/**
 * Not thread safe.
 */
public final class RandomUtils {

    private static final Random random;
    private static final char[] chars;
    private static final char[] charsDigits;
    private static final StringBuilder stringBuilder;
    private static final String loremIpsum;

    static {
        random = new Random();
        chars = " 1234567890 abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ !@#$%^&*() -= _+ \\| ,./ <>? ;' :\" [] {} ".toCharArray();
        charsDigits = "1234567890".toCharArray();
        stringBuilder = new StringBuilder();
        loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
    }

    private RandomUtils() {
    }

    public static int getInt() {
        return random.nextInt();
    }

    public static boolean getBool() {
        return random.nextBoolean();
    }

    /**
     * Returns a pseudo-RANDOM uniformly distributed {@code int} in the
     * half-open range [0, n). If n == 0 returns 0.
     */
    public static int getInt(int n) {
        return n > 0 ? random.nextInt(n) : 0;
    }

    public static String getString(int maxLength) {
        int length = getInt(maxLength + 1);

        stringBuilder.delete(0, stringBuilder.length());
        for (int i = 0; i < length; ++i) {
            stringBuilder.append(chars[getInt(chars.length)]);
        }

        return stringBuilder.toString();
    }

    public static String getLoremString(int maxLength) {
        int length = getInt(maxLength > loremIpsum.length() ? loremIpsum.length() : maxLength + 1);
        return loremIpsum.substring(0, length);
    }

    public static String getPhoneNumber() {
        boolean isShortFormat = getInt(10) % 2 == 0;
        int length = isShortFormat ? 9 : 12;

        stringBuilder.delete(0, stringBuilder.length());

        stringBuilder.append(isShortFormat ? "0" : "+");

        for (int i = 0; i < length; ++i) {
            stringBuilder.append(charsDigits[getInt(charsDigits.length)]);
        }

        return stringBuilder.toString();
    }

    public static long getDateInMillis(int maxOffsetInDays) {
        int minusDays = getInt(maxOffsetInDays + 1);
        long minusSeconds = getInt((int) (DateUtils.DAY_IN_MILLIS / DateUtils.SECOND_IN_MILLIS));

        return System.currentTimeMillis() - (minusDays > 0 ? minusDays * DateUtils.DAY_IN_MILLIS
                : DateUtils.DAY_IN_MILLIS) + (minusSeconds * DateUtils.SECOND_IN_MILLIS);
    }

    public static LatLng getCoordinates(LatLng point, int radiusInMeters) {
        return SphericalUtil.computeOffset(point, RandomUtils.getInt(radiusInMeters), RandomUtils.getInt(360));
    }

}
