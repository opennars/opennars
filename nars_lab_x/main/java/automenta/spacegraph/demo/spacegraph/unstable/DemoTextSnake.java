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
public class DemoTextSnake extends Surface {

//    private List<TextRect> tr = new LinkedList();
//    float o = 0, ov = 0;
//    private String text;
//    private final Space2D defaultSpace;
//
//    public DemoTextSnake() {
//        super();
//
//        setText("Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Nulla in mi ut augue laoreet gravida. Quisque sodales vehicula ligula.  " +
//    "Etiam varius sagittis lorem. Vivamus iaculis condimentum tortor. Nunc sollicitudin scelerisque dolor. Nunc condimentum fringilla nisl. Fusce purus mauris, blandit eu, lacinia eget, vestibulum nec, massa. Nulla vitae libero. Suspendisse potenti. Aliquam iaculis, lorem eu adipiscing tempor, ipsum dui aliquam sem, eu vehicula leo leo eu ipsum. Pellentesque faucibus. Nullam porttitor ligula eget nibh. Cras elementum mi ac libero. Praesent pellentesque pede vitae quam. Sed nec arcu id ante cursus mollis. Suspendisse quis ipsum. Maecenas feugiat interdum neque. Nullam dui diam, convallis at, condimentum vitae, mattis vitae, metus. Integer sollicitudin, diam id lacinia posuere, quam velit fringilla dolor, eu semper sapien felis ac elit.");
////    "Ut a magna vitae lectus euismod hendrerit. Quisque varius consectetuer sapien. Suspendisse ligula. Nullam feugiat venenatis mauris. In consequat lorem at neque. Pellentesque libero. In eget lectus in velit auctor facilisis. Donec nec metus. Aliquam facilisis eros vel dui. Integer a diam. Donec interdum, eros faucibus blandit venenatis, ante ante ornare enim, a gravida ante lectus id metus. Ut sem.",
////    "Duis consectetuer leo quis elit. Suspendisse pretium nunc ac dolor. Quisque eleifend fringilla nisl. Suspendisse potenti. Duis vel ipsum at enim tincidunt consectetuer. Aliquam tempor justo nec metus. Nunc ac velit id nibh consequat vulputate. Cras vel dolor eu massa lacinia volutpat. Curabitur nibh nisi, auctor et, tincidunt eget, molestie vel, neque. Sed semper viverra neque. Nullam rhoncus hendrerit libero. Nulla adipiscing. Fusce pede nibh, lacinia a, malesuada a, dictum nec, pede. Etiam ut lorem. Donec quis massa vitae est pharetra mattis.",
////    "Nullam dui. Morbi nulla quam, imperdiet iaculis, consectetuer a, porttitor eu, sem. Donec id ipsum vitae nisi viverra porta. In hac habitasse platea dictumst. In ligula libero, dapibus eleifend, eleifend vel, accumsan sit amet, felis. Morbi tortor. Donec mattis ultricies arcu. Ut eget leo. Sed vel quam at ipsum sodales semper. Curabitur tincidunt quam id odio. Quisque porta, magna vel nonummy pulvinar, ligula tellus fringilla tellus, ut pharetra turpis velit ac eros. Cras eu enim vel mi suscipit malesuada. Phasellus ut orci. Aenean vitae turpis vitae lectus malesuada aliquet."
//
//    }
//
//    public void setText(String t) {
//        tr.clear();
//        //removeAll();
//        
//        this.text = t;
//
//        for (int i = 0; i < text.length(); i++) {
//            char c = text.charAt(i);
//            TextRect r = new TextRect(new String(new char[] { c }));
//            add(r);
//            tr.add(r);
//        }
//    }
//
//    @Override
//    public void mouseDragged(MouseEvent e) {
//        super.mouseDragged(e);
//    }
//    
//            Vec3f v = new Vec3f();
//
//    @Override
//    protected void updateSpace(GL2 gl) {
//        o += ov * getDT();
//
//        float t = -text.length()/2 + o;
//        float width = 1.5f;
//        float charsPerLine = 32.0f;
//        for (TextRect r : tr) {
////            float tx = (float)Math.sin(2.0f*t/charsPerLine)*width;
////            float ty = (t + (float)Math.sin(t/charsPerLine))/(charsPerLine);
////            float ta = ty * 2f * 180.0f;
//
//            float tx = (float)Math.sin(3.0f*t/charsPerLine)*width;
//            float ty = (t /*+ (float)Math.sin(t/charsPerLine)*0.5f*/)/(charsPerLine);
//            float tz = -Math.abs(ty)*1.5f;
//            float ta = ty * 2f * 180.0f;
//
//            //constraint: tx(o)=0, ty(o)=0, ta(o) = 0
//            r.getCenter().set(tx, ty, tz);
//            r.getRotation().set(0, 0, ta);
//
//            //box.getSize().set(1.0f + (float)Math.cos(getT())/2f, 1.0f + (float)Math.sin(getT())/2f, 1.0f);
//            t+= 1.0f;// / ((float)(tr.size()));
//        }
//    }
//
//    @Override
//    public void keyPressed(KeyEvent e) {
//        super.keyPressed(e);
//        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
////            getCamera().camPos.add(new Vec3f(-0.1f, 0, 0));
////            getCamera().camTarget.add(new Vec3f(-0.1f, 0, 0));
//            ov -= getDT() * 7.5;
//        }
//        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
////            getCamera().camPos.add(new Vec3f(0.1f, 0, 0));
////            getCamera().camTarget.add(new Vec3f(0.1f, 0, 0));
//            ov += getDT() * 7.5;
//        }
//        if (e.getKeyCode() == KeyEvent.VK_UP) {
//            getCamera().camPos.add(new Vec3f(0.0f, 0, 0.1f));
//            getCamera().camTarget.add(new Vec3f(0.0f, 0, 0.1f));
//        }
//        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//            getCamera().camPos.add(new Vec3f(0.0f, 0, -0.1f));
//            getCamera().camTarget.add(new Vec3f(0.0f, 0, -0.1f));
//        }
//        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//            setText(JOptionPane.showInputDialog(null, "Text"));
//        }
//    }
//
//    public static void main(String[] args) {
//        new SGWindow("DemoSGCanvas", new DemoTextSnake());
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
