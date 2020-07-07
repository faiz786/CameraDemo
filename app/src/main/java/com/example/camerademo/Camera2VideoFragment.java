/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.camerademo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.legacy.app.FragmentCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Camera2VideoFragment extends Fragment
        implements View.OnClickListener, FragmentCompat.OnRequestPermissionsResultCallback {

    static final int STATUS_NONE = 0;
    static final int STATUS_OPENING = 1;
    static final int STATUS_RUNNING = 2;
    static final int STATUS_CLOSING = 3;
    static final int STATUS_CLOSED = 4;
    static final int Factor = 1;

    public static LinearLayout outputView;

    public String sSelectedCamera;

    public int sCameraOrientation = -1;

    public final String VideoCodec = MediaFormat.MIMETYPE_VIDEO_AVC;
    public final int VideoWidthsend = 640 / Factor; // 640
    public final int VideoHeightsend = 480 / Factor;
    public final int VideoWidthRecieve = 480 / Factor; // 640
    public final int VideoHeightReceive = 640 / Factor;

    private boolean cameraFront = false;

    private int current_Camera = 0;

    private float degrees;

    public VideoInputThread mVideoInputThread;

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    private static final String TAG = "Camera2VideoFragment";
    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    boolean firstTime = true;
    boolean secondTime = true;
    boolean isRunning = false;
    public static final String ENCODING = "h264";

    private PlayerThread mPlayer = null;
    private PlayerThread2 mPlayer2 = null;
    //    Handler handler = null;
    public static byte[] SPS = null;
    public static byte[] PPS = null;
    public static byte[] SPS1 = null;
    public static byte[] PPS1 = null;
    public static int frameID = 0;
    public static int frameID1 = 0;
    BlockingQueue<Frame> queue = new ArrayBlockingQueue<Frame>(100);
    BlockingQueue<Frame> queue1 = new ArrayBlockingQueue<Frame>(100);
    DisplayMetrics displayMetrics = new DisplayMetrics();
    int width, height;
    SurfaceView surfaceView1, surfaceView2;
    SurfaceView sv;
    TextureView tv1, tv2;
    LinearLayout cameraPreview;
    static final int VideoWidthHD = 320;
    static final int VideoHeightHD = 240;
    static final int VideoWidthLD = 320;
    static final int VideoHeightLD = 240;
    Surface mSurface, mSurface2;

    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    /**
     * Button to record video
     */
    private Button mButtonVideo;

    /**
     * A reference to the opened {@link android.hardware.camera2.CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    Camera camera;

    /**
     * A reference to the current {@link android.hardware.camera2.CameraCaptureSession} for
     * preview.
     */
    private CameraCaptureSession mPreviewSession;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            openCamera(width, height);
//            setupCapture();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * The {@link android.util.Size} of video recording.
     */
    private Size mVideoSize;

    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;

    /**
     * Whether the app is recording video now
     */
    private boolean mIsRecordingVideo;

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its status.
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
            if (null != mTextureView) {
                configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };
    private Integer mSensorOrientation;
    private String mNextVideoAbsolutePath;
    private CaptureRequest.Builder mPreviewBuilder;

    public static Camera2VideoFragment newInstance() {
        return new Camera2VideoFragment();
    }

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
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
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2_video, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        mButtonVideo = (Button) view.findViewById(R.id.video);
        outputView = (LinearLayout) view.findViewById(R.id.decoderOutputLayout);
//        tv1 = new TextureView(getActivity().getApplicationContext());
//        tv1.setLayoutParams(new android.widget.FrameLayout.LayoutParams(width / 4, height / 4));
//        outputView.addView(tv1);
        tv1 = (TextureView) view.findViewById(R.id.textureView1);
        tv2 = (TextureView) view.findViewById(R.id.textureView2);
        tv1.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                System.out.println("came in surface texture available");
                mSurface = new Surface(surface);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
        tv2.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                System.out.println("came in surface texture available");
                mSurface2 = new Surface(surface);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
        mButtonVideo.setOnClickListener(this);
        view.findViewById(R.id.info).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    void setupCapture() {

        if (sSelectedCamera == null)
            sSelectedCamera = findCamera(findFrontFacingCamera());
//        sSelectedCamera = "1";

//        int index = getFrontCameraId();
//        Camera c = Camera.open(index);
        // if (mVideoInputThread != null)
        // if (mVideoInputThread.isAlive()) {
        // mVideoInputThread.close();
        // mVideoInputThread.interrupt();
        // mVideoInputThread = null;
        // }
        mVideoInputThread = new VideoInputThread();
        mVideoInputThread.open();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video: {
                if (mIsRecordingVideo) {
                    stopRecordingVideo();
                } else {
//                    startRecordingVideo();
                    setupCapture();
                }
                break;
            }
            case R.id.info: {
                Activity activity = getActivity();
                if (null != activity) {
                    new AlertDialog.Builder(activity)
                            .setMessage(R.string.intro_message)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
                break;
            }
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets whether you should show UI with rationale for requesting permissions.
     *
     * @param permissions The permissions your app wants to request.
     * @return Whether you can show permission rationale UI.
     */
    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (FragmentCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Requests permissions needed for recording video.
     */
    private void requestVideoPermissions() {
        if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            FragmentCompat.requestPermissions(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.length == VIDEO_PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        ErrorDialog.newInstance(getString(R.string.permission_request))
                                .show(getChildFragmentManager(), FRAGMENT_DIALOG);
                        break;
                    }
                }
            } else {
                ErrorDialog.newInstance(getString(R.string.permission_request))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tries to open a {@link CameraDevice}. The result is listened by `mStateCallback`.
     */
    @SuppressWarnings("MissingPermission")
    private void openCamera(int width, int height) {
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            requestVideoPermissions();
            return;
        }
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            Log.d(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId = manager.getCameraIdList()[0];

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    width, height, mVideoSize);

            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(640, 480);
//                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                mTextureView.setAspectRatio(640, 480);
            }
            configureTransform(width, height);
            mMediaRecorder = new MediaRecorder();
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
            activity.finish();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Start the camera preview.
     */
    private void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Activity activity = getActivity();
                            if (null != activity) {
                                Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private void setUpMediaRecorder() throws IOException {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            mNextVideoAbsolutePath = getVideoFilePath(getActivity());
        }
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        mMediaRecorder.prepare();
    }

    private String getVideoFilePath(Context context) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                + System.currentTimeMillis() + ".mp4";
    }

    private void startRecordingVideo() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // UI
                            mButtonVideo.setText(R.string.stop);
                            mIsRecordingVideo = true;

                            // Start recording
                            mMediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Activity activity = getActivity();
                    if (null != activity) {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }

    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    private void stopRecordingVideo() {
        // UI
        mIsRecordingVideo = false;
        mButtonVideo.setText(R.string.record);
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();

        Activity activity = getActivity();
        if (null != activity) {
            Toast.makeText(activity, "Video saved: " + mNextVideoAbsolutePath,
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath);
        }
        mNextVideoAbsolutePath = null;
        startPreview();
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.permission_request)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FragmentCompat.requestPermissions(parent, VIDEO_PERMISSIONS,
                                    REQUEST_VIDEO_PERMISSIONS);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    parent.getActivity().finish();
                                }
                            })
                    .create();
        }

    }


    class VideoInputThread extends HandlerThread {

        Handler mHandler;
        CameraDevice mCamera;
        CameraCaptureSession mCameraCaptureSession;
        MediaCodec mVideoEncoder;
        VideoEncoderCore videoEncoderCore, videoEncoderCore2;

        {
            try {
                videoEncoderCore = new VideoEncoderCore(VideoWidthsend, VideoHeightsend, 400000 / Factor);
                videoEncoderCore2 = new VideoEncoderCore(VideoWidthsend, VideoHeightsend, 256000 / Factor);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("exception while creating video encoder" + e.getMessage());
            }
        }

        int mStatus;

        public VideoInputThread() {

            super("Video Input");
            super.start();
            mHandler = new Handler(getLooper());
        }

        public void open() {

            mHandler.post(() -> {

                android.util.Log.w(TAG, "+open VideoInput");
                mStatus = STATUS_OPENING;
                openVideoInput(sSelectedCamera, false);
                mStatus = STATUS_RUNNING;
            });
        }

        public void startVideoEncoder() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    android.util.Log.w(TAG, "+startEncoder: ");
                    if (videoEncoderCore.mEncoder == null) {
                        openVideoEncoder();
                    }
                }
            });
        }

        public void close() {
            // try {

            if (mStatus == STATUS_RUNNING) {
                // if (mHandler.getLooper().getThread().isAlive()) {
                // this.interrupt();

                mHandler.post(() -> {

                    mStatus = STATUS_CLOSING;

                    // try {
                    // mCameraOpenCloseLock.acquire();

                    if (null != mCameraCaptureSession) {
                        // try {
                        // mCameraCaptureSession.stopRepeating();
                        // mCameraCaptureSession.abortCaptures();
                        // } catch (CameraAccessException e) {
                        // e.printStackTrace();
                        // }
                        mCameraCaptureSession.close();
                        mCameraCaptureSession = null;
                    }

                    if (null != mCamera) {
                        mCamera.close();
                        mCamera = null;
                    }

//                    if (mVideoEncoder != null) {
//                        mVideoEncoder.release();
//                        mVideoEncoder = null;
//                    }

                    if (videoEncoderCore != null) {
                        videoEncoderCore.release();
                        videoEncoderCore.mEncoder = null;
                    }

                    stopBackgroundThread();
                    mStatus = STATUS_CLOSED;
                    // } catch (InterruptedException e) {
                    // e.printStackTrace();
                    // } finally {
                    // mCameraOpenCloseLock.release();
                    // }
                });
                waitUntilClosed();
            }
            // setupRender();

            // } catch (Exception e) {
            // e.printStackTrace();
            // mStatus = STATUS_CLOSED;
            // }

        }

        public void close_video_encoder() {
            // try {

            // if (mStatus == STATUS_RUNNING) {
            // // if (mHandler.getLooper().getThread().isAlive()) {
            // // this.interrupt();
            // mHandler.post(new Runnable() {
            // @Override
            // public void run() {

            // mStatus = STATUS_CLOSING;

            // if (mCamera != null) {
            // mCamera.close();
            // mCamera = null;
            // }

            if (videoEncoderCore.mEncoder != null) {
                videoEncoderCore.mEncoder.release();
                videoEncoderCore.mEncoder = null;
            }

            // quit();
            // mStatus = STATUS_CLOSED;
            // }
            // });
            // waitUntilClosed();
            // }
            // setupRender();

            // } catch (Exception e) {
            // e.printStackTrace();
            // mStatus = STATUS_CLOSED;
            // }

        }

        public void waitUntilClosed() {

            if (mStatus == STATUS_NONE)
                return;

            try {

                int timeWaited = 0;
                while (mStatus != STATUS_CLOSED) {
                    if (timeWaited <= 5000) {
                        Thread.sleep(10);
                        // Log.w("Camera wait", "waiting..");
                        timeWaited = timeWaited + 10;
                    } else {

                        if (videoEncoderCore.mEncoder != null) {
                            videoEncoderCore.mEncoder.release();
                            videoEncoderCore.mEncoder = null;
                        }
                        stopBackgroundThread();
                        mStatus = STATUS_CLOSED;
                    }

                }
            } catch (InterruptedException ex) {
                Log.w("Camera wait", "waiting.." + ex.getMessage());
            }
        }

        private void openVideoInput(String cameraName, boolean iscamera_switching) {

            try {

                android.util.Log.w(TAG, "+openVideoInput: " + cameraName);
                System.out.println("openVideoInput" + cameraName);

//                camera = Camera.open();
//
//                camera.setPreviewCallback(new Camera.PreviewCallback() {
//                    @Override
//                    public void onPreviewFrame(byte[] bytes, Camera camera) {
//
//                    }
//                });
//
//                camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
//                    @Override
//                    public void onPreviewFrame(byte[] bytes, Camera camera) {
//
//                    }
//                });

                final CameraManager cameraManager = (CameraManager) getActivity()
                        .getSystemService(Context.CAMERA_SERVICE);

                final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {

                    @Override
                    public void onOpened(@NonNull CameraDevice camera) {
                        Log.e("onOpened", "Camera onOpened");

                        mCamera = camera;
                        openVideoEncoder();

                        // mCameraOpenCloseLock.release();
                        // if (null != mTextureView) {
                        // configureTransform(mTextureView.getWidth(), mTextureView.getHeight());

                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {
                        Log.e("onDisconnected", "Camera onDisconnected");
                        // mCameraOpenCloseLock.release();
                        if (mCameraCaptureSession != null) {

                            mCameraCaptureSession.close();
                            mCameraCaptureSession = null;
                        }
                        camera.close();
                        mCamera = null;
                    }

                    @Override
                    public void onError(@NonNull CameraDevice camera, int error) {
                        Log.e("onError", "Camera onError");
                        // mCameraOpenCloseLock.release();
                        if (mCameraCaptureSession != null) {

                            mCameraCaptureSession.close();
                            mCameraCaptureSession = null;
                        }
                        camera.close();
                        mCamera = null;
                        // stopBackgroundThread();
                        // onCameraSwitchclicked();
                    }

                    @Override
                    public void onClosed(@NonNull CameraDevice camera) {
                        Log.e("onClosed", "Camera onClosed");
                        super.onClosed(camera);
                        if (mCameraCaptureSession != null) {

                            mCameraCaptureSession.close();
                            mCameraCaptureSession = null;
                        }
                        mCamera = null;
                        // mStatus=

                    }
                };

                startBackgroundThread();

                cameraManager.openCamera(cameraName, stateCallback, mBackgroundHandler);

//                int index = getFrontCameraId();
//                Camera.open(index);
                // cameraManager.openCamera(cameraName, stateCallback, null);

                // try {
                // if (!mCameraOpenCloseLock.tryAcquire(3000, TimeUnit.MILLISECONDS)) {
                // throw new RuntimeException("Time out waiting to lock camera opening.");
                // }
                // cameraManager.openCamera(cameraName, stateCallback, mBackgroundHandler);
                // } catch (InterruptedException e) {
                // throw new RuntimeException("Interrupted while trying to lock camera
                // opening.", e);
                // }

            } catch (SecurityException | NullPointerException | CameraAccessException ex) {
                android.util.Log.e(TAG, "Error in openVideoInput: " + android.util.Log.getStackTraceString(ex));
            }
        }

        private void startBackgroundThread() {
            mBackgroundThread = new HandlerThread("CameraBackground");
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        }

        /**
         * Stops the background thread and its {@link Handler}.
         */
        private void stopBackgroundThread() {
            if (mBackgroundThread != null) {
                mBackgroundThread.quitSafely();
                try {
                    mBackgroundThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBackgroundThread = null;
                mBackgroundHandler = null;
            }
        }

        private void openVideoEncoder() {

            try {

                videoEncoderCore.mEncoder.setCallback(new MediaCodec.Callback() {
                    @Override
                    public void onInputBufferAvailable(MediaCodec codec, int index) {

                    }

                    @Override
                    public void onOutputBufferAvailable(MediaCodec codec, int index,
                                                        MediaCodec.BufferInfo info) {
                        System.out.println("buffer available from camera" + info.size);
                        byte[] outData = new byte[info.size];
//                        // mVideoOutputThread.render( info.flags,tmp);
//                        if (info.flags == 2) {
//                          callInfoBeanObj.setCall_SenderConfig(tmp);
//                        } else {
//                            send_command(tmp, info.flags);
//                        }
                        if (index >= 0) {
                            Frame frame = new Frame(frameID);
                            ByteBuffer outBuffer = codec.getOutputBuffer(index);
                            byte idrFrameType = 0x65;
                            int dataLength = 0;

                            outBuffer.get(outData);

                            // If SPS & PPS is not ready then
                            if (ENCODING.equalsIgnoreCase("h264") && ((SPS == null || SPS.length == 0) || (PPS == null || PPS.length == 0)))
                                getSPS_PPS(outData, 0);

                            dataLength = outData.length;

                            // If the frame is an IDR Frame then adding SPS & PPS in front of the actual frame data
                            if (ENCODING.equalsIgnoreCase("h264") && outData[4] == idrFrameType) {
                                int totalDataLength = dataLength + SPS.length + PPS.length;

                                frame.frameData = new byte[totalDataLength];

                                System.arraycopy(SPS, 0, frame.frameData, 0, SPS.length);
                                System.arraycopy(PPS, 0, frame.frameData, SPS.length, PPS.length);
                                System.arraycopy(outData, 0, frame.frameData, SPS.length + PPS.length, dataLength);
                            } else {
                                frame.frameData = new byte[dataLength];
                                System.arraycopy(outData, 0, frame.frameData, 0, dataLength);
                            }

                            // for testing
                            Log.e("EncodeDecode 1 ", "Frame no :: " + frameID + " :: frameSize:: " + frame.frameData.length + " :: ");
                            printByteArray(frame.frameData);

                            System.out.println("Size after Encoding " + dataLength + " " + frame.frameData.length);

                            // if encoding type is h264 and sps & pps is ready then, enqueueing the frame in the queue
                            // if encoding type is h263 then, enqueueing the frame in the queue
                            if ((ENCODING.equalsIgnoreCase("h264") && SPS != null && PPS != null && SPS.length != 0 && PPS.length != 0) || ENCODING.equalsIgnoreCase("h263")) {
                                Log.d("EncodeDecode", "enqueueing frame no: " + (frameID));

                                try {
                                    queue.put(frame);
                                } catch (InterruptedException e) {
                                    Log.e("EncodeDecode", "interrupted while waiting");
                                    e.printStackTrace();
                                } catch (NullPointerException e) {
                                    Log.e("EncodeDecode", "frame is null");
                                    e.printStackTrace();
                                } catch (IllegalArgumentException e) {
                                    Log.e("EncodeDecode", "problem inserting in the queue");
                                    e.printStackTrace();
                                }

                                Log.d("EncodeDecode", "frame enqueued. queue size now: " + queue.size());

                                if (firstTime) {
                                    if (mPlayer == null) {
                                        mPlayer = new PlayerThread(mSurface);
                                        mPlayer.start();
                                        System.out.println("PlayerThread started");
                                        Log.d("EncodeDecode", "PlayerThread started");
                                    }
                                    firstTime = false;
                                }
                            }


                            frameID++;
                            codec.releaseOutputBuffer(index, false);

                        } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                            outputBuffers = codec.getOutputBuffers();
                            Log.e("EncodeDecode", "output buffer of encoder : info changed");
                        } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            Log.e("EncodeDecode", "output buffer of encoder : format changed");
                        } else {
                            Log.e("EncodeDecode", "unknown value of outputBufferIndex : " + index);
                        }
//                        codec.releaseOutputBuffer(index, false);
                    }

                    @Override
                    public void onError(MediaCodec codec, MediaCodec.CodecException e) {
                        android.util.Log.w(TAG, "videoEncoderCore.mEncoder.onError: " + e);
                    }

                    @Override
                    public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                        // set camera change
                        try {
                            current_Camera = Integer.parseInt(mCamera.getId());
                        } catch (Exception e) {
                            System.out.println("exeption in camera:" + e.getMessage());
                        }
                    }
                });

                videoEncoderCore2.mEncoder.setCallback(new MediaCodec.Callback() {
                    @Override
                    public void onInputBufferAvailable(MediaCodec codec, int index) {

                    }

                    @Override
                    public void onOutputBufferAvailable(MediaCodec codec, int index,
                                                        MediaCodec.BufferInfo info) {
                        System.out.println("buffer available from camera 2 " + info.size);
                        byte[] outData = new byte[info.size];
//                        // mVideoOutputThread.render( info.flags,tmp);
//                        if (info.flags == 2) {
//                          callInfoBeanObj.setCall_SenderConfig(tmp);
//                        } else {
//                            send_command(tmp, info.flags);
//                        }
                        if (index >= 0) {
                            Frame frame = new Frame(frameID1);
                            ByteBuffer outBuffer = codec.getOutputBuffer(index);
                            byte idrFrameType = 0x65;
                            int dataLength = 0;

                            outBuffer.get(outData);

                            // If SPS1 & PPS1 is not ready then
                            if (ENCODING.equalsIgnoreCase("h264") && ((SPS1 == null || SPS1.length == 0) || (PPS1 == null || PPS1.length == 0)))
                                getSPS_PPS1(outData, 0);

                            dataLength = outData.length;

                            // If the frame is an IDR Frame then adding SPS1 & PPS1 in front of the actual frame data
                            if (ENCODING.equalsIgnoreCase("h264") && outData[4] == idrFrameType) {
                                int totalDataLength = dataLength + SPS1.length + PPS1.length;

                                frame.frameData = new byte[totalDataLength];

                                System.arraycopy(SPS1, 0, frame.frameData, 0, SPS1.length);
                                System.arraycopy(PPS1, 0, frame.frameData, SPS1.length, PPS1.length);
                                System.arraycopy(outData, 0, frame.frameData, SPS1.length + PPS1.length, dataLength);
                            } else {
                                frame.frameData = new byte[dataLength];
                                System.arraycopy(outData, 0, frame.frameData, 0, dataLength);
                            }

                            // for testing
                            Log.e("EncodeDecode 1 ", "Frame no :: " + frameID1 + " :: frameSize:: " + frame.frameData.length + " :: ");
                            printByteArray(frame.frameData);

                            System.out.println("Size after Encoding " + dataLength + " " + frame.frameData.length);

                            // if encoding type is h264 and SPS1 & PPS1 is ready then, enqueueing the frame in the queue1
                            // if encoding type is h263 then, enqueueing the frame in the queue1
                            if ((ENCODING.equalsIgnoreCase("h264") && SPS1 != null && PPS1 != null && SPS1.length != 0 && PPS1.length != 0) || ENCODING.equalsIgnoreCase("h263")) {
                                Log.d("EncodeDecode", "enqueueing frame no: " + (frameID1));

                                try {
                                    queue1.put(frame);
                                } catch (InterruptedException e) {
                                    Log.e("EncodeDecode", "interrupted while waiting");
                                    e.printStackTrace();
                                } catch (NullPointerException e) {
                                    Log.e("EncodeDecode", "frame is null");
                                    e.printStackTrace();
                                } catch (IllegalArgumentException e) {
                                    Log.e("EncodeDecode", "problem inserting in the queue1");
                                    e.printStackTrace();
                                }

                                Log.d("EncodeDecode", "frame enqueued. queue1 size now: " + queue1.size());

                                if (secondTime) {
                                    if (mPlayer2 == null) {
                                        mPlayer2 = new PlayerThread2(mSurface2);
                                        mPlayer2.start();
                                        System.out.println("PlayerThread started");
                                        Log.d("EncodeDecode", "PlayerThread started");
                                    }
                                    secondTime = false;
                                }
                            }


                            frameID1++;
                            codec.releaseOutputBuffer(index, false);

                        } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                            outputBuffers = codec.getOutputBuffers();
                            Log.e("EncodeDecode", "output buffer of encoder : info changed");
                        } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            Log.e("EncodeDecode", "output buffer of encoder : format changed");
                        } else {
                            Log.e("EncodeDecode", "unknown value of outputBufferIndex : " + index);
                        }
//                        codec.releaseOutputBuffer(index, false);
                    }

                    @Override
                    public void onError(MediaCodec codec, MediaCodec.CodecException e) {
                        android.util.Log.w(TAG, "videoEncoderCore.mEncoder.onError: " + e);
                    }

                    @Override
                    public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                        // set camera change
                        try {
                            current_Camera = Integer.parseInt(mCamera.getId());
                        } catch (Exception e) {
                            System.out.println("exeption in camera:" + e.getMessage());
                        }
                    }
                });
