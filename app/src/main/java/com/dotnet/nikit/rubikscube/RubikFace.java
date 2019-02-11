package com.dotnet.nikit.rubikscube;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.LinkedList;
import java.util.List;

import ar.ColorRecognition;
import ar.Util;

public class RubikFace {
    // A Rubik Face can exist in the following states:
    public enum FaceRecognitionStatusEnum {
        UNKNOWN,
        INSUFFICIENT,            // Insufficient Provided Rhombi to attempt solution
        BAD_METRICS,             // Metric Calculation did not produce reasonable results
        INCOMPLETE,              // Rhombi did not converge to proper solution
        INADEQUATE,              // We require at least one Rhombus in each row and column
        BLOCKED,                 // Attempt to improve Rhombi layout in face was blocked: incorrect move direction reported
        INVALID_MATH,            // LMS algorithm result in invalid math.
        UNSTABLE,                // Last Tile move resulted in a increase in the overall error (LMS).
        SOLVED }                 // Full and proper solution obtained.
    public FaceRecognitionStatusEnum faceRecognitionStatus = FaceRecognitionStatusEnum.UNKNOWN;

    // A 3x3 matrix of Logical Tiles.  All elements must be non-null for an appropriate Face solution.
    // The rotation of this array is the output of the Face Recognizer as per the current spatial
    // rotation of the cube.
    public Tile [][] observedTileArray = new Tile[3][3];

    // A 3x3 matrix of Logical Tiles.  All elements must be non-null for an appropriate Face solution.
    // The rotation of this array has been adjusted so that, in the final cube state, the faces are read
    // and rendered correctly with respect to the "unfolded cube layout convention."
    public Tile [][] transformedTileArray = new Tile[3][3];

    public List<Rectangle> rhombusList = new LinkedList<Rectangle>();

    // Record actual RGB colors measured at the center of each tile.
    public double[][][] measuredColorArray = new double[3][3][4];

    // Face Designation: i.e., Up, Down, ....
    public Constants.FaceNameEnum faceNameEnum;

    // Angle of Alpha-Axis (N) stored in radians.
    public double angle = 0.0;

    public RubikFace() {
        angle = 90.0 * Math.PI / 180.0;
    }

    public void calculateTiles(List<Rectangle> rhombusList, Mat image)
    {

    }

    /**
     * Process rectangles
     *
     * Given the rectangles list, attempt to recognize the grid dimensions and orientation,
     * and full tile color set.
     */
    public void processRhombuses(List<Rectangle> rhombusList, Mat image) {

        this.rhombusList = rhombusList;

        // Don't even attempt if less than three rhombus are identified.
        if(rhombusList.size() < 3) {
            faceRecognitionStatus = FaceRecognitionStatusEnum.INSUFFICIENT;
            return;
        }

        // Calculate average alpha and beta angles, and also gamma ratio.
        // Sometimes (but probably only when certain bugs exist) can contain NaN data.
        if( calculateMetrics() == false) {
            faceRecognitionStatus =  FaceRecognitionStatusEnum.BAD_METRICS;
            return;
        }


        // Loop until some resolution


        // Obtain Logical Tiles
/*
        new ColorRecognition.Face(this).faceTileColorRecognition(image);

        // Calculate a hash code that is unique for the given collection of Logical Tiles.
        // Added right rotation to obtain unique number with respect to locations.
        myHashCode = 0;
        for(int n=0; n<3; n++)
            for(int m=0; m<3; m++)
                myHashCode = observedTileArray[n][m].hashCode() ^ Integer.rotateRight(myHashCode, 1);

*/

        faceRecognitionStatus =  FaceRecognitionStatusEnum.SOLVED;
    }


    /**
     * Calculate Metrics
     *
     * Obtain alpha beta and gammaRatio from Rect set.
     */
    private boolean calculateMetrics() {

        int numElements = rhombusList.size();
        for(Rectangle rhombus : rhombusList) {

            angle += rhombus.alphaAngle;
        }

        angle = angle / numElements * Math.PI / 180.0;

        // =+= currently, always return OK
        return true;
    }

