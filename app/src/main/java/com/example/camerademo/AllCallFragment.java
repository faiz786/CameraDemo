//package com.example.camerademo;
//
//
//import static com.general.android.messagingapp.six.beans.CallInfoBean.mProximityWakeLock;
//
//import java.io.File;
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import org.jetbrains.annotations.NotNull;
//
//import com.general.android.messagingapp.six.A;
//import com.general.android.messagingapp.six.BaseFragment;
//import com.general.android.messagingapp.six.R;
//import com.general.android.messagingapp.six.beans.AppDetailsBean;
//import com.general.android.messagingapp.six.beans.CallInfoBean;
//import com.general.android.messagingapp.six.beans.ClientInfo;
//import com.general.android.messagingapp.six.beans.Commands;
//import com.general.android.messagingapp.six.beans.ConfigSettings;
//import com.general.android.messagingapp.six.beans.GlobalObjectBean;
//import com.general.android.messagingapp.six.communication.MAppCommunicationService;
//import com.general.android.messagingapp.six.custom.elasticprogressbar.VectorCompat.ResourcesCompat;
//import com.general.android.messagingapp.six.interfaces.ICallTimerListner;
//import com.general.android.messagingapp.six.interfaces.IOnCallResponseListner;
//import com.general.android.messagingapp.six.interfaces.IOnResponseListner;
//import com.general.android.messagingapp.six.interfaces.IOnVideoCallResponseListner;
//import com.general.android.messagingapp.six.interfaces.IPermissionObserver;
//import com.general.android.messagingapp.six.processes.CallEngine;
//import com.general.android.messagingapp.six.processes.FileOperationUtil;
//import com.general.android.messagingapp.six.processes.PermissionProcessEngine;
//import com.general.android.messagingapp.six.utilities.AndroidUtilities;
//import com.general.android.messagingapp.six.utilities.ArrayProcessUtility;
//import com.general.android.messagingapp.six.utilities.ConstantsUtil;
//import com.general.android.messagingapp.six.utilities.ContactProcessUtil;
//import com.general.android.messagingapp.six.utilities.DateTimeProcessorUtil;
//import com.general.android.messagingapp.six.utilities.Dialogs;
//import com.general.android.messagingapp.six.utilities.Log;
//import com.general.android.messagingapp.six.utilities.ProcessUtil;
//import com.general.android.messagingapp.six.utilities.SingletonUtil;
//import com.general.android.messagingapp.six.utilities.Util;
//import com.general.android.messagingapp.six.utilities.timer.Timer;
//import com.general.android.messagingapp.six.utilities.timer.TimerListener;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.squareup.picasso.Callback;
//import com.squareup.picasso.MemoryPolicy;
//import com.squareup.picasso.Picasso;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.ActivityInfo;
//import android.content.res.Configuration;
//import android.graphics.Matrix;
//import android.graphics.SurfaceTexture;
//import android.graphics.Typeface;
//import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraCaptureSession;
//import android.hardware.camera2.CameraCharacteristics;
//import android.hardware.camera2.CameraDevice;
//import android.hardware.camera2.CameraManager;
//import android.hardware.camera2.CameraMetadata;
//import android.hardware.camera2.CaptureRequest;
//import android.media.MediaCodec;
//import android.media.MediaCodecInfo;
//import android.media.MediaFormat;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Message;
//import android.os.PowerManager;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.Surface;
//import android.view.TextureView;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.widget.FrameLayout;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.constraintlayout.widget.ConstraintLayout;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//import butterknife.OnTouch;
//
//public class AllCallFragment extends BaseFragment
//        implements
//        IOnResponseListner,
//        IOnVideoCallResponseListner,
//        ICallTimerListner,
//        IOnCallResponseListner,
//        IPermissionObserver,
//        TimerListener {
//
//    static final int STATUS_NONE = 0;
//    static final int STATUS_OPENING = 1;
//    static final int STATUS_RUNNING = 2;
//    static final int STATUS_CLOSING = 3;
//    static final int STATUS_CLOSED = 4;
//    static final int Factor = 1;
//    private final static String TAG = "All Call Fragment";
//    private static int packet_id = 0;
//    public final int VideoWidthRecieve = 480 / Factor; // 640
//    public final int VideoHeightReceive = 640 / Factor;
//    public final int VideoWidthsend = 640 / Factor; // 640
//    public final int VideoHeightsend = 480 / Factor;
//    public final String VideoCodec = MediaFormat.MIMETYPE_VIDEO_AVC;
//    final Matrix matrix = new Matrix();
//    public boolean isCallOngoing = false;
//    public String sSelectedCamera;
//    public int sCameraOrientation = -1;
//    public VideoInputThread mVideoInputThread;
//    public VideoOutputThread mVideoOutputThread;
//    public Timer autoButtonHideTimer;
//    @BindView(R.id.video_remoteTextureVu)
//    TextureView mPeerView;
//    @BindView(R.id.video_selfTextureVu)
//    TextureView videoMyself;
//    SurfaceTexture mPeerSurfaceTexture, mPeerSurfaceTextureMyself;
//    @BindView(R.id.toggleButton)
//    ImageView toggle;
//    @BindView(R.id.toggleAudioButton)
//    ImageView toggleAudioButton;
//    @BindView(R.id.video_call_stop_fab)
//    ImageView video_call_stop_fab;
//    @BindView(R.id.video_call_voice_mute_fab)
//    ImageView video_call_voice_mute_fab;
//    @BindView(R.id.preview_myself_toggle_fab)
//    ImageView preview_myself_toggle_fab;
//    @BindView(R.id.show_and_hide_ll_fab)
//    ImageView show_and_hide_ll_fab;
//    @BindView(R.id.call_duration_video_call_txt)
//    TextView call_duration_video_call_txt;
//    @BindView(R.id.call_remote_Name_txt)
//    TextView call_remote_Name_txt;
//    @BindView(R.id.video_Root_RL)
//    RelativeLayout video_call_RL;
//    @BindView(R.id.video_actionBar_CL)
//    ConstraintLayout video_actionBar_CL;
//    @BindView(R.id.video_bottomBar_CL)
//    ConstraintLayout video_bottomBar_CL;
//
//    @BindView(R.id.call_reciver_name_txt)
//    TextView call_reciver_name_txt;
//    @BindView(R.id.call_receiver_photo_Imgvu)
//    ImageView call_receiver_photo_Imgvu;
//    @BindView(R.id.call_end_fab)
//    FloatingActionButton call_end_fab;
//    @BindView(R.id.call_audio_Button)
//    ImageButton call_audio_Button;
//    @BindView(R.id.call_mute_Button)
//    ImageButton call_mute_Button;
//    @BindView(R.id.initiate_Video_Call)
//    ImageButton initiate_Video_Call;
//    @BindView(R.id.call_status_txt)
//    TextView call_status_txt;
//    @BindView(R.id.call_duration_voice_call_txt)
//    TextView call_duration_voice_call_txt;
//    @BindView(R.id.call_reciver_number_txt)
//    TextView call_reciver_number_txt;
//    @BindView(R.id.voice_call_only_ll)
//    LinearLayout voice_call_only_LL;
//
//    int switching_camera_int = 0;
//    private int default_camera = 0;
//    private int current_Camera = 0;
//    private CallInfoBean callInfoBeanObj;
//    private MAppCommunicationService mAppCommunicationServiceObj;
//    private String currentContactNumber, currentContactName;
//    private String callServiceType;
//    private String callViewMode;
//    private boolean isConfigDone = false;
//    private boolean isNormalCall;
//
//    private static View call_View = null;// call Minimized
//    int current_os;
//    public boolean isbackpressed = false;// call Minimized
//
//    public int call_maximized = 1;// call Minimized
//    public Handler handlerShowCallingView = new Handler() {
//
//        @Override
//        public void handleMessage(Message msg) {
//            Bundle b = msg.getData();
//            String command = b.getString("Command");
//
//            if (command != null) {
//                if (command.equalsIgnoreCase(Commands.CALL)) {
//
//                    if (generalInfoBeanObj.isRegistered()) {
//
//                        boolean isVideoWithRTPVoiceCall = b.getBoolean("IsVideoWithRTPVoiceCall");
//                        String callViewMode = b.getString("CallViewMode");// not
//                        // using
//                        // now
//                        SingletonUtil.get().getCallEngine().setCallFrom(true);
//                        if (isVideoWithRTPVoiceCall) {
//
//                            isNormalCall = true;
//                            callServiceType = ConstantsUtil.TCP_VIDEO_RTP_CALL_RELAY;
//                        } else {
//
//                            isNormalCall = true;
//                            callServiceType = ConstantsUtil.VIDEO_CALL_RELAY;
//                        }
//                        showCallLayout(ConstantsUtil.VIEW_MODE_VOICE_ONLY_CALL_APP_TO_APP);
//                    } else {
//
//                        Dialogs.showAlertNoConnection(mainActivity);
//                    }
//                }
//            }
//        }
//    };
//    private boolean isBottomLLShow, isPreviewMyselfShow;
//    private long time = 0;
//    private float degrees;
//    private int _xDelta;
//    private int _yDelta;
//    private int currentSenderOs, currentCameraSwitch;
//    private DateTimeProcessorUtil utils;
//    private Handler mHandler;
//    private final Runnable mRunnable = new Runnable() {
//        public void run() {
//            if (SingletonUtil.get().isCallOngoing) {// call Minimized
//
//                String timeDisplay = getTime();
//                if (call_duration_voice_call_txt != null) {
//                    call_duration_voice_call_txt.setText(timeDisplay);
//                }
//                if (call_duration_video_call_txt != null) {
//                    call_duration_video_call_txt.setText(timeDisplay);
//                }
//                mainActivity.call_back_view.setText(" Tap return to call   " + timeDisplay);// call Minimized
//                mainActivity.call_back_view.setBackgroundColor(mainActivity.getResources().getColor(R.color.color_2));
//                mainActivity.call_back_view.setTextColor(mainActivity.getResources().getColor(R.color.white));
//
//                mHandler.postDelayed(mRunnable, 1000);
//            } else {
//
//            }
//        }
//    };
//    public Handler handlerStartClock = new Handler() {
//
//        @Override
//        public void handleMessage(Message msg) {
//
//            if (!SingletonUtil.get().isCallOngoing) {// call Minimized
//
//                utils = new DateTimeProcessorUtil();
//                time = 0;
//                mHandler = new Handler();
//                SingletonUtil.get().isCallOngoing = true;// call Minimized
//                mHandler.post(mRunnable);
//            }
//        }
//    };
//    private Handler showChatView = new Handler(new Handler.Callback() {
//
//        @Override
//        public boolean handleMessage(Message msg) {
//
//            mainActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            if (GlobalObjectBean.getGlobalObjectbean().isCallFromNotification()) {// ABDNT
//                ProcessUtil.closeAPPForPhonelocked(mainActivity, true);
//
//            } else {
//                char firstChar = currentContactNumber.charAt(0);
//                if (firstChar == '*') {
//
//                    mainActivity.getViewStack().removeAll(mainActivity.getViewStack());
//                    BaseFragment homeFragment = HomeFragment.getHomeNewInstance();
//                    mainActivity.showNextFragment(homeFragment);
//                    (((HomeFragment) homeFragment).handlerShowCalllogView).sendEmptyMessage(0);
//
//                } else {
//                    if (!SingletonUtil.get().isCallOngoing) {// call Minimized
//
//                        GlobalObjectBean.getGlobalObjectbean().getResponseHandler()
//                                .setVideoCallResponseReceivedListener(null);
//                        GlobalObjectBean.getGlobalObjectbean().getResponseHandler()
//                                .setCallResponseReceivedListener(null);
//
//                    }
//                    if (A.isActivityVisible()) {
//                        mainActivity.getViewStack().remove(mainActivity.getViewStack().size() - 1);
//                        if (mainActivity.all_call_fragment_frame.getVisibility() == View.VISIBLE) {
//                            // if (!isbackpressed)
//
//                            mainActivity.getSupportFragmentManager().beginTransaction().remove(mainActivity
//                                    .getSupportFragmentManager().findFragmentById(R.id.loading_fragment_container))
//                                    .commit();
//                            mainActivity.all_call_fragment_frame.setVisibility(View.INVISIBLE);
//                            // mainActivity.getSupportFragmentManager().popBackStackImmediate();
//                        } else {
//                            mainActivity.all_call_fragment_frame.setVisibility(View.INVISIBLE);
//                            mainActivity.call_back_view.setVisibility(View.GONE);
//                        }
//
//                        mainActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                        mainActivity.main_fragment_frame.setVisibility(View.VISIBLE);
//                        BaseFragment fragment = ChatHistoryFragment.getChatHistoryNewInstance(currentContactName,
//                                currentContactNumber, "0");
//                        mainActivity.showNextFragment(fragment);
//                    }
//                }
//
//            }
//
//            return true;
//
//        }
//    });
//    private Handler handlerStopClock = new Handler() {
//
//        @Override
//        public void handleMessage(Message msg) {
//            SingletonUtil.get().isCallOngoing = false;// call Minimized
//        }
//    };
//    private Handler updateCallStatusText = new Handler(new Handler.Callback() {
//
//        @Override
//        public boolean handleMessage(Message messageObj) {
//
//            Bundle b = messageObj.getData();
//            String command = b.getString("Command");
//            String action = b.getString("Action");
//            String status = b.getString("Status");
//
//            call_status_txt.setText(status);
//            call_status_txt.setTypeface(
//                    Typeface.createFromAsset(A.applicationContext.getAssets(), "fonts/Roboto-Medium.ttf" + ""));
//            return true;
//        }
//    });
//    private String call_current_click = "VOICE";
//    private HandlerThread mBackgroundThread;
//    private Handler mBackgroundHandler;
//    // private Semaphore mCameraOpenCloseLock = new Semaphore(1);
//
//    public static AllCallFragment getAllCallNewInstance(String currentMobileNumber, String currentDisplayName,
//                                                        boolean isNormalCall, String callServiceType) {
//
//        Bundle chatHistoryBundle = new Bundle();
//        chatHistoryBundle.putString("currentContactNumber", currentMobileNumber);
//        chatHistoryBundle.putString("currentDisplayName", currentDisplayName);
//        chatHistoryBundle.putBoolean("isNormalCall", isNormalCall);
//        chatHistoryBundle.putString("callServiceType", callServiceType);
//        AllCallFragment fragment = new AllCallFragment();
//        fragment.setArguments(chatHistoryBundle);
//
//        return fragment;
//    }
//
//    public static int getActivityOrientation(@NonNull Activity context) {
//        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        Configuration configuration = context.getResources().getConfiguration();
//
//        final int rotation = windowManager.getDefaultDisplay().getRotation();
//        // final int rotation = getDisplayRotation(context);
//        switch (rotation) {
//            case Surface.ROTATION_0 :
//                if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
//                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
//                else
//                    return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
//            case Surface.ROTATION_90 :
//                if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
//                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
//                else
//                    return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
//            case Surface.ROTATION_180 :
//                if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
//                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
//                else
//                    return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
//            case Surface.ROTATION_270 :
//                if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
//                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
//                else
//                    return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
//            default :
//                android.util.Log.e("Degrees", "Unknown screen orientation. Defaulting to portrait.");
//                if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
//                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
//                else
//                    return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
//        }
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//
//        rootView = inflater.inflate(R.layout.fragment_all_call, container, false);
//        this.callViewMode = ConstantsUtil.VIEW_MODE_VOICE_ONLY_CALL_APP_TO_APP;
//
//        ContactProcessUtil contactProcessUtilObj = new ContactProcessUtil();
//        // mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        this.mAppCommunicationServiceObj = new MAppCommunicationService();
//        ClientInfo.notificationCallAcceptState = ConstantsUtil.PROTOCOL_CAll_CLICK_CONNECT;
//
//        Bundle bundle = this.getArguments();
//        this.currentContactNumber = bundle.getString("currentContactNumber");
//        this.currentContactName = bundle.getString("currentDisplayName");
//        this.isNormalCall = bundle.getBoolean("isNormalCall", true);
//        this.callServiceType = bundle.getString("callServiceType", ConstantsUtil.VIDEO_CALL_RELAY);
//        call_maximized = 1;
//
//        GlobalObjectBean.getGlobalObjectbean().getResponseHandler().setResponseReceivedListener(AllCallFragment.this);
//        GlobalObjectBean.getGlobalObjectbean().getResponseHandler()
//                .setVideoCallResponseReceivedListener(AllCallFragment.this);
//        GlobalObjectBean.getGlobalObjectbean().getResponseHandler()
//                .setCallResponseReceivedListener(AllCallFragment.this);
//
//        A.getGeneralTimerInstance().setCallViewListner(AllCallFragment.this);
//
//        // call Minimized
//        call_View = rootView;
//        Util.hideKeyboard(mainActivity, call_View);
//        // call_View.animate().x(0).y(20).setDuration(0).start();
//        setRootView(rootView);
//        // packet_id = 0;
//        mainActivity.call_back_view.setOnClickListener(new View.OnClickListener() {// call Minimized
//            @Override
//            public void onClick(View v) {
//                mainActivity.call_back_view.setVisibility(View.GONE);
//
//                if (video_call_RL.getVisibility() == View.VISIBLE) {
//
//                    loading_click();
//
//                    // mVideoOutputThread.configUI(current_os);
//                } else {
//
//                    // mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                    mainActivity.all_call_fragment_frame.setVisibility(View.VISIBLE);
//                    mainActivity.main_fragment_frame.setVisibility(View.INVISIBLE);
//                    mainActivity.main_fragment_frame.setFocusable(false);
//                    mainActivity.main_fragment_frame.setClickable(false);
//                    mainActivity.main_fragment_frame.setEnabled(false);
//                }
//                isbackpressed = false;
//            }
//        });
//        // packet_id = 0;
//        return rootView;
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//
//        super.onViewCreated(view, savedInstanceState);
//
//        // if (!SingletonUtil.get().iscallRejected) {
//        AppDetailsBean appDetailsBean = AppDetailsBean.getAppDetailsBean();
//        this.mAppCommunicationServiceObj = new MAppCommunicationService();
//        this.callInfoBeanObj = new CallInfoBean();
//        // mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        initializeResources();
//        call_maximized = 1;
//        Log.e("call", "callViewMode onviewcreated " + callViewMode);
//        showCallLayout(this.callViewMode);
//        initializeResourcesValue();
//        mainActivity.main_fragment_frame.setVisibility(View.INVISIBLE);
//        mainActivity.main_fragment_frame.setFocusable(false);
//        mainActivity.main_fragment_frame.setClickable(false);
//        mainActivity.main_fragment_frame.setEnabled(false);
//
//        // } else {
//        // SingletonUtil.get().iscallRejected = false;
//        // }
//
//        // if (autoButtonHideTimer != null) {
//        // autoButtonHideTimer.halt();
//        // }
//        // autoButtonHideTimer = new Timer(5000, this);
//    }
//
//    @Override
//    protected void initializeResources() {
//
//        ButterKnife.bind(this, rootView);
//
//        CallInfoBean.setMuteMode(false);
//        CallInfoBean.setSpeakerMode(false);
//        this.PlayAudioToSpeaker(false);
//
//    }
//
//    @Override
//    protected void initializeResourcesValue() {
//
//        call_reciver_name_txt.setText(this.currentContactName);
//        call_reciver_name_txt.setTypeface(
//                Typeface.createFromAsset(A.applicationContext.getAssets(), "fonts/Roboto-Medium.ttf" + ""));
//        call_reciver_number_txt.setText(this.currentContactNumber);
//        call_reciver_number_txt.setTypeface(
//                Typeface.createFromAsset(A.applicationContext.getAssets(), "fonts/Roboto-Medium.ttf" + ""));
//
//        this.setupProfilePic();
//
//        call_remote_Name_txt.setText(this.currentContactName);
//        switching_camera_int = 0;
//        default_camera = 0;
//
//        // if
//        // (!this.callViewMode.equals(ConstantsUtil.VIEW_MODE_VOICE_ONLY_CALL_APP_TO_APP))
//        // {
//        mPeerView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//            @Override
//            public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
//                mPeerSurfaceTexture = texture;
//                if (mVideoOutputThread != null) {
//                    mVideoOutputThread.reset();
//                }
//            }
//
//            @Override
//            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//                // configureTransform(width, height);
//            }
//
//            @Override
//            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//                if (mVideoOutputThread != null) {
//                    mVideoOutputThread.pause();
//                }
//                mPeerSurfaceTexture = null;
//                return true;
//            }
//
//            @Override
//            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//            }
//        });
//
//        videoMyself.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//            @Override
//            public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
//                mPeerSurfaceTextureMyself = texture;
//                //
//            }
//
//            @Override
//            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//            }
//
//            @Override
//            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//                mPeerSurfaceTextureMyself = null;
//                return true;
//            }
//
//            @Override
//            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//            }
//        });
//        videoMyself.setOnTouchListener(new OnDragTouchListener(videoMyself));
//
//        // setupRender();
//        // setupCapture();
//        // }
//
//        if (!(SingletonUtil.get().getCallEngine().isCallFrom())) {
//
//            callInfoBeanObj.call_CallStatus = (ConstantsUtil.CALLING_STATE);
//            SingletonUtil.get().getGeneralInfo().setCallStatus(callInfoBeanObj.call_CallStatus);
//            call_status_txt.setText(ConstantsUtil.CALLING_STATE_TEXT);
//            call_status_txt.setTypeface(
//                    Typeface.createFromAsset(A.applicationContext.getAssets(), "fonts/Roboto-Medium.ttf" + ""));
//
//            call_current_click = "VOICE";
//            if (new PermissionProcessEngine(mainActivity, this)
//                    .showPermissionCheck(new String[]{Manifest.permission.RECORD_AUDIO})) {
//
//                this.initializeCall();
//            } else {
//            }
//        } else {
//
//            callInfoBeanObj.call_CallStatus = (ConstantsUtil.ESTABLISHING_STATE);
//            SingletonUtil.get().getGeneralInfo().setCallStatus(callInfoBeanObj.call_CallStatus);
//            call_status_txt.setText(ConstantsUtil.ESTABLISHING_STATE_TEXT);
//            call_status_txt.setTypeface(
//                    Typeface.createFromAsset(A.applicationContext.getAssets(), "fonts/Roboto-Medium.ttf" + ""));
//        }
//    }
//
//    private void initializeCall() {
//        Thread newCallInitThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                if (isNormalCall) {
//                    callInfoBeanObj.call_CallServiceType = (ConstantsUtil.CALL_APP);
//                } else
//                    callInfoBeanObj.call_CallServiceType = (ConstantsUtil.CALL_NORMAL);
//
//                if (isNormalCall && callServiceType.equals(ConstantsUtil.VIDEO_CALL_RELAY)) {
//                    callInfoBeanObj.call_CallServiceType = ConstantsUtil.VIDEO_CALL_RELAY;
//                } else if (isNormalCall && callServiceType.equals(ConstantsUtil.TCP_VIDEO_RTP_CALL_RELAY)) {
//                    callInfoBeanObj.call_CallServiceType = ConstantsUtil.TCP_VIDEO_RTP_CALL_RELAY;
//                }
//
//                SingletonUtil.get().getCallEngine().createAndSendCallInitCmd(currentContactNumber, mainActivity,
//                        callInfoBeanObj, isNormalCall);
//            }
//        });
//        newCallInitThread.start();
//        // SingletonUtil.get().getCallEngine().createAndSendCallInitCmd(this.currentContactNumber,
//        // mainActivity,
//        // callInfoBeanObj, isNormalCall);
//    }
//
//    private void setupProfilePic() {
//
//        String profilePicThumpFilePath = AndroidUtilities.createAttIntPathFolder(ConstantsUtil.THUMPNAIL)
//                + File.separator + AndroidUtilities.createProfilePicThumpName(this.currentContactNumber);
//        String profilePicOrgFilePath = AndroidUtilities.createAttExtPathFolder(ConstantsUtil.USERPROFILEPIC)
//                + File.separator + AndroidUtilities.createProfilePicName(this.currentContactNumber);
//        File profilePicThumpFile = new File(profilePicThumpFilePath);
//        final File profilePicOrgFile = new File(profilePicOrgFilePath);
//        String currentPicturePath = profilePicOrgFilePath;
//
//        boolean isOrginalProfileAvailable = false;
//
//        boolean isProfileThumpAvailable = false;
//        if (profilePicOrgFile.exists()) {
//
//            isOrginalProfileAvailable = true;
//            isProfileThumpAvailable = true;
//
//            final File decFile = new FileOperationUtil().decryptMediaToTempFolder(profilePicOrgFile.getAbsolutePath());
//            call_receiver_photo_Imgvu.setScaleType(ImageView.ScaleType.CENTER);
//            Picasso.get().load(decFile).error(R.drawable.img_no_image).placeholder(R.drawable.img_no_image)
//                    .memoryPolicy(MemoryPolicy.NO_CACHE).into(call_receiver_photo_Imgvu, new Callback() {
//
//                @Override
//                public void onSuccess() {
//
//                    call_receiver_photo_Imgvu.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                    if (decFile != null)
//                        if (decFile.exists())
//                            decFile.delete();
//                }
//
//                @Override
//                public void onError(Exception e) {
//
//                }
//            });
//        }
//
//        if (!isOrginalProfileAvailable) {
//            if (profilePicThumpFile.exists()) {
//
//                isProfileThumpAvailable = true;
//                Picasso.get().load(profilePicThumpFile).error(R.drawable.img_no_image)
//                        .placeholder(R.drawable.img_no_image).memoryPolicy(MemoryPolicy.NO_CACHE)
//                        .into(call_receiver_photo_Imgvu, new Callback() {
//
//                            @Override
//                            public void onSuccess() {
//
//                                call_receiver_photo_Imgvu.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                            }
//
//                            @Override
//                            public void onError(Exception e) {
//
//                            }
//                        });
//            }
//        }
//    }
//
//    private String getTime() {
//
//        callInfoBeanObj = CallInfoBean.getCallInfoBeanObj();
//
//        // if (!(mainActivity.getCurrentView() instanceof AllCallFragment)) {
//        //
//        // isCallOngoing = false;
//        // }
//        time = time + 1000;
//
//        callInfoBeanObj.call_CallDuration = (time + "");
//        String stime = utils.milliSecondsToFullTimer(time);
//        callInfoBeanObj.call_StatusText = (stime);
//        CallInfoBean.setCallInfoBeanObj(callInfoBeanObj);
//        return stime;
//    }
//
//    public void PlayAudioToSpeaker(boolean isSpeaker) {
//
//        CallInfoBean.setSpeakerMode(isSpeaker);
//
//        if (isSpeaker) {
//
//            Util.PlayAudioToSpeaker(mainActivity);
//        } else {
//
//            Util.PlayAudioToReceiver(mainActivity);
//        }
//    }
//
//    void setupRender() {
//
//        mVideoOutputThread = new VideoOutputThread();
//        mVideoOutputThread.open();
//    }
//
//    void setupCapture() {
//
//        if (sSelectedCamera == null)
//            sSelectedCamera = findCamera(CameraCharacteristics.LENS_FACING_FRONT);
//        // if (mVideoInputThread != null)
//        // if (mVideoInputThread.isAlive()) {
//        // mVideoInputThread.close();
//        // mVideoInputThread.interrupt();
//        // mVideoInputThread = null;
//        // }
//        mVideoInputThread = new VideoInputThread();
//        mVideoInputThread.open();
//    }
//
//    private String findCamera(int facing_switch) {
//
//        try {
//            CameraManager cameraManager = (CameraManager) mainActivity.getSystemService(Context.CAMERA_SERVICE);
//            String[] cams = new String[0];
//            cams = cameraManager.getCameraIdList();
//            String selectedCam = null;
//            for (String name : cams) {
//
//                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(name);
//                int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
//                if (facing_switch == CameraCharacteristics.LENS_FACING_BACK
//                        && facing == CameraCharacteristics.LENS_FACING_BACK) {
//
//                    selectedCam = name;
//                    sCameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//                    degrees = 90.0f;
//                    break;
//                } else if (facing_switch == CameraCharacteristics.LENS_FACING_FRONT
//                        && facing == CameraCharacteristics.LENS_FACING_FRONT) {
//
//                    selectedCam = name;
//                    sCameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//                    degrees = 270.0f;
//                    break;
//                } else if (selectedCam == null)
//                    selectedCam = name;
//
//            }
//            return selectedCam;
//        } catch (CameraAccessException | NullPointerException ex) {
//
//            android.util.Log.e("Video Call", "Error in findCamera: " + android.util.Log.getStackTraceString(ex));
//            return null;
//        }
//    }
//
//    private int getDeviceDefaultOrientation() {
//        WindowManager windowManager = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
//        Configuration config = getResources().getConfiguration();
//        int rotation = windowManager.getDefaultDisplay().getRotation();
//        if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
//                && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
//                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)
//                && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
//            return Configuration.ORIENTATION_LANDSCAPE;
//        } else {
//            return Configuration.ORIENTATION_PORTRAIT;
//        }
//    }
//
//    @OnTouch({R.id.video_remoteTextureVu, R.id.video_Root_RL})
//    public boolean onViewTouched(View view, MotionEvent event) {
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN :
//                return true;
//            case MotionEvent.ACTION_UP :
//
//                if (autoButtonHideTimer != null)
//                    autoButtonHideTimer.halt();
//                if (isBottomLLShow) {
//                    isBottomLLShow = false;
//                    mainActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            video_actionBar_CL.setVisibility(View.GONE);
//                            video_bottomBar_CL.setVisibility(View.GONE);
//                        }
//                    });
//                } else {
//                    isBottomLLShow = true;
//                    mainActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            video_actionBar_CL.setVisibility(View.VISIBLE);
//                            video_bottomBar_CL.setVisibility(View.VISIBLE);
//                        }
//                    });
//                    autoButtonHideTimer = new Timer(5000, this);
//                    autoButtonHideTimer.start();
//                }
//                return true;
//        }
//        return true;
//    }
//
//    @OnClick(R.id.show_and_hide_ll_fab)
//    public void onShowAndHideLLClicked() {
//
//        if (isBottomLLShow) {
//            isBottomLLShow = false;
//            video_actionBar_CL.setVisibility(View.GONE);
//            video_bottomBar_CL.setVisibility(View.GONE);
//        } else {
//            isBottomLLShow = true;
//            video_actionBar_CL.setVisibility(View.VISIBLE);
//            video_bottomBar_CL.setVisibility(View.VISIBLE);
//        }
//    }
//
//    @OnClick(R.id.call_end_fab)
//    public void onCallEndClicked() {
//
//        callInfoBeanObj = CallInfoBean.getCallInfoBeanObj();
//        SingletonUtil.get().isCallOngoing = false;// call Minimized
//        mainActivity.all_call_fragment_frame.setVisibility(View.INVISIBLE);// call Minimized
//        Thread callDisconnectThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                int time = 0;
//                while (callInfoBeanObj == null) {
//
//                    callInfoBeanObj = CallInfoBean.getCallInfoBeanObj();
//                    try {
//                        Thread.sleep(200);
//                    } catch (InterruptedException e) {
//                    }
//                    time = time + 200;
//                    if (time >= 5000) {
//                        break;
//                    }
//                }
//                SingletonUtil.get().getCallEngine().createAndSendCallDisconnectCmd(mainActivity, callInfoBeanObj);
//            }
//        });
//        callDisconnectThread.start();
//        mainActivity.notficationLocalHelper.completed(ConstantsUtil.CALL_TIME_BACKGROUND);
//
//        showChatView.sendEmptyMessage(0);
//    }
//
//    @OnClick(R.id.preview_myself_toggle_fab)
//    public void onPreviewMyselfToggleClicked() {
//
//        if (isPreviewMyselfShow) {
//            isPreviewMyselfShow = false;
//            videoMyself.setVisibility(View.INVISIBLE);
//
//            preview_myself_toggle_fab
//                    .setImageDrawable(ResourcesCompat.getDrawable(mainActivity, R.drawable.ic_video_preview));
//        } else {
//            isPreviewMyselfShow = true;
//            videoMyself.setVisibility(View.VISIBLE);
//
//            preview_myself_toggle_fab
//                    .setImageDrawable(ResourcesCompat.getDrawable(mainActivity, R.drawable.ic_video_preview_off));
//        }
//
//        if (autoButtonHideTimer != null)
//            autoButtonHideTimer.halt();
//        autoButtonHideTimer = new Timer(5000, this);
//        autoButtonHideTimer.start();
//    }
//
//    @OnClick({R.id.call_audio_Button, R.id.toggleAudioButton})
//    public void onCallAudioClicked() {
//
//        if (!CallInfoBean.isSpeakerMode()) {
//
//            call_audio_Button
//                    .setImageDrawable(ResourcesCompat.getDrawable(mainActivity, R.drawable.btn_compound_audio));
//            toggleAudioButton
//                    .setImageDrawable(ResourcesCompat.getDrawable(mainActivity, R.drawable.ic_toolbar_speaker_on));
//            CallInfoBean.setSpeakerMode(true);
//            this.PlayAudioToSpeaker(true);
//        } else {
//
//            call_audio_Button
//                    .setImageDrawable(ResourcesCompat.getDrawable(mainActivity, R.drawable.btn_compound_audio_off));
//            toggleAudioButton
//                    .setImageDrawable(ResourcesCompat.getDrawable(mainActivity, R.drawable.ic_toolbar_speaker_off));
//            // toggleAudioButton.setColorFilter(new
//            // PorterDuffColorFilter(Theme.getColor(Theme.key_chat_messagePanelIcons),
//            // PorterDuff.Mode.MULTIPLY));
//            CallInfoBean.setSpeakerMode(false);
//            this.PlayAudioToSpeaker(false);
//        }
//
//        if (autoButtonHideTimer != null)
//            autoButtonHideTimer.halt();
//        autoButtonHideTimer = new Timer(5000, this);
//        autoButtonHideTimer.start();
//    }
//
//    @OnClick({R.id.call_mute_Button, R.id.video_call_voice_mute_fab})
//    public void onCallMuteClicked() {
//
//        if (CallInfoBean.isMuteMode()) {
//
//            call_mute_Button.setImageDrawable(ResourcesCompat.getDrawable(mainActivity, R.drawable.btn_compound_mute));
//            video_call_voice_mute_fab
//                    .setImageDrawable(ResourcesCompat.getDrawable(mainActivity, R.drawable.ic_mic_off_white));
//            // video_call_voice_mute_fab.setColorFilter(new PorterDuffColorFilter(
//            // Theme.getColor(Theme.key_chat_recordedVoicePlayPause),
//            // PorterDuff.Mode.MULTIPLY));
//            CallInfoBean.setMuteMode(false);
//
//            this.createandsendMutecmd(callInfoBeanObj, false);// CALLHOLD
//        } else {
//
//            call_mute_Button
//                    .setImageDrawable(ResourcesCompat.getDrawable(mainActivity, R.drawable.btn_compound_mute_off));
//            video_call_voice_mute_fab
//                    .setImageDrawable(ResourcesCompat.getDrawable(mainActivity, R.drawable.ic_mic_white));
//            // video_call_voice_mute_fab.setColorFilter(new PorterDuffColorFilter(
//            // Theme.getColor(Theme.key_chat_messagePanelIcons), PorterDuff.Mode.MULTIPLY));
//            CallInfoBean.setMuteMode(true);
//
//            this.createandsendMutecmd(callInfoBeanObj, true);// CALLHOLD
//        }
//
//        if (autoButtonHideTimer != null)
//            autoButtonHideTimer.halt();
//        autoButtonHideTimer = new Timer(5000, this);
//        autoButtonHideTimer.start();
//
//    }
//
//    @OnClick(R.id.initiate_Video_Call)
//    public void onInitiateVideoCallClicked() {
//
//        call_current_click = "VIDEO_FIRST";
//        if (new PermissionProcessEngine(mainActivity, this)
//                .showPermissionCheck(new String[]{Manifest.permission.CAMERA})) {
//            // isPermissioncheck=false;
//            this.startVideoCallClicked();
//        }
//    }
//
//    private void startVideoCallClicked() {
//
//        if (SingletonUtil.get().getCallEngine().isVideoCallSessionBegin) {
//            // this.showCallLayout(ConstantsUtil.VIEW_MODE_VIDEO_CALL_BOTH);
//            if (SingletonUtil.get().getGeneralInfo().getCallStatus().equalsIgnoreCase(ConstantsUtil.TALKING_STATE)) {
//                this.createandsendVideoControlCmd(callInfoBeanObj, true);// CALLHOLD
//                this.sendVideoControlInit(ConstantsUtil.VIDEOON, 3);
//                this.initiateVideoCall(ConstantsUtil.VIEW_MODE_VIDEO_CALL_SEND_ONLY, false);
//                toggle.setVisibility(View.VISIBLE);
//            }
//        } else {
//            // initiate_Video_Call.setEnabled(false);
//            Dialogs.showShortSnackbarMessage(rootView, "Video session not available.");
//        }
//
//        if (autoButtonHideTimer != null)
//            autoButtonHideTimer.halt();
//        autoButtonHideTimer = new Timer(5000, this);
//        autoButtonHideTimer.start();
//    }
//
//    @OnClick(R.id.video_call_end_fab)
//    public void onVideoCallEndClicked() {
//
//        callInfoBeanObj = CallInfoBean.getCallInfoBeanObj();
//        SingletonUtil.get().isCallOngoing = false;// call Minimized
//        mainActivity.all_call_fragment_frame.setVisibility(View.INVISIBLE);// call Minimized
//        SingletonUtil.get().getCallEngine().createAndSendCallDisconnectCmd(mainActivity, callInfoBeanObj);
//        mainActivity.notficationLocalHelper.completed(ConstantsUtil.CALL_TIME_BACKGROUND);
//        getFragmentManager().popBackStackImmediate();// call Minimized
//        showChatView.sendEmptyMessage(0);
//    }
//
//    @OnClick(R.id.video_call_stop_fab)
//    public void onVideoCallStopClicked() {
//        call_current_click = "VIDEO_SECOND";
//        if (new PermissionProcessEngine(mainActivity, this)
//                .showPermissionCheck(new String[]{Manifest.permission.CAMERA})) {
//
//            this.videoControlEvent(ConstantsUtil.BUTTON_CLICKED);
//        }
//    }
//
//    @Override
//    public void initiateVideoCall(String callViewMode, final boolean isActionForReceiver) {
//        Log.e("call", "callViewMode initiatevideocall " + callViewMode);
//        if (isbackpressed) {// call Minimized
//            try {
//                mainActivity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        mainActivity.call_back_view.setVisibility(View.GONE);
//                        mainActivity.all_call_fragment_frame.setVisibility(View.VISIBLE);
//                        FrameLayout.LayoutParams llparams = new FrameLayout.LayoutParams(
//                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                        call_View.setLayoutParams(llparams);
//                        call_View.animate().x(0).y(0).setDuration(0).start();
//                        call_View.setOnTouchListener(null);
//                        video_call_RL.setFocusable(true);
//                        video_call_RL.setClickable(true);
//                        video_call_RL.setEnabled(true);
//
//                        mPeerView.setFocusable(true);
//                        mPeerView.setClickable(true);
//                        mPeerView.setEnabled(true);
//
//                        if (videoMyself.getVisibility() == View.VISIBLE) {
//                            RelativeLayout.LayoutParams vgparams = new RelativeLayout.LayoutParams(
//                                    AndroidUtilities.dpToPx(120), AndroidUtilities.dpToPx(150));
//                            videoMyself.setLayoutParams(vgparams);
//                            videoMyself.animate().x(0).y(0).setDuration(0).start();
//                        }
//
//                        videoMyself.setFocusable(true);
//                        videoMyself.setClickable(true);
//                        videoMyself.setEnabled(true);
//                        // header.setVisibility(View.VISIBLE);
//                        video_actionBar_CL.setVisibility(View.VISIBLE);
//                        video_bottomBar_CL.setVisibility(View.VISIBLE);
//                        initiate_Video_Call.setVisibility(View.VISIBLE);
//                    }
//                });
//
//                isbackpressed = false;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        if (callViewMode.equals(ConstantsUtil.VIEW_MODE_VIDEO_CALL_FORCESTOP)) {
//
//            this.callViewMode = ConstantsUtil.VIEW_MODE_VOICE_ONLY_CALL_APP_TO_APP;
//            final String callViewModeFinal = this.callViewMode;
//            if (mVideoInputThread != null)
//                mVideoInputThread.close();
//            // mVideoOutputThread.close();
//            mainActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//
//                    showCallLayout(callViewModeFinal);
//                    video_call_stop_fab.setImageDrawable(
//                            ResourcesCompat.getDrawable(mainActivity, R.drawable.ic_video_call_off_organge));
//                    video_call_stop_fab.setImageTintList(getResources().getColorStateList(R.color.white));
//                }
//            });
//
//        } else if (this.callViewMode.equals(ConstantsUtil.VIEW_MODE_VOICE_ONLY_CALL_APP_TO_APP)) {
//
//            this.callViewMode = callViewMode;
//            final String callViewModeFinal = this.callViewMode;
//            mainActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    showCallLayout(callViewModeFinal);
//                    if (isActionForReceiver) {
//                        video_call_stop_fab.setImageDrawable(
//                                ResourcesCompat.getDrawable(mainActivity, R.drawable.ic_video_call_log_organge));
//                        video_call_stop_fab.setImageTintList(getResources().getColorStateList(R.color.white));
//                        toggle.setVisibility(View.GONE);
//                    }
//                }
//            });
//            if (isActionForReceiver) {
//                if (mVideoOutputThread == null)
//                    setupRender();
//            } else {
//                setupCapture();
//            }
//
//        } else if (callViewMode.equals(ConstantsUtil.VIEW_MODE_VOICE_ONLY_CALL_APP_TO_APP)) {
//
//            this.callViewMode = callViewMode;
//            final String callViewModeFinal = this.callViewMode;
//            onVideoCallStoppedNotified(true);
//            mainActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    showCallLayout(callViewModeFinal);
//                }
//            });
//
//        } else if (this.callViewMode.equals(ConstantsUtil.VIEW_MODE_VIDEO_CALL_BOTH)) {
//
//            this.callViewMode = callViewMode;
//            final String callViewModeFinal = this.callViewMode;
//            mainActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    showCallLayout(callViewModeFinal);
//                    if (isActionForReceiver) {
//                        toggle.setVisibility(View.VISIBLE);
//                    }
//                }
//            });
//
//        } else if ((this.callViewMode.equals(ConstantsUtil.VIEW_MODE_VIDEO_CALL_SEND_ONLY))
//                && (callViewMode.equals(ConstantsUtil.VIEW_MODE_VIDEO_CALL_RECEIVE_ONLY))) {
//
//            this.callViewMode = ConstantsUtil.VIEW_MODE_VIDEO_CALL_BOTH;
//            final String callViewModeFinal = this.callViewMode;
//            mainActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    showCallLayout(callViewModeFinal);
//                }
//            });
//            if (isActionForReceiver) {
//                if (mVideoOutputThread == null) {
//                    setupRender();
//                }
//            }
//
//        } else if ((this.callViewMode.equals(ConstantsUtil.VIEW_MODE_VIDEO_CALL_RECEIVE_ONLY))
//                && (callViewMode.equals(ConstantsUtil.VIEW_MODE_VIDEO_CALL_SEND_ONLY))) {
//
//            if (isActionForReceiver) {
//                this.callViewMode = ConstantsUtil.VIEW_MODE_VOICE_ONLY_CALL_APP_TO_APP;
//                final String callViewModeFinal = this.callViewMode;
//                // mVideoOutputThread.close();
//                mainActivity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        showCallLayout(callViewModeFinal);
//                    }
//                });
//            }
//        }
//
//    }
//
//    @Override
//    public void screenWakeLockProximitySensor(boolean isEnable) {
//
//        CallEngine callEngineObj = SingletonUtil.get().getCallEngine();
//        if (isEnable) {
//            Field f;
//            int proximityScreenOffWakeLock = 23;
//            try {
//
//                f = PowerManager.class.getDeclaredField("PROXIMITY_SCREEN_OFF_WAKE_LOCK");
//                proximityScreenOffWakeLock = (Integer) f.get(null);
//
//            } catch (SecurityException e) {
//
//                e.printStackTrace();
//            } catch (NoSuchFieldException e) {
//
//                e.printStackTrace();
//            } catch (IllegalArgumentException e) {
//
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//
//                e.printStackTrace();
//            }
//
//            if (mProximityWakeLock != null) {
//                if (mProximityWakeLock.isHeld()) {
//                    mProximityWakeLock.release();
//                    CallInfoBean.setmProximityWakeLock(mProximityWakeLock);
//                }
//            }
//            PowerManager pm = (PowerManager) mainActivity.getSystemService(Context.POWER_SERVICE);
//            mProximityWakeLock = pm.newWakeLock(proximityScreenOffWakeLock, "CallScreenLock");
//
//            if (!mProximityWakeLock.isHeld()) {
//                mProximityWakeLock.acquire();
//            }
//            // mainActivity.runOnUiThread(new Runnable() {
//            // @Override
//            // public void run() {
//            //
//            // mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            // }
//            // });
//        } else {
//
//            CallInfoBean.setmProximityWakeLock(mProximityWakeLock);
//            // mainActivity.runOnUiThread(new Runnable() {
//            // @Override
//            // public void run() {
//            //
//            // mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            // }
//            // });
//            if (mProximityWakeLock != null) {
//                if (mProximityWakeLock.isHeld()) {
//                    mProximityWakeLock.release();
//                    CallInfoBean.setmProximityWakeLock(mProximityWakeLock);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void setVideoConfigValues(int senderOs, int cameraSwitch) {
//        this.currentSenderOs = senderOs;
//        this.currentCameraSwitch = cameraSwitch;
//    }
//
//    @OnClick(R.id.toggleButton)
//    public void onCameraSwitchclicked() {
//
//        if (default_camera == 0) {
//            default_camera = 1;
//            degrees = 270.0f; // temporary check
//            sSelectedCamera = findCamera(CameraCharacteristics.LENS_FACING_BACK);
//
//        } else {
//            default_camera = 0;
//            degrees = 90.0f; // temporary check
//            sSelectedCamera = findCamera(CameraCharacteristics.LENS_FACING_FRONT);
//        }
//        //
//        // default_camera = 1;
//        // switching_camera = true;
//
//        /*
//         * this line calling open camera two times which was causing issue in camera
//         * swith
//         */
//        // mVideoInputThread.openVideoInput(sSelectedCamera, true);
//        mVideoInputThread.close();
//        setupCapture();
//        return;
//    }
//
//    private void showCallLayout(String call_mode) {
//        try {
//
//            if (call_mode.equals(ConstantsUtil.VIEW_MODE_VOICE_ONLY_CALL_APP_TO_APP)) {
//
//                video_call_RL.setVisibility(View.GONE);
//                voice_call_only_LL.setVisibility(View.VISIBLE);
//                isNormalCall = true;
//                isPreviewMyselfShow = true;
//                // callServiceType = ConstantsUtil.VIDEO_CALL_RELAY;
//
//                if (!Util.isBluetoothHeadsetConnected()) {
//                    CallInfoBean.setSpeakerMode(true);
//                } else {
//                    if (CallInfoBean.isSpeakerMode()) {
//                        CallInfoBean.setSpeakerMode(false);
//                    } else {
//                        CallInfoBean.setSpeakerMode(true);
//                    }
//                }
//                onCallAudioClicked();
//                screenWakeLockProximitySensor(true);
//
//            } else if (call_mode.equals(ConstantsUtil.VIEW_MODE_VIDEO_CALL_BOTH)) {
//
//                video_call_RL.setVisibility(View.VISIBLE);
//                voice_call_only_LL.setVisibility(View.GONE);
//                mPeerView.setVisibility(View.VISIBLE);
//                videoMyself.setVisibility(View.VISIBLE);
//                isNormalCall = true;
//                if (autoButtonHideTimer == null) {
//                    autoButtonHideTimer = new Timer(5000, this);
//                    autoButtonHideTimer.start();
//                }
//                // callServiceType = ConstantsUtil.VIDEO_CALL_RELAY;
//
//                if (!Util.isBluetoothHeadsetConnected()) {
//                    CallInfoBean.setSpeakerMode(false);
//                } else {
//                    if (CallInfoBean.isSpeakerMode()) {
//                        CallInfoBean.setSpeakerMode(false);
//                    } else {
//                        CallInfoBean.setSpeakerMode(true);
//                    }
//                }
//                onCallAudioClicked();
//                screenWakeLockProximitySensor(false);
//            } else if (call_mode.equals(ConstantsUtil.VIEW_MODE_VIDEO_CALL_SEND_ONLY)) {
//
//                video_call_RL.setVisibility(View.VISIBLE);
//                voice_call_only_LL.setVisibility(View.GONE);
//                mPeerView.setVisibility(View.GONE);
//                videoMyself.setVisibility(View.VISIBLE);
//                isNormalCall = true;
//                if (autoButtonHideTimer != null)
//                    autoButtonHideTimer.halt();
//                autoButtonHideTimer = new Timer(5000, this);
//                autoButtonHideTimer.start();
//                // callServiceType = ConstantsUtil.VIDEO_CALL_RELAY;
//
//                if (!Util.isBluetoothHeadsetConnected()) {
//                    CallInfoBean.setSpeakerMode(false);
//                } else {
//                    if (CallInfoBean.isSpeakerMode()) {
//                        CallInfoBean.setSpeakerMode(false);
//                    } else {
//                        CallInfoBean.setSpeakerMode(true);
//                    }
//                }
//                onCallAudioClicked();
//                screenWakeLockProximitySensor(false);
//            } else if (call_mode.equals(ConstantsUtil.VIEW_MODE_VIDEO_CALL_RECEIVE_ONLY)) {
//
//                video_call_RL.setVisibility(View.VISIBLE);
//                voice_call_only_LL.setVisibility(View.GONE);
//                mPeerView.setVisibility(View.VISIBLE);
//                videoMyself.setVisibility(View.GONE);
//                isNormalCall = true;
//                if (autoButtonHideTimer != null)
//                    autoButtonHideTimer.halt();
//                autoButtonHideTimer = new Timer(5000, this);
//                autoButtonHideTimer.start();
//                // callServiceType = ConstantsUtil.VIDEO_CALL_RELAY;
//
//                if (!Util.isBluetoothHeadsetConnected()) {
//                    CallInfoBean.setSpeakerMode(false);
//                } else {
//                    if (CallInfoBean.isSpeakerMode()) {
//                        CallInfoBean.setSpeakerMode(false);
//                    } else {
//                        CallInfoBean.setSpeakerMode(true);
//                    }
//                }
//                onCallAudioClicked();
//                screenWakeLockProximitySensor(false);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//
//        return keyCode == KeyEvent.KEYCODE_BACK;
//    }
//
//    @Override
//    public boolean onKeyDownBehaviour(int keyCode, KeyEvent event) {
//
//        if (keyCode == KeyEvent.KEYCODE_BACK) {// call minimized
//
//            if (GlobalObjectBean.getGlobalObjectbean().isCallFromNotification()) {
//            } else if (!((call_status_txt.getText().toString().equalsIgnoreCase(ConstantsUtil.TALKING_STATE_TEXT))
//                    || (call_status_txt.getText().toString().equalsIgnoreCase(ConstantsUtil.HOLDON_STATE_TEXT)))) {
//            } else {
//
//                mainActivity.main_fragment_frame.setVisibility(View.VISIBLE);
//                mainActivity.main_fragment_frame.setFocusable(true);
//                mainActivity.main_fragment_frame.setClickable(true);
//                mainActivity.main_fragment_frame.setEnabled(true);
//                if (video_call_RL.getVisibility() == View.VISIBLE) {
//
//                    mainActivity.all_call_fragment_frame.setVisibility(View.VISIBLE);
//                    if (callViewMode.equals(ConstantsUtil.VIEW_MODE_VIDEO_CALL_BOTH)
//                            || callViewMode.equals(ConstantsUtil.VIEW_MODE_VIDEO_CALL_SEND_ONLY)) {
//
//                        videoMyself.setVisibility(View.VISIBLE);
//                    }
//                    FrameLayout.LayoutParams llparams = new FrameLayout.LayoutParams(AndroidUtilities.dpToPx(150),
//                            AndroidUtilities.dpToPx(200));
//
//                    call_View.setLayoutParams(llparams);
//                    call_View.setOnTouchListener(new OnDragTouchListener(call_View));
//                    video_call_RL.setFocusable(false);
//                    video_call_RL.setClickable(false);
//                    video_call_RL.setEnabled(false);
//
//                    mPeerView.setFocusable(false);
//                    mPeerView.setClickable(false);
//                    mPeerView.setEnabled(false);
//                    mPeerView.setVisibility(View.VISIBLE);
//
//                    if (videoMyself.getVisibility() == View.VISIBLE) {
//
//                        RelativeLayout.LayoutParams vgparams = new RelativeLayout.LayoutParams(
//                                AndroidUtilities.dpToPx(60), AndroidUtilities.dpToPx(75));
//                        videoMyself.setLayoutParams(vgparams);
//                        videoMyself.animate().x(0).y(0).setDuration(0).start();
//                    }
//
//                    videoMyself.setFocusable(false);
//                    videoMyself.setClickable(false);
//                    videoMyself.setEnabled(false);
//                    video_bottomBar_CL.setVisibility(View.GONE);
//
//                    video_actionBar_CL.setVisibility(View.GONE);
//                    initiate_Video_Call.setVisibility(View.GONE);
//
//                } else {
//
//                    mainActivity.all_call_fragment_frame.setVisibility(View.INVISIBLE);
//                    mainActivity.call_back_view.setVisibility(View.VISIBLE);
//                    mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                }
//                // mainActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                //
//                isbackpressed = true;
//                call_maximized = 0;
//
//                new Handler().postDelayed(() -> {
//                    call_maximized = 0;
//
//                }, 2000);
//            }
//
//            return true;
//        } else {
//
//            return false;
//        }
//    }
//
//    private void disconnectCall() {
//
//        callInfoBeanObj = CallInfoBean.getCallInfoBeanObj();
//        SingletonUtil.get().getCallEngine().createAndSendCallDisconnectCmd(mainActivity, callInfoBeanObj);
//        mainActivity.notficationLocalHelper.completed(ConstantsUtil.CALL_TIME_BACKGROUND);
//    }
//
//    @Override
//    protected void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
//
//    }
//
//    @Override
//    public void onCallTimeOutNotified() {
//
//    }
//
//    @Override
//    public boolean onVideoCallDataReceivedNotified(int packetTypeFlag, byte[] buffer, int bufferAvailableDataLen,
//                                                   int camera_switch, int senderOS) {
//
//        try {
//
//            byte[] renderBuffer = new byte[bufferAvailableDataLen];
//            System.arraycopy(buffer, 0, renderBuffer, 0, bufferAvailableDataLen);
//
//            // switching_camera_int = default_camera;
//
//            if (camera_switch == 0) {
//                if (senderOS == ConstantsUtil.OS_VERSION_ANDROID)
//                    degrees = 270.0f;
//                else if (senderOS == ConstantsUtil.OS_VERSION_ANDROID_BLACKPHONE)
//                    degrees = 270.0f;
//                else
//                    degrees = 0.0f;
//            } else {
//                if (senderOS == ConstantsUtil.OS_VERSION_ANDROID) {
//                    degrees = 90.0f;
//                } else if (senderOS == ConstantsUtil.OS_VERSION_ANDROID_BLACKPHONE) {
//                    degrees = 90.0f;
//                } else {
//                    degrees = 0.0f;
//                }
//                CallInfoBean.setRotationDegree(degrees);
//            }
//            if (!isConfigDone
//                    && ((senderOS == ConstantsUtil.OS_VERSION_IOS) || (senderOS == ConstantsUtil.OS_VERSION_DESKTOP)
//                    || (senderOS == ConstantsUtil.OS_VERSION_ANDROID_BLACKPHONE))) {
//                mVideoOutputThread.configUI(senderOS);
//                switching_camera_int = camera_switch;
//                isConfigDone = true;
//            }
//            if (camera_switch != switching_camera_int) {
//
//                // mVideoOutputThread.reset();
//                mVideoOutputThread.configUI(senderOS);
//                switching_camera_int = camera_switch;
//
//                // byte[] renderBufferNill = new byte[bufferAvailableDataLen];
//                // mVideoOutputThread.render(packetTypeFlag, renderBufferNill);
//            }
//            if (call_maximized == 0) {// call Minimized
//                // current_os=senderOS;
//
//                mVideoOutputThread.configUI(senderOS);
//                call_maximized = 2;
//
//            } else if (call_maximized == 1) {
//                mVideoOutputThread.configUI(senderOS);
//
//                call_maximized = 2;
//            }
//            mVideoOutputThread.render(packetTypeFlag, renderBuffer);
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//
//    }
//
//    @Override
//    public void onVideoCallStoppedNotified(boolean isEndCall) {
//        if (mVideoInputThread != null)
//            if (!mVideoInputThread.isInterrupted()) {
//                if (isEndCall)
//                    mVideoInputThread.interrupt();
//                mVideoInputThread.close();
//            }
//        if (mVideoOutputThread != null)
//            if (!mVideoOutputThread.isInterrupted())
//                mVideoOutputThread.close();
//    }
//
//    @Override
//    public void onCallResponseReceivedNotified(Message messageObj) {
//
//        Bundle b = messageObj.getData();
//        String command = b.getString("Command");
//        String action = b.getString("Action");
//
//        if (action.equalsIgnoreCase(ConstantsUtil.ACTION_SHOW_CHAT_VIEW)) {
//
//            showChatView.sendEmptyMessage(0);
//        } else if (action.equalsIgnoreCase(ConstantsUtil.ACTION_UPDATE_CALL_STATUS_TEXT)) {
//
//            updateCallStatusText.sendMessage(messageObj);
//        } else if (action.equalsIgnoreCase(ConstantsUtil.ACTION_START_CLOCK)) {
//
//            handlerStartClock.sendMessage(messageObj);
//        } else if (action.equalsIgnoreCase(ConstantsUtil.ACTION_STOP_CLOCK)) {
//
//            handlerStopClock.sendMessage(messageObj);
//        } else if (action.equalsIgnoreCase(ConstantsUtil.ACTION_SHOW_CALLING_VIEW)) {
//
//            String CallVewMode = b.getString("CallViewMode");
//            b.putBoolean("IsVideoWithRTPVoiceCall", false);
//            b.putString("CallViewMode", CallVewMode);
//            messageObj.setData(b);
//            handlerShowCallingView.sendMessage(messageObj);
//        } else if (action.equalsIgnoreCase(ConstantsUtil.ACTION_SHOW_VIDEO_CALLING_VIEW)) {
//
//            String CallVewMode = b.getString("CallViewMode");
//            b.putBoolean("IsVideoWithRTPVoiceCall", false);
//            b.putString("CallViewMode", CallVewMode);
//            messageObj.setData(b);
//            handlerShowCallingView.sendMessage(messageObj);
//
//        } else if (action.equalsIgnoreCase(ConstantsUtil.ACTION_SHOW_VIDEO_CALLING_WITH_RTP_VOIC_CALL_VIEW)) {
//
//            String CallVewMode = b.getString("CallViewMode");
//            b.putBoolean("IsVideoWithRTPVoiceCall", true);
//            b.putString("CallViewMode", CallVewMode);
//            messageObj.setData(b);
//            handlerShowCallingView.sendMessage(messageObj);
//
//        }
//
//    }
//
//    private void send_command(byte[] encoded, int flag) {
//
//        byte[] info = new byte[13];
//        ArrayList<byte[]> infoBytesLst = new ArrayList<>(12);
//        byte flagByte = (byte) (flag), chunklast, cameraByte = 0, senderOs;
//        byte[] chunkid, chunk_size, config_packetLength;
//
//        byte[] data = new byte[this.callInfoBeanObj.getCall_SenderConfig().length + encoded.length];
//        System.arraycopy(this.callInfoBeanObj.getCall_SenderConfig(), 0, data, 0,
//                this.callInfoBeanObj.getCall_SenderConfig().length);
//        System.arraycopy(encoded, 0, data, this.callInfoBeanObj.getCall_SenderConfig().length, encoded.length);
//        encoded = data;
//
//        packet_id++;
//        byte[] packet = ArrayProcessUtility.ConvertIntTo2Bytes(packet_id);
//
//        // String videoCallDatePath =
//        // AndroidUtilities.createAttExtPath(ConstantsUtil.VIDEO) + File.separator
//        // + "videocall";
//        // videoCallDatePath = Environment.getExternalStorageDirectory() +
//        // File.separator + "callVideo";
//        // File videocallDataFile = new File(videoCallDatePath);
//        // FileOperationUtil fileOperationUtil = new FileOperationUtil();
//        // fileOperationUtil.writeFileFromBytesAppend(encoded, videocallDataFile);
//
//        ArrayList<byte[]> splittable = splitArray(encoded, ConstantsUtil.VIDEO_CALL_CHUNK_SIZE);
//        byte chunk_count = (byte) (splittable.size());
//
//        for (int i = 0; i < splittable.size(); i++) {
//
//            chunkid = ArrayProcessUtility.ConvertIntTo2Bytes(i);
//            chunk_size = ArrayProcessUtility.ConvertIntTo2Bytes(splittable.get(i).length);
//            if (i == 0)
//                config_packetLength = ArrayProcessUtility
//                        .ConvertIntTo2Bytes(this.callInfoBeanObj.getCall_SenderConfig().length);
//            else
//                config_packetLength = ArrayProcessUtility.ConvertIntTo2Bytes(0);
//
//            if (splittable.get(i).length < ConstantsUtil.VIDEO_CALL_CHUNK_SIZE || i == splittable.size() - 1) {
//                //
//                chunklast = (byte) (1);
//            } else {
//                chunklast = (byte) (0);
//            }
//
//            // if (default_camera == 0) {
//            // cameraByte = (byte) (0);
//            // } else if (default_camera == 1) {
//            // cameraByte = (byte) (1);
//            // }
//            if (current_Camera == 1) {
//                cameraByte = (byte) (0);
//            } else if (current_Camera == 0) {
//                cameraByte = (byte) (1);
//            }
//            // senderOs = (byte) (2);
//            if (ConfigSettings.KMessagingAppPlatformID.equals(ConstantsUtil.OS_VERSION_ANDROID + "")) {
//                senderOs = (byte) (ConstantsUtil.OS_VERSION_ANDROID);
//            } else {
//                senderOs = (byte) (ConstantsUtil.OS_VERSION_ANDROID_BLACKPHONE);
//            }
//            data = new byte[splittable.get(i).length + info.length];
//            System.arraycopy(chunk_size, 0, info, 0, chunk_size.length);
//            System.arraycopy(chunkid, 0, info, 2, chunkid.length);
//            info[4] = chunklast;
//            info[5] = flagByte;
//            info[6] = chunk_count;
//            System.arraycopy(packet, 0, info, 7, packet.length);
//            System.arraycopy(config_packetLength, 0, info, 9, config_packetLength.length);
//            info[11] = cameraByte;
//            info[12] = senderOs;
//
//            // infoBytesLst.add(chunk_size);
//            // infoBytesLst.add(chunkid);
//            // infoBytesLst.add(new byte[]{chunklast});
//            // infoBytesLst.add(new byte[]{flagByte});
//            // infoBytesLst.add(new byte[]{chunk_count});
//            // infoBytesLst.add(new byte[]{cameraByte});
//            // info = ArrayProcessUtility.ToPrimitiveByteArray(infoBytesLst);
//
//            System.arraycopy(info, 0, data, 0, info.length);
//            System.arraycopy(splittable.get(i), 0, data, info.length, splittable.get(i).length);
//            // Log.i("Senderpacketlossfinder", "Sender " + " packet_id = " + packet_id + "
//            // chunk_count = "
//            // + splittable.size() + " chunk_id = " + i);
//
//            // if ((flag == 1) || (packet_id % 5 == 0)) {
//            this.callInfoBeanObj.video_call_frame_id = packet_id;
//            this.callInfoBeanObj.video_call_frame_type = flag;
//            SingletonUtil.get().getCallEngine().sendVideoRtpGenerated(data, this.callInfoBeanObj);
//            // }
//        }
//
//    }
//
//    // CCONNECT 1:2653:151.253.171.59:11001:151.253.171.59:11002:0:4:4:2
//    // CCONNECT 1:2654:151.253.171.59:11001:151.253.171.59:11002:23321:4:4:2
//    private void sendVideoControlInit(String initStr, int frame_type) {// VideoRelay
//
//        try {
//            new Thread(() -> {
//
//                byte[] initStrBytes = null;
//                try {
//
//                    initStrBytes = initStr.getBytes(StandardCharsets.UTF_8);
//                } catch (Exception e) {
//                    initStrBytes = initStr.getBytes();
//                    e.printStackTrace();
//                }
//                callInfoBeanObj.video_call_frame_type = frame_type;
//                callInfoBeanObj.video_call_frame_id = 0;
//                SingletonUtil.get().getCallEngine().sendVideoRtpGenerated(initStrBytes, callInfoBeanObj);
//                Log.e("NewInit", "initsend " + initStr + " " + callInfoBeanObj.video_call_frame_type);
//            }).start();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private ArrayList<byte[]> splitArray(byte[] originalArray, int chunkSize) {
//
//        ArrayList<byte[]> listOfArrays = new ArrayList<byte[]>();
//        int totalSize = originalArray.length;
//        if (totalSize < chunkSize) {
//            chunkSize = totalSize;
//        }
//        int from = 0;
//        int to = chunkSize;
//
//        while (from < totalSize) {
//
//            byte[] partArray = Arrays.copyOfRange(originalArray, from, to);
//            listOfArrays.add(partArray);
//
//            from += chunkSize;
//            to = from + chunkSize;
//            if (to > totalSize) {
//                to = totalSize;
//            }
//        }
//
//        return listOfArrays;
//    }
//
//    @Override
//    public void permissionSelectResponse(boolean isPermissionGranted) {
//
//        if (isPermissionGranted) {
//            // isPermissioncheck=isPermissionGranted;
//            if (call_current_click.equals("VOICE")) {
//                this.initializeCall();
//            } else if (call_current_click.equals("VIDEO_FIRST")) {
//                this.startVideoCallClicked();
//            } else if (call_current_click.equals("VIDEO_SECOND")) {
//                this.videoControlEvent(ConstantsUtil.BUTTON_CLICKED);
//            }
//        } else {
//
//            if (!(SingletonUtil.get().getCallEngine().isCallFrom())) {
//
//                showChatView.sendEmptyMessage(0);
//            }
//        }
//
//    }
//
//    @Override
//    public void onResponseReceivedNotified(String command) {
//
//        if (command.equalsIgnoreCase(Commands.SERVERCONNECT)) {
//
//            GlobalObjectBean.getGlobalObjectbean().authenticateUser();
//        }
//
//    }
//
//    @Override
//    public void onTimeout(Timer t) {
//        if (t == autoButtonHideTimer) {
//            mainActivity.runOnUiThread(() -> {
//
//                video_actionBar_CL.setVisibility(View.GONE);
//                video_bottomBar_CL.setVisibility(View.GONE);
//            });
//        }
//
//    }
//
//    private void createandsendVideoControlCmd(CallInfoBean callInfoBeanObj, boolean videoMode) {// CALLHOLD
//
//        callInfoBeanObj.setMuteorVideoButtonClicked(ConstantsUtil.VIDEO_BUTTON_CLICKED);
//        CallInfoBean.setVideoMode(videoMode);
//        CallInfoBean.setCallInfoBeanObj(callInfoBeanObj);
//        mAppCommunicationServiceObj.sendCallCtrlCommand(callInfoBeanObj);
//    }
//
//    private void createandsendMutecmd(CallInfoBean callInfoBeanObj, boolean muteMode) {// CALLHOLD
//
//        callInfoBeanObj.setMuteorVideoButtonClicked(ConstantsUtil.MUTE_BUTTON_CLICKED);
//        CallInfoBean.setMuteMode(muteMode);
//        CallInfoBean.setCallInfoBeanObj(callInfoBeanObj);
//        mAppCommunicationServiceObj.sendCallCtrlCommand(callInfoBeanObj);
//    }
//
//    public void loading_click() {// call Minimized
//
//        // mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        Util.hideKeyboard(mainActivity, call_View);
//        mainActivity.main_fragment_frame.setVisibility(View.INVISIBLE);
//        mainActivity.main_fragment_frame.setFocusable(false);
//        mainActivity.main_fragment_frame.setClickable(false);
//        mainActivity.main_fragment_frame.setEnabled(false);
//        if (isbackpressed) {
//
//            if (video_call_RL.getVisibility() == View.VISIBLE) {
//                FrameLayout.LayoutParams llparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT);
//                call_View.setLayoutParams(llparams);
//                call_View.setOnTouchListener(null);
//                video_call_RL.setFocusable(true);
//                video_call_RL.setClickable(true);
//                video_call_RL.setEnabled(true);
//
//                mPeerView.setFocusable(true);
//                mPeerView.setClickable(true);
//                mPeerView.setEnabled(true);
//
//                videoMyself.setFocusable(true);
//                videoMyself.setClickable(true);
//                videoMyself.setEnabled(true);
//                if (videoMyself.getVisibility() == View.VISIBLE) {
//                    RelativeLayout.LayoutParams vgparams = new RelativeLayout.LayoutParams(AndroidUtilities.dpToPx(120),
//                            AndroidUtilities.dpToPx(150));
//                    videoMyself.setLayoutParams(vgparams);
//                    videoMyself.animate().x(0).y(0).setDuration(0).start();
//                }
//                video_actionBar_CL.setVisibility(View.VISIBLE);
//                video_bottomBar_CL.setVisibility(View.VISIBLE);
//                initiate_Video_Call.setVisibility(View.VISIBLE);
//
//            } else {
//                mainActivity.call_back_view.setVisibility(View.GONE);
//                mainActivity.all_call_fragment_frame.setVisibility(View.VISIBLE);
//
//            }
//
//            call_maximized = 1;
//            isbackpressed = false;
//
//            new Handler().postDelayed(() -> {
//
//                call_maximized = 1;
//            }, 2000);
//
//        }
//        // call_View.animate().x(0).y(30).setDuration(0).start();
//
//        call_View.setFocusable(true);
//        call_View.setClickable(true);
//        call_View.setEnabled(true);
//
//    }
//
//    class VideoInputThread extends HandlerThread {
//
//        Handler mHandler;
//        CameraDevice mCamera;
//        CameraCaptureSession mCameraCaptureSession;
//        MediaCodec mVideoEncoder;
//        int mStatus;
//
//        public VideoInputThread() {
//
//            super("Video Input");
//            super.start();
//            mHandler = new Handler(getLooper());
//        }
//
//        public void open() {
//
//            mHandler.post(() -> {
//
//                android.util.Log.w(TAG, "+open VideoInput");
//                mStatus = STATUS_OPENING;
//                openVideoInput(sSelectedCamera, false);
//                mStatus = STATUS_RUNNING;
//            });
//        }
//
//        public void startVideoEncoder() {
//
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    android.util.Log.w(TAG, "+startEncoder: ");
//                    if (mVideoEncoder == null) {
//                        openVideoEncoder();
//                    }
//                }
//            });
//        }
//
//        public void close() {
//            // try {
//
//            if (mStatus == STATUS_RUNNING) {
//                // if (mHandler.getLooper().getThread().isAlive()) {
//                // this.interrupt();
//
//                mHandler.post(() -> {
//
//                    mStatus = STATUS_CLOSING;
//
//                    // try {
//                    // mCameraOpenCloseLock.acquire();
//
//                    if (null != mCameraCaptureSession) {
//                        // try {
//                        // mCameraCaptureSession.stopRepeating();
//                        // mCameraCaptureSession.abortCaptures();
//                        // } catch (CameraAccessException e) {
//                        // e.printStackTrace();
//                        // }
//                        mCameraCaptureSession.close();
//                        mCameraCaptureSession = null;
//                    }
//
//                    if (null != mCamera) {
//                        mCamera.close();
//                        mCamera = null;
//                    }
//
//                    if (mVideoEncoder != null) {
//                        mVideoEncoder.release();
//                        mVideoEncoder = null;
//                    }
//
//                    stopBackgroundThread();
//                    mStatus = STATUS_CLOSED;
//                    // } catch (InterruptedException e) {
//                    // e.printStackTrace();
//                    // } finally {
//                    // mCameraOpenCloseLock.release();
//                    // }
//                });
//                waitUntilClosed();
//            }
//            // setupRender();
//
//            // } catch (Exception e) {
//            // e.printStackTrace();
//            // mStatus = STATUS_CLOSED;
//            // }
//
//        }
//
//        public void close_video_encoder() {
//            // try {
//
//            // if (mStatus == STATUS_RUNNING) {
//            // // if (mHandler.getLooper().getThread().isAlive()) {
//            // // this.interrupt();
//            // mHandler.post(new Runnable() {
//            // @Override
//            // public void run() {
//
//            // mStatus = STATUS_CLOSING;
//
//            // if (mCamera != null) {
//            // mCamera.close();
//            // mCamera = null;
//            // }
//
//            if (mVideoEncoder != null) {
//                mVideoEncoder.release();
//                mVideoEncoder = null;
//            }
//
//            // quit();
//            // mStatus = STATUS_CLOSED;
//            // }
//            // });
//            // waitUntilClosed();
//            // }
//            // setupRender();
//
//            // } catch (Exception e) {
//            // e.printStackTrace();
//            // mStatus = STATUS_CLOSED;
//            // }
//
//        }
//
//        public void waitUntilClosed() {
//
//            if (mStatus == STATUS_NONE)
//                return;
//
//            try {
//
//                int timeWaited = 0;
//                while (mStatus != STATUS_CLOSED) {
//                    if (timeWaited <= 5000) {
//                        Thread.sleep(10);
//                        // Log.w("Camera wait", "waiting..");
//                        timeWaited = timeWaited + 10;
//                    } else {
//
//                        if (mVideoEncoder != null) {
//                            mVideoEncoder.release();
//                            mVideoEncoder = null;
//                        }
//                        stopBackgroundThread();
//                        mStatus = STATUS_CLOSED;
//                    }
//
//                }
//            } catch (InterruptedException ex) {
//                Log.w("Camera wait", "waiting.." + ex.getMessage());
//            }
//        }
//
//        private void openVideoInput(String cameraName, boolean iscamera_switching) {
//
//            try {
//
//                android.util.Log.w(TAG, "+openVideoInput: " + cameraName);
//
//                final CameraManager cameraManager = (CameraManager) mainActivity
//                        .getSystemService(Context.CAMERA_SERVICE);
//
//                final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
//
//                    @Override
//                    public void onOpened(@NonNull CameraDevice camera) {
//                        Log.e("onOpened", "Camera onOpened");
//
//                        mCamera = camera;
//                        openVideoEncoder();
//                        // mCameraOpenCloseLock.release();
//                        // if (null != mTextureView) {
//                        // configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
//
//                    }
//
//                    @Override
//                    public void onDisconnected(@NonNull CameraDevice camera) {
//                        Log.e("onDisconnected", "Camera onDisconnected");
//                        // mCameraOpenCloseLock.release();
//                        if (mCameraCaptureSession != null) {
//
//                            mCameraCaptureSession.close();
//                            mCameraCaptureSession = null;
//                        }
//                        camera.close();
//                        mCamera = null;
//                    }
//
//                    @Override
//                    public void onError(@NonNull CameraDevice camera, int error) {
//                        Log.e("onError", "Camera onError");
//                        // mCameraOpenCloseLock.release();
//                        if (mCameraCaptureSession != null) {
//
//                            mCameraCaptureSession.close();
//                            mCameraCaptureSession = null;
//                        }
//                        camera.close();
//                        mCamera = null;
//                        // stopBackgroundThread();
//                        // onCameraSwitchclicked();
//                    }
//
//                    @Override
//                    public void onClosed(@NonNull CameraDevice camera) {
//                        Log.e("onClosed", "Camera onClosed");
//                        super.onClosed(camera);
//                        if (mCameraCaptureSession != null) {
//
//                            mCameraCaptureSession.close();
//                            mCameraCaptureSession = null;
//                        }
//                        mCamera = null;
//                        // mStatus=
//
//                    }
//                };
//
//                startBackgroundThread();
//                cameraManager.openCamera(cameraName, stateCallback, mBackgroundHandler);
//                // cameraManager.openCamera(cameraName, stateCallback, null);
//
//                // try {
//                // if (!mCameraOpenCloseLock.tryAcquire(3000, TimeUnit.MILLISECONDS)) {
//                // throw new RuntimeException("Time out waiting to lock camera opening.");
//                // }
//                // cameraManager.openCamera(cameraName, stateCallback, mBackgroundHandler);
//                // } catch (InterruptedException e) {
//                // throw new RuntimeException("Interrupted while trying to lock camera
//                // opening.", e);
//                // }
//
//            } catch (SecurityException | NullPointerException | CameraAccessException ex) {
//                android.util.Log.e(TAG, "Error in openVideoInput: " + android.util.Log.getStackTraceString(ex));
//            }
//        }
//
//        private void startBackgroundThread() {
//            mBackgroundThread = new HandlerThread("CameraBackground");
//            mBackgroundThread.start();
//            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
//        }
//
//        /**
//         * Stops the background thread and its {@link Handler}.
//         */
//        private void stopBackgroundThread() {
//            if (mBackgroundThread != null) {
//                mBackgroundThread.quitSafely();
//                try {
//                    mBackgroundThread.join();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                mBackgroundThread = null;
//                mBackgroundHandler = null;
//            }
//        }
//
//        private void openVideoEncoder() {
//
//            try {
//
//                packet_id = 0;
//                MediaFormat format = MediaFormat.createVideoFormat(VideoCodec, VideoWidthsend, VideoHeightsend);
//                format.setInteger(MediaFormat.KEY_BIT_RATE, 400000 / Factor); // 1600000
//                format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
//                format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
//                format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);// 2
//
//                mVideoEncoder = MediaCodec.createEncoderByType(VideoCodec);
//                mVideoEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//                mVideoEncoder.setCallback(new MediaCodec.Callback() {
//                    @Override
//                    public void onInputBufferAvailable(@NotNull MediaCodec codec, int index) {
//                    }
//
//                    @Override
//                    public void onOutputBufferAvailable(@NotNull MediaCodec codec, int index,
//                                                        @NotNull MediaCodec.BufferInfo info) {
//                        ByteBuffer buf = codec.getOutputBuffer(index);
//                        //
//                        byte[] tmp = new byte[info.size];
//                        buf.get(tmp);
//                        // mVideoOutputThread.render( info.flags,tmp);
//                        if (info.flags == 2) {
//
//                            callInfoBeanObj.setCall_SenderConfig(tmp);
//                        } else {
//
//                            send_command(tmp, info.flags);
//
//                        }
//                        codec.releaseOutputBuffer(index, false);
//                    }
//
//                    @Override
//                    public void onError(@NotNull MediaCodec codec, @NotNull MediaCodec.CodecException e) {
//                        android.util.Log.w(TAG, "mVideoEncoder.onError: " + e);
//                    }
//
//                    @Override
//                    public void onOutputFormatChanged(@NotNull MediaCodec codec, @NotNull MediaFormat format) {
//                        // set camera change
//                        current_Camera = Integer.parseInt(mCamera.getId());
//                    }
//                });
//                Surface encoderSurface = mVideoEncoder.createInputSurface();
//                mVideoEncoder.start();
//
//                List<Surface> surfaces = new ArrayList<>();
//                surfaces.add(encoderSurface);
//
//                // Creating surface from texture to display my video
//                try {
//                    while (mPeerSurfaceTextureMyself == null)
//                        Thread.sleep(1);
//                } catch (InterruptedException ex) {
//                    return;
//                }
//                Surface mSurfaceMyself = new Surface(mPeerSurfaceTextureMyself);
//                surfaces.add(mSurfaceMyself);
//
//                CaptureRequest.Builder builder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
//                // builder.setRepeatingRequest(builder.build(), yourCaptureCallback,
//                // yourBackgroundHandler);
//                // builder.addTarget(encoderSurface);
//                for (Surface s : surfaces)
//                    builder.addTarget(s);
//
//                final CaptureRequest captureRequest = builder.build();
//
//                mCamera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
//
//                    @Override
//                    public void onConfigured(@NonNull CameraCaptureSession session) {
//                        if (null == mCamera) {
//                            return;
//                        }
//                        mCameraCaptureSession = session;
//                        try {
//
//                            setUpCaptureRequestBuilder(builder);
//                            HandlerThread thread = new HandlerThread("CameraPreview");
//                            thread.start();
//                            // session.setRepeatingRequest(captureRequest, null, mBackgroundHandler);
//                            session.setRepeatingRequest(builder.build(), null, mBackgroundHandler);
//                        } catch (CameraAccessException ex) {
//
//                            mCameraCaptureSession = null;
//                            android.util.Log.e(TAG, "Error in setRepeatingRequest: " + ex);
//                        } catch (IllegalStateException ex) {
//
//                            mCameraCaptureSession = null;
//                            android.util.Log.e(TAG, "Error in setRepeatingRequest: " + ex);
//                        }
//                    }
//
//                    @Override
//                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
//                        mCameraCaptureSession = null;
//                        android.util.Log.e(TAG, "onConfigureFailed");
//                    }
//
//                }, mBackgroundHandler);
//
//            } catch (IOException | CameraAccessException ex) {
//                android.util.Log.e(TAG, "Error in openVideoEncoder: " + android.util.Log.getStackTraceString(ex));
//            }
//        }
//
//        private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
//            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//        }
//    }
//
//    /* Video Decoder Config */
//    class VideoOutputThread extends HandlerThread {
//
//        Handler mHandler;
//        boolean mRunning = false;
//        MediaCodec mVideoDecoder;
//        Surface mSurface;
//        byte[] mCodecConfig;
//        boolean mIsKeyed = false;
//
//        public VideoOutputThread() {
//            super("Video Output");
//            super.start();
//            mHandler = new Handler(getLooper());
//        }
//
//        public void open() {
//
//            android.util.Log.w(TAG, "VideoOutputThread.open");
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    openVideoOutput();
//                }
//            });
//            mRunning = true;
//        }
//
//        public void close() {
//            android.util.Log.w(TAG, "VideoOutputThread.close");
//            if (mRunning) {
//                mRunning = false;
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (mSurface != null) {
//                            mSurface.release();
//                            mSurface = null;
//                        }
//                        if (mVideoDecoder != null) {
//                            mVideoDecoder.release();
//                            mVideoDecoder = null;
//                        }
//                        quit();
//                    }
//                });
//            }
//        }
//
//        public synchronized void pause() {
//            mIsKeyed = false;
//            mSurface = null;
//        }
//
//        public void reset() {
//            android.util.Log.w(TAG, "VideoOutputThread.reset");
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (mVideoDecoder != null) {
//                        android.util.Log.w(TAG, "+VideoOutputThread.reset");
//                        mVideoDecoder.release();
//                        mVideoDecoder = null;
//                        openVideoOutput();
//                        android.util.Log.w(TAG, "-VideoOutputThread.reset");
//                        android.util.Log.w(TAG, "-VideoOutputThread.reset");
//                    }
//                }
//            });
//        }
//
//        public void render(final int flags, final byte[] data) {
//            if ((flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//                mCodecConfig = data;
//                return;
//            }
//            if (mRunning)
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        renderInternal(flags, data);
//                    }
//                });
//        }
//
//        private void configUI(int sender_os) {
//
//            android.util.Log.w(TAG, "VideoOutputThread.configCodec");
//            try {
//                while (mPeerSurfaceTexture == null)
//                    Thread.sleep(1);
//            } catch (InterruptedException ex) {
//                return;
//            }
//
//            if (sender_os == ConstantsUtil.OS_VERSION_IOS) {
//                // int width=360;
//                // int height=480;
//                // int aw= mPeerView.getWidth();
//                // int ah= mPeerView.getHeight();
//                float sx = VideoWidthRecieve / (float) mPeerView.getWidth();
//                float sy = VideoHeightReceive / (float) mPeerView.getHeight();
//
//                //
//                if (VideoWidthRecieve > VideoHeightReceive == mPeerView.getWidth() > mPeerView.getHeight()) {
//                    float max = Math.max(sx, sy);
//                    // matrix.setScale(((sx / max) + 1), ((-sy / max) - 1), mPeerView.getWidth() *
//                    // 0.5f,
//                    // mPeerView.getHeight() * 0.5f);
//                    matrix.setScale(sx / max, sy / max, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
//
//                } else {
//                    float max = Math.max(VideoWidthRecieve / (float) mPeerView.getHeight(),
//                            VideoHeightReceive / (float) mPeerView.getWidth());
//                    // matrix.setScale(((sx / max) + 1), ((sy / max) + 1), mPeerView.getWidth() *
//                    // 0.5f,
//                    // mPeerView.getHeight() * 0.5f);
//                    matrix.setScale(sx / max, sy / max, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
//
//                }
//            } else if (sender_os == ConstantsUtil.OS_VERSION_DESKTOP) {
//                // int width=360;
//                // int height=480;
//                // int aw= mPeerView.getWidth();
//                // int ah= mPeerView.getHeight();
//                float sx = VideoWidthsend / (float) mPeerView.getWidth();
//                float sy = VideoHeightsend / (float) mPeerView.getHeight();
//
//                //
//                if (VideoWidthsend > VideoHeightsend == mPeerView.getWidth() > mPeerView.getHeight()) {
//                    float max = Math.max(sx, sy);
//                    // matrix.setScale(((sx / max) + 1), ((-sy / max) - 1), mPeerView.getWidth() *
//                    // 0.5f,
//                    // mPeerView.getHeight() * 0.5f);
//                    matrix.setScale(sx / max, sy / max, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
//
//                } else {
//                    float max = Math.max(VideoWidthsend / (float) mPeerView.getHeight(),
//                            VideoHeightsend / (float) mPeerView.getWidth());
//                    // matrix.setScale(((sx / max) + 1), ((sy / max) + 1), mPeerView.getWidth() *
//                    // 0.5f,
//                    // mPeerView.getHeight() * 0.5f);
//                    matrix.setScale(sx / max, sy / max, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
//
//                }
//            } else if (sender_os == ConstantsUtil.OS_VERSION_ANDROID) {
//
//                float sx = VideoWidthsend / (float) mPeerView.getWidth();
//                float sy = VideoHeightsend / (float) mPeerView.getHeight();
//                if (VideoWidthsend > VideoHeightsend == mPeerView.getWidth() > mPeerView.getHeight()) {
//                    float max = Math.max(sx, sy);
//                    // matrix.setScale(((sx / max) + 1), ((-sy / max) - 1), mPeerView.getWidth() *
//                    // 0.5f,
//                    // mPeerView.getHeight() * 0.5f);
//                    matrix.setScale(sx / max, -sy / max, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
//                } else {
//                    float max = Math.max(VideoWidthsend / (float) mPeerView.getHeight(),
//                            VideoHeightsend / (float) mPeerView.getWidth());
//                    // matrix.setScale(((sx / max) + 1), ((sy / max) + 1), mPeerView.getWidth() *
//                    // 0.5f,
//                    // mPeerView.getHeight() * 0.5f);
//                    matrix.setScale(sx / max, sy / max, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
//
//                    matrix.postRotate(degrees, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
//
//                }
//            } else if (sender_os == ConstantsUtil.OS_VERSION_ANDROID_BLACKPHONE) {
//
//                float sx = VideoWidthsend / (float) mPeerView.getWidth();
//                float sy = VideoHeightsend / (float) mPeerView.getHeight();
//                if (VideoWidthsend > VideoHeightsend == mPeerView.getWidth() > mPeerView.getHeight()) {
//                    float max = Math.max(sx, sy);
//                    // matrix.setScale(((sx / max) + 1), ((-sy / max) - 1), mPeerView.getWidth() *
//                    // 0.5f,
//                    // mPeerView.getHeight() * 0.5f);
//                    matrix.setScale(sx / max, -sy / max, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
//                } else {
//                    float max = Math.max(VideoWidthsend / (float) mPeerView.getHeight(),
//                            VideoHeightsend / (float) mPeerView.getWidth());
//                    // matrix.setScale(((sx / max) + 1), ((sy / max) + 1), mPeerView.getWidth() *
//                    // 0.5f,
//                    // mPeerView.getHeight() * 0.5f);
//                    matrix.setScale(sx / max, sy / max, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
//
//                    matrix.postRotate(degrees, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
//
//                }
//            }
//
//            mainActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//
//                    mPeerView.setTransform(matrix);
//
//                }
//            });
//        }
//
//        private void openVideoOutput() {
//            try {
//                mVideoDecoder = MediaCodec.createDecoderByType(VideoCodec);
//
//                if ((currentSenderOs == ConstantsUtil.OS_VERSION_IOS)
//                        || (currentSenderOs == ConstantsUtil.OS_VERSION_DESKTOP)) {
//                    degrees = 0.0f;
//                } else if (currentSenderOs == ConstantsUtil.OS_VERSION_ANDROID) {
//                    if (currentCameraSwitch == 0) {
//                        degrees = 270.0f;
//                    } else {
//                        degrees = 90.0f;
//                    }
//                } else if (currentSenderOs == ConstantsUtil.OS_VERSION_ANDROID_BLACKPHONE) {
//                    if (currentCameraSwitch == 0) {
//                        degrees = 90.0f;
//                    } else {
//                        degrees = 90.0f;
//                    }
//                }
//                configCodec();
//            } catch (IOException ex) {
//                android.util.Log.e(TAG, "Error in openVideoOutput: " + android.util.Log.getStackTraceString(ex));
//            }
//        }
//
//        private void configCodec() {
//
//            android.util.Log.w(TAG, "VideoOutputThread.configCodec");
//            try {
//                while (mPeerSurfaceTexture == null)
//                    Thread.sleep(1);
//            } catch (InterruptedException ex) {
//                return;
//            }
//            mSurface = new Surface(mPeerSurfaceTexture);
//            MediaFormat format = MediaFormat.createVideoFormat(VideoCodec, VideoWidthsend, VideoHeightsend);
//            format.setInteger(MediaFormat.KEY_ROTATION, getActivityOrientation(mainActivity));
//            mVideoDecoder.configure(format, mSurface, null, 0);
//            mVideoDecoder.start();
//
//            float sx = VideoWidthsend / (float) mPeerView.getWidth();
//            float sy = VideoHeightsend / (float) mPeerView.getHeight();
//            if (VideoWidthsend > VideoHeightsend == mPeerView.getWidth() > mPeerView.getHeight()) {
//                float max = Math.max(sx, sy);
//                matrix.setScale(sx / max, -sy / max, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
//            } else {
//                float max = Math.max(VideoWidthsend / (float) mPeerView.getHeight(),
//                        VideoHeightsend / (float) mPeerView.getWidth());
//                matrix.setScale(sx / max, sy / max, mPeerView.getWidth() * 0.5f, mPeerView.getHeight() * 0.5f);
//                // float ratio = (float) 1.33;
//                // matrix.setScale(ratio, ratio, mPeerView.getWidth() * 0.5f,
//                // mPeerView.getHeight() * 0.5f);
//                matrix.postRotate(CallInfoBean.getRotationDegree(), mPeerView.getWidth() * 0.5f,
//                        mPeerView.getHeight() * 0.5f);
//            }
//            mainActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mPeerView.setTransform(matrix);
//                }
//            });
//        }
//
//        private synchronized void renderInternal(int flags, byte[] data) {
//            // Log.w("Request", "" + data.length);
//            if (mSurface == null)
//                return;
//            if (mVideoDecoder != null) {
//                if (!mIsKeyed) {
//                    if ((flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0 && mCodecConfig != null) {
//                        mIsKeyed = true;
//                        int idx = mVideoDecoder.dequeueInputBuffer(-1);
//                        ByteBuffer buf = mVideoDecoder.getInputBuffer(idx);
//                        buf.clear();
//                        buf.put(mCodecConfig);
//                        mVideoDecoder.queueInputBuffer(idx, 0, mCodecConfig.length, 0,
//                                MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
//                    } else
//                        return;
//                }
//
//                int idx = mVideoDecoder.dequeueInputBuffer(-1);
//                ByteBuffer buf = mVideoDecoder.getInputBuffer(idx);
//                buf.clear();
//                buf.put(data);
//                mVideoDecoder.queueInputBuffer(idx, 0, data.length, 0, 0);
//
//                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
//                idx = mVideoDecoder.dequeueOutputBuffer(info, 10000);
//                if (idx >= 0) {
//                    mVideoDecoder.releaseOutputBuffer(idx, mSurface != null);
//                } else {
//                    call_maximized = 1;// call Minimized
//                    android.util.Log.w(TAG, "decoding fail: " + idx);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (call_maximized == 2)
//            this.videoControlEvent(ConstantsUtil.RESUME);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if (call_maximized == 2)
//            this.videoControlEvent(ConstantsUtil.PAUSE);
//    }
//
//    private void videoControlEvent(int event) {
//
//        // if (new PermissionProcessEngine(mainActivity, this)
//        // .showPermissionCheck(new String[]{Manifest.permission.CAMERA})) {
//        Log.e("call", "callViewMode videoControlEvent " + this.callViewMode + " " + event);
//        if (SingletonUtil.get().getCallEngine().isVideoCallSessionBegin) {
//            callInfoBeanObj = CallInfoBean.getCallInfoBeanObj();
//
//            if (this.callViewMode.equals(ConstantsUtil.VIEW_MODE_VIDEO_CALL_BOTH)) {
//
//                this.createandsendVideoControlCmd(callInfoBeanObj, false);// CALLHOLD
//                this.sendVideoControlInit(ConstantsUtil.VIDEOOFF, 4);
//                this.callViewMode = ConstantsUtil.VIEW_MODE_VIDEO_CALL_RECEIVE_ONLY;
//                if (event == ConstantsUtil.PAUSE) {
//                    // went to background no need to show view
//                } else {
//                    this.showCallLayout(this.callViewMode);
//                }
//
//                if (mVideoInputThread != null) {
//                    mVideoInputThread.interrupt();
//                    mVideoInputThread.close();
//                }
//                toggle.setVisibility(View.GONE);
//                video_call_stop_fab.setImageDrawable(
//                        ResourcesCompat.getDrawable(mainActivity, R.drawable.ic_video_call_log_organge));
//                video_call_stop_fab.setImageTintList(getResources().getColorStateList(R.color.white));
//            } else if ((this.callViewMode.equals(ConstantsUtil.VIEW_MODE_VIDEO_CALL_SEND_ONLY))) {
//
//                this.createandsendVideoControlCmd(callInfoBeanObj, false);// CALLHOLD
//                this.sendVideoControlInit(ConstantsUtil.VIDEOOFF, 4);
//                this.callViewMode = ConstantsUtil.VIEW_MODE_VOICE_ONLY_CALL_APP_TO_APP;
//                if (event == ConstantsUtil.PAUSE) {
//                    // went to background no need to show view
//
//                } else {
//                    this.showCallLayout(this.callViewMode);
//                }
//                mVideoInputThread.close();
//                video_call_stop_fab.setImageDrawable(
//                        ResourcesCompat.getDrawable(mainActivity, R.drawable.ic_video_call_off_organge));
//                video_call_stop_fab.setImageTintList(getResources().getColorStateList(R.color.white));
//            } else if ((this.callViewMode.equals(ConstantsUtil.VIEW_MODE_VIDEO_CALL_RECEIVE_ONLY))) {
//                this.callViewMode = ConstantsUtil.VIEW_MODE_VIDEO_CALL_BOTH;
//
//                toggle.setVisibility(View.VISIBLE);
//                if (mVideoInputThread != null) {
//                    mVideoInputThread.interrupt();
//                }
//                if (event == ConstantsUtil.PAUSE) {
//                    // went to background no need to show view
//                } else {
//                    this.createandsendVideoControlCmd(callInfoBeanObj, true);// CALLHOLD
//                    this.sendVideoControlInit(ConstantsUtil.VIDEOON, 3);
//
//                    this.showCallLayout(this.callViewMode);
//                    setupCapture();
//                }
//                // mVideoInputThread.close();
//
//                video_call_stop_fab.setImageDrawable(
//                        ResourcesCompat.getDrawable(mainActivity, R.drawable.ic_video_call_off_organge));
//                video_call_stop_fab.setImageTintList(getResources().getColorStateList(R.color.white));
//
//            } else if ((this.callViewMode.equals(ConstantsUtil.VIEW_MODE_VOICE_ONLY_CALL_APP_TO_APP))
//                    && (event == ConstantsUtil.RESUME)) {
//                //// this.callViewMode = ConstantsUtil.VIEW_MODE_VOICE_ONLY_CALL_APP_TO_APP;
//                this.showCallLayout(this.callViewMode);
//            }
//
//        } else {
//            if (event == ConstantsUtil.BUTTON_CLICKED)
//                Dialogs.showShortSnackbarMessage(rootView, "Video session not available.");
//
//        }
//        if (event == ConstantsUtil.BUTTON_CLICKED) {
//            if (autoButtonHideTimer != null)
//                autoButtonHideTimer.halt();
//            autoButtonHideTimer = new Timer(5000, this);
//            autoButtonHideTimer.start();
//        }
//
//        // }
//
//    }
//}
