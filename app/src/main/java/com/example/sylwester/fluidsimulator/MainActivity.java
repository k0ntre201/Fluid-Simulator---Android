package com.example.sylwester.fluidsimulator;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    FluidSimulation fluidSimulation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fluidSimulation = new FluidSimulation(550, 30, this);

        MySeekBar diffuseSeekBar = (MySeekBar)findViewById(R.id.diffuseSeekBar);
        diffuseSeekBar.setText((TextView)findViewById(R.id.diffuseText));
        diffuseSeekBar.isDiffuse(true);
        diffuseSeekBar.setFluidSimulation(fluidSimulation);

        MySeekBar viscocitySeekBar = (MySeekBar)findViewById(R.id.viscocitySeekBar);
        viscocitySeekBar.setText((TextView)findViewById(R.id.viscocityText));
        viscocitySeekBar.isDiffuse(false);
        viscocitySeekBar.setFluidSimulation(fluidSimulation);

        final FluidBox fluidBox = (FluidBox)findViewById(R.id.fluidBox);
        fluidBox.setFluidSimulation(fluidSimulation);

        Button button = (Button)findViewById(R.id.startSimulation);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fluidSimulation.isStartedSimulation()) {
                    fluidSimulation.startSimulator();
                    fluidSimulation.start();
                }

                else {
                    fluidSimulation.stopSimulator();
                    try {
                        fluidSimulation.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        fluidSimulation.setOnDrawFPS(new DrawFPSText((TextView)findViewById(R.id.FPS)));
        AddSourceSwitch addSourceSwitch = (AddSourceSwitch)findViewById(R.id.addSource);
        addSourceSwitch.setFluidSimulation(fluidSimulation);
    }
}
