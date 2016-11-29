package codes.evo.screenshotlib;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;

public interface ScreenshotController {

    interface ScreenshotListener {

        void onScreenTaken(Image image);
    }

    interface ScreenSaveListener {

        void onScreenSaved(String screenPath);
    }

    void startProjection();

    void requestPermission(Activity activity);

    void stopProjection();

    void takeScreen() throws ScreenshotException;

    void handleActivityResult(int requestCode, int resultCode, Intent data);

    boolean isEnabledScreenSharing();

    void setScreenshotListener(ScreenshotListener listener);
}
