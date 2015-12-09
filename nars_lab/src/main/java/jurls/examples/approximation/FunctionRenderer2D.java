/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.approximation;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author thorsten2
 */
public class FunctionRenderer2D extends JPanel {

    private RenderFunction2D renderFunction2D = null;
    private final int n = 20;

    public void setRenderFunction2D(RenderFunction2D renderFunction2D) {
        this.renderFunction2D = renderFunction2D;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (renderFunction2D != null) {
            for (int i = 0; i < n; ++i) {
                int x = i * getWidth() / n;
                double mx = (i + 0.5) / n;

                for (int j = 0; j < n; ++j) {

                    int y = j * getHeight() / n;
                    double my = (j + 0.5) / n;

                    double z = renderFunction2D.compute(mx, my);

                    float red = 0.5f * (float) Math.sin(z * 0.1 + 0.2) + 0.5f;
                    float green = 0.5f * (float) Math.sin(z * 0.2 - 0.2) + 0.5f;
                    float blue = 0.5f * (float) Math.sin(z * 0.3 + 0.1) + 0.5f;

                    Color c = new Color(red, green, blue);
                    g.setColor(c);
                    g.fillRect(x, y, getWidth() / n, getHeight() / n);
                }
            }
        }else{
            g.setColor(Color.black);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

}
