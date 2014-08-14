package me.tabak.bottlerecognitiondemo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.TargetFinder;
import com.qualcomm.vuforia.TargetSearchResult;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.template.SampleApplicationControl;
import com.qualcomm.vuforia.template.SampleApplicationException;
import com.qualcomm.vuforia.template.SampleApplicationGLView;
import com.qualcomm.vuforia.template.SampleApplicationSession;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import me.tabak.bottlerecognitiondemo.model.Metadata;
import me.tabak.bottlerecognitiondemo.util.RecognitionRenderer;
import me.tabak.bottlerecognitiondemo.util.VuforiaHelper;

public class RecognitionFragment extends Fragment implements SampleApplicationControl {
    private static final String CAMERA_HIDDEN = "camera hidden";
    SampleApplicationSession mVuforiaAppSession;

    // Our OpenGL view:
    private SampleApplicationGLView mGlView;

    // Our renderer:
    private GLSurfaceView.Renderer mRenderer;

    boolean mFinderStarted = false;

    private static final String kAccessKey = "dba4a868311fb6d786017d2d31d359be4de62565";
    private static final String kSecretKey = "d9d032e20319f202307d02d9dff0ad1028e6a717";

    // Error message handling:
    private int mLastErrorCode = 0;
    private int mInitErrorCode = 0;
    private boolean mFinishActivityOnError;

    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;
    private VuforiaHelper mVuforiaHelper = new VuforiaHelper();

    boolean mIsDroidDevice = false;

