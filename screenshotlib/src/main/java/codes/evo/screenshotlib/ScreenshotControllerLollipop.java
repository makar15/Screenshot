package codes.evo.screenshotlib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import codes.evo.screenshotlib.utils.BackgroundWorker;
import codes.evo.screenshotlib.utils.LocalFileStorage;

@TargetApi(21)
public class ScreenshotControllerLollipop implements ScreenshotController {

    private static final String TAG = "ScreenshotControllerLollipop";
    private static final String DISPLAY_NAME = "screenshot";
    private static final int PROJECTION_REQUEST_CODE = 123;
    private static final int DISPLAY_FLAG = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;

    private final BackgroundWorker.Client mBgClient;
    private final MediaProjectionManager mProjectionManager;
    private final int mWidth;
    private final int mHeight;
    private final int mDensity;

    private MediaProjection mProjection;
    private ImageReader mImageReader;
    private ScreenshotListener mListener;
    private boolean mEnabledScreenSharing;
    private Intent mSavedData;

    private final ImageReader.OnImageAvailableListener mImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    if (!LocalFileStorage.hasAccessToSdCard()) {
                        Log.e(TAG, "Do not access to external storage sources");
                        return;
                    }
                    if (mListener != null) {
                        Image image = reader.acquireNextImage();
                        mListener.onScreenTaken(image);
                        mImageReader.setOnImageAvailableListener(null, null);
                    }
                }
            };

    private final MediaProjection.Callback mProjectionStopCallback =
            new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    Log.d(TAG, "Stopping media projection");
                    if (mImageReader != null) {
                        mImageReader.setOnImageAvailableListener(null, null);
                        mImageReader.close();
                        mImageReader = null;
                    }
                }
            };

    public ScreenshotControllerLollipop(Context context, BackgroundWorker backgroundWorker) {
        mBgClient = backgroundWorker.getDefault();
        mProjectionManager = (MediaProjectionManager)
                context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getRealMetrics(metrics);
        mDensity = metrics.densityDpi;
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;
    }

    @Override
    public void startProjection() {
        startInternal();
    }

    @Override
    public void requestPermission(Activity activity) {
        if (mSavedData == null) {
            Intent intent = mProjectionManager.createScreenCaptureIntent();
            activity.startActivityForResult(intent, PROJECTION_REQUEST_CODE);
        }
    }

    @Override
    public void stopProjection() {
        if (mProjection != null) {
            mProjection.stop();
            mProjection = null;
        }
    }

    @Override
    public void takeScreen() throws ScreenshotException {
        if (mProjection == null) {
            throw new ScreenshotException("User denied screen sharing permission");
        }
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mImageReader.setOnImageAvailableListener(mImageAvailableListener, mBgClient.getHandler());

        mProjection.createVirtualDisplay(DISPLAY_NAME, mWidth, mHeight, mDensity,
                DISPLAY_FLAG, mImageReader.getSurface(), null, null);
    }

    @Override
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PROJECTION_REQUEST_CODE) {
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            Log.w(TAG, "User denied screen sharing permission");
            mEnabledScreenSharing = false;
            mSavedData = null;
            return;
        }
        Log.d(TAG, "Acquired permission to screen capture");
        mEnabledScreenSharing = true;
        mSavedData = data;
    }

    @Override
    public boolean isEnabledScreenSharing() {
        return mEnabledScreenSharing;
    }

    @Override
    public void setScreenshotListener(ScreenshotListener listener) {
        mListener = listener;
    }

    private void startInternal() {
        if (mProjection == null && mSavedData != null) {
            mProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, (Intent) mSavedData.clone());
            mProjection.registerCallback(mProjectionStopCallback, mBgClient.getHandler());
        }
    }
}
