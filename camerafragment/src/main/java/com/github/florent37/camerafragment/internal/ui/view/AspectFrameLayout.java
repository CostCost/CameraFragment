package com.github.florent37.camerafragment.internal.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/*
 * Layout that adjusts to maintain a specific aspect ratio.
 */
public class AspectFrameLayout extends FrameLayout {

    private double targetAspectRatio = -1.0;        // initially use default window size

    public AspectFrameLayout(Context context) {
        super(context);
    }

    public AspectFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAspectRatio(double aspectRatio) {
        if (aspectRatio < 0) {
            throw new IllegalArgumentException();
        }

        if (targetAspectRatio != aspectRatio) {
            targetAspectRatio = aspectRatio;
            requestLayout();
        }
    }

    /**
     * TODO 所以当预览的照片按照长度自适应填满屏幕之后，多余出来的宽度无法显示是这个控件导致的？？
     * TODO 那么拍照的时候该如何控制以自适应地进行显示呢？不会让拍摄出来的照片和预览的效果不一致，又不会变形
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (targetAspectRatio > 0) {
            int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
            int initialHeight = MeasureSpec.getSize(heightMeasureSpec);

            // padding
            int horizontalPadding = getPaddingLeft() + getPaddingRight();
            int verticalPadding = getPaddingTop() + getPaddingBottom();
            initialWidth -= horizontalPadding;
            initialHeight -= verticalPadding;

            double viewAspectRatio = (double) initialWidth / initialHeight;
            double aspectDifference = targetAspectRatio / viewAspectRatio - 1;

            if (Math.abs(aspectDifference) < 0.01) {
                //no changes
            } else {
                if (aspectDifference > 0) {
                    initialHeight = (int) (initialWidth / targetAspectRatio);
                } else {
                    initialWidth = (int) (initialHeight * targetAspectRatio);
                }
                initialWidth += horizontalPadding;
                initialHeight += verticalPadding;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
