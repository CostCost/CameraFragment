package com.github.florent37.camerafragment.internal.ui.model;

import com.github.florent37.camerafragment.configuration.Configuration;
import com.github.florent37.camerafragment.internal.utils.Size;

/**
 * 包装类，包含了照片的质量，查看 {@link com.github.florent37.camerafragment.internal.manager.impl.Camera2Manager#getPhotoQualityOptions()}
 * 方法来看它是如何被构建放进一个列表中的！！{@link VideoQualityOption} 同理！！
 *
 * Created by memfis on 12/1/16.
 */
public class PhotoQualityOption implements CharSequence {

    @Configuration.MediaQuality
    private int mediaQuality;
    private String title;
    private Size size;

    public PhotoQualityOption(@Configuration.MediaQuality int mediaQuality, Size size) {
        this.mediaQuality = mediaQuality;
        this.size = size;

        title = String.valueOf(size.getWidth()) + " x " + String.valueOf(size.getHeight());
    }

    public Size getSize() {
        return size;
    }

    @Configuration.MediaQuality
    public int getMediaQuality() {
        return mediaQuality;
    }

    @Override
    public int length() {
        return title.length();
    }

    @Override
    public char charAt(int index) {
        return title.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return title.subSequence(start, end);
    }

    @Override
    public String toString() {
        return title;
    }
}
