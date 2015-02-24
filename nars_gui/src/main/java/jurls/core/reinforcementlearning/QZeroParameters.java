/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.reinforcementlearning;

/**
 *
 * @author thorsten
 */
public class QZeroParameters {

    private double alpha;
    private double gamma;

    public QZeroParameters(double alpha, double gamma) {
        this.alpha = alpha;
        this.gamma = gamma;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }
}
