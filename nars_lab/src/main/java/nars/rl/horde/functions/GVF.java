package nars.rl.horde.functions;


import nars.rl.horde.HordeAgent;
import org.apache.commons.math3.linear.RealVector;

public interface GVF extends HordeAgent.OffPolicyTD {
    double update(double pi_t, double b_t, RealVector x_t, RealVector x_tp1, double r_tp1, double gamma_tp1, double z_tp1);
}
