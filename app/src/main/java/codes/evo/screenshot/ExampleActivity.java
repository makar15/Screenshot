package codes.evo.screenshot;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import codes.evo.screenshotlib.ScreenshotController;
import codes.evo.screenshotlib.ScreenshotControllerCompat;
import codes.evo.screenshotlib.ScreenshotException;
import codes.evo.screenshotlib.utils.BackgroundWorker;
import codes.evo.screenshotlib.utils.LocalFileStorage;
import codes.evo.screenshotlib.utils.ScreenshotSaver;

public class ExampleActivity extends AppCompatActivity {

    private static final String TAG = "ExampleActivity";

    // To start a task, you can use BackgroundWorker.Client or run a background thread in another way
    private BackgroundWorker.Client mBgClient;
    private ScreenshotController mScreenController;
    private String mScreenName;
    private int mCounter = 0;

    private final ScreenshotController.ScreenshotListener mScreenshotListener =
            new ScreenshotController.ScreenshotListener() {
                @Override
                public void onScreenTaken(Image image) {
                    showToast(ExampleActivity.this, "onScreenTaken");
                    ScreenshotSaver saver = new ScreenshotSaver(image, mScreenName);
                    saver.setScreenSaveListener(mScreenSaveListener);
                    mBgClient.post(saver);
                }
            };

    private final ScreenshotController.ScreenSaveListener mScreenSaveListener =
            new ScreenshotController.ScreenSaveListener() {
                @Override
                public void onScreenSaved(String screenPath) {
                    showToast(ExampleActivity.this, "onScreenSaved to : " + screenPath);
                    // Then you can send the screen to the cloud, or some other action
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);

        // If you'll use our ScreenshotSaver, you must initialize LocalFileStorage to save the screenshots on sdCard
        LocalFileStorage.init(this);

        BackgroundWorker backgroundWorker = new BackgroundWorker();
        mBgClient = backgroundWorker.getDefault();
        mScreenController = ScreenshotControllerCompat.get(this, backgroundWorker);
        mScreenController.requestPermission(this);
        mScreenController.setScreenshotListener(mScreenshotListener);

        Button start = (Button) findViewById(R.id.start_btn);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mScreenController.startProjection();
            }
        });

        final String name = "/file_name";
        Button capture = (Button) findViewById(R.id.capture_btn);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (mScreenController.isEnabledScreenSharing()) {
                        mScreenController.takeScreen();
                        mScreenName = name + "_" + String.valueOf(mCounter);
                        mCounter++;
                    }
                } catch (ScreenshotException e) {
                    Log.e(TAG, "You have not permission for screen capture", e);
                }
            }
        });

        Button close = (Button) findViewById(R.id.stop_btn);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mScreenController.stopProjection();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mScreenController.handleActivityResult(requestCode, resultCode, data);
    }

    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
