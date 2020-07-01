package com.example.camerademo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class AndroidVideoCaptureExample extends Activity {
	private Camera mCamera;
	private CameraPreview mPreview;
	TextureView textureView1,textureView2;
	Surface surface1,surface2;
	SurfaceView surfaceView1,surfaceView2;
	private byte[] sps;
	private byte[] pps;
	TextureView mPeerView;
	SurfaceTexture mPeerSurfaceTexture;
	private MediaRecorder mediaRecorder;
	private Button capture, switchCamera;
	private Context myContext;
	private LinearLayout cameraPreview;
	private boolean cameraFront = false;
	Activity activity = AndroidVideoCaptureExample.this;
	static final int Factor = 1;
    // video output dimension
    static final int OUTPUT_WIDTH = 640;
    static final int OUTPUT_HEIGHT = 480;
	static final int VideoWidthHD = 640;
	static final int VideoHeightHD = 480;
	static final int VideoWidthLD = 320;
	static final int VideoHeightLD = 240;
    VideoEncoder mEncoder;
    VideoDecoder mDecoder;

	MediaCodec.BufferInfo mBufferInfo;

	MediaCodec mMediaCodec,mMediaCodec2;

	public int sCameraOrientation = -1;

	public final String VideoCodec = MediaFormat.MIMETYPE_VIDEO_AVC;
	public final int VideoWidthsend = 640 / Factor; // 640
	public final int VideoHeightsend = 480 / Factor;
	public final int VideoWidthRecieve = 480 / Factor; // 640
	public final int VideoHeightReceive = 640 / Factor;

	private static final int REQUEST_VIDEO_PERMISSIONS = 1;

	private static final String[] VIDEO_PERMISSIONS = {
			Manifest.permission.CAMERA,
			Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
	};
	VideoEncoderCore videoEncoderCore,videoEncoderCore2;
	VideoOutputThread mVideoOutputThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		requestVideoPermissions();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		myContext = this;
		initialize();
	}

	private int findFrontFacingCamera() {
		int cameraId = -1;
		// Search for the front facing camera
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
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
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
				cameraId = i;
				cameraFront = false;
				break;
			}
		}
		return cameraId;
	}

	public void onResume() {
		super.onResume();
		if (!hasCamera(myContext)) {
			Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
			toast.show();
			finish();
		}
		if (mCamera == null) {
			// if the front facing camera does not exist
			if (findFrontFacingCamera() < 0) {
				Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
				switchCamera.setVisibility(View.GONE);
			}
			mCamera = Camera.open(findFrontFacingCamera());
			mCamera.setDisplayOrientation(90);
			mCamera.setPreviewCallback(new Camera.PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					encode(data);
					encodeWithDifferentConfigurations(data);
//                    mEncoder = new MyEncoder(data);
//                    mDecoder = new VideoDecoder();
//                    mEncoder.start();
//                    mDecoder.start();
				}
			});
//			mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
//				@Override
//				public void onPreviewFrame(byte[] bytes, Camera camera) {
//
//				}
//			});

