package com.evgeniysharafan.takephoto.util.lib;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;

public final class EncodeUtils {

    private EncodeUtils() {
    }

    public static String getBase64Encode(String stringToEncode, int flags) {
        return Base64.encodeToString(stringToEncode.getBytes(), flags);
    }

    public static String getBase64Decode(String stringToDecode, int flags) {
        return new String(Base64.decode(stringToDecode.getBytes(), flags));
    }

    public static long getAdler32(String string) {
        Adler32 adler32 = new Adler32();
        adler32.update(string.getBytes());

        return adler32.getValue();
    }

    public static String getAdler32Hex(String string) {
        return Long.toHexString(getAdler32(string));
    }

    public static String getMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes());
        } catch (NoSuchAlgorithmException e) {
            L.e(e);
            return "";
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    public static String getSHA1(String text) {
        String result = "";

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            byte[] sha1hash = md.digest();

            result = convertToHexString(sha1hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            L.e(e);
        }

        return result;
    }

    public static String convertToHexString(byte[] data) {
        StringBuilder buf = new StringBuilder();

        for (byte aData : data) {
            int halfByte = (aData >>> 4) & 0x0F;
            int twoHalves = 0;

            do {
                if ((0 <= halfByte) && (halfByte <= 9)) {
                    buf.append((char) ('0' + halfByte));
                } else {
                    buf.append((char) ('a' + (halfByte - 10)));
                }

                halfByte = aData & 0x0F;
            } while (twoHalves++ < 1);
        }

        return buf.toString();
    }

    /**
     * @param format  CompressFormat.JPEG, CompressFormat.PNG, CompressFormat.WEBP.
     * @param quality 0...100.
     */
    public static String convertBitmapToBase64(Bitmap image, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(format, quality, byteArrayOutputStream);

        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

}
