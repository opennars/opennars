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
public class RLParameters {

    private double gamma;
    private double lambda;

    public RLParameters(double gamma, double lambda) {
        this.gamma = gamma;
        this.lambda = lambda;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

}
