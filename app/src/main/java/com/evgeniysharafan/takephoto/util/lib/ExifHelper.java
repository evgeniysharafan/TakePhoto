package com.evgeniysharafan.takephoto.util.lib;

import android.annotation.TargetApi;
import android.location.Location;
import android.media.ExifInterface;
import android.os.Build;

import java.io.IOException;

public class ExifHelper {

    private String orientation;
    private String datetime;
    private String make;
    private String model;
    private String flash;
    private String imageWidth;
    private String imageLength;
    private String gpsLatitude;
    private String gpsLongitude;
    private String gpsLatitudeRef;
    private String gpsLongitudeRef;

    private String exposureTime;
    private String aperature;
    private String iso;

    private String gpsAltitude;
    private String gpsAltitudeRef;
    private String gpsTimestamp;
    private String gpsDateStamp;
    private String whiteBalance;
    private String focalLength;
    private String gpsProcessingMethod;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void readExifData(String filePath) {
        ExifInterface inputExif;
        try {
            inputExif = new ExifInterface(filePath);
        } catch (IOException e) {
            L.e(e);
            return;
        }

        orientation = inputExif.getAttribute(ExifInterface.TAG_ORIENTATION);
        datetime = inputExif.getAttribute(ExifInterface.TAG_DATETIME);
        make = inputExif.getAttribute(ExifInterface.TAG_MAKE);
        model = inputExif.getAttribute(ExifInterface.TAG_MODEL);
        flash = inputExif.getAttribute(ExifInterface.TAG_FLASH);
        imageWidth = inputExif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
        imageLength = inputExif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
        gpsLatitude = inputExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        gpsLongitude = inputExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        gpsLatitudeRef = inputExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        gpsLongitudeRef = inputExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

        if (Utils.hasHoneycomb()) {
            exposureTime = inputExif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
            aperature = inputExif.getAttribute(ExifInterface.TAG_APERTURE);
            iso = inputExif.getAttribute(ExifInterface.TAG_ISO);
        }

        gpsAltitude = inputExif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
        gpsAltitudeRef = inputExif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF);
        gpsTimestamp = inputExif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
        gpsDateStamp = inputExif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
        whiteBalance = inputExif.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
        focalLength = inputExif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
        gpsProcessingMethod = inputExif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void writeExifData(String filePath) {
        ExifInterface outputExif;
        try {
            outputExif = new ExifInterface(filePath);
        } catch (IOException e) {
            L.e(e);
            return;
        }

        if (orientation != null) {
            outputExif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
        }

        if (datetime != null) {
            outputExif.setAttribute(ExifInterface.TAG_DATETIME, datetime);
        }

        if (make != null) {
            outputExif.setAttribute(ExifInterface.TAG_MAKE, make);
        }

        if (model != null) {
            outputExif.setAttribute(ExifInterface.TAG_MODEL, model);
        }

        if (flash != null) {
            outputExif.setAttribute(ExifInterface.TAG_FLASH, flash);
        }

        if (imageWidth != null) {
            outputExif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, imageWidth);
        }

        if (imageLength != null) {
            outputExif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, imageLength);
        }

        if (gpsLatitude != null) {
            outputExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, gpsLatitude);
        }

        if (gpsLongitude != null) {
            outputExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, gpsLongitude);
        }

        if (gpsLatitudeRef != null) {
            outputExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, gpsLatitudeRef);
        }

        if (gpsLongitudeRef != null) {
            outputExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, gpsLongitudeRef);
        }

        if (Utils.hasHoneycomb()) {
            if (exposureTime != null) {
                outputExif.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, exposureTime);
            }

            if (aperature != null) {
                outputExif.setAttribute(ExifInterface.TAG_APERTURE, aperature);
            }

            if (iso != null) {
                outputExif.setAttribute(ExifInterface.TAG_ISO, iso);
            }
        }

        if (gpsAltitude != null) {
            outputExif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, gpsAltitude);
        }

        if (gpsAltitudeRef != null) {
            outputExif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, gpsAltitudeRef);
        }

        if (gpsTimestamp != null) {
            outputExif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, gpsTimestamp);
        }

        if (gpsDateStamp != null) {
            outputExif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, gpsDateStamp);
        }

        if (whiteBalance != null) {
            outputExif.setAttribute(ExifInterface.TAG_WHITE_BALANCE, whiteBalance);
        }

        if (focalLength != null) {
            outputExif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, focalLength);
        }

        if (gpsProcessingMethod != null) {
            outputExif.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, gpsProcessingMethod);
        }

        try {
            outputExif.saveAttributes();
        } catch (IOException e) {
            L.e(e);
        }
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public void setImageWidth(String imageWidth) {
        this.imageWidth = imageWidth;
    }

    public void setImageLength(String imageLength) {
        this.imageLength = imageLength;
    }

    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(filepath);
        } catch (IOException e) {
            L.e(e);
        }

        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }

        return degree;
    }

    public static Location getExifLocation(String filepath) {
        Location location = null;

        try {
            final ExifInterface exif = new ExifInterface(filepath);
            final float[] latLong = new float[2];

            if (exif.getLatLong(latLong)) {
                location = new Location("");
                location.setLatitude(latLong[0]);
                location.setLongitude(latLong[1]);
            }
        } catch (IOException e) {
            L.e(e);
        }

        return location;
    }

}