//			videoEncoderCore2.mEncoder.setCallback(new MediaCodec.Callback() {
//				@Override
//				public void onInputBufferAvailable( MediaCodec codec, int index) {
//				}
//
//				@Override
//				public void onOutputBufferAvailable( MediaCodec codec, int index,
//													 MediaCodec.BufferInfo info) {
//					ByteBuffer buf = codec.getOutputBuffer(index);
//					System.out.println("buffer available from camera 2"+info);
//					//
//					byte[] tmp = new byte[info.size];
//					buf.get(tmp);
////                        // mVideoOutputThread.render( info.flags,tmp);
////                        if (info.flags == 2) {
////
//////                            callInfoBeanObj.setCall_SenderConfig(tmp);
////                        } else {
////
//////                            send_command(tmp, info.flags);
////
////                        }
//					codec.releaseOutputBuffer(index, false);
//				}
//
//				@Override
//				public void onError( MediaCodec codec,  MediaCodec.CodecException e) {
//					android.util.Log.w("video encoder", "videoEncoderCore.mEncoder.onError: " + e);
//				}
//
//				@Override
//				public void onOutputFormatChanged( MediaCodec codec,  MediaFormat format) {
//					// set camera change
//					try {
////						current_Camera = Integer.parseInt(mCamera.getId());
//					}catch (Exception e)
//					{
//						System.out.println("exeption in camera:"+e.getMessage());
//					}
//				}
//			});
//
//			videoEncoderCore2.mEncoder.start();
			mPreview.refreshCamera(mCamera);
		}
	}

	public void initialize() {

		initCodec();
		initSecondCodec();

//		{
//			try {
//				videoEncoderCore = new VideoEncoderCore(VideoWidthHD,VideoHeightHD,400000/Factor,null);
//				videoEncoderCore2 = new VideoEncoderCore(VideoWidthLD,VideoHeightLD,256000/Factor);
//			} catch (IOException e) {
//				e.printStackTrace();
//				System.out.println("exception while creating video encoder"+e.getMessage());
//			}
//		}
		mPeerView = (TextureView) findViewById(R.id.textureView1);
		mPeerView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
			@Override
			public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
//				Log.d(TAG, "onSurfaceTextureAvailable: " + width + "x" + height);
				mPeerSurfaceTexture = texture;
				if (mVideoOutputThread != null) {
					mVideoOutputThread.reset();
				}
			}

			@Override
			public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//				Log.d(TAG, "onSurfaceTextureSizeChanged: " + width + "x" + height);
			}

			@Override
			public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//				Log.d(TAG, "onSurfaceTextureDestroyed");
				if (mVideoOutputThread != null) {
					mVideoOutputThread.pause();
				}
				mPeerSurfaceTexture = null;
				return true;
			}

			@Override
			public void onSurfaceTextureUpdated(SurfaceTexture surface) {
			}
		});
		setupRender();
		cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);

//	    surfaceView1 = findViewById(R.id.textureView1);

	    surfaceView2 = findViewById(R.id.textureView2);

		mPreview = new CameraPreview(myContext, mCamera);
		cameraPreview.addView(mPreview);

