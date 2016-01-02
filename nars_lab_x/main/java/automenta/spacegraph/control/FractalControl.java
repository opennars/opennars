/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.spacegraph.control;

import automenta.spacegraph.SG.KeyStates;
import automenta.spacegraph.Surface;
import automenta.spacegraph.math.linalg.Vec2f;
import automenta.spacegraph.math.linalg.Vec3f;
import automenta.spacegraph.shape.Rect;
import automenta.spacegraph.video.SGPanel;

/**
 *
 * @author seh
 */
public class FractalControl implements MouseListener, MouseMotionListener, MouseWheelListener, Repeat {

    private Vec2f downPixel = new Vec2f();
    private Vec2f downWorld = new Vec2f();
    private Vec3f downPointPos = new Vec3f();
    private Vec3f downPointTarget = new Vec3f();
    private Vec3f targetPos = new Vec3f(0, 0, 10);
    private Vec3f targetTarget = new Vec3f(0, 0, 0);
    private Vec3f targetUp = new Vec3f(0, 1, 0);
    private final Surface surface;
    float camLerp = 0.95f;
    float tiltLerp = 0.75f;
    float panSpeed = 2.0f;
    float wheelDZ = 0.5f;
    float tiltAngle = (float) Math.PI / 2.0f;
    float tiltSpeed = 0.002f;
    private boolean panning = false;
    private boolean hasPanned = false;
    float zoomDilation = 1.05f;
    float nearPadding = 1.05f;
    final float DRAG_THRESHOLD = 4.0f; //in pixel lengths
    private boolean dragging;
    private boolean hasDragged;
    private Draggable dragged;

    public FractalControl(SGPanel panel) {
        super();
        targetPos.set(panel.getSurface().getCamera().camPos);
        targetTarget.set(panel.getSurface().getCamera().camTarget);
        this.surface = panel.getSurface();
        surface.add(this);
        panel.getCanvas().addMouseListener(this);
        panel.getCanvas().addMouseMotionListener(this);
        panel.getCanvas().addMouseWheelListener(this);
    }

