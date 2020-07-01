package com.example.camerademo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MainActivity4 extends Activity /*implements SurfaceHolder.Callback*/ {

    Camera mCamera;
    FileOutputStream fos;
    File mVideoFile;
    MediaCodec mMediaCodec, mMediaCodec2;
    ByteBuffer[] inputBuffers;
    ByteBuffer[] outputBuffers;
    ByteBuffer[] inputBuffers1;
    ByteBuffer[] outputBuffers1;
    MySurfaceView cameraSurfaceView;
    SurfaceView decodedSurfaceView;
    LinearLayout ll;
    RelativeLayout rl;
    Button btn;
    boolean mPreviewRunning = false;
    boolean firstTime = true;
    boolean secondTime = true;
    boolean isRunning = false;
    private boolean cameraFront = false;
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
    public static LinearLayout outputView;
    static final int VideoWidthHD = 320;
    static final int VideoHeightHD = 240;
    static final int VideoWidthLD = 320;
    static final int VideoHeightLD = 240;
    public int sCameraOrientation = -1;
    Surface mSurface, mSurface2;
    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    EncodeDecodeTest encodeDecodeTest;

    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    Activity activity = MainActivity4.this;


    private static class Frame {
        public int id;
        public byte[] frameData;

        public Frame(int id) {
            this.id = id;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialise();
        checkPermissions();
//
//        setContentView(R.layout.activity_main2);
//
//        cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
//        surfaceView1 = findViewById(R.id.textureView1);
//        surfaceView2 = findViewById(R.id.textureView2);

    }

    public void initialise()
    {
        encodeDecodeTest = new EncodeDecodeTest(getApplicationContext());
        ll = new LinearLayout(getApplicationContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        setContentView(ll);
//        if (!hasPermissionsGranted(VIDEO_PERMISSIONS))
//            MainActivity.this.requestPermissions(VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
//        else initializeFurther();
        initializeFurther();
    }

    public void initializeFurther() {
//        ll = new LinearLayout(getApplicationContext());
//        ll.setOrientation(LinearLayout.VERTICAL);
        outputView = new LinearLayout(getApplicationContext());
        outputView.setOrientation(LinearLayout.HORIZONTAL);
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        cameraSurfaceView = new MySurfaceView(getApplicationContext());
        if (ENCODING.equalsIgnoreCase("h264")) {
            cameraSurfaceView.setLayoutParams(new android.widget.FrameLayout.LayoutParams(width, height / 2));
        } else if (ENCODING.equalsIgnoreCase("h263")) {
            cameraSurfaceView.setLayoutParams(new android.widget.FrameLayout.LayoutParams(width, height / 2));
        }
        ll.addView(cameraSurfaceView);
        ll.addView(outputView);

        initCodec();
        initCodec2();
        setContentView(ll);
    }


    /**
     * Checks the dynamically-controlled permissions and requests missing permissions from end user.
     */
    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : VIDEO_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_VIDEO_PERMISSIONS);
        } else {
            final int[] grantResults = new int[VIDEO_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_VIDEO_PERMISSIONS, VIDEO_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_VIDEO_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                initialise();
                break;
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
        mPreviewRunning = false;

        if (cameraSurfaceView != null && cameraSurfaceView.isEnabled())
            cameraSurfaceView.setEnabled(false);
        cameraSurfaceView = null;

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }

//        System.exit(0);

        mMediaCodec.stop();
        mMediaCodec.release();
        mMediaCodec = null;
        mMediaCodec2.stop();
        mMediaCodec2.release();
        mMediaCodec2 = null;

    }

    ;


    private void initCodec() {

        MediaFormat mediaFormat = null;

        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }

        if (ENCODING.equalsIgnoreCase("h264")) {
            try {
                mMediaCodec = MediaCodec.createEncoderByType("video/avc");
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaFormat = MediaFormat.createVideoFormat("video/avc",
                    VideoWidthHD,
                    VideoHeightHD);
        } else if (ENCODING.equalsIgnoreCase("h263")) {
            try {
                mMediaCodec = MediaCodec.createEncoderByType("video/3gpp");
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaFormat = MediaFormat.createVideoFormat("video/3gpp",
                    352,
                    288);
        }

        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 10000000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
//        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 8000);
//        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);

        try {
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);

            mMediaCodec.configure(mediaFormat,
                    null,
                    null,
                    MediaCodec.CONFIGURE_FLAG_ENCODE);
            frameID = 0;
            mMediaCodec.start();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "mediaformat error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    private void initCodec2() {

        MediaFormat mediaFormat = null;

        if (mMediaCodec2 != null) {
            mMediaCodec2.stop();
            mMediaCodec2.release();
            mMediaCodec2 = null;
        }

        if (ENCODING.equalsIgnoreCase("h264")) {
            try {
                mMediaCodec2 = MediaCodec.createEncoderByType("video/avc");
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaFormat = MediaFormat.createVideoFormat("video/avc",
                    VideoWidthHD,
                    VideoHeightHD);
        } else if (ENCODING.equalsIgnoreCase("h263")) {
            try {
                mMediaCodec2 = MediaCodec.createEncoderByType("video/3gpp");
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaFormat = MediaFormat.createVideoFormat("video/3gpp",
                    352,
                    288);
        }

        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 95000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
//        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 8000);
//        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);

        try {
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);

            mMediaCodec2.configure(mediaFormat,
                    null,
                    null,
                    MediaCodec.CONFIGURE_FLAG_ENCODE);
            frameID1 = 0;
            mMediaCodec2.start();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "mediaformat error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
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
     * For H264 encoding, this function will retrieve SPS & PPS from the given data and will insert into SPS & PPS global arrays.
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
     * Prints the byte array in hex
     */
    private void printByteArray(byte[] array) {
        StringBuilder sb1 = new StringBuilder();
        for (byte b : array) {
            sb1.append(String.format("%02X ", b));
        }
        Log.d("EncodeDecode", sb1.toString());
    }

    public static byte[] YV12toYUV420PackedSemiPlanar(final byte[] input, final int width, final int height) {
        /*
         * COLOR_TI_FormatYUV420PackedSemiPlanar is NV12
         * We convert by putting the corresponding U and V bytes together (interleaved).
         */
        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;
        byte[] output = new byte[input.length];
        System.out.println("input length and frame size for raw bytes "+frameSize +" "+input.length+" "+output.length);

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

    public static byte[] YV12toYUV420PackedSemiPlanar1(final byte[] input, final int width, final int height) {
        /*
         * COLOR_TI_FormatYUV420PackedSemiPlanar is NV12
         * We convert by putting the corresponding U and V bytes together (interleaved).
         */
        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;
        byte[] output = new byte[input.length];


        System.arraycopy(input, 0, output, 0, frameSize);
        for (int i = 0; i < (qFrameSize); i++) {
            byte b = (input[frameSize + qFrameSize + i - 32 - VideoWidthHD]);
            output[frameSize + i * 2] = b;
            output[frameSize + i * 2 + 1] = (input[frameSize + i - 32 - VideoWidthHD]);
        }


        System.arraycopy(input, 0, output, 0, frameSize); // Y

        for (int i = 0; i < qFrameSize; i++) {
            output[frameSize + i * 2] = input[frameSize + i + qFrameSize]; // Cb (U)
            output[frameSize + i * 2 + 1] = input[frameSize + i]; // Cr (V)
        }
        return output;
    }

    private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    /**========================================================================*/
    /**
     * When camera receives a frame this function is called with the frame data as its parameter. It encodes the given data and then stores in frameQueue.
     */
    private void encode(byte[] data) {
        Log.d("EncodeDecode", "ENCODE FUNCTION CALLED");
        inputBuffers = mMediaCodec.getInputBuffers();
        outputBuffers = mMediaCodec.getOutputBuffers();
        System.out.println("Size after Encoding 111 "+outputBuffers.length);

        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(0);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();

            int size = inputBuffer.limit();
            System.out.println("Size after Encoding 11 "+size);
            //inputBuffer.put(data);

            // color right, but rotated
            byte[] output = YV12toYUV420PackedSemiPlanar(data, VideoWidthHD, VideoHeightHD);
            System.out.println("Size after Encoding 1 "+output.length);
//            byte[] output =rotateYUV420Degree90(output1,VideoWidthHD,VideoHeightHD);
            inputBuffer.put(output);

            // color almost right, orientation ok but distorted
            /*byte[] output = YV12toYUV420PackedSemiPlanar(data,320,240);
            output = rotateYUV420Degree90(output,320,240);
            inputBuffer.put(output);*/

            mMediaCodec.queueInputBuffer(inputBufferIndex, 0 /* offset */, size, 0 /* timeUs */, 0);
            Log.d("EncodeDecode", "InputBuffer queued");
        } else {
            Log.d("EncodeDecode", "inputBufferIndex < 0, returning null");
            return;
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        Log.d("EncodeDecode", "outputBufferIndex = " + outputBufferIndex);
        do {
            if (outputBufferIndex >= 0) {
                Frame frame = new Frame(frameID);
                ByteBuffer outBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
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

                System.out.println("Size after Encoding "+dataLength+ " "+frame.frameData.length);

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
                        Log.d("EncodeDecode", "adding a surface to layout for decoder");
                        sv = new SurfaceView(getApplicationContext());
                        sv.setLayoutParams(new android.widget.FrameLayout.LayoutParams(width / 2, height / 2));
                        tv1 = new TextureView(getApplicationContext());
                        tv1.setLayoutParams(new android.widget.FrameLayout.LayoutParams(width / 4, height / 4));
                        tv1.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                            @Override
                            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                                mSurface = new Surface(surface);

                                final Matrix matrix = new Matrix();
                                float sx = VideoWidthHD / (float) tv1.getWidth();
                                float sy = VideoHeightHD / (float) tv1.getHeight();
                                if (VideoWidthHD > VideoHeightHD == tv1.getWidth() > tv1.getHeight()) {
                                    float max = Math.max(sx, sy);
                                    matrix.setScale(sx / max, sy / max, tv1.getWidth() * 0.5f, tv1.getHeight() * 0.5f);
                                } else {
                                    float max = Math.max(VideoWidthHD / (float) tv1.getHeight(),
                                            VideoHeightHD / (float) tv1.getWidth());
                                    matrix.setScale(sx / max, sy / max, tv1.getWidth() * 0.5f, tv1.getHeight() * 0.5f);
                                    matrix.postRotate(-90.0f, tv1.getWidth() * 0.5f, tv1.getHeight() * 0.5f);
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv1.setTransform(matrix);
                                    }
                                });

//                                Matrix matrix1 = new Matrix();
//                                //The video will be streched if the aspect ratio is in 1,5(recording at VideoHeightHD)
//                                RectF src;
//                                if (true)
////In my case, I changed this line, because with my onMeasure() and onLayout() methods my container view is already rotated and scaled, so I need to sent the inverted params to the src.
//                                    src = new RectF(0, 0,tv1.getWidth(), tv1.getHeight());
//                                else
//                                    src = new RectF(0, 0, tv1.getWidth(),tv1.getHeight());
//                                RectF dst = new RectF(0, 0, width, height);
//                                RectF screen = new RectF(dst);
////                                Log.d(TAG, "Matrix: " + width + "x" + height);
////                                Log.d(TAG, "Matrix: " + mThumbnailContainer.getmWidth() + "x" + mThumbnailContainer.getmHeight());
//                                matrix1.postRotate(90, screen.centerX(), screen.centerY());
//                                matrix1.mapRect(dst);
//
//                                matrix1.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
//                                matrix1.mapRect(src);
//
//                                matrix1.setRectToRect(screen, src, Matrix.ScaleToFit.FILL);
//                                matrix1.postRotate(180, screen.centerX(), screen.centerY());
//
//                                tv1.setTransform(matrix1);
                                if (mPlayer == null) {
                                    mPlayer = new PlayerThread(mSurface);
                                    mPlayer.start();
                                    System.out.println("PlayerThread started");
                                    Log.d("EncodeDecode", "PlayerThread started");
                                }
                            }

                            @Override
                            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                            }

                            @Override
                            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                                if (mPlayer != null) {
                                    mPlayer.interrupt();
                                }
                                return false;
                            }

                            @Override
                            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                            }
                        });
                        sv.getHolder().addCallback(new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(SurfaceHolder holder) {
                                Log.d("EncodeDecode", "mainActivity surfaceCreated");

                            }

                            @Override
                            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                                Log.d("EncodeDecode", "mainActivity surfaceChanged.");
                                if (mPlayer == null) {
                                    mPlayer = new PlayerThread(holder.getSurface());
                                    mPlayer.start();
                                    System.out.println("PlayerThread started");
                                    Log.d("EncodeDecode", "PlayerThread started");
                                }
                            }

                            @Override
                            public void surfaceDestroyed(SurfaceHolder holder) {
                                if (mPlayer != null) {
                                    mPlayer.interrupt();
                                }
                            }
                        });
                        outputView.addView(tv1);
                        MainActivity4.this.setContentView(ll);
                        firstTime = false;
                    }
                }

                frameID++;
                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);

            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mMediaCodec.getOutputBuffers();
                Log.e("EncodeDecode", "output buffer of encoder : info changed");
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.e("EncodeDecode", "output buffer of encoder : format changed");
            } else {
                Log.e("EncodeDecode", "unknown value of outputBufferIndex : " + outputBufferIndex);
                //printByteArray(data);
            }
        } while (outputBufferIndex >= 0);
    }


    /**========================================================================*/
    /**
     * When camera receives a frame this function is called with the frame data as its parameter. It encodes the given data and then stores in frameQueue.
     */
    private void encode2(byte[] data) {
        try{
            Log.d("EncodeDecode", "ENCODE FUNCTION CALLED");
            inputBuffers1 = mMediaCodec2.getInputBuffers();
            outputBuffers1 = mMediaCodec2.getOutputBuffers();

            int inputBufferIndex = mMediaCodec2.dequeueInputBuffer(0);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers1[inputBufferIndex];
                inputBuffer.clear();

                int size = inputBuffer.limit();
                System.out.println("input length and frame size for encoder 2"+size);
                //inputBuffer.put(data);

                // color right, but rotated
                byte[] output = YV12toYUV420PackedSemiPlanar(data, VideoWidthHD, VideoHeightHD);
//            byte[] output =rotateYUV420Degree90(output1,320,420);
                inputBuffer.put(output);

                // color almost right, orientation ok but distorted
            /*byte[] output = YV12toYUV420PackedSemiPlanar(data,320,240);
            output = rotateYUV420Degree90(output,320,240);
            inputBuffer.put(output);*/

                mMediaCodec2.queueInputBuffer(inputBufferIndex, 0 /* offset */, size, 0 /* timeUs */, 0);
                Log.d("EncodeDecode", "InputBuffer queued");
            } else {
                Log.d("EncodeDecode", "inputBufferIndex < 0, returning null");
                return;
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mMediaCodec2.dequeueOutputBuffer(bufferInfo, 0);
            Log.d("EncodeDecode", "outputBufferIndex = " + outputBufferIndex);
            do {
                if (outputBufferIndex >= 0) {
                    Frame frame = new Frame(frameID1);
                    ByteBuffer outBuffer = outputBuffers1[outputBufferIndex];
                    byte[] outData = new byte[bufferInfo.size];
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
                    Log.e("EncodeDecode 2 ", "Frame no :: " + frameID1 + " :: frameSize:: " + frame.frameData.length + " :: ");
                    printByteArray(frame.frameData);

                    // if encoding type is h264 and SPS1 & PPS1 is ready then, enqueueing the frame in the queue
                    // if encoding type is h263 then, enqueueing the frame in the queue
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
                            Log.d("EncodeDecode", "adding a surface to layout for decoder");
                            SurfaceView sv2 = new SurfaceView(getApplicationContext());
                            sv2.setLayoutParams(new android.widget.FrameLayout.LayoutParams(width / 2, height / 2));
                            tv2 = new TextureView(getApplicationContext());
                            tv2.setLayoutParams(new android.widget.FrameLayout.LayoutParams(width / 2, height / 2));
                            tv2.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                                @Override
                                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                                    mSurface2 = new Surface(surface);

                                    final Matrix matrix = new Matrix();
                                    float sx = VideoWidthHD / (float) tv2.getWidth();
                                    float sy = VideoHeightHD / (float) tv2.getHeight();
                                    if (VideoWidthHD > VideoHeightHD == tv2.getWidth() > tv2.getHeight()) {
                                        float max = Math.max(sx, sy);
                                        matrix.setScale(sx / max, sy / max, tv2.getWidth() * 0.5f, tv2.getHeight() * 0.5f);
                                    } else {
                                        float max = Math.max(VideoWidthHD / (float) tv2.getHeight(),
                                                VideoHeightHD / (float) tv2.getWidth());
                                        matrix.setScale(sx / max, sy / max, tv2.getWidth() * 0.5f, tv2.getHeight() * 0.5f);
                                        matrix.postRotate(-90.0f, tv2.getWidth() * 0.5f, tv2.getHeight() * 0.5f);
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            tv2.setTransform(matrix);
                                        }
                                    });
                                    if (mPlayer2 == null) {
                                        mPlayer2 = new PlayerThread2(mSurface2);
                                        mPlayer2.start();
                                        System.out.println("PlayerThread started");
                                        Log.d("EncodeDecode", "PlayerThread started");
                                    }
                                }

                                @Override
                                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                                }

                                @Override
                                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                                    if (mPlayer2 != null) {
                                        mPlayer2.interrupt();
                                    }
                                    return false;
                                }

                                @Override
                                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                                }
                            });
                            sv2.getHolder().addCallback(new SurfaceHolder.Callback() {
                                @Override
                                public void surfaceCreated(SurfaceHolder holder) {
                                    Log.d("EncodeDecode", "mainActivity surfaceCreated");
                                }

                                @Override
                                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                                    Log.d("EncodeDecode", "mainActivity surfaceChanged.");
                                    if (mPlayer2 == null) {

                                        mPlayer2 = new PlayerThread2(holder.getSurface());
                                        mPlayer2.start();
                                        System.out.println("PlayerThread2 started");
                                        Log.d("EncodeDecode", "PlayerThread2 started");
                                    }
                                }

                                @Override
                                public void surfaceDestroyed(SurfaceHolder holder) {
                                    if (mPlayer2 != null) {
                                        mPlayer2.interrupt();
                                    }
                                }
                            });
                            outputView.addView(tv2);
                            MainActivity4.this.setContentView(ll);
                            secondTime = false;
                        }
                    }

                    frameID1++;
                    mMediaCodec2.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = mMediaCodec2.dequeueOutputBuffer(bufferInfo, 0);

                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    outputBuffers1 = mMediaCodec2.getOutputBuffers();
                    Log.e("EncodeDecode", "output buffer of encoder : info changed");
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    Log.e("EncodeDecode", "output buffer of encoder : format changed");
                } else {
                    Log.e("EncodeDecode", "unknown value of outputBufferIndex : " + outputBufferIndex);
                    //printByteArray(data);
                }
            } while (outputBufferIndex >= 0);
        }catch(Exception e){

        }

    }

    private class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
        SurfaceHolder holder;

        public MySurfaceView(Context context) {
            super(context);
            holder = this.getHolder();
            holder.addCallback(this);
        }

        public MySurfaceView(Context context, AttributeSet attrs) {
            super(context, attrs);
            holder = this.getHolder();
            holder.addCallback(this);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            try {
                try {
                    if (mCamera == null)
                        mCamera = Camera.open(findFrontFacingCamera());
                    mCamera.setDisplayOrientation(90);
                    Log.d("EncodeDecode", "Camera opened");
                } catch (Exception e) {
                    Log.d("EncodeDecode", "Camera open failed");
                    e.printStackTrace();
                }

                Camera.Parameters p = mCamera.getParameters();

                if (ENCODING.equalsIgnoreCase("h264"))
                    p.setPreviewSize(VideoWidthHD, VideoHeightHD);
                else if (ENCODING.equalsIgnoreCase("h263"))
                    p.setPreviewSize(352, 288);

                mCamera.setParameters(p);
                mCamera.setPreviewDisplay(holder);

                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        Log.d("EncodeDecode", "onPreviewFrame, calling encode function");
                        encode(data);
                        encode2(data);
//                        try {
//                            encodeDecodeTest.testEncodeDecodeVideoFromBufferToBufferQCIF();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                    }
                });
                mCamera.startPreview();
                mPreviewRunning = true;
            } catch (IOException e) {
                Log.e("EncodeDecode", "surfaceCreated():: in setPreviewDisplay(holder) function");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.e("EncodeDecode", "surfaceCreated Nullpointer");
                e.printStackTrace();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mPreviewRunning) {
                mCamera.stopPreview();
                Log.e("EncodeDecode", "preview stopped");
            }