    private View mProgressBar;
    private ViewGroup mContainer;
    private RecognitionActivity mRecognitionActivity;
    private MenuItem mCameraMenuItem;
    private ImageView mImageView;
    private boolean mCameraHidden;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(RecognitionFragment.class.getName(), "onCreate");
        super.onCreate(savedInstanceState);
        mRecognitionActivity = (RecognitionActivity) getActivity();
        mIsDroidDevice = Build.MODEL.toLowerCase().startsWith("droid");
        mVuforiaAppSession = new SampleApplicationSession(this);
        mVuforiaAppSession.initAR(getActivity(), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mCameraHidden = savedInstanceState.getBoolean(CAMERA_HIDDEN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recognition, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = view.findViewById(R.id.progressbar);
        mContainer = (ViewGroup) view.findViewById(R.id.container);
        mImageView = (ImageView) view.findViewById(R.id.wine_logo_imageview);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_recognition_menu, menu);
        mCameraMenuItem = menu.findItem(R.id.menu_camera);
        mCameraMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    mVuforiaAppSession.resumeAR();
                } catch (SampleApplicationException e) {
                    Log.e(RecognitionFragment.class.getName(), null, e);
                }
                mImageView.animate().alpha(0).setDuration(500).start();
                mCameraHidden = false;
                getActivity().invalidateOptionsMenu();
                mRecognitionActivity.onReset();
                return true;
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        mCameraMenuItem.setVisible(mCameraHidden);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onResume() {
        super.onResume();

        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice)
        {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        try {
            mVuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e) {
            Log.e(RecognitionFragment.class.getName(), null, e);
        }

        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
    }

    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        super.onConfigurationChanged(config);
        mVuforiaAppSession.onConfigurationChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            mVuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e) {
            Log.e(RecognitionFragment.class.getName(), null, e);
        }
        CameraDevice.getInstance().deinit();

        // Pauses the OpenGLView
        if (mGlView != null) {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(CAMERA_HIDDEN, mCameraHidden);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(RecognitionFragment.class.getName(), "onDestroy");
        try {
            mVuforiaAppSession.stopAR();
        } catch (SampleApplicationException e) {
            Log.e(RecognitionFragment.class.getName(), null, e);
        }
        Vuforia.deinit();
        System.gc();
    }

    // Initializes AR application components.
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        // Initialize the GLView with proper flags
        mGlView = new SampleApplicationGLView(getActivity());
        mGlView.init(translucent, depthSize, stencilSize);

        // Setups the Renderer of the GLView
        mRenderer = new RecognitionRenderer(mVuforiaAppSession, this);
        mGlView.setRenderer(mRenderer);
        Log.d(RecognitionFragment.class.getName(), "AR Initialized");
    }

    // Shows error messages as System dialogs
    public void showErrorMessage(int errorCode, double errorTime, boolean finishActivityOnError)
    {
        if (errorTime < (5.0) || errorCode == mLastErrorCode)
            return;

        mLastErrorCode = errorCode;
        mFinishActivityOnError = finishActivityOnError;

        if (isAdded()) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (mErrorDialog != null) {
                        mErrorDialog.dismiss();
                    }

                    // Generates an Alert Dialog to show the error message
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder
                            .setMessage(mVuforiaHelper.getStatusDescription(getActivity(), mLastErrorCode))
                            .setTitle(
                                    mVuforiaHelper.getStatusTitle(getActivity(), mLastErrorCode))
                            .setCancelable(false)
                            .setIcon(0)
                            .setPositiveButton(getString(R.string.button_ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            if (mFinishActivityOnError) {
                                                getActivity().finish();
                                            } else {
                                                dialog.dismiss();
                                            }
                                        }
                                    });

                    mErrorDialog = builder.create();
                    mErrorDialog.show();
                }
            });
        }
    }

    public void startFinderIfStopped()
    {
        if(!mFinderStarted)
        {
            mFinderStarted = true;

            // Get the image tracker:
            TrackerManager trackerManager = TrackerManager.getInstance();
            ImageTracker imageTracker = (ImageTracker) trackerManager
                    .getTracker(ImageTracker.getClassType());

            // Initialize target finder:
            TargetFinder targetFinder = imageTracker.getTargetFinder();

            targetFinder.clearTrackables();
            targetFinder.startRecognition();
        }
    }


    @Override
    public boolean doLoadTrackersData()
    {
        Log.d(RecognitionFragment.class.getName(), "doLoadTrackersData");

        // Get the image tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) trackerManager
                .getTracker(ImageTracker.getClassType());

        // Initialize target finder:
        TargetFinder targetFinder = imageTracker.getTargetFinder();

        // Start initialization:
        if (targetFinder.startInit(kAccessKey, kSecretKey))
        {
            targetFinder.waitUntilInitFinished();
        }

        int resultCode = targetFinder.getInitState();
        if (resultCode != TargetFinder.INIT_SUCCESS)
        {
            if(resultCode == TargetFinder.INIT_ERROR_NO_NETWORK_CONNECTION)
            {
                mInitErrorCode = VuforiaHelper.UPDATE_ERROR_NO_NETWORK_CONNECTION;
            }
            else
            {
                mInitErrorCode = VuforiaHelper.UPDATE_ERROR_SERVICE_NOT_AVAILABLE;
            }

            Log.e(RecognitionFragment.class.getName(), "Failed to initialize target finder.");
            return false;
        }

        // Use the following calls if you would like to customize the color of
        // the UI
        // targetFinder->setUIScanlineColor(1.0, 0.0, 0.0);
        // targetFinder->setUIPointColor(0.0, 0.0, 1.0);

        return true;
    }


    @Override
    public boolean doUnloadTrackersData()
    {
        return true;
    }


    @Override
    public void onInitARDone(SampleApplicationException exception)
    {
        if (exception == null)
        {
            initApplicationAR();

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            mContainer.addView(mGlView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            // Start the camera:
            try
            {
                mVuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
                startFinderIfStopped();
            } catch (SampleApplicationException e) {
                Log.e(RecognitionFragment.class.getName(), e.getString());
            }

            // Hides the Loading Dialog
            mProgressBar.setVisibility(View.GONE);

            mContainer.setBackgroundColor(Color.TRANSPARENT);

        } else {
            Log.e(RecognitionFragment.class.getName(), exception.getString());
            if(mInitErrorCode != 0)
            {
                showErrorMessage(mInitErrorCode, 10, true);
            }
            else
            {
                getActivity().finish();
            }
        }
    }


    @Override
    public void onQCARUpdate(State state)
    {
        // Get the tracker manager:
        TrackerManager trackerManager = TrackerManager.getInstance();

        // Get the image tracker:
        ImageTracker imageTracker = (ImageTracker) trackerManager
                .getTracker(ImageTracker.getClassType());

        // Get the target finder:
        TargetFinder finder = imageTracker.getTargetFinder();

        // Check if there are new results available:
        final int statusCode = finder.updateSearchResults();

        // Show a message if we encountered an error:
        if (statusCode < 0)
        {
            boolean closeAppAfterError = (
                    statusCode == VuforiaHelper.UPDATE_ERROR_NO_NETWORK_CONNECTION ||
                            statusCode == VuforiaHelper.UPDATE_ERROR_SERVICE_NOT_AVAILABLE);

            showErrorMessage(statusCode, state.getFrame().getTimeStamp(), closeAppAfterError);

        } else if (statusCode == TargetFinder.UPDATE_RESULTS_AVAILABLE) {
            // Process new search results
            if (finder.getResultCount() > 0)
            {
                TargetSearchResult result = finder.getResult(0);
                Metadata metadata = new Gson().fromJson(result.getMetaData(), Metadata.class);

                mRecognitionActivity.onWineRecognized(metadata);
                Picasso.with(getActivity())
                        .load(metadata.getImageUrl())
                        .noFade()
                        .into(mImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                try {
                                    mVuforiaAppSession.pauseAR();
                                } catch (SampleApplicationException e) {
                                    Log.e(RecognitionFragment.class.getName(), null, e);
                                }
                                mImageView.animate().alpha(1).setDuration(500).start();
                                mCameraHidden = true;
                                getActivity().invalidateOptionsMenu();
                            }

                            @Override
                            public void onError() { }
                        });
            }
        }
    }


    @Override
    public boolean doInitTrackers()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Indicate if the trackers were initialized correctly
        boolean result = true;

        tracker = tManager.initTracker(ImageTracker.getClassType());
        if (tracker == null)
        {
            Log.e(RecognitionFragment.class.getName(), "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(RecognitionFragment.class.getName(), "Tracker successfully initialized");
        }

        return result;
    }


    @Override
    public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        boolean result = true;

        // Start the tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) trackerManager
                .getTracker(ImageTracker.getClassType());
        imageTracker.start();

        // Start cloud based recognition if we are in scanning mode:
        TargetFinder targetFinder = imageTracker.getTargetFinder();
        targetFinder.startRecognition();
        mFinderStarted = true;

        return result;
    }


    @Override
    public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        TrackerManager trackerManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) trackerManager
                .getTracker(ImageTracker.getClassType());

        if(imageTracker != null)
        {
            imageTracker.stop();

            // Stop cloud based recognition:
            TargetFinder targetFinder = imageTracker.getTargetFinder();
            targetFinder.stop();
            mFinderStarted = false;

            // Clears the trackables
            targetFinder.clearTrackables();
        }
        else
        {
            result = false;
        }

        return result;
    }


    @Override
    public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ImageTracker.getClassType());

        return result;
    }
}
