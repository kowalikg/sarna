package pl.edu.agh.sarna.cloak_and_dagger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import static android.view.WindowManager.LayoutParams.*;

import java.util.Collection;
import java.util.LinkedList;

public class OverlayManager {

    private static final int LAYOUT_TYPE = TYPE_TOAST;

    private static final int LAYOUT_FORMAT = PixelFormat.TRANSLUCENT;

    private static final int NOT_TOUCHABLE_FLAGS = FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCHABLE;

    private static final int TOUCHABLE_FLAGS = FLAG_NOT_FOCUSABLE;

    private Context context;

    private int displayWidth;

    private int displayHeight;

    private WindowManager manager;

    private Collection<View> overlays;

    private boolean transparentMode;

    private static final int TRANSPARENT_COLOR = 0x88EEEEFF;

    public OverlayManager(Context context, boolean transparentMode) {
        this.context = context;
        manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        assert manager != null;
        Display display = manager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayWidth = size.x;
        displayHeight = size.y;
        overlays = new LinkedList<>();
        this.transparentMode = transparentMode;
    }

    @SuppressLint("RtlHardcoded")
    private void addOverlay(@LayoutRes int layoutRes, int x, int y, int width, int height, int flags, final Runnable
            onTouch) {
        View view = View.inflate(context, layoutRes, null);
        if (transparentMode) {
            view.setBackgroundColor(TRANSPARENT_COLOR);
        }
        if (onTouch != null) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent
                            .ACTION_CANCEL) {
                        onTouch.run();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(width, height, x, y, LAYOUT_TYPE, flags,
                LAYOUT_FORMAT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        overlays.add(view);
        manager.addView(view, params);
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public void addNotTouchableOverlay(@LayoutRes int layoutRes, int x, int y, int width, int height) {
        addOverlay(layoutRes, x, y, width, height, NOT_TOUCHABLE_FLAGS, null);
    }

    public void addTouchableOverlay(@LayoutRes int layoutRes, int x, int y, int width, int height, Runnable onTouch) {
        addOverlay(layoutRes, x, y, width, height, TOUCHABLE_FLAGS, onTouch);
    }

    private void clearOverlayCollection(Collection<View> overlays) {
        for (View overlay : overlays) {
            manager.removeView(overlay);
        }
        overlays.clear();
    }

    public void clear() {
        clearOverlayCollection(overlays);
    }

    public void clearDelayed(long millis) {
        final Collection<View> overlaysToClearLater = new LinkedList<>(overlays);
        overlays.clear();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                clearOverlayCollection(overlaysToClearLater);
            }
        }, millis);
    }

}
