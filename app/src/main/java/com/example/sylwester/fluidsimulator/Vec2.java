package com.example.sylwester.fluidsimulator;

/**
 * Created by Sylwester on 17/01/2018.
 */

public class Vec2 {
    private float x, y;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    Vec2(){}

    Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void checkBoundaryCondition() {
        if (this.x>1.0f) {
            this.x = 1.0f;
        }

        else if (this.x<-1.0f) {
            this.x = -1.0f;
        }

        if (this.y>1.0f) {
            this.y = 1.0f;
        }

        else  if (this.y<-1.0f){
            this.y = -1.0f;
        }
    }
}
