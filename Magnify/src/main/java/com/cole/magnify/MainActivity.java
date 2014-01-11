package com.cole.magnify;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import com.google.android.glass.touchpad.GestureDetector;

import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    Preview preview;
    Camera camera;
    private GestureDetector mGestureDetector;
    Activity act;
    Context ctx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mGestureDetector = createGestureDetector(this);
        super.onCreate(savedInstanceState);
        ctx = this;
        act = this;
        setContentView(R.layout.activity_main);


        preview = new Preview(this, (SurfaceView)findViewById(R.id.surfaceView));
        ((FrameLayout) findViewById(R.id.preview)).addView(preview);
        preview.setKeepScreenOn(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int wait = 0;
        int attempts = 0;
        while(camera == null && attempts < 10) {
            try {
                Log.d(TAG, "Opening a camera on resume.");
                camera = Camera.open();
            } catch(java.lang.RuntimeException e) {
                Log.e(TAG, e.getMessage());
                wait += 100;
                attempts += 1;
                Log.d(TAG, "Attempt " + attempts + ": Couldn't get camera. Waiting " + wait + "ms");
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        preview.setCamera(camera);
        camera.startPreview();
    }

    @Override
    protected void onPause() {
        if(camera != null) {
            camera.stopPreview();
            preview.setCamera(null);
            Log.d(TAG, "Releasing a camera on pause.");
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(camera != null) {
            camera.stopPreview();
            preview.setCamera(null);
            Log.d(TAG, "Releasing a camera on destory.");
            camera.release();
            camera = null;
        }
        super.onDestroy();
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
            @Override
            public boolean onScroll(float displacement, float delta, float velocity) {
                Log.d(TAG, "delta: " + delta);
                Log.d(TAG, "disp: " + displacement);
                Log.d(TAG, "velocity: " + velocity);
                preview.zoom((int) displacement);
                return true;
            }
        });
        return gestureDetector;
    }

    /*
     * Send generic motion events to the gesture detector
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }

}