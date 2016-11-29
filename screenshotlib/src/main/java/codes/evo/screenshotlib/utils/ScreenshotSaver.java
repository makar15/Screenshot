package codes.evo.screenshotlib.utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import codes.evo.screenshotlib.ScreenshotController;
import codes.evo.screenshotlib.ScreenshotException;

@TargetApi(19)
public class ScreenshotSaver implements Runnable {

    private static final String TAG = "ScreenshotSaver";

    private final Image mImage;
    private final String mScreenName;

    private ScreenshotController.ScreenSaveListener mListener;

    public ScreenshotSaver(Image image, String screenName) {
        mImage = image;
        mScreenName = screenName;
    }

    public void setScreenSaveListener(ScreenshotController.ScreenSaveListener listener) {
        mListener = listener;
    }

    @Override
    public void run() {
        FileOutputStream stream = null;
        Bitmap bitmap = null;

        try {
            if (mImage == null) {
                throw new ScreenshotException("Image null");
            }

            Image.Plane[] planes = mImage.getPlanes();
            if (planes[0].getBuffer() == null) {
                throw new ScreenshotException("Image format is private, because the " +
                        "image pixel data is not directly accessible");
            }

            ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * mImage.getWidth();
            int bitmapWidth = mImage.getWidth() + rowPadding / pixelStride;

            bitmap = Bitmap.createBitmap(bitmapWidth, mImage.getHeight(), Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);

            String screenName = mScreenName;
            String screenPath = LocalFileStorage.getPhotoFilePath(screenName);
            stream = new FileOutputStream(screenPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            if (mListener != null) {
                mListener.onScreenSaved(screenPath);
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to obtain a screenshot image", e);
        } finally {
            CloseableUtils.close(stream);
            if (bitmap != null) {
                bitmap.recycle();
            }
//            if (mImage != null) {
//                mImage.close();
//            }
        }
    }
}