//            try
//            {
            if (mCamera == null) {
                mCamera = Camera.open(findFrontFacingCamera());
                mCamera.setDisplayOrientation(90);
            }

            Camera.Parameters p = mCamera.getParameters();
            if (ENCODING.equalsIgnoreCase("h264"))
                p.setPreviewSize(VideoWidthHD, VideoHeightHD);
            else if (ENCODING.equalsIgnoreCase("h263"))
                p.setPreviewSize(352, 288);

            p.setPreviewFormat(ImageFormat.YV12);
            mCamera.setParameters(p);
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.unlock();
            try {
                mCamera.reconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Log.d("EncodeDecode", "onPreviewFrame, calling encode function");
                    encode(data);
                    encode2(data);
//                    try {
//                        encodeDecodeTest.testEncodeDecodeVideoFromBufferToBufferQCIF();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            });
            Log.d("EncodeDecode", "previewCallBack set");
            mCamera.startPreview();
            mPreviewRunning = true;
//            }
//            catch (Exception e)
//            {
//                Log.e("EncodeDecode","surface changed:set preview display failed");
//                e.printStackTrace();
//            }

        }

        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }


//    @Override
//    public void surfaceCreated(SurfaceHolder holder)
//    {
//        Log.d("EncodeDecode", "mainActivity surfaceCreated");
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
//    {
//        Log.d("EncodeDecode", "mainActivity surfaceChanged.");
//        if (mPlayer == null)
//        {
//            mPlayer = new PlayerThread(holder.getSurface());
//            mPlayer.start();
//            Log.d("EncodeDecode", "PlayerThread started");
//        }
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder)
//    {
//        if (mPlayer != null)
//        {
//            mPlayer.interrupt();
//        }
//    }

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

    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the back facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

