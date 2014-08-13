package me.tabak.bottlerecognitiondemo.util;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.template.CubeShaders;
import com.qualcomm.vuforia.template.SampleApplicationSession;
import com.qualcomm.vuforia.template.SampleUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import me.tabak.bottlerecognitiondemo.RecognitionFragment;

public class RecognitionRenderer implements GLSurfaceView.Renderer {
    private static final float OBJECT_SCALE_FLOAT = 3.0f;
    private final RecognitionFragment mRecognitionFragment;
    SampleApplicationSession mVuforiaAppSession;
    private int mShaderProgramID;
    private int mVertexHandle;
    private int mNormalHandle;
    private int mTextureCoordHandle;
    private int mMvpMatrixHandle;
    private int mTexSampler2DHandle;

    public RecognitionRenderer(SampleApplicationSession session, RecognitionFragment fragment) {
        mVuforiaAppSession = session;
        mRecognitionFragment = fragment;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Call function to initialize rendering:
        initRendering();

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        mVuforiaAppSession.onSurfaceCreated();

    }

    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Call Vuforia function to handle render surface size changes:
        mVuforiaAppSession.onSurfaceChanged(width, height);
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
        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);

        mShaderProgramID = SampleUtils.createProgramFromShaderSrc(
                CubeShaders.CUBE_MESH_VERTEX_SHADER,
                CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        mVertexHandle = GLES20.glGetAttribLocation(mShaderProgramID, "vertexPosition");
        mNormalHandle = GLES20.glGetAttribLocation(mShaderProgramID, "vertexNormal");
        mTextureCoordHandle = GLES20.glGetAttribLocation(mShaderProgramID, "vertexTexCoord");
        mMvpMatrixHandle = GLES20.glGetUniformLocation(mShaderProgramID, "modelViewProjectionMatrix");
        mTexSampler2DHandle = GLES20.glGetUniformLocation(mShaderProgramID, "texSampler2D");
    }

    // The render function.
    private void renderFrame()
    {
        // Clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Get the state from Vuforia and mark the beginning of a rendering section
        State state = Renderer.getInstance().begin();

        // Explicitly render the Video Background
        Renderer.getInstance().drawVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW);  // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW);   // Back camera

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        Renderer.getInstance().end();
    }


    private void renderAugmentation(TrackableResult trackableResult)
    {
        Matrix44F modelViewMatrix_Vuforia = Tool
                .convertPose2GLMatrix(trackableResult.getPose());
        float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();

        int textureIndex = 0;

        // deal with the modelview and projection matrices
        float[] modelViewProjection = new float[16];
        Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f, OBJECT_SCALE_FLOAT);
        Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);
        Matrix.multiplyMM(modelViewProjection, 0, mVuforiaAppSession.getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

        GLES20.glEnableVertexAttribArray(mVertexHandle);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);

        // pass the model view matrix to the shader
        GLES20.glUniformMatrix4fv(mMvpMatrixHandle, 1, false,
                modelViewProjection, 0);


        // disable the enabled arrays
        GLES20.glDisableVertexAttribArray(mVertexHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);

        SampleUtils.checkGLError("CloudReco renderFrame");
    }

}
