package com.dotnet.nikit.rubikscube.Graphics;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Renderer implements GLSurfaceView.Renderer{
    private final String vertexShaderCode =

            // This matrix member variable provides a hook to manipulate
            // the coordinates of objects that use this vertex shader.
            "uniform mat4 uMVPMatrix;   \n" +

                    "attribute vec4 vPosition;  \n" +
                    "void main(){               \n" +
                    // The matrix must be included as part of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    " gl_Position = uMVPMatrix * vPosition; \n" +

                    "}  \n";

    private final String fragmentShaderCode =
            "precision mediump float; \n" +
                    "uniform vec4 vColor;     \n" +
                    "void main(){             \n" +
                    "gl_FragColor = vColor;   \n" +
                    "} \n";

    // Projection Matrix:  basically defines a Frustum
    private float[] mProjectionMatrix = new float[16];

    Cube cube;
    // Android Application Context
    private Context context;

    int programId;

    public Renderer(Context context){
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        // Compile shaders
        int vertexShaderID = Tool.compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShaderID = Tool.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // Link the shaders together into a final GPU executable.
        programId = Tool.linkProgram(vertexShaderID, fragmentShaderID);

        // Clear Color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Create the GL pilot cube
        cube = new Cube();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);

        // make adjustments for screen ratio
        float ratio = (float) width / height;
        gl.glMatrixMode(GL10.GL_PROJECTION);        // set matrix to projection mode
        gl.glLoadIdentity();                        // reset the matrix to its default state
        gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);  // apply the projection matrix
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // View Matrix
        final float[] viewMatrix = new float[16];

        // Projection View Matrix
        final float[] pvMatrix = new float[16];

        // Model View Projection Matrix
        final float[] mvpMatrix = new float[16];

        // Enable Depth Testing and Occulsion
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix), a 4x4 matrix is created.
        Matrix.setLookAtM(viewMatrix, 0,
                0, 0, 0,    // Camera Location
                0f, 0f, -1f,   // Camera points down Z axis.
                0f, 1.0f, 0.0f);  // Specifies rotation of camera: in this case, standard upwards orientation.

        // Calculate the projection and view transformation
        // pvMatrix = mProjectionMatrix * viewMatrix
        Matrix.multiplyMM(pvMatrix, 0, mProjectionMatrix, 0, viewMatrix, 0);

        System.arraycopy(pvMatrix, 0, mvpMatrix, 0, pvMatrix.length);

        // Instead of using pose esitmator coordinates, instead position cube at
        // fix location.  We really just desire to observe rotation.
        Matrix.translateM(mvpMatrix, 0, -6.0f, 0.0f, -15.0f);

       // cube.draw();
    }
}
