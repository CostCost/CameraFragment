package com.github.florent37.camerafragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.support.annotation.RequiresPermission;

import com.github.florent37.camerafragment.configuration.Configuration;
import com.github.florent37.camerafragment.internal.ui.BaseAnncaFragment;

public class CameraFragment extends BaseAnncaFragment {

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.CAMERA)
    public static CameraFragment newInstance(Configuration configuration) {
        return (CameraFragment) BaseAnncaFragment.newInstance(new CameraFragment(), configuration);
    }
}
