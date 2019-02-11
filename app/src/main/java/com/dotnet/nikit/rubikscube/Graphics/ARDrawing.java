package com.dotnet.nikit.rubikscube.Graphics;

import com.dotnet.nikit.rubikscube.Constants;
import com.dotnet.nikit.rubikscube.RubikFace;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


//Draw AR around physical cube
public class ARDrawing {

    public Mat drawAR(Mat image)
    {
        drawOverlay(image);
        return image;
    }

    public void drawOverlay(Mat img)
    {
        Scalar color = Constants.ColorTileEnum.GREEN.cvColor;

        // Adjust drawing grid to start at edge of cube and not center of a tile.
        double x = 20;//face.lmsResult.origin.x - (face.alphaLatticLength + face.betaLatticLength) / 2;
        double y = 150;//face.lmsResult.origin.y - (face.alphaLatticLength + face.betaLatticLength) / 2;
        for(int n=0; n<4; n++) {
           Imgproc.line(
                    img,
                    new Point(
                            x + n * 50,
                            y + n * 50),
                    new Point(
                            x + 50 * 3 + n * 50,
                            y + 50 * 3 + n * 50),
                    color,
                    3);
        }

        for(int m=0; m<4; m++) {
            Imgproc.line(
                    img,
                    new Point(
                            x + m * 50,
                            y + m * 50),
                    new Point(
                            x + 50 * 3 + m * 50,
                            y + 50 * 3 + m * 50),
                    color,
                    3);
        }

		// Draw a circule at the Rhombus reported center of each tile.
		/*for(int n=0; n<3; n++) {
			for(int m=0; m<3; m++) {
				Rhombus rhombus = faceRhombusArray[n][m];
				if(rhombus != null)
        //Core.circle(img, rhombus.center, 5, Constants.ColorBlue, 3);
			}
		}*/

		// Draw the error vector from center of tile to actual location of Rhombus.
		/*for(int n=0; n<3; n++) {
			for(int m=0; m<3; m++) {
				Rhombus rhombus = faceRhombusArray[n][m];
				if(rhombus != null) {

					Point tileCenter = getTileCenterInPixels(n, m);
					Core.line(img, tileCenter, rhombus.center, Constants.ColorRed, 3);
					Core.circle(img, tileCenter, 5, Constants.ColorBlue, 1);
				}
			}
		}*/

		// Draw reported Logical Tile Color Characters in center of each tile.
		/*for(int n=0; n<3; n++) {
				for(int m=0; m<3; m++) {

					// Draw tile character in UV plane
					Point tileCenterInPixels = face.getTileCenterInPixels(n, m);
					tileCenterInPixels.x -= 10.0;
				tileCenterInPixels.y += 10.0;
					String text = "R";
					Imgproc.putText(img, text, tileCenterInPixels, Constants.FontFace, 3, Constants.ColorTileEnum.BLACK.cvColor, 3);
				}
			}*/
    }

