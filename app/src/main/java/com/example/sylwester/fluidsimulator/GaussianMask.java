package com.example.sylwester.fluidsimulator;

/**
 * Created by Sylwester on 17/01/2018.
 */



public class GaussianMask {
    private Matrix mask;

    public void buildMask() {
        this.mask = new Matrix(3, 3);
        mask.set(0, 0, 4.0 / 16);
        mask.set(0, 1, 4.0 / 8.0);
        mask.set(0, 2, 4.0 / 16.0);
        mask.set(1, 0, 4.0 / 8);
        mask.set(1, 1, 4.0 / 4.0);
        mask.set(1, 2, 4.0 / 8.0);
        mask.set(2, 0, 4.0 / 16);
        mask.set(2, 1, 4.0 / 8.0);
        mask.set(2, 2, 4.0 / 16.0);
    }

    public void convole(Matrix matrix, Vec2 index, double value) {
        for (int i = -1; i < 2; ++i) {
            for (int j = -1; j < 2; ++j) {
                double vel = matrix.at((int) index.getX() + i, (int) index.getY() + j);
                value = value + vel * mask.at(i + 1, j + 1);
                if (value<-1.0) {
                    value = -1.0;
                }

                else if (value>1.0) {
                    value = 1.0;
                }
                matrix.set((int) index.getX() + i, (int) index.getY() + j, value);
            }
        }
    }
}