//        SurfaceTexture texture1 = textureView1.getSurfaceTexture();
//
//        SurfaceTexture texture2 = textureView2.getSurfaceTexture();
//
//        surface1 = new Surface(texture1);
//
//        surface2 = new Surface(texture2);

		capture = (Button) findViewById(R.id.button_capture);
		capture.setOnClickListener(captrureListener);

		switchCamera = (Button) findViewById(R.id.button_ChangeCamera);
		switchCamera.setOnClickListener(switchCameraListener);
	}

	void setupRender() {
		mVideoOutputThread = new VideoOutputThread();
		mVideoOutputThread.open();
	}

	OnClickListener switchCameraListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// get the number of cameras
			if (!recording) {
				int camerasNumber = Camera.getNumberOfCameras();
				if (camerasNumber > 1) {
					// release the old camera instance
					// switch camera, from the front and the back and vice versa

					releaseCamera();
					chooseCamera();
				} else {
					Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
					toast.show();
				}
			}
		}
	};

	public void chooseCamera() {
		// if the camera preview is the front
		if (cameraFront) {
			int cameraId = findBackFacingCamera();
			if (cameraId >= 0) {
				// open the backFacingCamera
				// set a picture callback
				// refresh the preview

				mCamera = Camera.open(cameraId);
				// mPicture = getPictureCallback();
				mPreview.refreshCamera(mCamera);
			}
		} else {
			int cameraId = findFrontFacingCamera();
			if (cameraId >= 0) {
				// open the backFacingCamera
				// set a picture callback
				// refresh the preview

				mCamera = Camera.open(cameraId);
				// mPicture = getPictureCallback();
				mPreview.refreshCamera(mCamera);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// when on Pause, release camera in order to be used from other
		// applications
		releaseCamera();
	}

	private boolean hasCamera(Context context) {
		// check if the device has camera
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
			return true;
		} else {
			return false;
		}
	}

	boolean recording = false;
	OnClickListener captrureListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (recording) {
				// stop recording and release camera
				mediaRecorder.stop(); // stop the recording
				releaseMediaRecorder(); // release the MediaRecorder object
				Toast.makeText(AndroidVideoCaptureExample.this, "Video captured!", Toast.LENGTH_LONG).show();
				recording = false;
			} else {
				if (!prepareMediaRecorder()) {
					Toast.makeText(AndroidVideoCaptureExample.this, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
					finish();
				}
				// work on UiThread for better performance
				runOnUiThread(new Runnable() {
					public void run() {
						// If there are stories, add them to the table

						try {
							mediaRecorder.start();
						} catch (final Exception ex) {
							// Log.i("---","Exception in thread");
						}
					}
				});

				recording = true;
			}
		}
	};

	private void releaseMediaRecorder() {
		if (mediaRecorder != null) {
			mediaRecorder.reset(); // clear recorder configuration
			mediaRecorder.release(); // release the recorder object
			mediaRecorder = null;
			mCamera.lock(); // lock camera for later use
		}
	}

	private boolean prepareMediaRecorder() {

		mediaRecorder = new MediaRecorder();

		mCamera.unlock();
		mediaRecorder.setCamera(mCamera);

		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));

		mediaRecorder.setOutputFile("/sdcard/myvideo.mp4");
		mediaRecorder.setMaxDuration(600000); // Set max duration 60 sec.
		mediaRecorder.setMaxFileSize(50000000); // Set max file size 50M

		try {
			mediaRecorder.prepare();
		} catch (IllegalStateException e) {
		    System.out.println("exception in media:"+e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
            System.out.println("exception in media:"+e.getMessage());
            releaseMediaRecorder();
			return false;
		}
		return true;

	}

	private void releaseCamera() {
		// stop and release camera
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
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
			if (AndroidVideoCaptureExample.this.shouldShowRequestPermissionRationale( permission)) {
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
//			new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
            ActivityCompat.requestPermissions(activity, VIDEO_PERMISSIONS,
									REQUEST_VIDEO_PERMISSIONS);
		} else {
			AndroidVideoCaptureExample.this.requestPermissions( VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
										   @NonNull int[] grantResults) {
		Log.d("TAG", "onRequestPermissionsResult");
		if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
			if (grantResults.length == VIDEO_PERMISSIONS.length) {
				for (int result : grantResults) {
					if (result != PackageManager.PERMISSION_GRANTED) {
//						ErrorDialog.newInstance(getString(R.string.permission_request))
//								.show(getChildFragmentManager(), FRAGMENT_DIALOG);
						break;
					}
				}
			} else {
//				ErrorDialog.newInstance(getString(R.string.permission_request))
//						.show(getChildFragmentManager(), FRAGMENT_DIALOG);
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	private boolean hasPermissionsGranted(String[] permissions) {
		for (String permission : permissions) {
			if (ActivityCompat.checkSelfPermission(AndroidVideoCaptureExample.this, permission)
					!= PackageManager.PERMISSION_GRANTED) {
				return false;

//                ActivityCompat.requestPermissions(activity, VIDEO_PERMISSIONS,
//									REQUEST_VIDEO_PERMISSIONS);
			}
		}
		return true;
	}

//	public static class ErrorDialog extends DialogFragment {
//
//		private static final String ARG_MESSAGE = "message";
//
//		public static ErrorDialog newInstance(String message) {
//			ErrorDialog dialog = new ErrorDialog();
//			Bundle args = new Bundle();
//			args.putString(ARG_MESSAGE, message);
//			dialog.setArguments(args);
//			return dialog;
//		}
//
//		@Override
//		public Dialog onCreateDialog(Bundle savedInstanceState) {
//			final Activity activity = getActivity();
//			return new AlertDialog.Builder(activity)
//					.setMessage(getArguments().getString(ARG_MESSAGE))
//					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialogInterface, int i) {
//							activity.finish();
//						}
//					})
//					.create();
//		}
//
//	}

//	public static class ConfirmationDialog extends DialogFragment {
//
//		@Override
//		public Dialog onCreateDialog(Bundle savedInstanceState) {
//			final Fragment parent = getParentFragment();
//			return new AlertDialog.Builder(getActivity())
//					.setMessage(R.string.permission_request)
//					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							ActivityCompat.requestPermissions(activity, VIDEO_PERMISSIONS,
//									REQUEST_VIDEO_PERMISSIONS);
//						}
//					})
//					.setNegativeButton(android.R.string.cancel,
//							new DialogInterface.OnClickListener() {
//								@Override
//								public void onClick(DialogInterface dialog, int which) {
//									parent.getActivity().finish();
//								}
//							})
//					.create();
//		}
//
//	}

	private void initCodec() {
		try {
			mBufferInfo = new MediaCodec.BufferInfo();
			mMediaCodec = MediaCodec.createEncoderByType("video/avc");
		} catch (IOException e) {
			e.printStackTrace();
		}
		MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc",
				1920,
				1080);
		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
		mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
				MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
		mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
		mMediaCodec.configure(mediaFormat,
				null,
				null,
				MediaCodec.CONFIGURE_FLAG_ENCODE);
		mMediaCodec.start();
	}

    private void initSecondCodec() {
        try {
            mBufferInfo = new MediaCodec.BufferInfo();
            mMediaCodec2 = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc",
                1920,
                1080);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 256000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 24);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        mMediaCodec2.configure(mediaFormat,
                null,
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec2.start();
    }


	private synchronized void encode(byte[] data) {
		try {
			ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
			ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
			int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(data);
				mMediaCodec.queueInputBuffer(inputBufferIndex, 0, data.length, 0, 0);
			}

			MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
			int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
			while (outputBufferIndex >= 0) {
				ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
				byte[] outData = new byte[bufferInfo.size];
				outputBuffer.get(outData);
				if (sps != null && pps != null) {
					ByteBuffer frameBuffer = ByteBuffer.wrap(outData);
					frameBuffer.putInt(bufferInfo.size - 4);
					//frameListener.frameReceived(outData, 0, outData.length);
				} else {
					ByteBuffer spsPpsBuffer = ByteBuffer.wrap(outData);
					if (spsPpsBuffer.getInt() == 0x00000001) {
						System.out.println("parsing sps/pps");
					} else {
						System.out.println("something is amiss?");
					}
					int ppsIndex = 0;
					while (!(spsPpsBuffer.get() == 0x00 && spsPpsBuffer.get() == 0x00 && spsPpsBuffer.get() == 0x00 && spsPpsBuffer.get() == 0x01)) {

					}
					ppsIndex = spsPpsBuffer.position();
					sps = new byte[ppsIndex - 8];
					System.arraycopy(outData, 4, sps, 0, sps.length);
					pps = new byte[outData.length - ppsIndex];
					System.arraycopy(outData, ppsIndex, pps, 0, pps.length);
					//if (null != parameterSetsListener) {
					//parameterSetsListener.avcParametersSetsEstablished(sps, pps);
					//}
				}
				mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
				outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private synchronized void encode1(byte[] data) {
		ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();// here changes
		ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();

		int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
		if (inputBufferIndex >= 0) {
			ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
			inputBuffer.clear();
			inputBuffer.put(data);
			mMediaCodec.queueInputBuffer(inputBufferIndex, 0, data.length, 0, 0);
		} else {
			return;
		}

		int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 10000);
		do {
			if (outputBufferIndex >= 0) {
				ByteBuffer outBuffer = outputBuffers[outputBufferIndex];
				System.out.println("buffer info-->" + mBufferInfo.offset + "--"
						+ mBufferInfo.size + "--" + mBufferInfo.flags + "--"
						+ mBufferInfo.presentationTimeUs);
				byte[] outData = new byte[mBufferInfo.size];
				outBuffer.get(outData);
				if (mBufferInfo.offset != 0) {
					byte[] offsettedData;
					offsettedData = Arrays.copyOfRange(outData, mBufferInfo.offset, outData.length-1);
//                    pushFrame(offsettedData, offsettedData.length);
				} else {
//                    pushFrame(outData, outData.length);
				}
				mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
				outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo,
						0);

			} else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
				outputBuffers = mMediaCodec.getOutputBuffers();
			} else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				MediaFormat format = mMediaCodec.getOutputFormat();
			}
		} while (outputBufferIndex >= 0);
	}

    private synchronized void encodeWithDifferentConfigurations(byte[] data) {
        ByteBuffer[] inputBuffers = mMediaCodec2.getInputBuffers();// here changes
        ByteBuffer[] outputBuffers = mMediaCodec2.getOutputBuffers();

        int inputBufferIndex = mMediaCodec2.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(data);
            mMediaCodec2.queueInputBuffer(inputBufferIndex, 0, data.length, 0, 0);
        } else {
            return;
        }

        int outputBufferIndex = mMediaCodec2.dequeueOutputBuffer(mBufferInfo, 10000);
        do {
            if (outputBufferIndex >= 0) {
                ByteBuffer outBuffer = outputBuffers[outputBufferIndex];
                System.out.println("buffer info2-->" + mBufferInfo.offset + "--"
                        + mBufferInfo.size + "--" + mBufferInfo.flags + "--"
                        + mBufferInfo.presentationTimeUs);
                byte[] outData = new byte[mBufferInfo.size];
                outBuffer.get(outData);
                if (mBufferInfo.offset != 0) {
                    byte[] offsettedData;
                    offsettedData = Arrays.copyOfRange(outData, mBufferInfo.offset, outData.length-1);
//                    pushFrame(offsettedData, offsettedData.length);
                } else {
//                    pushFrame(outData, outData.length);
                }
                mMediaCodec2.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mMediaCodec2.dequeueOutputBuffer(mBufferInfo,
                        0);

            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mMediaCodec2.getOutputBuffers();
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat format = mMediaCodec2.getOutputFormat();
            }
        } while (outputBufferIndex >= 0);
    }

    private void decode(byte[] data)
    {
        try {
            mBufferInfo = new MediaCodec.BufferInfo();
            mMediaCodec2 = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc",
                1920,
                1080);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 256000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 24);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        mMediaCodec2.configure(mediaFormat,
                surfaceView2.getHolder().getSurface(),
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec2.start();
    }

    class MyEncoder extends VideoEncoder {

        SurfaceRenderer mRenderer;
        byte[] mBuffer = new byte[0];

        public MyEncoder(byte[] data) {
            super(OUTPUT_WIDTH, OUTPUT_HEIGHT);
        }

        // Both of onSurfaceCreated and onSurfaceDestroyed are called from codec's thread,
        // non-UI thread

        @Override
        protected void onSurfaceCreated(Surface surface) {
            // surface is created and codec is ready to accept input (Canvas)
//            mRenderer = new RenderActivity.MyRenderer(surface);
//            mRenderer.start();
        }

        @Override
        protected void onSurfaceDestroyed(Surface surface) {
            // need to make sure to block this thread to fully complete drawing cycle
            // otherwise unpredictable exceptions will be thrown (aka IllegalStateException)
//            mRenderer.stopAndWait();
//            mRenderer = null;
        }

        @Override
        protected void onEncodedSample(MediaCodec.BufferInfo info, ByteBuffer data) {
            // Here we could have just used ByteBuffer, but in real life case we might need to
            // send sample over network, etc. This requires byte[]
            System.out.println("buffer info4-->" + mBufferInfo.offset + "--"
                    + mBufferInfo.size + "--" + mBufferInfo.flags + "--"
                    + mBufferInfo.presentationTimeUs);
            if (mBuffer.length < info.size) {
                mBuffer = new byte[info.size];
            }
            data.position(info.offset);
            data.limit(info.offset + info.size);
            data.get(mBuffer, 0, info.size);

            if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                // this is the first and only config sample, which contains information about codec
                // like H.264, that let's configure the decoder
                mDecoder.configure(surfaceView1.getHolder().getSurface(),
                        OUTPUT_WIDTH,
                        OUTPUT_HEIGHT,
                        mBuffer,
                        0,
                        info.size);
            } else {
                // pass byte[] to decoder's queue to render asap
                mDecoder.decodeSample(mBuffer,
                        0,
                        info.size,
                        info.presentationTimeUs,
                        info.flags);
            }
        }
    }

	class VideoOutputThread extends HandlerThread {
		Handler mHandler;
		boolean mRunning = false;
		MediaCodec mVideoDecoder;
		Surface mSurface;
		byte[] mCodecConfig;

		public VideoOutputThread() {
			super("Video Output");
			super.start();
			mHandler = new Handler(getLooper());
		}

		public void open() {
//			Log.d(TAG, "VideoOutputThread.open");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					openVideoOutput();
				}
			});
			mRunning = true;
		}

		public void close() {
//			Log.d(TAG, "VideoOutputThread.close");
			if (mRunning) {
				mRunning = false;
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (mSurface != null) {
							mSurface.release();
							mSurface = null;
						}
						if (mVideoDecoder != null) {
							mVideoDecoder.release();
							mVideoDecoder = null;
						}
						quit();
					}
				});
			}
		}

		public synchronized void pause() {
			mIsKeyed = false;
			mSurface = null;
		}

		public void reset() {
//			Log.d(TAG, "VideoOutputThread.reset");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (mVideoDecoder != null) {
//						Log.d(TAG, "+VideoOutputThread.reset");
						mVideoDecoder.release();
						mVideoDecoder = null;
						openVideoOutput();
//						Log.d(TAG, "-VideoOutputThread.reset");
					}
				}
			});
		}

		boolean mIsKeyed = false;

		public void render(final int flags, final byte[] data) {
			if ((flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
				mCodecConfig = data;
				return;
			}
			if (mRunning)
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						renderInternal(flags, data);
					}
				});
		}

		private void configCodec() {
//			Log.d(TAG, "VideoOutputThread.configCodec");
			try {
				while (mPeerSurfaceTexture == null) Thread.sleep(1);
			} catch (InterruptedException ex) {
				return;
			}
			mSurface = new Surface(mPeerSurfaceTexture);
			MediaFormat format = MediaFormat.createVideoFormat(VideoCodec, VideoWidthHD, VideoHeightHD);
			if (sCameraOrientation == 90)
				format.setInteger(MediaFormat.KEY_ROTATION, 180);
			mVideoDecoder.configure(format, mSurface, null, 0);
			mVideoDecoder.start();

			final Matrix matrix = new Matrix();
			float sx = VideoWidthHD / (float) mPeerView.getWidth();
			float sy = VideoHeightHD / (float) mPeerView.getHeight();
			if (VideoWidthHD > VideoHeightHD == mPeerView.getWidth() > mPeerView.getHeight()) {
				float max = Math.max(sx, sy);
				matrix.setScale(sx / max, sy / max, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
			} else {
				float max = Math.max(VideoWidthHD / (float) mPeerView.getHeight(),
						VideoHeightHD / (float) mPeerView.getWidth());
				matrix.setScale(sx / max, sy / max, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
				matrix.postRotate(270.0f, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mPeerView.setTransform(matrix);
				}
			});
		}

		private void openVideoOutput() {
			try {
				mVideoDecoder = MediaCodec.createDecoderByType(VideoCodec);
				configCodec();
			} catch (IOException ex) {
//				Log.e(TAG, "Error in openVideoOutput: " + Log.getStackTraceString(ex));
			}
		}

		private synchronized void renderInternal(int flags, byte[] data) {
			if (mSurface == null) return;
			if (mVideoDecoder != null) {
				if (!mIsKeyed) {
					if ((flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0 && mCodecConfig != null) {
						mIsKeyed = true;
						int idx = mVideoDecoder.dequeueInputBuffer(-1);
						ByteBuffer buf = mVideoDecoder.getInputBuffer(idx);
						buf.clear();
						buf.put(mCodecConfig);
						mVideoDecoder.queueInputBuffer(idx, 0, mCodecConfig.length, 0, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
					} else return;
				}

				int idx = mVideoDecoder.dequeueInputBuffer(-1);
				ByteBuffer buf = mVideoDecoder.getInputBuffer(idx);
				buf.clear();
				buf.put(data);
				mVideoDecoder.queueInputBuffer(idx, 0, data.length, 0, 0);

				MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
				idx = mVideoDecoder.dequeueOutputBuffer(info, 10000);
				if (idx >= 0) {
					mVideoDecoder.releaseOutputBuffer(idx, mSurface != null);
				} else {
//					Log.d(TAG, "decoding fail: " + idx);
				}
			}
		}
	}
}