//    private void configCodec() {
////			Log.d(TAG, "VideoOutputThread.configCodec");
//        try {
//            while (mPeerSurfaceTexture == null) Thread.sleep(1);
//        } catch (InterruptedException ex) {
//            return;
//        }
//        mSurface = new Surface(mPeerSurfaceTexture);
//        MediaFormat format = MediaFormat.createVideoFormat(VideoCodec, VideoWidthHD, VideoHeightHD);
//        if (sCameraOrientation == 90)
//            format.setInteger(MediaFormat.KEY_ROTATION, 180);
//        mVideoDecoder.configure(format, mSurface, null, 0);
//        mVideoDecoder.start();
//
//        final Matrix matrix = new Matrix();
//        float sx = VideoWidthHD / (float) mPeerView.getWidth();
//        float sy = VideoHeightHD / (float) mPeerView.getHeight();
//        if (VideoWidthHD > VideoHeightHD == mPeerView.getWidth() > mPeerView.getHeight()) {
//            float max = Math.max(sx, sy);
//            matrix.setScale(sx / max, sy / max, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
//        } else {
//            float max = Math.max(VideoWidthHD / (float) mPeerView.getHeight(),
//                    VideoHeightHD / (float) mPeerView.getWidth());
//            matrix.setScale(sx / max, sy / max, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
//            matrix.postRotate(270.0f, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
//        }
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mPeerView.setTransform(matrix);
//            }
//        });
//    }

    private void updateTextureViewSize(SurfaceView sv, int viewWidth, int viewHeight) {
        int pivotPointX = viewWidth / 2;
        int pivotPointY = viewHeight / 2;

        Matrix matrix = new Matrix();

        if (false) {
            matrix.preRotate(0);
            matrix.setScale(1.0f, 1.0f, pivotPointX, pivotPointY);
//            sv..setTransform(matrix);
            sv.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
        } else {
            matrix.preRotate(0);
            matrix.setScale(1.0f, 1.0f, pivotPointX, pivotPointY);
            sv.setRotation(90);
            sv.setTranslationX(-viewWidth / 2);
            sv.setTranslationY(-viewHeight / 2);
            sv.setLayoutParams(new FrameLayout.LayoutParams(viewWidth * 2, viewHeight * 2));
        }

    }
}
