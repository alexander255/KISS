package fr.neamar.kiss;

import android.app.Activity;
import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KissApplication {
    /**
     * Number of ms to wait, after a click occurred, to record a launch
     * Setting this value to 0 removes all animations
     */
    public static final int TOUCH_DELAY = 120;
    private static DataHandler dataHandler;
    private static CameraHandler cameraHandler;
    private static RootHandler rootHandler;
    private static IconsHandler iconsPackHandler;
    private static Activity activity = null;
    private static ExecutorService executor = Executors.newCachedThreadPool();

    private KissApplication() {
    }

    public static DataHandler getDataHandler(Context ctx) {
        if (dataHandler == null) {
            dataHandler = new DataHandler(ctx);
        }
        return dataHandler;
    }

    public static void setDataHandler(DataHandler newDataHandler) {
        dataHandler = newDataHandler;
    }

	public static void setMainActivity(Activity activity) {
		KissApplication.activity = activity;
	}
	
	public static void unsetMainActivity() {
		KissApplication.activity = null;
	}
	
	/**
	 * Get the current forground activity or `null` if no activity is currently running
	 */
	public static Activity getMainActivity() {
		return KissApplication.activity;
	}

    public static CameraHandler getCameraHandler() {
        if (cameraHandler == null) {
            cameraHandler = new CameraHandler();
        }
        return cameraHandler;
    }

    public static RootHandler getRootHandler(Context ctx) {
        if (rootHandler == null) {
            rootHandler = new RootHandler(ctx);
        }
        return rootHandler;
    }

    public static void resetRootHandler(Context ctx) {
        rootHandler.resetRootHandler(ctx);
    }

    public static void initDataHandler(Context ctx) {
        if (dataHandler == null) {
            dataHandler = new DataHandler(ctx);
        }
    }

    public static IconsHandler getIconsHandler(Context ctx) {
        if (iconsPackHandler == null) {
            iconsPackHandler = new IconsHandler(ctx);
        }

        return iconsPackHandler;
    }

    public static void resetIconsHandler(Context ctx) {
        iconsPackHandler = new IconsHandler(ctx);
    }

    public static ExecutorService getThreadPoolExecutor() {
        return executor;
    }

}
