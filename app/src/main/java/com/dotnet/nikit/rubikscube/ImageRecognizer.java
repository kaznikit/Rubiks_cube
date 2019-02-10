package com.dotnet.nikit.rubikscube;

import android.util.Log;

import com.dotnet.nikit.rubikscube.Graphics.ARDrawing;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/*
** class for recognizing rubik faces and color of tiles
** algorithm consist of few steps:
** determine boundaries of the face and tiles, using gray, blur and canny filter
** then dilate boundaries and after that we can get coordinates of each tile
** find the center of tile and get it's color
* */
public class ImageRecognizer {
    Mat grayscale_image;
    Mat blur_image;
    Mat canny_image;
    Mat dilate_image;
    Mat gray_image;
    Mat rgba_gray_image;
    List<MatOfPoint> contours;
    List<Rectangle> polygonList;
    List<Rectangle> rectangleList;
    ARDrawing dr;

    public ImageRecognizer(){
        grayscale_image = new Mat();
        blur_image = new Mat();
        canny_image = new Mat();
        dilate_image = new Mat();
        contours = new LinkedList<MatOfPoint>();
        polygonList = new LinkedList<Rectangle>();
        rectangleList = new LinkedList<Rectangle>();
        dr = new ARDrawing();
    }

    public Mat processFrame(Mat rgbaImage) {

        RubikFace rubikFace = new RubikFace();
        /* **********************************************************************
         * Process to Grey Scale
         *
         * This algorithm finds highlights areas that are all of nearly
         * the same hue.  In particular, cube faces should be highlighted.
         */
        Imgproc.cvtColor(rgbaImage, grayscale_image, Imgproc.COLOR_BGR2GRAY);

        /* **********************************************************************
         * Gaussian Filter Blur prevents getting a lot of false hits
         */

        int kernelSize = 7;
        kernelSize = kernelSize % 2 == 0 ? kernelSize + 1 : kernelSize;  // make odd
        Imgproc.GaussianBlur(grayscale_image, blur_image, new Size(kernelSize, kernelSize), -1, -1);
        grayscale_image.release();

        /* **********************************************************************
         * Canny Edge Detection
         */
        Imgproc.Canny(blur_image, canny_image, 50, 100,
                3,         // Sobel Aperture size.  This seems to be typically value used in the literature: i.e., a 3x3 Sobel Matrix.
                false);    // use cheap gradient calculation: norm =|dI/dx|+|dI/dy|
        blur_image.release();

        /* **********************************************************************
         * Dilation Image Process
         */
        Imgproc.dilate(canny_image, dilate_image, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10, 10)));
        canny_image.release();

        /* **********************************************************************
         * **********************************************************************
         * Contour Generation
         */

        Mat heirarchy = new Mat();
        Imgproc.findContours(dilate_image, contours, heirarchy,
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE); // Note: tried other TC89 options, but no significant change or improvement on cpu time.
        dilate_image.release();
        heirarchy.release();

        // Create gray scale image but in RGB format, and then added yellow colored contours on top.
        gray_image = new Mat(rgbaImage.size(), CvType.CV_8UC4);
        rgba_gray_image = new Mat(rgbaImage.size(), CvType.CV_8UC4);
        Imgproc.cvtColor(rgbaImage, gray_image, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(gray_image, rgba_gray_image, Imgproc.COLOR_GRAY2BGRA, 3);
        Imgproc.drawContours(rgba_gray_image, contours, -1, Constants.ColorTileEnum.YELLOW.cvColor, 3);
        gray_image.release();

        /*//* **********************************************************************
         * Polygon Detection
         */
        for (MatOfPoint contour : contours) {

            // Keep only counter clockwise contours.  A clockwise contour is reported as a negative number.
            double contourArea = Imgproc.contourArea(contour, true);
            if (contourArea < 0.0)
                continue;

            // Keep only reasonable area contours
            if (contourArea < 100)
                continue;

            // Floating, instead of Double, for some reason required for approximate polygon detection algorithm.
            MatOfPoint2f contour2f = new MatOfPoint2f();
            MatOfPoint2f polygone2f = new MatOfPoint2f();
            MatOfPoint polygon = new MatOfPoint();

            // Make a Polygon out of a contour with provide Epsilon accuracy parameter.
            // It uses the Douglas-Peucker algorithm http://en.wikipedia.org/wiki/Ramer-Douglas-Peucker_algorithm
            contour.convertTo(contour2f, CvType.CV_32FC2);
            Imgproc.approxPolyDP(
                    contour2f,
                    polygone2f,
                    30,  // The maximum distance between the original curve and its approximation.
                    true);                                             // Resulting polygon representation is "closed:" its first and last vertices are connected.
            polygone2f.convertTo(polygon, CvType.CV_32S);

            polygonList.add(new Rectangle(polygon));
        }


        /* **********************************************************************
         * Rhombus Tile Recognition
         *
         * From polygon list, produces a list of suitable Parallelograms (Rhombi).
         */
        // Get only valid Rhombus(es) : actually parallelograms.
        for (Rectangle rhombus : polygonList) {
            rhombus.qualify();
            // if(rhombus.status == Rectangle.StatusEnum.VALID)
            rectangleList.add(rhombus);
        }
        Log.i("polygons count:", Integer.toString(polygonList.size()));

        // Filtering w.r.t. Rhmobus set characteristics
        Rectangle.removedOutlierRhombi(rectangleList);

        /* **********************************************************************
         * Face Recognition
         *
         * Takes a collection of Rhombus objects and determines if a valid
         * Rubik Face can be determined from them, and then also determines
         * initial color for all nine tiles.
         */
      //  rubikFace.processRhombuses(rectangleList, rgbaImage);

        polygonList.clear();
        rectangleList.clear();
        //dr.drawFaceColorMetrics(rgba_gray_image, rubikFace);
      //  rgba_gray_image.release();
        //      rgba_gray_image.release();
        return dr.drawAR(rgba_gray_image);
    }
}
