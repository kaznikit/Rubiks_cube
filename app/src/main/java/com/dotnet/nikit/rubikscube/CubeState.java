package com.dotnet.nikit.rubikscube;

import com.dotnet.nikit.rubikscube.Graphics.Tool;

import org.opencv.core.Scalar;

import java.util.HashMap;

//state of the cube in particular time
public class CubeState {
    // Rubik Face of latest processed frame: may or may not be any of the six state objects.
    public RubikFace activeRubikFace;

    /*
     * This is "Rubik Cube State" or "Rubik Cube Model" in model-view-controller vernacular.
     * Map of above rubik face objects index by FaceNameEnum
     */
    public HashMap<Constants.FaceNameEnum, RubikFace> nameRubikFaceMap = new HashMap<Constants.FaceNameEnum, RubikFace>(6);

    /*
     * This is a hash map of OpenCV colors that are initialized to those specified by field
     * rubikColor of ColorTileEnum.   Function reevauateSelectTileColors() adjusts these
     * colors according to a Mean-Shift algorithm to correct for lumonosity.
     */
    public HashMap<Constants.ColorTileEnum, Scalar> mutableTileColors = new HashMap<Constants.ColorTileEnum, Scalar>(6);

    // We assume that faces will be explored in a particular sequence.
    public int adoptFaceCount = 0;

    /**
     * Adopt Face
     *
     * Adopt faces in a particular sequence dictated by the user directed instruction on
     * how to rotate the code during the exploration phase.  Also tile name is
     * specified at this time, and "transformedTileArray" is created which is a
     * rotated version of the observed tile array so that the face orientations
     * match the convention of a cut-out rubik cube layout.
     *
     * =+= This logic duplicated in AppStateMachine
     *
     * @param rubikFace
     */
    public void adopt(RubikFace rubikFace) {


        switch(adoptFaceCount) {

            case 0:
                rubikFace.faceNameEnum = Constants.FaceNameEnum.UP;
                rubikFace.transformedTileArray =  rubikFace.observedTileArray.clone();
                break;
            case 1:
                rubikFace.faceNameEnum = Constants.FaceNameEnum.RIGHT;
                rubikFace.transformedTileArray = Tool.getTileArrayRotatedClockwise(rubikFace.observedTileArray);
                break;
            case 2:
                rubikFace.faceNameEnum = Constants.FaceNameEnum.FRONT;
                rubikFace.transformedTileArray = Tool.getTileArrayRotatedClockwise(rubikFace.observedTileArray);
                break;
            case 3:
                rubikFace.faceNameEnum = Constants.FaceNameEnum.DOWN;
                rubikFace.transformedTileArray = Tool.getTileArrayRotatedClockwise(rubikFace.observedTileArray);
                break;
            case 4:
                rubikFace.faceNameEnum = Constants.FaceNameEnum.LEFT;
                rubikFace.transformedTileArray = Tool.getTileArrayRotated180(rubikFace.observedTileArray);
                break;
            case 5:
                rubikFace.faceNameEnum = Constants.FaceNameEnum.BACK;
                rubikFace.transformedTileArray = Tool.getTileArrayRotated180(rubikFace.observedTileArray);
                break;

            default:
                // =+= log error ?
        }

        if(adoptFaceCount < 6) {

            // Record Face by Name: i.e., UP, DOWN, LEFT, ...
            nameRubikFaceMap.put(rubikFace.faceNameEnum, rubikFace);
        }

        adoptFaceCount++;
    }


    /**
     * Get Rubik Face by Name
     *
     * @param faceNameEnum
     * @return
     */
    public RubikFace getFaceByName(Constants.FaceNameEnum faceNameEnum) {
        return nameRubikFaceMap.get(faceNameEnum);
    }


    /**
     * Return the number of valid and adopted faces.  Maximum is of course six.
     *
     * @return
     */
    public int getNumObservedFaces() {
        return nameRubikFaceMap.size();
    }


    /**
     * Return true if all six faces have been observed and adopted.
     *
     * @return
     */
    public boolean isThereAfullSetOfFaces() {
        if(getNumObservedFaces() >= 6)
            return true;
        else
            return false;
    }

}