    @Override
    public void update(double dt, double t) {


        //TODO normalize targetUp to rotation, or use quaternion
        //surface.getCamera().camUp.lerp(targetUp, tiltLerp);

        surface.getCamera().camPos.lerp(targetPos, camLerp);
        surface.getCamera().camTarget.lerp(targetTarget, camLerp);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int button = e.getButton();
        if ((button == 3) || (button == 1)) {
            downPixel.set((float) e.getX(), (float) e.getY());
            downPointPos.set(targetPos);
            downPointTarget.set(targetTarget);
            downWorld.set(surface.getPointer().world.x(), surface.getPointer().world.y());
            if (button == 3) {
                panning = true;
                hasPanned = false;
            } else if (button == 1) {
                Touchable t =  surface.getPointer().getSmallestTouched();
                if (t instanceof Draggable) {
                    dragged = (Draggable)t;
                    dragging = true;
                    hasDragged = false;
                    dragged.onDragStart(surface.getPointer(), surface.getPointer().world);
                }
            }
        } else {
            panning = false;
            dragging = false;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (dragging) {
            if (dragged != null) {
                dragged.onDragEnd(surface.getPointer(), surface.getPointer().world);
            }
        }
        if ((panning) || (dragging)) {
            if ((!hasPanned) && (!hasDragged)) {
                if (e.getButton() == 3)
                    onRightClick();
            }
            panning = false;
            hasPanned = false;
            hasDragged = false;
            dragged = null;
        }
    }

    protected void onRightClick() {
        
        Touchable t = surface.getPointer().getSmallestTouched();
        System.out.println("zooming: " + t + " " + dragged);
        if (t != null) {
            zoomTo(t);
        }
    }

    public void zoomTo(Touchable s) {
        if (s instanceof Rect) {
            Rect r = (Rect) s;
            float targetZ = getTargetHeight(r); //TODO calculate correct height
            targetZ = Math.max(targetZ, surface.getNearF() * nearPadding);
            targetTarget.set(r.getCenter().x(), r.getCenter().y(), 0);
            targetPos.set(r.getCenter().x(), r.getCenter().y(), targetZ);
            targetUp.set(r.getRotation());
        }
    }

    public float getTargetHeight(Rect rect) {
        float r = rect.getScale().length() / 2.0f * zoomDilation;
        return (float) (r * Math.sin(Math.PI / 2.0 - Math.toRadians(surface.getFocus()) / 2.0) / Math.sin(Math.toRadians(surface.getFocus()) / 2.0));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private static float distSqr(float ax, float ay, float bx, float by) {
        return (ax - bx) * (ax - bx) + (ay - by) * (ay - by);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (panning) {
            if (!hasPanned) {
                if (distSqr(e.getX(), e.getY(), downPixel.x(), downPixel.y()) > DRAG_THRESHOLD * DRAG_THRESHOLD) {
                    hasPanned = true;
                }
            }

            if (hasPanned) {

                float xAng = surface.getPointer().world.x();
                float yAng = surface.getPointer().world.y();

                float dx = xAng - downWorld.x();
                float dy = yAng - downWorld.y();

                if (surface.keyStates.get(KeyStates.CONTROL)) {
                    tiltAngle += dy * tiltSpeed;
                    targetUp.set((float) Math.cos(tiltAngle), (float) Math.sin(tiltAngle), 0);
                } else {

                    //rotate by current tiltAngle
                    float nx = dx * (float) Math.cos(tiltAngle - (float) Math.PI / 2.0f) - dy * (float) Math.sin(tiltAngle - (float) Math.PI / 2.0f);
                    float ny = dx * (float) Math.sin(tiltAngle - (float) Math.PI / 2.0f) + dy * (float) Math.cos(tiltAngle - (float) Math.PI / 2.0f);

                    Vec3f delta = new Vec3f(nx, ny, 0);

                    delta.scale(-getPanSpeed());

                    targetPos.set(downPointPos);
                    targetTarget.set(downPointTarget);
                    targetPos.add(delta);
                    targetTarget.add(delta);
                }
            }
        }
        if (dragging) {
            if (!hasDragged) {
                if (distSqr(e.getX(), e.getY(), downPixel.x(), downPixel.y()) > DRAG_THRESHOLD * DRAG_THRESHOLD) {
                    hasDragged = true;
                }
            }

            if (hasDragged) {
                if (dragged!=null) {

                    float xAng = surface.getPointer().world.x();
                    float yAng = surface.getPointer().world.y();

                    float dx = xAng - downWorld.x();
                    float dy = yAng - downWorld.y();


                    //rotate by current tiltAngle
                    float nx = dx * (float) Math.cos(tiltAngle - (float) Math.PI / 2.0f) - dy * (float) Math.sin(tiltAngle - (float) Math.PI / 2.0f);
                    float ny = dx * (float) Math.sin(tiltAngle - (float) Math.PI / 2.0f) + dy * (float) Math.cos(tiltAngle - (float) Math.PI / 2.0f);

                    Vec3f delta = new Vec3f(nx, ny, 0);

                    delta.scale(-getPanSpeed());

                    dragged.onDragging(surface.getPointer(), surface.getPointer().world);
                }
            }
        }

    }

    public float getPanSpeed() {
        return panSpeed;
    }

    public float getWheelDZ() {
        return wheelDZ;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Vec3f delta = new Vec3f(0, 0, (float) e.getWheelRotation() * getWheelDZ());
        targetPos.add(delta);
        targetTarget.add(delta);
        float minZ = surface.getNearF() + 0.25f;
        targetPos.setZ(Math.max(targetPos.z(), minZ));
        targetTarget.setZ(Math.max(targetTarget.z(), minZ - 1.0f));

    }
}
