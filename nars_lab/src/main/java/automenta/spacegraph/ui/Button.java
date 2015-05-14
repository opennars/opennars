/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.spacegraph.ui;

import automenta.spacegraph.control.Pointer;
import automenta.spacegraph.control.Pressable;
import automenta.spacegraph.control.Touchable;
import automenta.spacegraph.math.linalg.Vec4f;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author me
 */
public class Button extends Panel implements Pressable, Touchable {

    private boolean touchable = true;
    private boolean pressed = false;
    private boolean touched = false;

    public interface ButtonAction {

        public void act(Pointer pointer, Button b);
    }
    List<ButtonAction> actions = new LinkedList();
    final Vec4f frontNormalColor = new Vec4f(0.8f, 0.8f, 0.8f, 1.0f);
    final Vec4f frontTouchedColor = new Vec4f(0.75f, 0.75f, 0.75f, 0.95f);
    final Vec4f frontPressedColor = new Vec4f(0.5f, 0.5f, 0.5f, 0.9f);

    @Override
    public Vec4f getBackgroundColor() {
        if (isPressed()) {
            return frontPressedColor;
        } else {
            if (isTouched()) {
                return frontTouchedColor;
            } else {
                return frontNormalColor;
            }
        }

    }

    @Override
    public void onTouchChange(Pointer pointer, boolean touched) {
        this.touched = touched;
        if (!touched) {
            if (pressed) {
                onPressChange(pointer, false);
            }
        }
    }

    @Override
    public boolean isTouchable() {
        return touchable;
    }

    @Override
    public void onPressChange(Pointer pointer, boolean pressed) {
        this.pressed = pressed;
        if (!pressed) {
            if (touched) {
                onRelease();

                for (ButtonAction ba : actions) {
                    ba.act(pointer, this);
                }
            }

        } else {
            onPress();
        }
    }

    @Override
    public boolean isPressable() {
        return true;
    }

    public boolean isPressed() {
        return pressed;
    }

    public boolean isTouched() {
        return touched;
    }

    /** when actually clicked */
    protected void onRelease() {
    }

    protected void onPress() {
    }

    public boolean addAction(ButtonAction a) {
        return actions.add(a);
    }

    public boolean removeAction(ButtonAction a) {
        return actions.remove(a);
    }
}
