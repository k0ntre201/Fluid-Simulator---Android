package com.example.sylwester.fluidsimulator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Sylwester on 16/01/2018.
 */

public class FluidBox extends SurfaceView {

    private SurfaceHolder surfaceHolder;
    private FluidSimulation fluidSimulation;

    public FluidBox(Context context) {
        super(context);
        init();
    }

    public FluidBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FluidBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public FluidBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init() {
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i("Size", Integer.toString(getWidth()) + " " + Integer.toString(getHeight()));
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                invalidate();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                fluidSimulation.stopSimulator();
                try {
                    fluidSimulation.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                boolean result = false;
                int x = (int)event.getX();
                int y = (int)event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.i("DOWN", Integer.toString(x) + " " + Integer.toString(y));
                        if (fluidSimulation.isAddSources()) {
                            fluidSimulation.addSources(x, y);
                        }

                        else {
                            fluidSimulation.setFirstPointVelocity(new Vec2(x,y));
                        }
                        result = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.i("MOVE", Integer.toString(x) + " " + Integer.toString(y));
                        if (fluidSimulation.isAddSources()) {
                            fluidSimulation.addSources(x, y);
                        }

                        else {
                            fluidSimulation.addVelocity(new Vec2(x,y));
                        }
                        result = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.i("UP", Integer.toString(x) + " " + Integer.toString(y));
                        result = true;
                        break;
                    default:
                        break;
                }
                fluidSimulation.drawDensity();

                return result;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i("SURFACEVIEW","Draw");

    }

    public FluidSimulation getFluidSimulation() {
        return fluidSimulation;
    }

    public void setFluidSimulation(FluidSimulation fluidSimulation) {
        this.fluidSimulation = fluidSimulation;
        fluidSimulation.setSurfaceHolder(surfaceHolder, this);
    }
}
