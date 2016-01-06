/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.becca;

/**
 * 
 * @author me
 */
public class Cog {
	private final Daisychain daisy;
	private final Ziptie zip;
	private double[] bundleActivity;

	public Cog(Daisychain d, Ziptie z) {
		daisy = d;
		zip = z;
	}

	public double[] in(double[] x) {
		double[] chainActivity = daisy.in(x);
		bundleActivity = zip.in(chainActivity, bundleActivity);
		return bundleActivity;
	}

}
