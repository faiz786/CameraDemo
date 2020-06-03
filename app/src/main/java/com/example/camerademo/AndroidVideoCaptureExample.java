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
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
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
	private MediaRecorder mediaRecorder;
	private Button capture, switchCamera;
	private Context myContext;
	private LinearLayout cameraPreview;
	private boolean cameraFront = false;
	Activity activity = AndroidVideoCaptureExample.this;
	static final int Factor = 1;

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
	};
	VideoEncoderCore videoEncoderCore,videoEncoderCore2;

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
			mCamera.setPreviewCallback(new Camera.PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					encode(data);
					encodeWithDifferentConfigurations(data);
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

		{
			try {
				videoEncoderCore = new VideoEncoderCore(VideoWidthsend,VideoHeightsend,400000/Factor,null);
				videoEncoderCore2 = new VideoEncoderCore(VideoWidthsend,VideoHeightsend,256000/Factor);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("exception while creating video encoder"+e.getMessage());
			}
		}
		cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);

		mPreview = new CameraPreview(myContext, mCamera);
		cameraPreview.addView(mPreview);

		capture = (Button) findViewById(R.id.button_capture);
		capture.setOnClickListener(captrureListener);

		switchCamera = (Button) findViewById(R.id.button_ChangeCamera);
		switchCamera.setOnClickListener(switchCameraListener);
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
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
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
			}
		}
		return true;
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
            mMediaCodec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {

                }

                @Override
                public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

                }
            });
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
            mMediaCodec2.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {

                }

                @Override
                public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

                }
            });
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
                null,
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec2.start();
    }
}