    /**
     * Find Closest Tile Color
     *
     * Two Pass algorithm:
     * 1) Find closest fit using just U and V axis.
     * 2) Calculate luminous correction value assuming above choices are correct (exclude Red and Orange)
     * 3) Find closed fit again using Y, U and V axis where Y is corrected.
     */
  /*  public void faceTileColorRecognition(Mat image) {

        double [][] colorError = new double[3][3];

        // Obtain actual measured tile color from image.
        for(int n=0; n<3; n++) {
            for(int m=0; m<3; m++) {

                Point tileCenter = getTileCenterInPixels(n, m);
                Size size = image.size();
                double width = size.width;
                double height = size.height;

                // Check location of tile on screen: can be too close to screen edge.
                if( tileCenter.x < 10 || tileCenter.x > width - 10 || tileCenter.y < 10 || tileCenter.y > height - 10) {
                    Log.w(ar.Constants.TAG_COLOR, String.format("Tile at [%1d,%1d] has coordinates x=%5.1f y=%5.1f too close to edge to assign color.", n, m, tileCenter.x, tileCenter.y));
                    rubikFace.measuredColorArray[n][m] = new double[4];  // This will default to back.
                }

                // Obtain measured color from average over 20 by 20 pixel squar.
                else {

                    try {
                        Mat mat = image.submat((int)(tileCenter.y - 10), (int)(tileCenter.y + 10), (int)(tileCenter.x - 10), (int)(tileCenter.x + 10));
                        rubikFace.measuredColorArray[n][m] = Core.mean(mat).val;
                    }

                    // Probably LMS calculations produced bogus tile location.
                    catch(CvException cvException) {
                        Log.e(ar.Constants.TAG_COLOR, "ERROR findClosestLogicalTiles(): x=" + tileCenter.x + " y=" + tileCenter.y + " img=" + image + " :" + cvException);
                        rubikFace.measuredColorArray[n][m] = new double[4];
                    }
                }
            }
        }


        // First Pass: Find closest logical color using only UV axis.
        for(int n=0; n<3; n++) {
            for(int m=0; m<3; m++) {

                double [] measuredColor = rubikFace.measuredColorArray[n][m];
                double [] measuredColorYUV   = Util.getYUVfromRGB(measuredColor);

                double smallestError = Double.MAX_VALUE;
                ar.Constants.ColorTileEnum bestCandidate = null;

                for(ar.Constants.ColorTileEnum candidateColorTile : ar.Constants.ColorTileEnum.values()) {

                    if(candidateColorTile.isRubikColor == true) {

                        double[] candidateColorYUV = Util.getYUVfromRGB(candidateColorTile.rubikColor.val);

                        // Only examine U and V axis, and not luminous.
                        double error =
                                (candidateColorYUV[1] - measuredColorYUV[1]) * (candidateColorYUV[1] - measuredColorYUV[1]) +
                                        (candidateColorYUV[2] - measuredColorYUV[2]) * (candidateColorYUV[2] - measuredColorYUV[2]);

                        colorError[n][m] = Math.sqrt(error);

                        if(error < smallestError) {
                            bestCandidate = candidateColorTile;
                            smallestError = error;
                        }
                    }
                }

//    				Log.d(Constants.TAG_COLOR, String.format( "Tile[%d][%d] has R=%3.0f, G=%3.0f B=%3.0f %c err=%4.0f", n, m, measuredColor[0], measuredColor[1], measuredColor[2], bestCandidate.character, smallestError));

                // Assign best candidate to this tile location.
                rubikFace.observedTileArray[n][m] = bestCandidate;
            }
        }

        // Calculate and record LMS error (including luminous).
        for(int n=0; n<3; n++) {
            for(int m=0; m<3; m++) {
                double[] selectedColor = rubikFace.observedTileArray[n][m].rubikColor.val;
                double[] measuredColor = rubikFace.measuredColorArray[n][m];
                colorErrorBeforeCorrection += calculateColorError(selectedColor, measuredColor, true, 0.0);
            }
        }

        // Diagnostics:  For each tile location print: measure RGB, measure YUV, logical RGB, logical YUV
        Log.d(ar.Constants.TAG_COLOR, "Table: Measure RGB, Measure YUV, Logical RGB, Logical YUV");
        Log.d(ar.Constants.TAG_COLOR, String.format( " m:n|----------0--------------|-----------1-------------|---------2---------------|") );
        Log.d(ar.Constants.TAG_COLOR, String.format( " 0  |%s|%s|%s|", Util.dumpRGB(rubikFace.measuredColorArray[0][0], colorError[0][0]), Util.dumpRGB(rubikFace.measuredColorArray[1][0], colorError[1][0]), Util.dumpRGB(rubikFace.measuredColorArray[2][0], colorError[2][0]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 0  |%s|%s|%s|", Util.dumpYUV(rubikFace.measuredColorArray[0][0]), Util.dumpYUV(rubikFace.measuredColorArray[1][0]), Util.dumpYUV(rubikFace.measuredColorArray[2][0]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 0  |%s|%s|%s|", Util.dumpRGB(rubikFace.observedTileArray[0][0]), Util.dumpRGB(rubikFace.observedTileArray[1][0]), Util.dumpRGB(rubikFace.observedTileArray[2][0]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 0  |%s|%s|%s|", Util.dumpYUV(rubikFace.observedTileArray[0][0].rubikColor.val), Util.dumpYUV(rubikFace.observedTileArray[1][0].rubikColor.val), Util.dumpYUV(rubikFace.observedTileArray[2][0].rubikColor.val) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( "    |-------------------------|-------------------------|-------------------------|") );
        Log.d(ar.Constants.TAG_COLOR, String.format( " 1  |%s|%s|%s|", Util.dumpRGB(rubikFace.measuredColorArray[0][1], colorError[0][1]), Util.dumpRGB(rubikFace.measuredColorArray[1][1], colorError[1][1]), Util.dumpRGB(rubikFace.measuredColorArray[2][1], colorError[2][1]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 1  |%s|%s|%s|", Util.dumpYUV(rubikFace.measuredColorArray[0][1]), Util.dumpYUV(rubikFace.measuredColorArray[1][1]), Util.dumpYUV(rubikFace.measuredColorArray[2][1]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 1  |%s|%s|%s|", Util.dumpRGB(rubikFace.observedTileArray[0][1]), Util.dumpRGB(rubikFace.observedTileArray[1][1]), Util.dumpRGB(rubikFace.observedTileArray[2][1]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 1  |%s|%s|%s|", Util.dumpYUV(rubikFace.observedTileArray[0][1].rubikColor.val), Util.dumpYUV(rubikFace.observedTileArray[1][1].rubikColor.val), Util.dumpYUV(rubikFace.observedTileArray[2][1].rubikColor.val) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( "    |-------------------------|-------------------------|-------------------------|") );
        Log.d(ar.Constants.TAG_COLOR, String.format( " 2  |%s|%s|%s|", Util.dumpRGB(rubikFace.measuredColorArray[0][2], colorError[0][2]), Util.dumpRGB(rubikFace.measuredColorArray[1][2], colorError[1][2]), Util.dumpRGB(rubikFace.measuredColorArray[2][2], colorError[2][2]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 2  |%s|%s|%s|", Util.dumpYUV(rubikFace.measuredColorArray[0][2]), Util.dumpYUV(rubikFace.measuredColorArray[1][2]), Util.dumpYUV(rubikFace.measuredColorArray[2][2]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 2  |%s|%s|%s|", Util.dumpRGB(rubikFace.observedTileArray[0][2]), Util.dumpRGB(rubikFace.observedTileArray[1][2]), Util.dumpRGB(rubikFace.observedTileArray[2][2]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 2  |%s|%s|%s|", Util.dumpYUV(rubikFace.observedTileArray[0][2].rubikColor.val), Util.dumpYUV(rubikFace.observedTileArray[1][2].rubikColor.val), Util.dumpYUV(rubikFace.observedTileArray[2][2].rubikColor.val) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( "    |-------------------------|-------------------------|-------------------------|") );
        Log.d(ar.Constants.TAG_COLOR, "Total Color Error Before Correction: " + colorErrorBeforeCorrection);


        // Now compare Actual Luminous against expected luminous, and calculate an offset.
        // However, do not use Orange and Red because they are most likely to be miss-identified.
        // =+= TODO: Also, diminish weight on colors that are repeated.
        luminousOffset = 0.0;
        int count = 0;
        for(int n=0; n<3; n++) {
            for(int m=0; m<3; m++) {
                ar.Constants.ColorTileEnum colorTile = rubikFace.observedTileArray[n][m];
                if(colorTile == ar.Constants.ColorTileEnum.RED || colorTile == ar.Constants.ColorTileEnum.ORANGE)
                    continue;
                double measuredLuminousity = Util.getYUVfromRGB(rubikFace.measuredColorArray[n][m])[0];
                double expectedLuminousity = Util.getYUVfromRGB(colorTile.rubikColor.val)[0];
                luminousOffset += (expectedLuminousity - measuredLuminousity);
                count++;
            }
        }
        luminousOffset = (count == 0) ? 0.0 : luminousOffset / count;
        Log.d(ar.Constants.TAG_COLOR, "Luminousity Offset: " + luminousOffset);


        // Second Pass: Find closest logical color using YUV but add luminousity offset to measured values.
        for(int n=0; n<3; n++) {
            for(int m=0; m<3; m++) {

                double [] measuredColor = rubikFace.measuredColorArray[n][m];
                double [] measuredColorYUV   = Util.getYUVfromRGB(measuredColor);

                double smallestError = Double.MAX_VALUE;
                ar.Constants.ColorTileEnum bestCandidate = null;

                for(ar.Constants.ColorTileEnum candidateColorTile : ar.Constants.ColorTileEnum.values() ) {

                    if(candidateColorTile.isRubikColor == true) {

                        double[] candidateColorYUV = Util.getYUVfromRGB(candidateColorTile.rubikColor.val);

                        // Calculate Error based on U, V, and Y, but adjust with luminous offset.
                        double error =
                                (candidateColorYUV[0] - (measuredColorYUV[0] + luminousOffset)) * (candidateColorYUV[0] - (measuredColorYUV[0] + luminousOffset)) +
                                        (candidateColorYUV[1] -  measuredColorYUV[1]) * (candidateColorYUV[1] - measuredColorYUV[1]) +
                                        (candidateColorYUV[2] -  measuredColorYUV[2]) * (candidateColorYUV[2] - measuredColorYUV[2]);

                        colorError[n][m] = Math.sqrt(error);

                        if(error < smallestError) {
                            bestCandidate = candidateColorTile;
                            smallestError = error;
                        }
                    }
                }

//    				Log.d(Constants.TAG_COLOR, String.format( "Tile[%d][%d] has R=%3.0f, G=%3.0f B=%3.0f %c err=%4.0f", n, m, measuredColor[0], measuredColor[1], measuredColor[2], bestCandidate.character, smallestError));

                // Check and possibly re-assign this tile location with a different color.
                if(bestCandidate != rubikFace.observedTileArray[n][m]) {
                    Log.i(ar.Constants.TAG_COLOR, String.format("Reclassiffying tile [%d][%d] from %c to %c", n, m, rubikFace.observedTileArray[n][m].symbol, bestCandidate.symbol));
                    rubikFace.observedTileArray[n][m] = bestCandidate;
                }
            }
        }

        // Calculate and record LMS error (includeing LMS).
        for(int n=0; n<3; n++) {
            for(int m=0; m<3; m++) {
                double[] selectedColor = rubikFace.observedTileArray[n][m].rubikColor.val;
                double[] measuredColor = rubikFace.measuredColorArray[n][m];
                colorErrorAfterCorrection += calculateColorError(selectedColor, measuredColor, true, luminousOffset);
            }
        }

        // Diagnostics:
        Log.d(ar.Constants.TAG_COLOR, "Table: Measure RGB, Measure YUV, Logical RGB, Logical YUV");
        Log.d(ar.Constants.TAG_COLOR, String.format( " m:n|----------0--------------|-----------1-------------|---------2---------------|") );
        Log.d(ar.Constants.TAG_COLOR, String.format( " 0  |%s|%s|%s|", Util.dumpRGB(rubikFace.measuredColorArray[0][0], colorError[0][0]), Util.dumpRGB(rubikFace.measuredColorArray[1][0], colorError[1][0]), Util.dumpRGB(rubikFace.measuredColorArray[2][0], colorError[2][0]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 0  |%s|%s|%s|", Util.dumpYUV(rubikFace.measuredColorArray[0][0]), Util.dumpYUV(rubikFace.measuredColorArray[1][0]), Util.dumpYUV(rubikFace.measuredColorArray[2][0]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 0  |%s|%s|%s|", Util.dumpRGB(rubikFace.observedTileArray[0][0]), Util.dumpRGB(rubikFace.observedTileArray[1][0]), Util.dumpRGB(rubikFace.observedTileArray[2][0]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 0  |%s|%s|%s|", Util.dumpYUV(rubikFace.observedTileArray[0][0].rubikColor.val), Util.dumpYUV(rubikFace.observedTileArray[1][0].rubikColor.val), Util.dumpYUV(rubikFace.observedTileArray[2][0].rubikColor.val) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( "    |-------------------------|-------------------------|-------------------------|") );
        Log.d(ar.Constants.TAG_COLOR, String.format( " 1  |%s|%s|%s|", Util.dumpRGB(rubikFace.measuredColorArray[0][1], colorError[0][1]), Util.dumpRGB(rubikFace.measuredColorArray[1][1], colorError[1][1]), Util.dumpRGB(rubikFace.measuredColorArray[2][1], colorError[2][1]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 1  |%s|%s|%s|", Util.dumpYUV(rubikFace.measuredColorArray[0][1]), Util.dumpYUV(rubikFace.measuredColorArray[1][1]), Util.dumpYUV(rubikFace.measuredColorArray[2][1]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 1  |%s|%s|%s|", Util.dumpRGB(rubikFace.observedTileArray[0][1]), Util.dumpRGB(rubikFace.observedTileArray[1][1]), Util.dumpRGB(rubikFace.observedTileArray[2][1]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 1  |%s|%s|%s|", Util.dumpYUV(rubikFace.observedTileArray[0][1].rubikColor.val), Util.dumpYUV(rubikFace.observedTileArray[1][1].rubikColor.val), Util.dumpYUV(rubikFace.observedTileArray[2][1].rubikColor.val) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( "    |-------------------------|-------------------------|-------------------------|") );
        Log.d(ar.Constants.TAG_COLOR, String.format( " 2  |%s|%s|%s|", Util.dumpRGB(rubikFace.measuredColorArray[0][2], colorError[0][2]), Util.dumpRGB(rubikFace.measuredColorArray[1][2], colorError[1][2]), Util.dumpRGB(rubikFace.measuredColorArray[2][2], colorError[2][2]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 2  |%s|%s|%s|", Util.dumpYUV(rubikFace.measuredColorArray[0][2]), Util.dumpYUV(rubikFace.measuredColorArray[1][2]), Util.dumpYUV(rubikFace.measuredColorArray[2][2]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 2  |%s|%s|%s|", Util.dumpRGB(rubikFace.observedTileArray[0][2]), Util.dumpRGB(rubikFace.observedTileArray[1][2]), Util.dumpRGB(rubikFace.observedTileArray[2][2]) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( " 2  |%s|%s|%s|", Util.dumpYUV(rubikFace.observedTileArray[0][2].rubikColor.val), Util.dumpYUV(rubikFace.observedTileArray[1][2].rubikColor.val), Util.dumpYUV(rubikFace.observedTileArray[2][2].rubikColor.val) ));
        Log.d(ar.Constants.TAG_COLOR, String.format( "    |-------------------------|-------------------------|-------------------------|") );

        Log.d(ar.Constants.TAG_COLOR, "Color Error After Correction: " + colorErrorAfterCorrection);
    }*/
}
