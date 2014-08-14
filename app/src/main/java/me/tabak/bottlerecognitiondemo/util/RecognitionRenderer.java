package me.tabak.bottlerecognitiondemo.util;

import android.opengl.GLSurfaceView;
import android.util.Log;

import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.template.SampleApplicationSession;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import me.tabak.bottlerecognitiondemo.RecognitionFragment;

public class RecognitionRenderer implements GLSurfaceView.Renderer {
    SampleApplicationSession mVuforiaAppSession;

    public RecognitionRenderer(SampleApplicationSession session, RecognitionFragment fragment) {
        mVuforiaAppSession = session;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(RecognitionRenderer.class.getName(), "onSurfaceCreated Start");
        // Call function to initialize rendering:
        initRendering();

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        mVuforiaAppSession.onSurfaceCreated();
        Log.d(RecognitionRenderer.class.getName(), "onSurfaceCreated Complete");
    }

    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Call Vuforia function to handle render surface size changes:
        Log.d(RecognitionRenderer.class.getName(), "onSurfaceChanged Start");
        mVuforiaAppSession.onSurfaceChanged(width, height);
        Log.d(RecognitionRenderer.class.getName(), "onSurfaceChanged Complete");
    }

    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        // Call our function to render content
        renderFrame();
    }


    // Function for initializing the renderer.
    private void initRendering()
    {
    }

    // The render function.
    private void renderFrame()
    {
        Renderer.getInstance().begin();

        // Explicitly render the Video Background
        Renderer.getInstance().drawVideoBackground();

        Renderer.getInstance().end();
    }
}
