package com.github.florent37.camerafragment.internal.controller.impl;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;

import com.github.florent37.camerafragment.configuration.Configuration;
import com.github.florent37.camerafragment.configuration.ConfigurationProvider;
import com.github.florent37.camerafragment.internal.controller.CameraController;
import com.github.florent37.camerafragment.internal.controller.view.CameraView;
import com.github.florent37.camerafragment.internal.manager.CameraManager;
import com.github.florent37.camerafragment.internal.manager.impl.Camera1Manager;
import com.github.florent37.camerafragment.internal.manager.listener.CameraCloseListener;
import com.github.florent37.camerafragment.internal.manager.listener.CameraOpenListener;
import com.github.florent37.camerafragment.internal.manager.listener.CameraPhotoListener;
import com.github.florent37.camerafragment.internal.manager.listener.CameraVideoListener;
import com.github.florent37.camerafragment.internal.ui.view.AutoFitSurfaceView;
import com.github.florent37.camerafragment.internal.utils.CameraHelper;
import com.github.florent37.camerafragment.internal.utils.Size;
import com.github.florent37.camerafragment.listeners.CameraFragmentResultListener;

import java.io.File;

/**
 * TODO 两个 Controller 中基本都是调用各自的 Manager 来实现的，感觉没有必要增加这一层设计，在 Fragment 里面直接调用 Manager 即可
 *
 * Created by memfis on 7/7/16.
 */
