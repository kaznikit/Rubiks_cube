package com.dotnet.nikit.rubikscube.Graphics;

import android.opengl.GLES20;
import android.util.Log;

import com.dotnet.nikit.rubikscube.Constants;
import com.dotnet.nikit.rubikscube.Tile;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glShaderSource;

public class Tool {
    //compile shader
    public static int compileShader(int type, String shaderCode) {
        // Create a new shader object.
        final int shaderObjectId = glCreateShader(type);

        if (shaderObjectId == 0) {
            //if (Constants.LOGGER) {
            //   Log.w(Constants.TAG_OPENGL, "Could not create new shader.");
            // }

            return 0;
        }
        // Pass in the shader source.
        glShaderSource(shaderObjectId, shaderCode);

        // Compile the shader.
        glCompileShader(shaderObjectId);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);

  /*      if (Constants.LOGGER) {
            // Print the shader info log to the Android log output.
            Log.v(Constants.TAG_OPENGL, "Results of compiling source:" + "\n" + shaderCode + "\n:"
                    + glGetShaderInfoLog(shaderObjectId));
        }
*/
        // Verify the compile status.
        if (compileStatus[0] == 0) {
            // If it failed, delete the shader object.
            glDeleteShader(shaderObjectId);

/*            if (Constants.LOGGER) {
                Log.w(Constants.TAG_OPENGL, "Compilation of shader failed.");
            }*/

            return 0;
        }

        // Return the shader object ID.
        return shaderObjectId;
    }

    /**
     * Link together shaders and for a final program.
     */
    public static int linkProgram(int ... shaderIDs) {

        // create empty OpenGL Program
        int programID = GLES20.glCreateProgram();

        // add the shader to program
        for( int shaderID : shaderIDs)
            GLES20.glAttachShader(programID, shaderID);

        // create OpenGL program executables
        GLES20.glLinkProgram(programID);

        // Get the link status.
        final int[] linkStatus = new int[1];
        glGetProgramiv(programID, GL_LINK_STATUS, linkStatus, 0);

        // Print the program info log to the Android log output.
        //   if (Constants.LOGGER)
        //       Log.v(Constants.TAG_OPENGL, "Results of linking program:\n" + glGetProgramInfoLog(programID));

        // Verify the link status.
        if (linkStatus[0] == 0) {

            // If it failed, delete the program object.
            glDeleteProgram(programID);

/*            if (Constants.LOGGER) {
                Log.e(Constants.TAG_OPENGL, "Linking of program failed.");
            }*/
        }

        return programID;
    }

    /**
     * Get YUV from RGB
     */
    public static double[] getYUVfromRGB(double [] rgb) {

        if(rgb == null)  {
            return new double[]{0, 0, 0 , 0};
        }
        double [] yuv = new double [4];
        yuv[0] =  0.229 * rgb[0]  +   0.587 * rgb[1]  +  0.114 * rgb[2];
        yuv[1] = -0.147 * rgb[0]  +  -0.289 * rgb[1]  +  0.436 * rgb[2];
        yuv[2] =  0.615 * rgb[0]  +  -0.515 * rgb[1]  + -0.100 * rgb[2];
        return yuv;
    }

    public static Tile[][] getTileArrayRotated180(Tile[][] arg) {
        return getTileArrayRotatedClockwise( getTileArrayRotatedClockwise( arg));
    }

    public static Tile[][] getTileArrayRotatedCounterClockwise(Tile[][] arg) {
        return getTileArrayRotatedClockwise( getTileArrayRotatedClockwise( getTileArrayRotatedClockwise( arg)));
    }

    public static Tile[][] getTileArrayRotatedClockwise(Tile[][] arg) {
        //         n -------------->
        //   m     0-0    1-0    2-0
        //   |     0-1    1-1    2-1
        //   v     0-2    1-2    2-2
        Tile[][] result = new Tile[3][3];
        result[1][1] = arg[1][1];
        result[2][0] = arg[0][0];
        result[2][1] = arg[1][0];
        result[2][2] = arg[2][0];
        result[1][2] = arg[2][1];
        result[0][2] = arg[2][2];
        result[0][1] = arg[1][2];
        result[0][0] = arg[0][2];
        result[1][0] = arg[0][1];

        return result;
    }
}
