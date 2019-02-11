package com.dotnet.nikit.rubikscube;

public class Tile {
    public double sideLength = 0.0;
    public Constants.ColorTileEnum color;

    public Tile(Constants.ColorTileEnum color){
        sideLength = 50.0;
        this.color = color;
    }
}