@SuppressWarnings("deprecation")
public class Camera1Controller implements CameraController<Integer>,
        CameraOpenListener<Integer, SurfaceHolder.Callback>, CameraPhotoListener, CameraCloseListener<Integer>, CameraVideoListener {

    private static final String TAG = "Camera1Controller";

    private final Context context;

    private Integer currentCameraId;
    private ConfigurationProvider configurationProvider;
    private CameraManager<Integer, SurfaceHolder.Callback> cameraManager;
    private CameraView cameraView;

    private File outputFile;

    public Camera1Controller(Context context, CameraView cameraView, ConfigurationProvider configurationProvider) {
        this.context = context;
        this.cameraView = cameraView;
        this.configurationProvider = configurationProvider;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        cameraManager = new Camera1Manager();
        cameraManager.initializeCameraManager(configurationProvider, context);
        setCurrentCameraId(cameraManager.getFaceBackCameraId());
    }

    private void setCurrentCameraId(Integer cameraId) {
        this.currentCameraId = cameraId;
        cameraManager.setCameraId(cameraId);
    }

    /**
     * 这些方法近乎 Fragment 等的生命周期
     */
    @Override
    public void onResume() {
        cameraManager.openCamera(currentCameraId, this);
    }

    /**
     * 关闭相机
     */
    @Override
    public void onPause() {
        cameraManager.closeCamera(null);
    }

    /**
     * TODO 释放资源，提供这个方法还是比较重要的！
     */
    @Override
    public void onDestroy() {
        cameraManager.releaseCameraManager();
    }

    @Override
    public void takePhoto(CameraFragmentResultListener callback) {
        takePhoto(callback, null, null);
    }

    /**
     * 提供的拍摄照片的 API 方法
     *
     * @param callback 结果回调
     * @param direcoryPath 输出目录
     * @param fileName 文件名
     */
    @Override
    public void takePhoto(CameraFragmentResultListener callback, @Nullable String direcoryPath, @Nullable String fileName) {
        outputFile = CameraHelper.getOutputMediaFile(context, Configuration.MEDIA_ACTION_PHOTO, direcoryPath, fileName);
        cameraManager.takePhoto(outputFile, this, callback);
    }

    @Override
    public void startVideoRecord() {
        startVideoRecord(null, null);
    }

    /**
     * 开启视频录制
     *
     * @param direcoryPath 目录
     * @param fileName 文件名
     */
    @Override
    public void startVideoRecord(@Nullable String direcoryPath, @Nullable String fileName) {
        outputFile = CameraHelper.getOutputMediaFile(context, Configuration.MEDIA_ACTION_VIDEO, direcoryPath, fileName);
        cameraManager.startVideoRecord(outputFile, this);
    }

    /**
     * 停止录制
     *
     * @param callback 结果回调
     */
    @Override
    public void stopVideoRecord(CameraFragmentResultListener callback) {
        cameraManager.stopVideoRecord(callback);
    }

    /**
     * 判断视频是否再录制中
     *
     * @return 布尔类型
     */
    @Override
    public boolean isVideoRecording() {
        return cameraManager.isVideoRecording();
    }

    /**
     * 转换相机
     *
     * @param cameraFace 相机的前后
     */
    @Override
    public void switchCamera(@Configuration.CameraFace final int cameraFace) {
        final Integer backCameraId = cameraManager.getFaceBackCameraId();
        final Integer frontCameraId = cameraManager.getFaceFrontCameraId();
        final Integer currentCameraId = cameraManager.getCurrentCameraId();

        if (cameraFace == Configuration.CAMERA_FACE_REAR && backCameraId != null) {
            setCurrentCameraId(backCameraId);
            cameraManager.closeCamera(this);
        } else if (frontCameraId != null && !frontCameraId.equals(currentCameraId)) {
            setCurrentCameraId(frontCameraId);
            cameraManager.closeCamera(this);
        }
    }

    /**
     * 闪光灯相关
     *
     * @param flashMode
     */
    @Override
    public void setFlashMode(@Configuration.FlashMode int flashMode) {
        cameraManager.setFlashMode(flashMode);
    }

    /**
     * 切换图片质量
     */
    @Override
    public void switchQuality() {
        cameraManager.closeCamera(this);
    }

    /**
     * 可用的相机的数量
     *
     * @return 相机数量
     */
    @Override
    public int getNumberOfCameras() {
        return cameraManager.getNumberOfCameras();
    }

    /**
     * 是视频还是照片
     *
     * @return 视频还是照片，对应整型变量
     */
    @Override
    public int getMediaAction() {
        return configurationProvider.getMediaAction();
    }

    @Override
    public File getOutputFile() {
        return outputFile;
    }

    @Override
    public Integer getCurrentCameraId() {
        return currentCameraId;
    }

    @Override
    public void onCameraOpened(Integer cameraId, Size previewSize, SurfaceHolder.Callback surfaceCallback) {
        cameraView.updateUiForMediaAction(configurationProvider.getMediaAction());
        cameraView.updateCameraPreview(previewSize, new AutoFitSurfaceView(context, surfaceCallback));
        cameraView.updateCameraSwitcher(getNumberOfCameras());
    }

    @Override
    public void onCameraOpenError() {
        Log.e(TAG, "onCameraOpenError");
    }

    @Override
    public void onCameraClosed(Integer closedCameraId) {
        cameraView.releaseCameraPreview();

        cameraManager.openCamera(currentCameraId, this);
    }

    @Override
    public void onPhotoTaken(byte[] bytes, File photoFile, CameraFragmentResultListener callback) {
        cameraView.onPhotoTaken(bytes, callback);
    }

    @Override
    public void onPhotoTakeError() {
    }

    @Override
    public void onVideoRecordStarted(Size videoSize) {
        cameraView.onVideoRecordStart(videoSize.getWidth(), videoSize.getHeight());
    }

    @Override
    public void onVideoRecordStopped(File videoFile, @Nullable CameraFragmentResultListener callback) {
        cameraView.onVideoRecordStop(callback);
    }

    @Override
    public void onVideoRecordError() {

    }

    @Override
    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public CharSequence[] getVideoQualityOptions() {
        return cameraManager.getVideoQualityOptions();
    }

    @Override
    public CharSequence[] getPhotoQualityOptions() {
        return cameraManager.getPhotoQualityOptions();
    }
}
