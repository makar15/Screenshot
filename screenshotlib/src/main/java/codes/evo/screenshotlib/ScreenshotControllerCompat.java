package codes.evo.screenshotlib;

import android.content.Context;
import android.os.Build;

import codes.evo.screenshotlib.utils.BackgroundWorker;

public class ScreenshotControllerCompat {

    public static ScreenshotController get(Context context, BackgroundWorker backgroundWorker) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new ScreenshotControllerLollipop(context, backgroundWorker);
        } else {
            return new ScreenshotControllerStub();
        }
    }
}
