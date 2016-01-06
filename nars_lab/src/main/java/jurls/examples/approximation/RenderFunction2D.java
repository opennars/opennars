/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.approximation;

import java.awt.*;

/**
 * 
 * @author thorsten
 */
public interface RenderFunction2D {

	double compute(double x, double y);

	Color getColor();
}
