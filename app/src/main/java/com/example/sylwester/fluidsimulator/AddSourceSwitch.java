package com.example.sylwester.fluidsimulator;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Created by Sylwester on 16/01/2018.
 */

public class AddSourceSwitch extends Switch {

    private FluidSimulation fluidSimulation;

    public AddSourceSwitch(Context context) {
        super(context);
        init();
    }

    public AddSourceSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AddSourceSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public AddSourceSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        this.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fluidSimulation.setAddSources(isChecked);
                if (isChecked){
                    Log.i("SWITH", "Add source");
                }

                else {
                    Log.i("SWITH", "Force");
                }
            }
        });
    }


    public void setFluidSimulation(FluidSimulation fluidSimulation) {
        this.fluidSimulation = fluidSimulation;
    }
}
