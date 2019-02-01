package com.github.florent37.camerafragment.internal.ui.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.TextureView;

/**
 * TODO 对于 SurfaceView 还是 TextureView 的问题，作者选择的是 TextureView-> Camera2, SurfaceView -> Camera1
 * Google 非官方的 CameraView 的配置方式，

 API Level	Camera API	Preview View
 9-13	    Camera1	    SurfaceView
 14-20	    Camera1	    TextureView
 21-23	    Camera2	    TextureView
 24	        Camera2	    SurfaceView

 * Created by memfis on 7/6/16.
 */
@SuppressLint("ViewConstructor")
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AutoFitTextureView extends TextureView {

    private static final String TAG = "AutoFitTextureView";

    private int ratioWidth = 0;
    private int ratioHeight = 0;

    public AutoFitTextureView(Context context, TextureView.SurfaceTextureListener surfaceTextureListener) {
        super(context, null);
        setSurfaceTextureListener(surfaceTextureListener);
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated fromList the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        ratioWidth = width;
        ratioHeight = height;

        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        if (0 == ratioWidth || 0 == ratioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * (ratioWidth / (float) ratioHeight)) {
                setMeasuredDimension(width, (int) (width * (ratioWidth / (float) ratioHeight)));
            } else {
                setMeasuredDimension((int) (height * (ratioWidth / (float) ratioHeight)), height);
            }
        }
    }
}
