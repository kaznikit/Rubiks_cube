package com.dotnet.nikit.rubikscube;

import org.opencv.core.Core;
import org.opencv.core.Scalar;

public class Constants {
    public enum ColorTileEnum {
        //                     Target Measurement Colors                   Graphics (both CV and GL)
        RED   ( true, 'R', new Scalar(220.0,   20.0,  30.0), new float [] {1.0f, 0.0f, 0.0f, 1.0f}),
        ORANGE( true, 'O', new Scalar(240.0,   80.0,   0.0), new float [] {0.9f, 0.4f, 0.0f, 1.0f}),
        YELLOW( true, 'Y', new Scalar(230.0,  230.0,  20.0), new float [] {0.9f, 0.9f, 0.2f, 1.0f}),
        GREEN ( true, 'G', new Scalar(0.0,    140.0,  60.0), new float [] {0.0f, 1.0f, 0.0f, 1.0f}),
        BLUE  ( true, 'B', new Scalar(0.0,     60.0, 220.0), new float [] {0.2f, 0.2f, 1.0f, 1.0f}),
        WHITE ( true, 'W', new Scalar(225.0,  225.0, 225.0), new float [] {1.0f, 1.0f, 1.0f, 1.0f}),

        BLACK (false, 'K', new Scalar(  0.0,    0.0,   0.0) ),
        GREY  (false, 'E', new Scalar( 50.0,   50.0,  50.0) );


        // A Rubik Color
        public final boolean isRubikColor;

        // Measuring and Decision Testing in OpenCV
        public final Scalar rubikColor;

        // Rendering in OpenCV
        public final Scalar cvColor;

        // Rendering in OpenGL
        public final float [] glColor;

        // Single letter character
        public final char symbol;

        /**
         * Color Tile Enum Constructor
         *
         * Accept an Rubik Color and derive OpenCV and OpenGL colors from this.
         *
         * @param isRubik
         * @param symbol
         * @param rubikColor
         */
        private ColorTileEnum(boolean isRubik, char symbol, Scalar rubikColor) {
            this.isRubikColor = isRubik;
            this.cvColor = rubikColor;
            this.rubikColor = rubikColor;
            this.glColor =  new float [] {(float)rubikColor.val[0] / 255f, (float)rubikColor.val[1] / 255f, (float)rubikColor.val[2] / 255f, 1.0f};
            this.symbol = symbol;
        }


        /**
         * Color Tile Enum Constructor
         *
         * Accept an Rubik Color and an OpenGL color.  Derive OpenCV color from OpenGL color.
         *
         * @param isRubik
         * @param symbol
         * @param rubikColor
         */
        private ColorTileEnum(boolean isRubik, char symbol, Scalar rubikColor, float[] renderColor) {
            this.isRubikColor = isRubik;
            this.cvColor = new Scalar(renderColor[0] * 255, renderColor[1] * 255, renderColor[2] * 255);
            this.rubikColor = rubikColor;
            this.glColor =  renderColor;
            this.symbol = symbol;
        }
    }

    // Conventional Rubik Face nomenclature
    public enum FaceNameEnum { UP, DOWN, LEFT, RIGHT, FRONT, BACK};

    public final static int FontFace = Core.FONT_HERSHEY_PLAIN;

}
