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
public class DemoShapeTextRect extends Surface {

//    float xAng = 0;
//    float yAng = 0;
//    private TextRect box;
//    private final Space2D defaultSpace;
//
//    public DemoShapeTextRect() {
//        super();
//
//        String t = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Nulla in mi ut augue laoreet gravida. Quisque sodales vehicula ligula. Donec posuere. Morbi aliquet, odio vitae tempus mattis, odio dolor vestibulum leo, congue laoreet risus felis vitae dolor. Nulla arcu. Morbi non quam. Vestibulum pretium dolor fermentum erat. Proin dictum volutpat nibh. Morbi egestas mauris a diam. Vestibulum mauris eros, porttitor at, fermentum a, varius eu, mauris. Cras rutrum felis ut diam. Aenean porttitor risus a nunc. Aliquam et ante eu dolor pretium adipiscing. Sed fermentum, eros in dapibus lacinia, augue nunc fermentum tellus, eu egestas justo elit at mauris. Sed leo nisl, fermentum in, pretium vitae, tincidunt at, lacus. Curabitur non diam.";
////    "Etiam varius sagittis lorem. Vivamus iaculis condimentum tortor. Nunc sollicitudin scelerisque dolor. Nunc condimentum fringilla nisl. Fusce purus mauris, blandit eu, lacinia eget, vestibulum nec, massa. Nulla vitae libero. Suspendisse potenti. Aliquam iaculis, lorem eu adipiscing tempor, ipsum dui aliquam sem, eu vehicula leo leo eu ipsum. Pellentesque faucibus. Nullam porttitor ligula eget nibh. Cras elementum mi ac libero. Praesent pellentesque pede vitae quam. Sed nec arcu id ante cursus mollis. Suspendisse quis ipsum. Maecenas feugiat interdum neque. Nullam dui diam, convallis at, condimentum vitae, mattis vitae, metus. Integer sollicitudin, diam id lacinia posuere, quam velit fringilla dolor, eu semper sapien felis ac elit.",
////    "Ut a magna vitae lectus euismod hendrerit. Quisque varius consectetuer sapien. Suspendisse ligula. Nullam feugiat venenatis mauris. In consequat lorem at neque. Pellentesque libero. In eget lectus in velit auctor facilisis. Donec nec metus. Aliquam facilisis eros vel dui. Integer a diam. Donec interdum, eros faucibus blandit venenatis, ante ante ornare enim, a gravida ante lectus id metus. Ut sem.",
////    "Duis consectetuer leo quis elit. Suspendisse pretium nunc ac dolor. Quisque eleifend fringilla nisl. Suspendisse potenti. Duis vel ipsum at enim tincidunt consectetuer. Aliquam tempor justo nec metus. Nunc ac velit id nibh consequat vulputate. Cras vel dolor eu massa lacinia volutpat. Curabitur nibh nisi, auctor et, tincidunt eget, molestie vel, neque. Sed semper viverra neque. Nullam rhoncus hendrerit libero. Nulla adipiscing. Fusce pede nibh, lacinia a, malesuada a, dictum nec, pede. Etiam ut lorem. Donec quis massa vitae est pharetra mattis.",
////    "Nullam dui. Morbi nulla quam, imperdiet iaculis, consectetuer a, porttitor eu, sem. Donec id ipsum vitae nisi viverra porta. In hac habitasse platea dictumst. In ligula libero, dapibus eleifend, eleifend vel, accumsan sit amet, felis. Morbi tortor. Donec mattis ultricies arcu. Ut eget leo. Sed vel quam at ipsum sodales semper. Curabitur tincidunt quam id odio. Quisque porta, magna vel nonummy pulvinar, ligula tellus fringilla tellus, ut pharetra turpis velit ac eros. Cras eu enim vel mi suscipit malesuada. Phasellus ut orci. Aenean vitae turpis vitae lectus malesuada aliquet."
//
//        box = this.add(new TextRect(t));
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
//        //box.getRotation().set(xAng, yAng, 0);
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
//        if (e.getKeyCode() == KeyEvent.VK_UP) {
//            getCamera().camPos.add(new Vec3f(0.0f, 0, 0.1f));
//            getCamera().camTarget.add(new Vec3f(0.0f, 0, 0.1f));
//        }
//        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//            getCamera().camPos.add(new Vec3f(0.0f, 0, -0.1f));
//            getCamera().camTarget.add(new Vec3f(0.0f, 0, -0.1f));
//        }
//    }
//
//    public static void main(String[] args) {
//        new SwingWindow(new SGPanel(new DemoShapeTextRect()), 600, 400, true);
//    }
//
//    public <D extends Drawable> D add(D d) {
//        defaultSpace.add(d);
//        return d;
//    }
//
//    public boolean remove(Drawable d) {
//        return defaultSpace.remove(d);
//    }
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
