package com.github.florent37.camerafragment.internal.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.CamcorderProfile;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.github.florent37.camerafragment.configuration.Configuration;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by memfis on 7/6/16.
 * <p/>
 * Class with some common methods to work with camera.
 */
public final class CameraHelper {

    public static final String TAG = "CameraHelper";

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    private CameraHelper() { }

    // region 获取摄像头信息
    /**
     * 是否存在可用的相机
     *
     * @param context 上下文
     * @return 是否存在可用的相机
     */
    public static boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    /**
     * 检测 Camera2 是否可用
     *
     * @param context 上下文
     * @return 是否可用
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean hasCamera2(Context context) {
        if (context == null) return false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false;
        try {
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String[] idList = manager.getCameraIdList();
            boolean notNull = true;
            if (idList.length == 0) {
                notNull = false;
            } else {
                for (final String str : idList) {
                    if (str == null || str.trim().isEmpty()) {
                        notNull = false;
                        break;
                    }
                    final CameraCharacteristics characteristics = manager.getCameraCharacteristics(str);

                    final int supportLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                    if (supportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                        notNull = false;
                        break;
                    }
                }
            }
            return notNull;
        } catch (Throwable ignore) {
            return false;
        }
    }
    // endregion

    // region 与输出的媒体文件相关的方法
    /**
     * 创建存储的目录
     *
     * @param context 上下文
     * @param pathToDirectory 存储文件的目录
     * @return 文件目录
     */
    public static File generateStorageDir(Context context, @Nullable String pathToDirectory) {
        File mediaStorageDir = null;
        if (pathToDirectory != null) {
            mediaStorageDir = new File(pathToDirectory);
        } else {
            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), context.getPackageName());
        }

		// 创建目录
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Failed to create directory.");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * 获取存储多媒体文件的 File 对象
     *
     * @param context 上下文
     * @param mediaAction 多媒体类型
     * @param pathToDirectory 存储多媒体的目录
     * @param fileName 文件名
     * @return 文件
     */
    public static File getOutputMediaFile(Context context,
                                          @Configuration.MediaAction int mediaAction,
                                          @Nullable String pathToDirectory,
                                          @Nullable String fileName) {
        final File mediaStorageDir = generateStorageDir(context, pathToDirectory);
        File mediaFile = null;

        if (mediaStorageDir != null) {
            if (fileName == null) {
                final String timeStamp = simpleDateFormat.format(new Date());
                if (mediaAction == Configuration.MEDIA_ACTION_PHOTO) {
                    fileName = "IMG_" + timeStamp;
                } else if (mediaAction == Configuration.MEDIA_ACTION_VIDEO) {
                    fileName = "VID_" + timeStamp;
                }

            }
            final String mediaStorageDirPath = mediaStorageDir.getPath();
            if (mediaAction == Configuration.MEDIA_ACTION_PHOTO) {
                mediaFile = new File(mediaStorageDirPath + File.separator + fileName + ".jpg");
            } else if (mediaAction == Configuration.MEDIA_ACTION_VIDEO) {
                mediaFile = new File(mediaStorageDirPath + File.separator + fileName + ".mp4");
            }
        }

        return mediaFile;
    }
    // endregion

    // region 获取用于预览等的尺寸相关的方法

    /**
     * TODO 研究一下这个方法！根据媒体的质量计算出合适的尺寸
     *
     * @param choices 所有可选的只存
     * @param mediaQuality 媒体文件的质量
     * @return 最终的尺寸
     */
    @SuppressWarnings("deprecation")
    public static Size getPictureSize(List<Size> choices, @Configuration.MediaQuality int mediaQuality) {
        if (choices == null || choices.isEmpty()) return null;
        if (choices.size() == 1) return choices.get(0);

        Size result = null;
        Size maxPictureSize = Collections.max(choices, new CompareSizesByArea2());
        Size minPictureSize = Collections.min(choices, new CompareSizesByArea2());

        Collections.sort(choices, new CompareSizesByArea2());

        if (mediaQuality == Configuration.MEDIA_QUALITY_HIGHEST) {
            result = maxPictureSize;
        } else if (mediaQuality == Configuration.MEDIA_QUALITY_LOW) {
            if (choices.size() == 2) result = minPictureSize;
            else {
                int half = choices.size() / 2;
                int lowQualityIndex = (choices.size() - half) / 2;
                result = choices.get(lowQualityIndex + 1);
            }
        } else if (mediaQuality == Configuration.MEDIA_QUALITY_HIGH) {
            if (choices.size() == 2) result = maxPictureSize;
            else {
                int half = choices.size() / 2;
                int highQualityIndex = (choices.size() - half) / 2;
                result = choices.get(choices.size() - highQualityIndex - 1);
            }
        } else if (mediaQuality == Configuration.MEDIA_QUALITY_MEDIUM) {
            if (choices.size() == 2) result = minPictureSize;
            else {
                int mediumQualityIndex = choices.size() / 2;
                result = choices.get(mediumQualityIndex);
            }
        } else if (mediaQuality == Configuration.MEDIA_QUALITY_LOWEST) {
            result = minPictureSize;
        }

        return result;
    }

    /**
     * 跟上面的方法的作用和逻辑一样，只是要求传入的参数类型不同
     *
     * @param sizes 尺寸
     * @param mediaQuality 媒体文件的质量
     * @return 尺寸
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Size getPictureSize(Size[] sizes, @Configuration.MediaQuality int mediaQuality) {
        if (sizes == null || sizes.length == 0) return null;

        List<Size> choices = Arrays.asList(sizes);

        if (choices.size() == 1) return choices.get(0);

        Size result = null;
        Size maxPictureSize = Collections.max(choices, new CompareSizesByArea2());
        Size minPictureSize = Collections.min(choices, new CompareSizesByArea2());

        Collections.sort(choices, new CompareSizesByArea2());

        if (mediaQuality == Configuration.MEDIA_QUALITY_HIGHEST) {
            result = maxPictureSize;
        } else if (mediaQuality == Configuration.MEDIA_QUALITY_LOW) {
            if (choices.size() == 2) result = minPictureSize;
            else {
                int half = choices.size() / 2;
                int lowQualityIndex = (choices.size() - half) / 2;
                result = choices.get(lowQualityIndex + 1);
            }
        } else if (mediaQuality == Configuration.MEDIA_QUALITY_HIGH) {
            if (choices.size() == 2) result = maxPictureSize;
            else {
                int half = choices.size() / 2;
                int highQualityIndex = (choices.size() - half) / 2;
                result = choices.get(choices.size() - highQualityIndex - 1);
            }
        } else if (mediaQuality == Configuration.MEDIA_QUALITY_MEDIUM) {
            if (choices.size() == 2) result = minPictureSize;
            else {
                int mediumQualityIndex = choices.size() / 2;
                result = choices.get(mediumQualityIndex);
            }
        } else if (mediaQuality == Configuration.MEDIA_QUALITY_LOWEST) {
            result = minPictureSize;
        }

        return result;
    }

    /**
     * 获取可选的预览尺寸
     *
     * @param sizes 尺寸
     * @param width 宽度
     * @param height 高度
     * @return 族中的尺寸
     */
    @SuppressWarnings("deprecation")
    public static Size getOptimalPreviewSize(List<Size> sizes, int width, int height) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) height / width;

        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        for (Size size : sizes) {
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @SuppressWarnings("deprecation")
    public static Size getSizeWithClosestRatio(List<Size> sizes, int width, int height) {

        if (sizes == null) return null;

        double MIN_TOLERANCE = 100;
        double targetRatio = (double) height / width;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        for (Size size : sizes) {
            if (size.getWidth() == width && size.getHeight() == height)
                return size;

            double ratio = (double) size.getHeight() / size.getWidth();

            if (Math.abs(ratio - targetRatio) < MIN_TOLERANCE) {
                MIN_TOLERANCE = Math.abs(ratio - targetRatio);
                minDiff = Double.MAX_VALUE;
            }
            else continue;

            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Size getOptimalPreviewSize(Size[] sizes, int width, int height) {

        if (sizes == null) return null;

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) height / width;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        for (Size size : sizes) {
//            if (size.getWidth() == width && size.getHeight() == height)
//                return size;
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Size getSizeWithClosestRatio(Size[] sizes, int width, int height) {

        if (sizes == null) return null;

        double MIN_TOLERANCE = 100;
        double targetRatio = (double) height / width;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        for (Size size : sizes) {
//            if (size.getWidth() == width && size.getHeight() == height)
//                return size;

            double ratio = (double) size.getHeight() / size.getWidth();

            if (Math.abs(ratio - targetRatio) < MIN_TOLERANCE) {
                MIN_TOLERANCE = Math.abs(ratio - targetRatio);
                minDiff = Double.MAX_VALUE;
            }
            else continue;

            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea2());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return null;
        }
    }

    // endregion

    // region 计算视频相关的逻辑
    private static double calculateApproximateVideoSize(CamcorderProfile camcorderProfile, int seconds) {
        return ((camcorderProfile.videoBitRate / (float) 1 + camcorderProfile.audioBitRate / (float) 1) * seconds) / (float) 8;
    }

    public static double calculateApproximateVideoDuration(CamcorderProfile camcorderProfile, long maxFileSize) {
        return 8 * maxFileSize / (camcorderProfile.videoBitRate + camcorderProfile.audioBitRate);
    }

    private static long calculateMinimumRequiredBitRate(CamcorderProfile camcorderProfile, long maxFileSize, int seconds) {
        return 8 * maxFileSize / seconds - camcorderProfile.audioBitRate;
    }
    // endregion

    // region 获取 CamcorderProfile
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static CamcorderProfile getCamcorderProfile(String cameraId, long maximumFileSize, int minimumDurationInSeconds) {
        if (TextUtils.isEmpty(cameraId)) {
            return null;
        }
        int cameraIdInt = Integer.parseInt(cameraId);
        return getCamcorderProfile(cameraIdInt, maximumFileSize, minimumDurationInSeconds);
    }

    public static CamcorderProfile getCamcorderProfile(int currentCameraId, long maximumFileSize, int minimumDurationInSeconds) {
        if (maximumFileSize <= 0)
            return CamcorderProfile.get(currentCameraId, Configuration.MEDIA_QUALITY_HIGHEST);

        int[] qualities = new int[]{Configuration.MEDIA_QUALITY_HIGHEST,
                Configuration.MEDIA_QUALITY_HIGH, Configuration.MEDIA_QUALITY_MEDIUM,
                Configuration.MEDIA_QUALITY_LOW, Configuration.MEDIA_QUALITY_LOWEST};

        CamcorderProfile camcorderProfile;
        for (int i = 0; i < qualities.length; ++i) {
            camcorderProfile = CameraHelper.getCamcorderProfile(qualities[i], currentCameraId);
            double fileSize = CameraHelper.calculateApproximateVideoSize(camcorderProfile, minimumDurationInSeconds);

            if (fileSize > maximumFileSize) {
                long minimumRequiredBitRate = calculateMinimumRequiredBitRate(camcorderProfile, maximumFileSize, minimumDurationInSeconds);

                if (minimumRequiredBitRate >= camcorderProfile.videoBitRate / 4 && minimumRequiredBitRate <= camcorderProfile.videoBitRate) {
                    camcorderProfile.videoBitRate = (int) minimumRequiredBitRate;
                    return camcorderProfile;
                }
            } else return camcorderProfile;
        }
        return CameraHelper.getCamcorderProfile(Configuration.MEDIA_QUALITY_LOWEST, currentCameraId);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static CamcorderProfile getCamcorderProfile(@Configuration.MediaQuality int mediaQuality, String cameraId) {
        if (TextUtils.isEmpty(cameraId)) {
            return null;
        }
        int cameraIdInt = Integer.parseInt(cameraId);
        return getCamcorderProfile(mediaQuality, cameraIdInt);
    }

    public static CamcorderProfile getCamcorderProfile(@Configuration.MediaQuality int mediaQuality, int cameraId) {
        if (Build.VERSION.SDK_INT > 10) {
            if (mediaQuality == Configuration.MEDIA_QUALITY_HIGHEST) {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
            } else if (mediaQuality == Configuration.MEDIA_QUALITY_HIGH) {
                if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P)) {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_1080P);
                } else if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
                } else {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
                }
            } else if (mediaQuality == Configuration.MEDIA_QUALITY_MEDIUM) {
                if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
                } else if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
                } else {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
                }
            } else if (mediaQuality == Configuration.MEDIA_QUALITY_LOW) {
                if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
                } else {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
                }
            } else if (mediaQuality == Configuration.MEDIA_QUALITY_LOWEST) {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
            } else {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
            }
        } else {
            if (mediaQuality == Configuration.MEDIA_QUALITY_HIGHEST) {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
            } else if (mediaQuality == Configuration.MEDIA_QUALITY_HIGH) {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
            } else if (mediaQuality == Configuration.MEDIA_QUALITY_MEDIUM) {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
            } else if (mediaQuality == Configuration.MEDIA_QUALITY_LOW) {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
            } else if (mediaQuality == Configuration.MEDIA_QUALITY_LOWEST) {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
            } else {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
            }
        }
    }
    // endregion

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class CompareSizesByArea2 implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }
}
