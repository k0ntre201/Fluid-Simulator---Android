package com.example.sylwester.fluidsimulator;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Filter;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Sylwester on 16/01/2018.
 */

public class MySeekBar extends android.support.v7.widget.AppCompatSeekBar {
    public MySeekBar(Context context) {
        super(context);
        init();
    }

    public MySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MySeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private TextView textView;
    private String initialText;
    private int accuracy;
    private boolean diffuse;
    private FluidSimulation fluidSimulation;

    private void init() {
        accuracy = 10000;
        setMax(accuracy);
        setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float correctProgress = (float)progress/(float)accuracy;
                textView.setText(initialText + " " + Float.toString(correctProgress));
                if (diffuse) {
                    if (fluidSimulation!=null)
                        fluidSimulation.setDiffuseCoefficient(correctProgress);
                }

                else {
                    if (fluidSimulation!=null)
                        fluidSimulation.setViscocityCoefficient(correctProgress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public TextView getText() {
        return textView;
    }

    public void setText(TextView textView) {
        this.initialText = textView.getText().toString();
        this.textView = textView;
        setProgress(2000);
    }

    public void isDiffuse(boolean diffuse) {
        this.diffuse = diffuse;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        setMax(accuracy);
        this.accuracy = accuracy;
    }

    public void setFluidSimulation(FluidSimulation fluidSimulation) {
        this.fluidSimulation = fluidSimulation;
        if (diffuse) {
            if (fluidSimulation!=null)
                fluidSimulation.setDiffuseCoefficient((float)getProgress()/accuracy);
        }

        else {
            if (fluidSimulation!=null)
                fluidSimulation.setViscocityCoefficient((float)getProgress()/accuracy);
        }
    }
}
