package com.dotnet.nikit.rubikscube.Graphics;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Cube {
    public enum Transparency { OPAQUE, TRANSLUCENT, TRANSPARENT, WIREFRAME };

    // Buffer for vertex-array
    private FloatBuffer vertexBuffer;

    // number of cube faces: of course it is 6!
    private static final int NUM_FACES = 6;

    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 3;

    // number of bytes in a float
    private static final int BYTES_PER_FLOAT = 4;

    // number of total bytes in vertex stride: 12 in this case.
    private static final int VERTEX_STRIDE = COORDS_PER_VERTEX * BYTES_PER_FLOAT;

    // A completely transparent OpenGL color
    private static float [] transparentBlack = { 0f, 0f, 0f, 0f};

    // A grey translucent OpenGL color
    private static float [] translusentGrey = { 0.5f, 0.5f, 0.5f, 0.5f};

    // An opaque white OpenGL color
    private static float [] opaqueWhite = { 1.0f, 1.0f, 1.0f, 1.0f};

    private static float[] vertices = {  // Vertices of the 6 faces
            // FRONT
            -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
            1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
            -1.0f,  1.0f,  1.0f,  // 2. left-top-front
            1.0f,  1.0f,  1.0f,  // 3. right-top-front
            // BACK
            1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
            -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
            1.0f,  1.0f, -1.0f,  // 7. right-top-back
            -1.0f,  1.0f, -1.0f,  // 5. left-top-back
            // LEFT
            -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
            -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
            -1.0f,  1.0f, -1.0f,  // 5. left-top-back
            -1.0f,  1.0f,  1.0f,  // 2. left-top-front
            // RIGHT
            1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
            1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
            1.0f,  1.0f,  1.0f,  // 3. right-top-front
            1.0f,  1.0f, -1.0f,  // 7. right-top-back
            // UP
            -1.0f,  1.0f,  1.0f,  // 2. left-top-front
            1.0f,  1.0f,  1.0f,  // 3. right-top-front
            -1.0f,  1.0f, -1.0f,  // 5. left-top-back
            1.0f,  1.0f, -1.0f,  // 7. right-top-back
            // DOWN
            -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
            1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
            -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
            1.0f, -1.0f,  1.0f   // 1. right-bottom-front
    };



    public Cube() {
        // Setup vertex-array buffer. Vertices in float. A float has 4 bytes
        // This reserves memory that GPU has direct access to (correct?).
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder()); // Use native byte order
        vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
        vertexBuffer.put(vertices);         // Copy data into buffer
        vertexBuffer.position(0);           // Rewind
    }


    public void draw(GL10 gl) {

        GLES20.glEnable(GLES20.GL_CULL_FACE);

        //cule back elements
        GLES20.glCullFace(GLES20.GL_BACK);

        // Add program to OpenGL environment
 //      GLES20.glUseProgram(programID);

        // get handle to vertex shader's vPosition member
    //    int vertexArrayID = GLES20.glGetAttribLocation(programID, "vPosition");

        // Enable a handle to the cube vertices
    //    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
    //   gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

 /*       // get handle to fragment shader's vColor member
        int colorID = GLES20.glGetUniformLocation(programID, "vColor");

        // get handle to shape's transformation matrix
        int mvpMatrixID = GLES20.glGetUniformLocation(programID, "uMVPMatrix");*/

        // Apply the projection and view transformation
        //    GLES20.glUniformMatrix4fv(mvpMatrixID, 1, false, mvpMatrix, 0);

        // Render all the faces
    //    for (int faceIndex = 0; faceIndex < NUM_FACES; faceIndex++) {

            // Specify color
            /*switch (transparencyMode) {

                case TRANSPARENT:
                    GLES20.glUniform4fv(colorID, 1, transparentBlack, 0);
                    break;

                case OPAQUE:

                    // Get Face
                    RubikFace rubikFace = null;
                    switch(faceIndex) {
                        case 0: rubikFace = stateModel.nameRubikFaceMap.get( FaceNameEnum.FRONT); break;
                        case 1: rubikFace = stateModel.nameRubikFaceMap.get( FaceNameEnum.BACK);  break;
                        case 2: rubikFace = stateModel.nameRubikFaceMap.get( FaceNameEnum.LEFT);  break;
                        case 3: rubikFace = stateModel.nameRubikFaceMap.get( FaceNameEnum.RIGHT); break;
                        case 4: rubikFace = stateModel.nameRubikFaceMap.get( FaceNameEnum.UP);    break;
                        case 5: rubikFace = stateModel.nameRubikFaceMap.get( FaceNameEnum.DOWN);  break;
                    }

                    // Color in GL format
                    float [] colorGL = (rubikFace != null && rubikFace.observedTileArray != null && rubikFace.observedTileArray[1][1] != null) ?
                            rubikFace.observedTileArray[1][1].glColor : Constants.ColorTileEnum.GREY.glColor;

                    // Render
                    GLES20.glUniform4fv(colorID, 1, colorGL, 0);
                    break;

                case TRANSLUCENT:
                    GLES20.glUniform4fv(colorID, 1, translusentGrey, 0);
                    break;

                case WIREFRAME:
                    GLES20.glUniform4fv(colorID, 1, opaqueWhite, 0);
                    break;
            }*/
            //GLES20.glUniform4fv(colorID, 1, translusentGrey, 0);

            // Draw Triangles
            GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0 * BYTES_PER_FLOAT, BYTES_PER_FLOAT);


            // Disable vertex array
        //    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

            GLES20.glDisable(GLES20.GL_CULL_FACE);
      //  }
    }
}
