package com.dotnet.nikit.rubikscube.Graphics;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import ar.Constants;

public class OverlayCube {
    // Buffer for vertex-array
    private FloatBuffer vertexBuffer;

    // Used to obtain center tile color information
 //   private StateModel stateModel;

    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 3;

    // number of bytes in a float
    private static final int BYTES_PER_FLOAT = 4;

    // number of total bytes in vertex stride: 12 in this case.
    private static final int VERTEX_STRIDE = COORDS_PER_VERTEX * BYTES_PER_FLOAT;

    // OpenGL Colors
    private static float [] opaqueRed =   { 1.0f, 0.0f, 0.0f, 1.0f};
    private static float [] opaqueGreen = { 0.0f, 1.0f, 0.0f, 1.0f};

    // Cube will be 2 units on a side by this definition and vertices table.
    private static final float unit = 1.01f / 3.0f;

    // Vertices that form wire frame.  Note, edge lines are duplicated, but this is easier to understand.
    private static float[] vertices = {

            //  X       Y        Z          X       Y         Z

            // FRONT
            -3*unit, -3*unit, +3*unit,   +3*unit, -3*unit, +3*unit,  // front-left -> front-right
            -3*unit, -1*unit, +3*unit,   +3*unit, -1*unit, +3*unit,  // front-left -> front-right
            -3*unit, +1*unit, +3*unit,   +3*unit, +1*unit, +3*unit,  // front-left -> front-right
            -3*unit, +3*unit, +3*unit,   +3*unit, +3*unit, +3*unit,  // front-left -> front-right

            -3*unit, -3*unit, +3*unit,   -3*unit, +3*unit, +3*unit,  // front-bottom -> front-top
            -1*unit, -3*unit, +3*unit,   -1*unit, +3*unit, +3*unit,  // front-bottom -> front-top
            +1*unit, -3*unit, +3*unit,   +1*unit, +3*unit, +3*unit,  // front-bottom -> front-top
            +3*unit, -3*unit, +3*unit,   +3*unit, +3*unit, +3*unit,  // front-bottom -> front-top

            // BACK
            -3*unit, -3*unit, -3*unit,   +3*unit, -3*unit, -3*unit,  // back-left -> back-right
            -3*unit, -1*unit, -3*unit,   +3*unit, -1*unit, -3*unit,  // back-left -> back-right
            -3*unit, +1*unit, -3*unit,   +3*unit, +1*unit, -3*unit,  // back-left -> back-right
            -3*unit, +3*unit, -3*unit,   +3*unit, +3*unit, -3*unit,  // back-left -> back-right

            -3*unit, -3*unit, -3*unit,   -3*unit, +3*unit, -3*unit,  // back-bottom -> back-top
            -1*unit, -3*unit, -3*unit,   -1*unit, +3*unit, -3*unit,  // back-bottom -> back-top
            +1*unit, -3*unit, -3*unit,   +1*unit, +3*unit, -3*unit,  // back-bottom -> back-top
            +3*unit, -3*unit, -3*unit,   +3*unit, +3*unit, -3*unit,  // back-bottom -> back-top

            // TOP
            -3*unit, +3*unit, -3*unit,   +3*unit, +3*unit, -3*unit,  // top-left -> top-right
            -3*unit, +3*unit, -1*unit,   +3*unit, +3*unit, -1*unit,  // top-left -> top-right
            -3*unit, +3*unit, +1*unit,   +3*unit, +3*unit, +1*unit,  // top-left -> top-right
            -3*unit, +3*unit, +3*unit,   +3*unit, +3*unit, +3*unit,  // top-left -> top-right

            -3*unit, +3*unit, -3*unit,   -3*unit, +3*unit, +3*unit,  // top-front -> top-back
            -1*unit, +3*unit, -3*unit,   -1*unit, +3*unit, +3*unit,  // top-front -> top-back
            +1*unit, +3*unit, -3*unit,   +1*unit, +3*unit, +3*unit,  // top-front -> top-back
            +3*unit, +3*unit, -3*unit,   +3*unit, +3*unit, +3*unit,  // top-front -> top-back

            // BOTTOM
            -3*unit, -3*unit, -3*unit,   +3*unit, -3*unit, -3*unit,  // bottom-left -> bottom-right
            -3*unit, -3*unit, -1*unit,   +3*unit, -3*unit, -1*unit,  // bottom-left -> bottom-right
            -3*unit, -3*unit, +1*unit,   +3*unit, -3*unit, +1*unit,  // bottom-left -> bottom-right
            -3*unit, -3*unit, +3*unit,   +3*unit, -3*unit, +3*unit,  // bottom-left -> bottom-right

            -3*unit, -3*unit, -3*unit,   -3*unit, -3*unit, +3*unit,  // bottom-front -> bottom-back
            -1*unit, -3*unit, -3*unit,   -1*unit, -3*unit, +3*unit,  // bottom-front -> bottom-back
            +1*unit, -3*unit, -3*unit,   +1*unit, -3*unit, +3*unit,  // bottom-front -> bottom-back
            +3*unit, -3*unit, -3*unit,   +3*unit, -3*unit, +3*unit,  // bottom-front -> bottom-back

            // RIGHT
            +3*unit, -3*unit, -3*unit,   +3*unit, -3*unit, +3*unit,  // right-front -> right back
            +3*unit, -1*unit, -3*unit,   +3*unit, -1*unit, +3*unit,  // right-front -> right back
            +3*unit, +1*unit, -3*unit,   +3*unit,  1*unit, +3*unit,  // right-front -> right back
            +3*unit, +3*unit, -3*unit,   +3*unit, +3*unit, +3*unit,  // right-front -> right back

            +3*unit, -3*unit, -3*unit,   +3*unit, +3*unit, -3*unit,  // right-bottom -> right top
            +3*unit, -3*unit, -1*unit,   +3*unit, +3*unit, -1*unit,  // right-bottom -> right top
            +3*unit, -3*unit, +1*unit,   +3*unit, +3*unit, +1*unit,  // right-bottom -> right top
            +3*unit, -3*unit, +3*unit,   +3*unit, +3*unit, +3*unit,  // right-bottom -> right top

            // LEFT
            -3*unit, -3*unit, -3*unit,   -3*unit, -3*unit, +3*unit,  // left-front -> left back
            -3*unit, -1*unit, -3*unit,   -3*unit, -1*unit, +3*unit,  // left-front -> left back
            -3*unit, +1*unit, -3*unit,   -3*unit,  1*unit, +3*unit,  // left-front -> left back
            -3*unit, +3*unit, -3*unit,   -3*unit, +3*unit, +3*unit,  // left-front -> left back

            -3*unit, -3*unit, -3*unit,   -3*unit, +3*unit, -3*unit,  // left-bottom -> left top
            -3*unit, -3*unit, -1*unit,   -3*unit, +3*unit, -1*unit,  // left-bottom -> left top
            -3*unit, -3*unit, +1*unit,   -3*unit, +3*unit, +1*unit,  // left-bottom -> left top
            -3*unit, -3*unit, +3*unit,   -3*unit, +3*unit, +3*unit,  // left-bottom -> left top
    };




    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     * =+= ?? Exactly which GPU memory is used?
     *
   //  * @param stateModel
    // * @param programID2
     */
    public OverlayCube(){//StateModel stateModel) {

    //    this.stateModel = stateModel;

        // Setup vertex-array buffer. Vertices in float. A float has 4 bytes
        // This reserves memory that GPU has direct access to (correct?). =+= HOW?? These are NOT openGL APIs!
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder()); // Use native byte order
        vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
        vertexBuffer.put(vertices);         // Copy data into buffer
        vertexBuffer.position(0);           // Rewind
    }



    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     * @param programID
     */
    public void draw(float[] mvpMatrix, int programID) {

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);   // =+= ?? Why ??

        // Add program to OpenGL environment
        GLES20.glUseProgram(programID);

        // get handle to vertex shader's vPosition member
        int vertexArrayID = GLES20.glGetAttribLocation(programID, "vPosition");

        // Enable a handle to the cube vertices
        GLES20.glEnableVertexAttribArray(vertexArrayID);

        // Prepare the cube coordinate data
        GLES20.glVertexAttribPointer(
                vertexArrayID,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                VERTEX_STRIDE,
                vertexBuffer);

        // get handle to fragment shader's vColor member
        int colorID = GLES20.glGetUniformLocation(programID, "vColor");

        // get handle to shape's transformation matrix
        int mvpMatrixID = GLES20.glGetUniformLocation(programID, "uMVPMatrix");
        Util.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mvpMatrixID, 1, false, mvpMatrix, 0);
        Util.checkGlError("glUniformMatrix4fv");

        // Select Red or Green lie color.
  /*      if(stateModel.gestureRecogniztionState == Constants.GestureRecogniztionStateEnum.STABLE ||
                stateModel.gestureRecogniztionState == Constants.GestureRecogniztionStateEnum.NEW_STABLE)*/
            // Green line color
            GLES20.glUniform4fv(colorID, 1, opaqueGreen, 0);
  //      else
            // Red line color
    //        GLES20.glUniform4fv(colorID, 1, opaqueRed, 0);

        // Five pixel width ?
        GLES20.glLineWidth(5.0f);

        // Draw Lines
        GLES20.glDrawArrays(
                GLES20.GL_LINES,
                0,
                vertices.length / COORDS_PER_VERTEX);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(vertexArrayID);

        GLES20.glDisable(GLES20.GL_CULL_FACE);
    }
}
