package com.dotnet.nikit.rubikscube;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import ar.Annotation;
import ar.ColorRecognition;
import ar.Constants;
import ar.MenuAndParams;
import ar.Profiler;
import ar.RubikFace;

public class ImageRecognizer {
    public Mat processFrame(Mat rgbaImage){
        /* **********************************************************************
         * **********************************************************************
         * Process to Grey Scale
         *
         * This algorithm finds highlights areas that are all of nearly
         * the same hue.  In particular, cube faces should be highlighted.
         */
        Mat greyscale_image = new Mat();
        Imgproc.cvtColor(rgbaImage, greyscale_image, Imgproc.COLOR_BGR2GRAY);

        return greyscale_image;
    }
}