    public void drawFaceColorMetrics(Mat image, RubikFace face) {

        Imgproc.rectangle(image, new Point(0, 0), new Point(570, 720), Constants.ColorTileEnum.BLACK.cvColor, -1);

        if(face == null || face.faceRecognitionStatus != RubikFace.FaceRecognitionStatusEnum.SOLVED)
            return;

        // Draw simple grid
        Imgproc.rectangle(image, new Point(-256 + 256, -256 + 400), new Point(256 + 256, 256 + 400), Constants.ColorTileEnum.WHITE.cvColor);
        Imgproc.line(image, new Point(0 + 256, -256 + 400), new Point(0 + 256, 256 + 400), Constants.ColorTileEnum.WHITE.cvColor);
        Imgproc.line(image, new Point(-256 + 256, 0 + 400), new Point(256 + 256, 0 + 400), Constants.ColorTileEnum.WHITE.cvColor);
//		Core.putText(image, String.format("Luminosity Offset = %4.0f", face.luminousOffset), new Point(0, -256 + 400 - 60), Constants.FontFace, 2, ColorTileEnum.WHITE.cvColor, 2);
//		Core.putText(image, String.format("Color Error Before Corr = %4.0f", face.colorErrorBeforeCorrection), new Point(0, -256 + 400 - 30), Constants.FontFace, 2, ColorTileEnum.WHITE.cvColor, 2);
//		Core.putText(image, String.format("Color Error After Corr = %4.0f", face.colorErrorAfterCorrection), new Point(0, -256 + 400), Constants.FontFace, 2, ColorTileEnum.WHITE.cvColor, 2);

        for(int n=0; n<3; n++) {
            for(int m=0; m<3; m++) {

                double [] measuredTileColor = face.measuredColorArray[n][m];
//				Log.e(Constants.TAG, "RGB: " + logicalTileArray[n][m].character + "=" + actualTileColor[0] + "," + actualTileColor[1] + "," + actualTileColor[2] + " x=" + x + " y=" + y );
                double[] measuredTileColorYUV   = Tool.getYUVfromRGB(measuredTileColor);
//				Log.e(Constants.TAG, "Lum: " + logicalTileArray[n][m].character + "=" + acutalTileYUV[0]);


                double luminousScaled     = measuredTileColorYUV[0] * 2 - 256;
                double uChromananceScaled = measuredTileColorYUV[1] * 2;
                double vChromananceScaled = measuredTileColorYUV[2] * 2;

                String text = Character.toString(face.observedTileArray[n][m].color.symbol);

                // Draw tile character in UV plane
                Imgproc.putText(image, text, new Point(uChromananceScaled + 256, vChromananceScaled + 400), Constants.FontFace, 3, face.observedTileArray[n][m].color.cvColor, 3);

                // Draw tile characters on INSIDE right side for Y axis for adjusted luminosity.
//				Core.putText(image, text, new Point(512 - 40, luminousScaled + 400 + face.luminousOffset), Constants.FontFace, 3, face.observedTileArray[n][m].cvColor, 3);

                // Draw tile characters on OUTSIDE right side for Y axis as directly measured.
                Imgproc.putText(image, text, new Point(512 + 20, luminousScaled + 400), Constants.FontFace, 3, face.observedTileArray[n][m].color.cvColor, 3);
//				Log.e(Constants.TAG, "Lum: " + logicalTileArray[n][m].character + "=" + luminousScaled);
            }
        }

        Scalar rubikRed    = Constants.ColorTileEnum.RED.rubikColor;
        Scalar rubikOrange = Constants.ColorTileEnum.ORANGE.rubikColor;
        Scalar rubikYellow = Constants.ColorTileEnum.YELLOW.rubikColor;
        Scalar rubikGreen  = Constants.ColorTileEnum.GREEN.rubikColor;
        Scalar rubikBlue   = Constants.ColorTileEnum.BLUE.rubikColor;
        Scalar rubikWhite  = Constants.ColorTileEnum.WHITE.rubikColor;


        // Draw Color Calibration in UV plane as dots
        Imgproc.circle(image, new Point(2*Tool.getYUVfromRGB(rubikRed.val)[1] +    256, 2*Tool.getYUVfromRGB(rubikRed.val)[2] + 400), 10, rubikRed, -1);
        Imgproc.circle(image, new Point(2*Tool.getYUVfromRGB(rubikOrange.val)[1] + 256, 2*Tool.getYUVfromRGB(rubikOrange.val)[2] + 400), 10, rubikOrange, -1);
        Imgproc.circle(image, new Point(2*Tool.getYUVfromRGB(rubikYellow.val)[1] + 256, 2*Tool.getYUVfromRGB(rubikYellow.val)[2] + 400), 10, rubikYellow, -1);
        Imgproc.circle(image, new Point(2*Tool.getYUVfromRGB(rubikGreen.val)[1] +  256, 2*Tool.getYUVfromRGB(rubikGreen.val)[2] + 400), 10, rubikGreen, -1);
        Imgproc.circle(image, new Point(2*Tool.getYUVfromRGB(rubikBlue.val)[1] +   256, 2*Tool.getYUVfromRGB(rubikBlue.val)[2] + 400), 10, rubikBlue, -1);
        Imgproc.circle(image, new Point(2*Tool.getYUVfromRGB(rubikWhite.val)[1] +  256, 2*Tool.getYUVfromRGB(rubikWhite.val)[2] + 400), 10, rubikWhite, -1);

        // Draw Color Calibration on right side Y axis as dots
        Imgproc.line(image, new Point(502, -256 + 2*Tool.getYUVfromRGB(rubikRed.val)[0] + 400),    new Point(522, -256 + 2*Tool.getYUVfromRGB(rubikRed.val)[0] + 400), rubikRed, 3);
        Imgproc.line(image, new Point(502, -256 + 2*Tool.getYUVfromRGB(rubikOrange.val)[0] + 400), new Point(522, -256 + 2*Tool.getYUVfromRGB(rubikOrange.val)[0] + 400), rubikOrange, 3);
        Imgproc.line(image, new Point(502, -256 + 2*Tool.getYUVfromRGB(rubikGreen.val)[0] + 400),  new Point(522, -256 + 2*Tool.getYUVfromRGB(rubikGreen.val)[0] + 400), rubikGreen, 3);
        Imgproc.line(image, new Point(502, -256 + 2*Tool.getYUVfromRGB(rubikYellow.val)[0] + 400), new Point(522, -256 + 2*Tool.getYUVfromRGB(rubikYellow.val)[0] + 400), rubikYellow, 3);
        Imgproc.line(image, new Point(502, -256 + 2*Tool.getYUVfromRGB(rubikBlue.val)[0] + 400),   new Point(522, -256 + 2*Tool.getYUVfromRGB(rubikBlue.val)[0] + 400), rubikBlue, 3);
        Imgproc.line(image, new Point(502, -256 + 2*Tool.getYUVfromRGB(rubikWhite.val)[0] + 400),  new Point(522, -256 + 2*Tool.getYUVfromRGB(rubikWhite.val)[0] + 400), rubikWhite, 3);
    }
}