//                Surface encoderSurface = videoEncoderCore.mEncoder.createInputSurface();
                Surface encoderSurface = videoEncoderCore.getInputSurface();
                Surface encoderSurface1 = videoEncoderCore2.getInputSurface();
                videoEncoderCore.mEncoder.start();
                videoEncoderCore2.mEncoder.start();


                List<Surface> surfaces = new ArrayList<>();
                surfaces.add(encoderSurface);
                surfaces.add(encoderSurface1);


                // Creating surface from texture to display my video
//                try {
//                    while (mPeerSurfaceTextureMyself == null)
//                        Thread.sleep(1);
//                } catch (InterruptedException ex) {
//                    return;
//                }
                Surface mSurfaceMyself = new Surface(mTextureView.getSurfaceTexture());
                surfaces.add(mSurfaceMyself);

                CaptureRequest.Builder builder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                // builder.setRepeatingRequest(builder.build(), yourCaptureCallback,
                // yourBackgroundHandler);
                // builder.addTarget(encoderSurface);
                for (Surface s : surfaces)
                    builder.addTarget(s);

                final CaptureRequest captureRequest = builder.build();

                mCamera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        if (null == mCamera) {
                            return;
                        }
                        mCameraCaptureSession = session;
                        try {

                            setUpCaptureRequestBuilder(builder);
                            HandlerThread thread = new HandlerThread("CameraPreview");
                            thread.start();
                            // session.setRepeatingRequest(captureRequest, null, mBackgroundHandler);
                            session.setRepeatingRequest(builder.build(), null, mBackgroundHandler);
                        } catch (CameraAccessException ex) {

                            mCameraCaptureSession = null;
                            android.util.Log.e(TAG, "Error in setRepeatingRequest: " + ex);
                        } catch (IllegalStateException ex) {

                            mCameraCaptureSession = null;
                            android.util.Log.e(TAG, "Error in setRepeatingRequest: " + ex);
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        mCameraCaptureSession = null;
                        android.util.Log.e(TAG, "onConfigureFailed");
                    }

                }, mBackgroundHandler);

            } catch (CameraAccessException ex) {
                android.util.Log.e(TAG, "Error in openVideoEncoder: " + android.util.Log.getStackTraceString(ex));
            }
        }

        private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        }
    }

    private String findCamera(int facing_switch) {

        try {
            CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            String[] cams = new String[0];
            cams = cameraManager.getCameraIdList();
            String selectedCam = null;
            for (String name : cams) {

                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(name);
                int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing_switch == CameraCharacteristics.LENS_FACING_BACK
                        && facing == CameraCharacteristics.LENS_FACING_BACK) {

                    selectedCam = name;
                    sCameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    degrees = 90.0f;
                    break;
                } else if (facing_switch == CameraCharacteristics.LENS_FACING_FRONT
                        && facing == CameraCharacteristics.LENS_FACING_FRONT) {

                    selectedCam = name;
                    sCameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    degrees = 270.0f;
                    break;
                } else if (selectedCam == null)
                    selectedCam = name;

            }
            return selectedCam;
        } catch (CameraAccessException | NullPointerException ex) {

            android.util.Log.e("Video Call", "Error in findCamera: " + android.util.Log.getStackTraceString(ex));
            return null;
        }
    }

    int getFrontCameraId() {
        Camera.CameraInfo ci = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) return i;
        }
        return -1; // No front-facing camera found
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private static class Frame {
        public int id;
        public byte[] frameData;

        public Frame(int id) {
            this.id = id;
        }
    }

    private class PlayerThread extends Thread {
        //private MediaExtractor extractor;
        private MediaCodec decoder;
        private Surface surface;

        public PlayerThread(Surface surface) {
            this.surface = surface;
        }

        @Override
        public void run() {
            while (SPS == null || PPS == null || SPS.length == 0 || PPS.length == 0) {
                try {
                    Log.d("EncodeDecode", "DECODER_THREAD:: sps,pps not ready yet");
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            }

            Log.d("EncodeDecode", "DECODER_THREAD:: sps,pps READY");

            if (ENCODING.equalsIgnoreCase("h264")) {
                try {
                    decoder = MediaCodec.createDecoderByType("video/avc");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", 1920, 1080);
                mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 60000000);
                mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 90);
                mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
                mediaFormat.setInteger(MediaFormat.KEY_ROTATION, -360);
                mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(SPS));
                mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(PPS));
                decoder.configure(mediaFormat, surface /* surface */, null /* crypto */, 0 /* flags */);
            } else if (ENCODING.equalsIgnoreCase("h263")) {
                try {
                    decoder = MediaCodec.createDecoderByType("video/3gpp");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/3gpp", 352, 288);
                decoder.configure(mediaFormat, surface /* surface */, null /* crypto */, 0 /* flags */);
            }

            if (decoder == null) {
                Log.e("DecodeActivity", "DECODER_THREAD:: Can't find video info!");
                return;
            }

            decoder.start();
            Log.d("EncodeDecode", "DECODER_THREAD:: decoder.start() called");

            ByteBuffer[] inputBuffers = decoder.getInputBuffers();
            ByteBuffer[] outputBuffers = decoder.getOutputBuffers();


            int i = 0;
            while (!Thread.interrupted()) {
                Frame currentFrame = null;
                try {
                    Log.d("EncodeDecode", "DECODER_THREAD:: calling queue.take(), if there is no frame in the queue it will wait");
                    currentFrame = queue.take();
                } catch (InterruptedException e) {
                    Log.e("EncodeDecode", "DECODER_THREAD:: interrupted while PlayerThread was waiting for the next frame");
                    e.printStackTrace();
                }

                if (currentFrame == null)
                    Log.e("EncodeDecode", "DECODER_THREAD:: null frame dequeued");
                else
                    Log.d("EncodeDecode", "DECODER_THREAD:: " + currentFrame.id + " no frame dequeued");

                if (currentFrame != null && currentFrame.frameData != null && currentFrame.frameData.length != 0) {
                    Log.d("EncodeDecode", "DECODER_THREAD:: decoding frame no: " + i + " , dataLength = " + currentFrame.frameData.length);

                    int inIndex = 0;
                    while ((inIndex = decoder.dequeueInputBuffer(1)) < 0)
                        ;

                    if (inIndex >= 0) {
                        Log.d("EncodeDecode", "DECODER_THREAD:: sample size: " + currentFrame.frameData.length);

                        ByteBuffer buffer = inputBuffers[inIndex];
                        buffer.clear();
                        buffer.put(currentFrame.frameData);
                        decoder.queueInputBuffer(inIndex, 0, currentFrame.frameData.length, 0, 0);

                        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                        int outIndex = decoder.dequeueOutputBuffer(info, 100000);

                        switch (outIndex) {
                            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                                Log.e("EncodeDecode", "DECODER_THREAD:: INFO_OUTPUT_BUFFERS_CHANGED");
                                outputBuffers = decoder.getOutputBuffers();
                                break;
                            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                                Log.e("EncodeDecode", "DECODER_THREAD:: New format " + decoder.getOutputFormat());

                                break;
                            case MediaCodec.INFO_TRY_AGAIN_LATER:
                                Log.e("EncodeDecode", "DECODER_THREAD:: dequeueOutputBuffer timed out!");
                                break;
                            default:
                                Log.d("EncodeDecode", "DECODER_THREAD:: decoded SUCCESSFULLY!!!");
                                ByteBuffer outbuffer = outputBuffers[outIndex];
                                decoder.releaseOutputBuffer(outIndex, true);
                                break;
                        }
                        i++;
                    }
                }
            }

            decoder.stop();
            decoder.release();

        }
    }

    private class PlayerThread2 extends Thread {
        //private MediaExtractor extractor;
        private MediaCodec decoder;
        private Surface surface;

        public PlayerThread2(Surface surface) {
            this.surface = surface;
        }

        @Override
        public void run() {
            while (SPS1 == null || PPS1 == null || SPS1.length == 0 || PPS1.length == 0) {
                System.out.println("thread 2 running");
                try {
                    Log.d("EncodeDecode", "DECODER_THREAD:: sps,pps not ready yet");
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            }

            Log.d("EncodeDecode", "DECODER_THREAD:: sps,pps READY");

            if (ENCODING.equalsIgnoreCase("h264")) {
                try {
                    decoder = MediaCodec.createDecoderByType("video/avc");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", VideoWidthHD, VideoHeightHD);
                mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(SPS1));
                mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(PPS1));
                decoder.configure(mediaFormat, surface /* surface */, null /* crypto */, 0 /* flags */);
            } else if (ENCODING.equalsIgnoreCase("h263")) {
                try {
                    decoder = MediaCodec.createDecoderByType("video/3gpp");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/3gpp", 352, 288);
                decoder.configure(mediaFormat, surface /* surface */, null /* crypto */, 0 /* flags */);
            }

            if (decoder == null) {
                Log.e("DecodeActivity", "DECODER_THREAD:: Can't find video info!");
                return;
            }

            decoder.start();
            Log.d("EncodeDecode", "DECODER_THREAD:: decoder.start() called");

            ByteBuffer[] inputBuffers = decoder.getInputBuffers();
            ByteBuffer[] outputBuffers = decoder.getOutputBuffers();


            int i = 0;
            while (!Thread.interrupted()) {
                Frame currentFrame = null;
                try {
                    Log.d("EncodeDecode", "DECODER_THREAD:: calling queue1.take(), if there is no frame in the queue1 it will wait");
                    currentFrame = queue1.take();
                } catch (InterruptedException e) {
                    Log.e("EncodeDecode", "DECODER_THREAD:: interrupted while PlayerThread was waiting for the next frame");
                    e.printStackTrace();
                }

                if (currentFrame == null)
                    Log.e("EncodeDecode", "DECODER_THREAD:: null frame dequeued");
                else
                    Log.d("EncodeDecode", "DECODER_THREAD:: " + currentFrame.id + " no frame dequeued");

                if (currentFrame != null && currentFrame.frameData != null && currentFrame.frameData.length != 0) {
                    Log.d("EncodeDecode", "DECODER_THREAD:: decoding frame no: " + i + " , dataLength = " + currentFrame.frameData.length);

                    int inIndex = 0;
                    while ((inIndex = decoder.dequeueInputBuffer(1)) < 0)
                        ;

                    if (inIndex >= 0) {
                        Log.d("EncodeDecode", "DECODER_THREAD:: sample size: " + currentFrame.frameData.length);

                        ByteBuffer buffer = inputBuffers[inIndex];
                        buffer.clear();
                        buffer.put(currentFrame.frameData);
                        decoder.queueInputBuffer(inIndex, 0, currentFrame.frameData.length, 0, 0);

                        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                        int outIndex = decoder.dequeueOutputBuffer(info, 100000);

                        switch (outIndex) {
                            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                                Log.e("EncodeDecode", "DECODER_THREAD:: INFO_OUTPUT_BUFFERS_CHANGED");
                                outputBuffers = decoder.getOutputBuffers();
                                break;
                            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                                Log.e("EncodeDecode", "DECODER_THREAD:: New format " + decoder.getOutputFormat());

                                break;
                            case MediaCodec.INFO_TRY_AGAIN_LATER:
                                Log.e("EncodeDecode", "DECODER_THREAD:: dequeueOutputBuffer timed out!");
                                break;
                            default:
                                Log.d("EncodeDecode", "DECODER_THREAD:: decoded SUCCESSFULLY!!!");
                                ByteBuffer outbuffer = outputBuffers[outIndex];
                                decoder.releaseOutputBuffer(outIndex, true);
                                break;
                        }
                        i++;
                    }
                }
            }

            decoder.stop();
            decoder.release();

        }
    }

    /**========================================================================*/
    /**
     * Prints the byte array in hex
     */
    private void printByteArray(byte[] array) {
        StringBuilder sb1 = new StringBuilder();
        for (byte b : array) {
            sb1.append(String.format("%02X ", b));
        }
        Log.d("EncodeDecode", sb1.toString());
    }


    /**========================================================================*/
    /**
     * For H264 encoding, this function will retrieve SPS & PPS from the given data and will insert into SPS & PPS global arrays.
     */
    public static void getSPS_PPS(byte[] data, int startingIndex) {
        byte[] spsHeader = {0x00, 0x00, 0x00, 0x01, 0x67};
        byte[] ppsHeader = {0x00, 0x00, 0x00, 0x01, 0x68};
        byte[] frameHeader = {0x00, 0x00, 0x00, 0x01};

        int spsStartingIndex = -1;
        int nextFrameStartingIndex = -1;
        int ppsStartingIndex = -1;

        spsStartingIndex = find(data, spsHeader, startingIndex);
        Log.d("EncodeDecode", "spsStartingIndex: " + spsStartingIndex);
        if (spsStartingIndex >= 0) {
            nextFrameStartingIndex = find(data, frameHeader, spsStartingIndex + 1);
            int spsLength = 0;
            if (nextFrameStartingIndex >= 0)
                spsLength = nextFrameStartingIndex - spsStartingIndex;
            else
                spsLength = data.length - spsStartingIndex;
            if (spsLength > 0) {
                SPS = new byte[spsLength];
                System.arraycopy(data, spsStartingIndex, SPS, 0, spsLength);
            }
        }

        ppsStartingIndex = find(data, ppsHeader, startingIndex);
        Log.d("EncodeDecode", "ppsStartingIndex: " + ppsStartingIndex);
        if (ppsStartingIndex >= 0) {
            nextFrameStartingIndex = find(data, frameHeader, ppsStartingIndex + 1);
            int ppsLength = 0;
            if (nextFrameStartingIndex >= 0)
                ppsLength = nextFrameStartingIndex - ppsStartingIndex;
            else
                ppsLength = data.length - ppsStartingIndex;
            if (ppsLength > 0) {
                PPS = new byte[ppsLength];
                System.arraycopy(data, ppsStartingIndex, PPS, 0, ppsLength);
            }
        }
    }

    /**========================================================================*/
    /**
     * For H264 encoding, this function will retrieve SPS1 & PPS from the given data and will insert into SPS1 & PPS global arrays.
     */
    public static void getSPS_PPS1(byte[] data, int startingIndex) {
        byte[] spsHeader = {0x00, 0x00, 0x00, 0x01, 0x67};
        byte[] ppsHeader = {0x00, 0x00, 0x00, 0x01, 0x68};
        byte[] frameHeader = {0x00, 0x00, 0x00, 0x01};

        int spsStartingIndex = -1;
        int nextFrameStartingIndex = -1;
        int ppsStartingIndex = -1;

        spsStartingIndex = find(data, spsHeader, startingIndex);
        Log.d("EncodeDecode", "spsStartingIndex: " + spsStartingIndex);
        if (spsStartingIndex >= 0) {
            nextFrameStartingIndex = find(data, frameHeader, spsStartingIndex + 1);
            int spsLength = 0;
            if (nextFrameStartingIndex >= 0)
                spsLength = nextFrameStartingIndex - spsStartingIndex;
            else
                spsLength = data.length - spsStartingIndex;
            if (spsLength > 0) {
                SPS1 = new byte[spsLength];
                System.arraycopy(data, spsStartingIndex, SPS1, 0, spsLength);
            }
        }

        ppsStartingIndex = find(data, ppsHeader, startingIndex);
        Log.d("EncodeDecode", "ppsStartingIndex: " + ppsStartingIndex);
        if (ppsStartingIndex >= 0) {
            nextFrameStartingIndex = find(data, frameHeader, ppsStartingIndex + 1);
            int ppsLength = 0;
            if (nextFrameStartingIndex >= 0)
                ppsLength = nextFrameStartingIndex - ppsStartingIndex;
            else
                ppsLength = data.length - ppsStartingIndex;
            if (ppsLength > 0) {
                PPS1 = new byte[ppsLength];
                System.arraycopy(data, ppsStartingIndex, PPS1, 0, ppsLength);
            }
        }
    }


    /**========================================================================*/
    /**
     * This function gets the starting index of the first appearance of match array in source array. The function will search in source array from startIndex position.
     */
    public static int find(byte[] source, byte[] match, int startIndex) {
        if (source == null || match == null) {
            Log.d("EncodeDecode", "ERROR in find : null");
            return -1;
        }
        if (source.length == 0 || match.length == 0) {
            Log.d("EncodeDecode", "ERROR in find : length 0");
            return -1;
        }

        int ret = -1;
        int spos = startIndex;
        int mpos = 0;
        byte m = match[mpos];
        for (; spos < source.length; spos++) {
            if (m == source[spos]) {
                // starting match
                if (mpos == 0)
                    ret = spos;
                    // finishing match
                else if (mpos == match.length - 1)
                    return ret;

                mpos++;
                m = match[mpos];
            } else {
                ret = -1;
                mpos = 0;
                m = match[mpos];
            }
        }
        return ret;
    }

    public static byte[] YV12toYUV420PackedSemiPlanar(final byte[] input, final int width, final int height) {
        /*
         * COLOR_TI_FormatYUV420PackedSemiPlanar is NV12
         * We convert by putting the corresponding U and V bytes together (interleaved).
         */
        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;
        byte[] output = new byte[input.length];
        System.out.println("input length and frame size for raw bytes " + frameSize + " " + input.length + " " + output.length);

        System.arraycopy(input, 0, output, 0, frameSize);
        for (int i = 0; i < (qFrameSize); i++) {
            byte b = (input[frameSize + qFrameSize + i - 17 - width]);
            output[frameSize + i * 2] = b;
            output[frameSize + i * 2 + 1] = (input[frameSize + i - 17 - width]);
        }


        System.arraycopy(input, 0, output, 0, frameSize); // Y

        for (int i = 0; i < qFrameSize; i++) {
            output[frameSize + i * 2] = input[frameSize + i + qFrameSize]; // Cb (U)
            output[frameSize + i * 2 + 1] = input[frameSize + i]; // Cr (V)
        }
        return output;
    }

}