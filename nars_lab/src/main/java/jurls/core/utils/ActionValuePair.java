/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.utils;

/**
 *
 * @author thorsten
 */
public class ActionValuePair {

    private double v;
    private int a;

    public ActionValuePair(int a, double v) {
        this.v = v;
        this.a = a;
    }

    public double getV() {
        return v;
    }

    public void setV(double v) {
        this.v = v;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    @Override
    public String toString() {
        return a + "=" + v;
    }
}
