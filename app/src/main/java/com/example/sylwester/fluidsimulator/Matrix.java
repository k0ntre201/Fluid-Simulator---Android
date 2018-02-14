package com.example.sylwester.fluidsimulator;

import java.lang.reflect.Constructor;

/**
 * Created by Sylwester on 17/01/2018.
 */

public class Matrix {
    private double[] data;
    private int m, n;


    Matrix(int m, int n) {
        this.m = m;
        this.n = n;
        this.data = new double[n*m];
    }

    public double[] getData() { return this.data; }
    public void setData(double[] data) {
        this.data = data;
    }

    private int getIndex(int i, int j) {
        return j*n + i;
    }

    public double at(int i, int j) {
        return this.data[getIndex(i,j)];
    }

    public void set(int i, int j, double value) {
        this.data[getIndex(i,j)] = value;
    }

    public void setAll(double value) {
        for (int i=0;i<this.data.length;++i) {
            this.data[i] = value;
        }
    }

    public void swap(double l, double r) {
        double temp = l;
        l = r;
        r = temp;
    }

    public void swap(Matrix matrix){
        double[] temp = matrix.getData();
        matrix.setData(this.data);
        this.data = temp;
    }



    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }
}
