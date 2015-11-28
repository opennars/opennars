/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package automenta.spacegraph.control;

/**
 *
 * @author me
 */
public interface Pressable {
    public void onPressChange(Pointer pointer, boolean pressed);

    public boolean isPressable();
}
