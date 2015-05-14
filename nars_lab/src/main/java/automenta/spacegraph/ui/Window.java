/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package automenta.spacegraph.ui;

import automenta.spacegraph.control.Draggable;
import automenta.spacegraph.control.Pointer;
import automenta.spacegraph.control.Touchable;
import automenta.spacegraph.math.linalg.Vec3f;

/**
 *
 * @author me
 */
public class Window extends Panel implements Touchable, Draggable {

    private boolean touchable = true;
    private boolean pressable = true;
    private Vec3f dragStartPoint;
    private Vec3f dragOrigin;
    

    @Override
    public void onDragStart(Pointer pointer, Vec3f worldStart) {
        //System.out.println("drag start: " + worldStart);
        dragOrigin = new Vec3f(this.center);
        dragStartPoint = new Vec3f(worldStart);
    }

    @Override
    public void onDragging(Pointer pointer, Vec3f world) {
        //System.out.println("dragging: " + world);
        center(world.x()-dragStartPoint.x()+dragOrigin.x(), world.y()-dragStartPoint.y()+dragOrigin.y(), world.z()-dragStartPoint.z()+dragOrigin.z());
    }

    @Override
    public void onDragEnd(Pointer pointer, Vec3f worldEnd) {
        System.out.println("drag end: " + worldEnd);
    }

}
