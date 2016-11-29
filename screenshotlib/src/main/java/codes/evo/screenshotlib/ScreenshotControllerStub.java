package codes.evo.screenshotlib;

import android.app.Activity;
import android.content.Intent;

public class ScreenshotControllerStub implements ScreenshotController {

    @Override
    public void requestPermission(Activity activity) {

    }

    @Override
    public void startProjection() {

    }

    @Override
    public void stopProjection() {

    }

    @Override
    public void takeScreen() throws ScreenshotException {

    }

    @Override
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public boolean isEnabledScreenSharing() {
        return false;
    }

    @Override
    public void setScreenshotListener(ScreenshotListener listener) {

    }
}
