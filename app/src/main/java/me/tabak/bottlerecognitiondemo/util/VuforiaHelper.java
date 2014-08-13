package me.tabak.bottlerecognitiondemo.util;

import android.content.Context;

import me.tabak.bottlerecognitiondemo.R;

public class VuforiaHelper {
    // These codes match the ones defined in TargetFinder in Vuforia.jar
    public static final int INIT_SUCCESS = 2;
    public static final int INIT_ERROR_NO_NETWORK_CONNECTION = -1;
    public static final int INIT_ERROR_SERVICE_NOT_AVAILABLE = -2;
    public static final int UPDATE_ERROR_AUTHORIZATION_FAILED = -1;
    public static final int UPDATE_ERROR_PROJECT_SUSPENDED = -2;
    public static final int UPDATE_ERROR_NO_NETWORK_CONNECTION = -3;
    public static final int UPDATE_ERROR_SERVICE_NOT_AVAILABLE = -4;
    public static final int UPDATE_ERROR_BAD_FRAME_QUALITY = -5;
    public static final int UPDATE_ERROR_UPDATE_SDK = -6;
    public static final int UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE = -7;
    public static final int UPDATE_ERROR_REQUEST_TIMEOUT = -8;

    // Returns the error message for each error code
    public String getStatusDescription(Context context, int code)
    {
        switch (code) {
            case UPDATE_ERROR_AUTHORIZATION_FAILED:
                return context.getString(R.string.UPDATE_ERROR_AUTHORIZATION_FAILED_DESC);
            case UPDATE_ERROR_PROJECT_SUSPENDED:
                return context.getString(R.string.UPDATE_ERROR_PROJECT_SUSPENDED_DESC);
            case UPDATE_ERROR_NO_NETWORK_CONNECTION:
                return context.getString(R.string.UPDATE_ERROR_NO_NETWORK_CONNECTION_DESC);
            case UPDATE_ERROR_SERVICE_NOT_AVAILABLE:
                return context.getString(R.string.UPDATE_ERROR_SERVICE_NOT_AVAILABLE_DESC);
            case UPDATE_ERROR_UPDATE_SDK:
                return context.getString(R.string.UPDATE_ERROR_UPDATE_SDK_DESC);
            case UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE:
                return context.getString(R.string.UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE_DESC);
            case UPDATE_ERROR_REQUEST_TIMEOUT:
                return context.getString(R.string.UPDATE_ERROR_REQUEST_TIMEOUT_DESC);
            case UPDATE_ERROR_BAD_FRAME_QUALITY:
                return context.getString(R.string.UPDATE_ERROR_BAD_FRAME_QUALITY_DESC);
            default:
                return context.getString(R.string.UPDATE_ERROR_UNKNOWN_DESC);
        }
    }

    public String getStatusTitle(Context context, int code)
    {
        switch (code) {
            case UPDATE_ERROR_AUTHORIZATION_FAILED:
                return context.getString(R.string.UPDATE_ERROR_AUTHORIZATION_FAILED_TITLE);
            case UPDATE_ERROR_PROJECT_SUSPENDED:
                return context.getString(R.string.UPDATE_ERROR_PROJECT_SUSPENDED_TITLE);
            case UPDATE_ERROR_NO_NETWORK_CONNECTION:
                return context.getString(R.string.UPDATE_ERROR_NO_NETWORK_CONNECTION_TITLE);
            case UPDATE_ERROR_SERVICE_NOT_AVAILABLE:
                return context.getString(R.string.UPDATE_ERROR_SERVICE_NOT_AVAILABLE_TITLE);
            case UPDATE_ERROR_UPDATE_SDK:
                return context.getString(R.string.UPDATE_ERROR_UPDATE_SDK_TITLE);
            case UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE:
                return context.getString(R.string.UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE_TITLE);
            case UPDATE_ERROR_REQUEST_TIMEOUT:
                return context.getString(R.string.UPDATE_ERROR_REQUEST_TIMEOUT_TITLE);
            case UPDATE_ERROR_BAD_FRAME_QUALITY:
                return context.getString(R.string.UPDATE_ERROR_BAD_FRAME_QUALITY_TITLE);
            default:
                return context.getString(R.string.UPDATE_ERROR_UNKNOWN_TITLE);
        }
    }
}
