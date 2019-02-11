package com.dotnet.nikit.rubikscube;

import android.util.Log;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import ar.Constants;
import ar.MenuAndParams;
import ar.Rhombus;

public class Rectangle {

    // Various forms of storing the corner points.
    private MatOfPoint polygonMatrix;
    private List<Point> polygonPointList;  // =+= possibly eliminate
    private Point[] polygonePointArray;    // =+= note order is adjusted

    // Possible states that this Rhombus can be identified.
    public enum StatusEnum { NOT_PROCESSED, NOT_4_POINTS, NOT_CONVEX, AREA, CLOCKWISE, OUTLIER, VALID };

    // Current Status
    public Rhombus.StatusEnum status = Rhombus.StatusEnum.NOT_PROCESSED;

    // Center of Polygon
    Point center = new Point();

    // Area of Quadrilateral.
    double area;

    // Smaller angle (in degrees: between 0 and 180) that two set parallelogram edges make to x-axis.
    double alphaAngle;

    // Larger angle (in degrees: between 0 and 180) that two set parallelogram edges make to x-axis.
    double betaAngle;

    // Best estimate (average) of parallelogram alpha side length.
    double alphaLength;

    // Best estimate (average) of parallelogram beta side length.
    double betaLength;

    // Ratio of beta to alpha length.
    double gammaRatio;

    public Rectangle(MatOfPoint polygon){
        polygonMatrix = polygon;
        polygonPointList = polygon.toList();
        polygonePointArray = polygon.toArray();
    }

    /**
     * Determine is polygon is a value Rubik Face Parallelogram
     */
    public void qualify() {

        // Calculate center
        double x=0; double y=0;
        for(Point point : polygonPointList) {
            x += point.x;
            y += point.y;
        }
        center.x = x / polygonPointList.size();
        center.y = y / polygonPointList.size();

        // Check if has four sizes and endpoints.
        if(polygonPointList.size() != 4) {
            status = Rhombus.StatusEnum.NOT_4_POINTS;
            return;
        }

        // Check if convex
        // =+= I don't believe this is working.  result should be either true or
        // =+= false indicating clockwise or counter-clockwise depending if image
        // =+= is a "hole" or a "blob".
        if(Imgproc.isContourConvex(polygonMatrix) == false) {
            status = Rhombus.StatusEnum.NOT_CONVEX;
            return;
        }

        // Compute area; check if it is reasonable.
        area = areaOfConvexQuadrilateral(polygonePointArray);
        if( (area < MenuAndParams.minimumRhombusAreaParam.value) || (area > MenuAndParams.maximumRhombusAreaParam.value) ) {
            status = Rhombus.StatusEnum.AREA;
            return;
        }

        // Adjust vertices such that element 0 is at bottom and order is counter clockwise.
        // =+= return true here if points are counter-clockwise.
        // =+= sometimes both rotations are provided.
        if( adjustQuadrilaterVertices() == true) {
            status = Rhombus.StatusEnum.CLOCKWISE;
            return;
        }


        // =+= beta calculation is failing when close to horizontal.
        // =+= Can vertices be chooses so that we do not encounter the roll over problem at +180?
        // =+= Or can math be performed differently?

        /*
         * Calculate angles to X axis of Parallelogram sides.  Take average of both sides.
         * =+= To Do:
         *   1) Move to radians.
         *   2) Move to +/- PIE representation.
         */
        alphaAngle = 180.0 / Math.PI * Math.atan2(
                (polygonePointArray[1].y - polygonePointArray[0].y) + (polygonePointArray[2].y - polygonePointArray[3].y),
                (polygonePointArray[1].x - polygonePointArray[0].x) + (polygonePointArray[2].x - polygonePointArray[3].x) );

        betaAngle = 180.0 / Math.PI * Math.atan2(
                (polygonePointArray[2].y - polygonePointArray[1].y) + (polygonePointArray[3].y - polygonePointArray[0].y),
                (polygonePointArray[2].x - polygonePointArray[1].x) + (polygonePointArray[3].x - polygonePointArray[0].x) );

        alphaLength = (lineLength(polygonePointArray[0], polygonePointArray[1]) + lineLength(polygonePointArray[3], polygonePointArray[2]) ) / 2;
        betaLength  = (lineLength(polygonePointArray[0], polygonePointArray[3]) + lineLength(polygonePointArray[1], polygonePointArray[2]) ) / 2;

        gammaRatio = betaLength / alphaLength;


        status = Rhombus.StatusEnum.VALID;


        Log.d(Constants.TAG, String.format( "Rhombus: %4.0f %4.0f %6.0f %4.0f %4.0f %3.0f %3.0f %5.2f {%4.0f,%4.0f} {%4.0f,%4.0f} {%4.0f,%4.0f} {%4.0f,%4.0f}",
                center.x,
                center.y,
                area,
                alphaAngle,
                betaAngle,
                alphaLength,
                betaLength,
                gammaRatio,
                polygonePointArray[0].x,
                polygonePointArray[0].y,
                polygonePointArray[1].x,
                polygonePointArray[1].y,
                polygonePointArray[2].x,
                polygonePointArray[2].y,
                polygonePointArray[3].x,
                polygonePointArray[3].y) + " " + status);
    }

