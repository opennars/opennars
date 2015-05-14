/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.spacegraph.demo.spacegraph.unstable;

import automenta.spacegraph.Surface;

/**
 *
 * @author seh
 */
public class DemoShapeRect extends Surface {

//    float xAng = 0;
//    float yAng = 0;
//    private final Rect box;
//
//    public DemoShapeRect() {
//        super();
//
//        box = add(new Rect());
//    }
//
//    @Override
//    public void mouseDragged(MouseEvent e) {
//        super.mouseDragged(e);
//        xAng = e.getX();
//        yAng = e.getY();
//    }
//
//    @Override
//    protected void updateSpace(GL2 gl) {
//        xAng += 15 * (float) time.deltaT();
//        yAng += 15 * (float) time.deltaT();
//
//        //box.getCenter().set((float)Math.cos(getT()), (float)Math.sin(getT()), 0);
//        //box.getSize().set(1.0f + (float)Math.cos(getT())/2f, 1.0f + (float)Math.sin(getT())/2f, 1.0f);
//        box.getRotation().set(xAng, yAng, 0);        
//    }
//
//    @Override
//    public void keyPressed(KeyEvent e) {
//        super.keyPressed(e);
//        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
//            getCamera().camPos.add(new Vec3f(-0.1f, 0, 0));
//            getCamera().camTarget.add(new Vec3f(-0.1f, 0, 0));
//        }
//        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
//            getCamera().camPos.add(new Vec3f(0.1f, 0, 0));
//            getCamera().camTarget.add(new Vec3f(0.1f, 0, 0));
//        }
//    }
//
//    public static void main(String[] args) {
//        new SGWindow("DemoSGCanvas", new DemoShapeRect());
//    }
//
//
//    @Override
//    protected synchronized void handleTouch(Pointer p) {
//        super.handleTouch(p);
//        Set<Touchable> touchingNow = new HashSet();
//        final Vec2f v = new Vec2f(p.world.x(), p.world.y());
//        synchronized (defaultSpace.getDrawables()) {
//            for (Drawable d : defaultSpace.getDrawables()) {
//                if (d instanceof Touchable) {
//                    Touchable t = (Touchable) d;
//                    if (t.isTouchable()) {
//                        if (t.intersects(v)) {
//                            touchingNow.add(t);
//                        }
//                    }
//                }
//            }
//        }
//        for (Touchable t : touchingNow) {
//            if (!p.touching.contains(t)) {
//                t.onTouchChange(p, true);
//                p.touching.add(t);
//            }
//        }
//        List<Touchable> toRemove = new LinkedList();
//        for (Touchable t : p.touching) {
//            if (!touchingNow.contains(t)) {
//                t.onTouchChange(p, false);
//                toRemove.add(t);
//            } else {
//                t.onTouchChange(p, true);
//            }
//        }
//        for (Touchable t : toRemove) {
//            p.touching.remove(t);
//        }
//    }
}
