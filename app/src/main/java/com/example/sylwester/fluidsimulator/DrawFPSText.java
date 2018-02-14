package com.example.sylwester.fluidsimulator;

import android.widget.TextView;

/**
 * Created by Sylwester on 19/01/2018.
 */

public class DrawFPSText implements DrawFPSInterface{
    private TextView textView;

    public DrawFPSText(TextView textView) {
        this.textView = textView;
    }

    @Override
    public void drawFPS(double fps) {
        this.textView.setText("FPS: " + Double.toString(fps));
    }
}
