package com.example.sylwester.fluidsimulator;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.CollapsibleActionView;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Sylwester on 17/01/2018.
 */

public class FluidSimulation extends Thread {
    private com.example.sylwester.fluidsimulator.Matrix density;
    private com.example.sylwester.fluidsimulator.Matrix u;
    private com.example.sylwester.fluidsimulator.Matrix v;
    private com.example.sylwester.fluidsimulator.Matrix densityPrev;
    private com.example.sylwester.fluidsimulator.Matrix uPrev;
    private com.example.sylwester.fluidsimulator.Matrix vPrev;
    private int gridSize, fluidBoxSize;
    private float gridBoxStep;
    private boolean isStarted;
    private Vec2 velocity;
    private float dt;
    private GaussianMask mask;
    private float diffuseCoefficient;
    private float viscocityCoefficient;
    private SurfaceHolder surfaceHolder;
    private SystemClock clock;
    private Activity activity;
    private SurfaceView surfaceView;
    private boolean isAddSources;
    private DrawFPSInterface onDrawFPS;

    FluidSimulation(int fluidBoxSize, int gridSize, Activity activity){
        this.fluidBoxSize = fluidBoxSize;
        this.gridSize = gridSize;
        this.gridBoxStep = fluidBoxSize/gridSize;
        this.isStarted = false;
        this.isAddSources = true;
        density = new com.example.sylwester.fluidsimulator.Matrix(gridSize,gridSize);
        u = new com.example.sylwester.fluidsimulator.Matrix(gridSize,gridSize);
        v = new com.example.sylwester.fluidsimulator.Matrix(gridSize,gridSize);
        densityPrev = new com.example.sylwester.fluidsimulator.Matrix(gridSize,gridSize);
        uPrev = new com.example.sylwester.fluidsimulator.Matrix(gridSize,gridSize);
        vPrev = new com.example.sylwester.fluidsimulator.Matrix(gridSize,gridSize);
        density.setAll(0);
        densityPrev.setAll(0);
        u.setAll(0);
        uPrev.setAll(0);
        v.setAll(0);
        vPrev.setAll(0);
        this.velocity = new Vec2();
        velocity.setX(0);
        velocity.setY(0);
        this.dt = 0.0f;
        this.mask = new GaussianMask();
        this.mask.buildMask();
        this.activity = activity;
    }

    public void startSimulator() {
        this.isStarted = true;
    }

    public void stopSimulator() {
        this.isStarted = false;
    }

    @Override
    public void run() {

        long prevMiliseconds = 0, nowMiliseconds = 0;
        boolean firstClock = true;
        while (isStarted) {
            if (firstClock) {
                prevMiliseconds = SystemClock.currentThreadTimeMillis();
                firstClock = false;
            }
            nowMiliseconds = SystemClock.currentThreadTimeMillis();
            dt = (float)(nowMiliseconds - prevMiliseconds)/1000.0f;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onDrawFPS.drawFPS(1.0/dt);
                }
            });