    /**
     * Area of Convex Quadrilateral
     *
     * @param quadrilateralPointArray
     * @return
     */
    private static double areaOfConvexQuadrilateral(Point[] quadrilateralPointArray) {

        double area = areaOfaTriangle(
                lineLength(quadrilateralPointArray[0], quadrilateralPointArray[1]),
                lineLength(quadrilateralPointArray[1], quadrilateralPointArray[2]),
                lineLength(quadrilateralPointArray[2], quadrilateralPointArray[0]) )
                +
                areaOfaTriangle(
                        lineLength(quadrilateralPointArray[0], quadrilateralPointArray[3]),
                        lineLength(quadrilateralPointArray[3], quadrilateralPointArray[2]),
                        lineLength(quadrilateralPointArray[2], quadrilateralPointArray[0]) );

        return area;
    }

    /**
     * Area of a triangle specified by the three side lengths.
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    private static double areaOfaTriangle(double a, double b, double c) {


        double area = Math.sqrt(
                (a + b - c) *
                        (a - b + c) *
                        (-a + b + c) *
                        (a + b + c)
        ) / 4.0;

//		Log.i(Constants.TAG, String.format( "Triangle Area: %4.0f %4.0f %4.0f %6.0f", a, b, c, area));

        return area;
    }

    /**
     * Line length between two points.
     *
     * @param a
     * @param b
     * @return
     */
    private static double lineLength(Point a, Point b) {
        double length = Math.sqrt(
                (a.x - b.x) * (a.x - b.x) +
                        (a.y - b.y) * (a.y - b.y) );

//		Log.i(Constants.TAG, String.format( "Line Length: %6.0f", length));

        return length;
    }

    /**
     * Adjust Quadrilater Vertices such that:
     *   1) Element 0 has the minimum y coordinate.
     *   2) Order draws a counter clockwise quadrilater.
     */
    private boolean adjustQuadrilaterVertices() {

        // Find minimum.
        double y_min = Double.MAX_VALUE;
        int index = 0;
        for(int i=0; i< polygonePointArray.length; i++) {
            if(polygonePointArray[i].y < y_min) {
                y_min = polygonePointArray[i].y;
                index = i;
            }
        }

        // Rotate to get the minimum Y element ("index") as element 0.
        for(int i=0; i<index; i++) {
            Point tmp = polygonePointArray[0];
            polygonePointArray[0] = polygonePointArray[1];
            polygonePointArray[1] = polygonePointArray[2];
            polygonePointArray[2] = polygonePointArray[3];
            polygonePointArray[3] = tmp;
        }

        // Return true if points are as depicted above and in a clockwise manner.
        if(polygonePointArray[1].x < polygonePointArray[3].x)
            return true;
        else
            return false;
    }

    /**
     * Remove Outlier Rhombi
     *
     * For Alpha and Beta Angles:
     *   1) Find Median Value: i.e. value in which half are greater and half are less.
     *   2) Remove any that are > 10 degrees different
     *
     */
    public static void removedOutlierRhombi(List<Rectangle> rhombusList) {

        final double angleOutlierTolerance = MenuAndParams.angleOutlierThresholdPaaram.value;

        if(rhombusList.size() < 3)
            return;

        int midIndex = rhombusList.size() / 2;

        Collections.sort(rhombusList, new Comparator<Rectangle>() {
            @Override
            public int compare(Rectangle lhs, Rectangle rhs) {
                return (int) (lhs.alphaAngle - rhs.alphaAngle);
            } } );
        double medianAlphaAngle = rhombusList.get(midIndex).alphaAngle;

        Collections.sort(rhombusList, new Comparator<Rectangle>() {
            @Override
            public int compare(Rectangle lhs, Rectangle rhs) {
                return (int) (lhs.betaAngle - rhs.betaAngle);
            } } );
        double medianBetaAngle = rhombusList.get(midIndex).betaAngle;

        Log.i(Constants.TAG, String.format( "Outlier Filter medianAlphaAngle=%6.0f medianBetaAngle=%6.0f", medianAlphaAngle, medianBetaAngle));

        Iterator<Rectangle> rhombusItr = rhombusList.iterator();
        while(rhombusItr.hasNext())  {

            Rectangle rhombus = rhombusItr.next();

            if( (Math.abs(rhombus.alphaAngle - medianAlphaAngle) > angleOutlierTolerance) ||
                    (Math.abs(rhombus.betaAngle - medianBetaAngle) > angleOutlierTolerance) ) {
                rhombus.status = Rhombus.StatusEnum.OUTLIER;
                rhombusItr.remove();
                Log.i(Constants.TAG, String.format( "Removed Outlier Rhombus with alphaAngle=%6.0f betaAngle=%6.0f", rhombus.alphaAngle, rhombus.betaAngle));
            }
        }
    }

}
