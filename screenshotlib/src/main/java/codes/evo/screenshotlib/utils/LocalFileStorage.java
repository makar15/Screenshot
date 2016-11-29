package codes.evo.screenshotlib.utils;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

public class LocalFileStorage {

    private static final String TAG = "LocalFileStorage";
    private static final String PHOTO_EXT = ".jpg";

    private static String MEDIA_PATH;

    public static void init(Context context) {
        File mediaDirExternalStorageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        MEDIA_PATH = mediaDirExternalStorageDir == null ? null : mediaDirExternalStorageDir.getAbsolutePath();
        createNonExistingDir(MEDIA_PATH);
    }

    public static boolean hasAccessToSdCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static String getPhotoFilePath(String name) {
        return getMediaFilePath(name + PHOTO_EXT);
    }

    private static String getMediaFilePath(String fileName) {
        return new File(MEDIA_PATH + fileName).getPath();
    }

    private static boolean createNonExistingDir(@Nullable String dirPath) {
        if (dirPath == null) {
            return false;
        }

        File dir = new File(dirPath);
        if (dir.exists() && dir.isDirectory()) {
            return true;
        }

        dir.delete();
        dir.mkdirs();
        if (!dir.isDirectory()) {
            Log.e(TAG, "Cannot create directory: " + dirPath);
            return false;
        }
        return true;
    }
}