//            Log.i("FPS", Float.toString(1.0f/dt));

            diffuseVelocity();
            projectVelocity();
            advectVelocity();
            projectVelocity();

            diffuseDensity();
            advectDensity();

            drawDensity();

            prevMiliseconds = nowMiliseconds;
        }
    }

    public void setFirstPointVelocity(Vec2 v)
    {
        this.velocity = this.fromImageToGrid(v);
    }

    public void addVelocity(Vec2 v)
    {
        Vec2 temp = fromImageToGrid(v);
        temp.setX(temp.getX() - velocity.getX());
        temp.setY(temp.getY() - velocity.getY());
        Vec2 gridIndex = new Vec2((v.getX() / this.gridBoxStep), (v.getY() / this.gridBoxStep));
        if ((int)gridIndex.getX() > 0 && (int)gridIndex.getY() > 0 && (int)gridIndex.getX() < (this.gridSize - 1) && (int)gridIndex.getY() < (this.gridSize - 1))
        {
            Log.i("ADDVELOCITY", Double.toString(temp.getX()) + " " + Double.toString(temp.getY()));
            this.mask.convole(this.u, gridIndex, temp.getX());
            this.mask.convole(this.v, gridIndex, temp.getY());
            this.setFirstPointVelocity(v);
        }

    }

    private Vec2 fromImageToGrid(Vec2 v)
    {
        Vec2 temp = new Vec2();
        temp.setX(v.getX()/ this.fluidBoxSize);
        temp.setY(v.getY()/ this.fluidBoxSize);
        return temp;
    }

    public void addSources(int x, int y) {
        int xh = x / (int)this.gridBoxStep;
        int yh = y / (int)this.gridBoxStep;

        int steps = this.gridSize/10;
        for (int i=-1;i<steps-1;++i)
        {
            for (int j=-1;j<steps-1;++j)
            {
                int xxh = xh + i;
                int yyh = yh + j;

                if (xxh>=0 && xxh<gridSize && yyh>=0 && yyh<gridSize)
                {
                    double gridDensity = this.density.at(xxh, yyh);
                    gridDensity += 0.2;
                    if (gridDensity >= 1.0)
                    {
                        gridDensity = 1.0;
                    }
                    this.density.set(xxh, yyh, gridDensity);
                }
            }
        }


//        int x1 = xh * (int)this.gridBoxStep;
//        int y1 = yh * (int)this.gridBoxStep;
//        int x2 = (xh + 1) * (int)this.gridBoxStep;
//        int y2 = (yh + 1) * (int)this.gridBoxStep;
//        int value = (int)(gridDensity * 240.0);
    }


    public void swap(com.example.sylwester.fluidsimulator.Matrix lhs, com.example.sylwester.fluidsimulator.Matrix rhs)
    {
        lhs.swap(rhs);
    }

    public void diffuseDensity()
    {
        this.swap(this.density, this.densityPrev);
        double a = dt * diffuseCoefficient * (this.gridSize - 1) * (this.gridSize - 1);
//        Log.i("DiffuseCoefficient", Double.toString(density.at(15,15)));
        //double a = 0.1;

        for (int k = 0; k < 20; ++k)
        {
            for (int i = 1; i < this.gridSize - 1; ++i)
            {
                for (int j = 1; j < this.gridSize - 1; ++j)
                {
                    double dens = (this.densityPrev.at(i, j) + a * (this.density.at(i - 1, j) + this.density.at(i + 1, j) + this.density.at(i, j - 1) + this.density.at(i, j + 1))) / (1.0 + 4.0 * a);
                    this.density.set(i, j, dens);
                }
            }
            this.setBoundaryConditionsDiffuse(0, this.density);
        }
    }

    private boolean checkBoundaryCondition(int i) {
        return (i>=0 && i<gridSize);
    }

    public void advectDensity()
    {
        swap(this.densityPrev, this.density);
        int i0, j0, i1, j1;
        double x, y, s0, s1, t1, t0, dt0;

        dt0 = dt * (this.gridSize - 1);

        for (int i = 1; i < this.gridSize - 1; ++i)
        {
            for (int j = 1; j < this.gridSize - 1; ++j)
            {
                x = i - dt0 * this.u.at(i, j);
                y = j - dt0 * this.v.at(i, j);
                if (x < 0.5) x = 0.5;
                if (y < 0.5) y = 0.5;
                if (x > this.gridSize - 0.5) x = this.gridSize - 0.5;
                if (y > this.gridSize - 0.5) y = this.gridSize - 0.5;
                i0 = (int)x; i1 = i0 + 1;
                j0 = (int)y; j1 = j0 + 1;
                s1 = x - i0; s0 = 1 - s1; t1 = y - j0; t0 = 1 - t1;

                if (checkBoundaryCondition(i0) && checkBoundaryCondition(j0) && checkBoundaryCondition(i1) && checkBoundaryCondition(j1)) {
                    double dens = s0 * (t0 * this.densityPrev.at(i0, j0) + t1 * this.densityPrev.at(i0, j1)) +
                            s1 * (t0 * this.densityPrev.at(i1, j0) + t1 * this.densityPrev.at(i1, j1));
                    this.density.set(i, j, dens);
                }
            }
        }
        this.setBoundaryConditionsDiffuse(0, this.density);
    }

    public void setBoundaryConditionsDiffuse(int b, com.example.sylwester.fluidsimulator.Matrix matrix)
    {
        for (int i = 1; i < this.gridSize - 1; ++i)
        {
            double e1 = b == 1 ? -matrix.at(1, i) : matrix.at(1, i);
            matrix.set(0, i, e1);
            double e2 = b == 1 ? -matrix.at(gridSize - 2, i) : matrix.at(gridSize - 2, i);
            matrix.set(this.gridSize - 1, i, e2);

            double e3 = b == 2 ? -matrix.at(i, 1) : matrix.at(i, 1);
            matrix.set(i, 0, e3);
            double e4 = b == 2 ? -matrix.at(i, gridSize - 2) : matrix.at(i, gridSize - 2);
            matrix.set(i, this.gridSize - 1, e4);
        }

        matrix.set(0, 0, 0.5 * (matrix.at(0, 1) + matrix.at(1, 0)));
        matrix.set(0, this.gridSize - 1, 0.5 * (matrix.at(1, this.gridSize - 1) + matrix.at(0, this.gridSize - 2)));
        matrix.set(this.gridSize - 1, 0, 0.5 * (matrix.at(this.gridSize - 2, 0) + matrix.at(this.gridSize - 1, 0)));
        matrix.set(this.gridSize - 1, this.gridSize - 1, 0.5 * (matrix.at(this.gridSize - 2, this.gridSize - 1) + matrix.at(this.gridSize - 1, this.gridSize - 2)));
    }

    public void setBoundaryConditionsVelocity(int b, com.example.sylwester.fluidsimulator.Matrix matrix)
    {
        for (int i = 1; i < this.gridSize - 1; ++i)
        {
            double e1 = b == 1 ? -1.0 * matrix.at(1, i) : matrix.at(1, i);
            matrix.set(0, i, e1);
            double e2 = b == 1 ? -1.0 * matrix.at(gridSize - 2, i) : matrix.at(gridSize - 2, i);
            matrix.set(this.gridSize - 1, i, e2);

            double e3 = b == 2 ? -1.0 * matrix.at(i, 1) : matrix.at(i, 1);
            matrix.set(i, 0, e3);
            double e4 = b == 2 ? -1.0 * matrix.at(i, gridSize - 2) : matrix.at(i, gridSize - 2);
            matrix.set(i, this.gridSize - 1, e4);
        }

        matrix.set(0, 0, 0.5 * (matrix.at(0, 1) + matrix.at(1, 0)));
        matrix.set(0, this.gridSize - 1, 0.5 * (matrix.at(1, this.gridSize - 1) + matrix.at(0, this.gridSize - 2)));
        matrix.set(this.gridSize - 1, 0, 0.5 * (matrix.at(this.gridSize - 2, 0) + matrix.at(this.gridSize - 1, 0)));
        matrix.set(this.gridSize - 1, this.gridSize - 1, 0.5 * (matrix.at(this.gridSize - 2, this.gridSize - 1) + matrix.at(this.gridSize - 1, this.gridSize - 2)));
    }

    public void drawDensity() {
//        Log.i("DiffuseCoefficient", Double.toString(density.at(15,15)));
        Canvas canvas = surfaceHolder.lockCanvas();
        canvas.drawColor(Color.BLACK);
        synchronized (surfaceHolder) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            for (int x = 0; x < this.gridSize; ++x) {
                for (int y = 0; y < this.gridSize; ++y) {
                    double gridDensity = this.density.at(x, y);
                    int x1 = x * (int) this.gridBoxStep;
                    int y1 = y * (int) this.gridBoxStep;
                    int x2 = (x + 1) * (int) this.gridBoxStep;
                    int y2 = (y + 1) * (int) this.gridBoxStep;
                    int value = (int) (gridDensity * 240.0);
                    paint.setColor(Color.argb(255, 0, 0, value));
                    canvas.drawRect(x1, y1, x2, y2, paint);
                    surfaceView.onDrawForeground(canvas);
                }
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }


    public void diffuseVelocity()
    {
//        double diff = 0.004;
        this.swap(this.u, this.uPrev);
        this.swap(this.v, this.vPrev);
        double a = dt * viscocityCoefficient * (this.gridSize - 1) * (this.gridSize - 1);

        //double a = 0.1;

        for (int k = 0; k < 20; ++k)
        {
            for (int i = 1; i < this.gridSize - 1; ++i)
            {
                for (int j = 1; j < this.gridSize - 1; ++j)
                {
                    double velX = (this.uPrev.at(i, j) + a * (this.u.at(i - 1, j) + this.u.at(i + 1, j) + this.u.at(i, j - 1) + this.u.at(i, j + 1))) / (1.0 + 4.0 * a);
                    this.u.set(i, j, velX);
                    double velV = (this.vPrev.at(i, j) + a * (this.v.at(i - 1, j) + this.v.at(i + 1, j) + this.v.at(i, j - 1) + this.v.at(i, j + 1))) / (1.0 + 4.0 * a);
                    this.v.set(i, j, velV);
                }
            }
            this.setBoundaryConditionsVelocity(1, u);
            this.setBoundaryConditionsVelocity(2, v);
        }
    }


    public void advectVelocity()
    {
        this.swap(this.u, this.uPrev);
        this.swap(this.v, this.vPrev);
        int i0, j0, i1, j1;
        double x, y, s0, s1, t1, t0, dt0;

        dt0 = dt * (this.gridSize - 1);

        for (int i = 1; i < this.gridSize - 1; ++i)
        {
            for (int j = 1; j < this.gridSize - 1; ++j)
            {
                x = i - dt0 * this.u.at(i, j);
                y = j - dt0 * this.v.at(i, j);
                if (x < 0.5) x = 0.5;
                if (y < 0.5) y = 0.5;
                if (x > this.gridSize - 0.5) x = this.gridSize - 0.5;
                if (y > this.gridSize - 0.5) y = this.gridSize - 0.5;
                i0 = (int)x; i1 = i0 + 1;
                j0 = (int)y; j1 = j0 + 1;
                s1 = x - i0; s0 = 1.0 - s1; t1 = y - j0; t0 = 1.0 - t1;

                double velX = s0 * (t0 * this.uPrev.at(i0, j0) + t1 * this.uPrev.at(i0, j1)) +
                        s1 * (t0 * this.uPrev.at(i1, j0) + t1 * this.uPrev.at(i1, j1));
                this.u.set(i, j, velX);

                double velY = s0 * (t0 * this.vPrev.at(i0, j0) + t1 * this.vPrev.at(i0, j1)) +
                        s1 * (t0 * this.vPrev.at(i1, j0) + t1 * this.vPrev.at(i1, j1));
                this.v.set(i, j, velY);
            }
        }
        this.setBoundaryConditionsVelocity(1, this.u);
        this.setBoundaryConditionsVelocity(2, this.v);
        //Set boundary condition for the velocity
    }

    public void projectVelocity()
    {
        double h = 1.0 / this.gridSize;

        for (int i = 1; i < this.gridSize - 1; ++i)
        {
            for (int j = 1; j < this.gridSize - 1; ++j)
            {
                double vec = -0.5 * h * (this.u.at(i + 1, j) - this.u.at(i - 1, j) + this.v.at(i, j + 1) - this.v.at(i, j - 1) );
                this.uPrev.set(i,j,0);
                this.vPrev.set(i, j, vec);
            }
        }
        this.setBoundaryConditionsVelocity(0, this.vPrev);
        for (int k = 0; k < 20; ++k)
        {
            for (int i = 1; i < this.gridSize - 1; ++i)
            {
                for (int j = 1; j < this.gridSize - 1; ++j)
                {
                    double vec = (this.vPrev.at(i,j)+ this.uPrev.at(i - 1, j) + this.uPrev.at(i + 1, j) + this.uPrev.at(i, j - 1) + this.uPrev.at(i, j + 1)) / 4.0;
                    this.uPrev.set(i, j, vec);
                }
            }
            this.setBoundaryConditionsVelocity(0, this.uPrev);
        }

        for (int i = 1; i < this.gridSize - 1; ++i)
        {
            for (int j = 1; j < this.gridSize - 1; ++j)
            {
                Vec2 vec = new Vec2();
                vec.setX((float)(this.u.at(i, j) - 0.5 * (this.uPrev.at(i + 1, j) - this.uPrev.at(i - 1, j)) / h));
                vec.setY((float)(this.v.at(i, j) - 0.5 * (this.uPrev.at(i , j + 1) - this.uPrev.at(i , j - 1)) / h));
                this.u.set(i, j, vec.getX());
                this.v.set(i, j, vec.getY());
            }
        }
        this.setBoundaryConditionsVelocity(1, this.u);
        this.setBoundaryConditionsVelocity(1, this.v);
    }

    public void setDiffuseCoefficient(float diffuseCoefficient) {
        this.diffuseCoefficient = diffuseCoefficient/100.0f;
    }

    public void setViscocityCoefficient(float viscocityCoefficient) {
        this.viscocityCoefficient = viscocityCoefficient/100.0f;
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder, SurfaceView surfaceView) {
        this.surfaceHolder = surfaceHolder;
        this.surfaceView = surfaceView;
    }

    public boolean isStartedSimulation() {
        return isStarted;
    }

    public boolean isAddSources() {
        return isAddSources;
    }

    public void setAddSources(boolean addSources) {
        isAddSources = addSources;
    }

    public void setOnDrawFPS(DrawFPSInterface onDrawFPS) {
        this.onDrawFPS = onDrawFPS;
    }
}
