///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package automenta.spacegraph.demo.physics;
//
//import automenta.spacegraph.physics.BodyControl;
//import automenta.spacegraph.physics.BodyControl.MatrixScreen;
//import com.bulletphysics.dynamics.RigidBody;
//import javax.vecmath.Vector3f;
//
///**
// *
// * @author seh
// */
//public class DemoBlockBitmap extends DemoBlock {
//
//    public DemoBlockBitmap() {
//        super();
//    }
//
//    @Override
//    public BodyControl getBlockControl() {
//        Vector3f c = new Vector3f(0.5f, 0.8f, 0.5f);
//
//        Vector3f[][] bitmap = getMatrix(4, 4, "100010001111010101001");
//
//        MatrixScreen control = new MatrixScreen(c, bitmap) {
//
//            float t = 0;
//
//            @Override
//            public void update(RigidBody r, double dt) {
//                super.update(r, dt);
//                setPixelRatio(0.5f + (float)(Math.sin(t) + 1.0)/2.0f);
//                t += dt;
//            }
//
//        };
//        control.setPixelRatio(0.75f);
//
//        return control;
//    }
//
//    public static Vector3f[][] getMatrix(int x, int y, String string) {
//        final Vector3f onColor = new Vector3f(1.0f, 1.0f, 1.0f);
//        final Vector3f offColor = new Vector3f(0f, 0f, 0f);
//
//        Vector3f[][] v = new Vector3f[y][];
//        int p = 0;
//        for (int j = 0; j < y; j++) {
//            v[j] = new Vector3f[x];
//            for (int i = 0; i < x; i++) {
//                char c = string.charAt(p++);
//                v[j][i] = c == '0' ? offColor : onColor;
//            }
//        }
//        return v;
//    }
//
//    public static void main(String[] args) {
//        start(new DemoBlockBitmap());
//    }
//
//}
