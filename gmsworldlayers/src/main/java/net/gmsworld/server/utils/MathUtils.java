/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gmsworld.server.utils;

/**
 *
 * @author jstakun
 */
public class MathUtils {

    public static double[] normalizeE6(final double[] coords) {
        double[] norm = new double[2];

        norm[0] = normalizeE6(coords[0]);
        norm[1] = normalizeE6(coords[1]);

        return norm;
    }

    public static double normalizeE6(final double coord) {
        int tmp = (int) (coord * 1E6);
        return (tmp * 0.000001);
    }
    
    public static double normalizeE6(final int coord) {
        return (coord * 0.000001);
    }

    public static double normalizeE2(final double coord) {
        int tmp = (int) (coord * 1E2);
        return (tmp * 0.01);
    }
    
    public static int coordDoubleToInt(double coord) {
        return (int)(coord * 1E6);
    }
}
