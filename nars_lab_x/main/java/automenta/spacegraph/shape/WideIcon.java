/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package automenta.spacegraph.shape;

import automenta.spacegraph.math.linalg.Vec3f;
import automenta.spacegraph.math.linalg.Vec4f;
import com.jogamp.opengl.GL2;

/**
 *
 * @author seh
 */
public class WideIcon extends Rect {

    private final TextRect textRect;
    int MAX_LABEL_LENGTH = 32;
    
    public WideIcon(String label, Vec4f backgroundColor, Vec4f textColor) {
        super();

        setBackgroundColor(backgroundColor);

        if (label.length() > MAX_LABEL_LENGTH) {
            label = label.substring(0, MAX_LABEL_LENGTH);
        }

        textRect = new TextRect(label) {
            @Override public Vec3f getCenter() {
                return WideIcon.this.getCenter();
            }
            @Override public Vec3f getScale() {
                return WideIcon.this.getScale();
            }
            @Override public Vec3f getRotation() {
                return WideIcon.this.getRotation();
            }
        };

        setTextColor(textColor);

    }

    private void setTextColor(Vec4f textColor) {
        textRect.setTextColor(textColor);
    }

    @Override
    public void draw(GL2 gl) {
        super.draw(gl);

        textRect.draw(gl);
    }




}
