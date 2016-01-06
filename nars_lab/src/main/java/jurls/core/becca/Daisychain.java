/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.becca;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

/**
 * 
 * @author me
 */
public class Daisychain {
	private double[] cable, cablePre;

	/** transition probability matrix: chain(i,j) = [cable i,cable j] */
	private Array2DRowRealMatrix tp;
	private double[] chainVector;
	double updateRate = 0.5;

	public int getNumChains() {
		return cable.length * cable.length;
	}

	public double[] in(double[] cable) {
		this.cable = cable;

		double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;

		if ((tp == null) || (tp.getRowDimension() != cable.length)) {
			tp = new Array2DRowRealMatrix(cable.length, cable.length);
			// TODO transfer old probability to the new instance
		}

		if (cablePre != null) {
			for (int i = 0; i < cablePre.length; i++) {
				for (int j = 0; j < cable.length; j++) {
					double c = cablePre[i] * cable[j];

					double prevProb = tp.getEntry(i, j);
					if (Double.isNaN(prevProb))
						prevProb = 0;

					double newProb = c * (updateRate) + prevProb
							* (1.0 - updateRate);

					if (newProb < min)
						min = newProb;
					if (newProb > max)
						max = newProb;

					tp.setEntry(i, j, newProb);
				}
			}
		}

		if ((cablePre == null) || (cablePre.length != cable.length))
			cablePre = new double[cable.length];

		System.arraycopy(cable, 0, cablePre, 0, cable.length);

		return ravel(tp, chainVector, 0, max);
	}

	public static double[] ravel(Array2DRowRealMatrix t, double[] x, double min, double max) {
        int size = t.getRowDimension() * t.getColumnDimension();
        if ((x == null) || (x.length!=size))
            x = new double[size];
        
        double[][] xd = t.getDataRef();
        int p = 0;
        
        double factor = min!=max ? (1.0 / (max-min)) : 1.0;

        for (double[] row : xd) {
            for (int i = 0; i < row.length; i++) row[i] = (row[i] - min) * factor;

            System.arraycopy(row, 0, x, p, row.length);
            p += row.length;
        }
        
        return x;
    }
}
