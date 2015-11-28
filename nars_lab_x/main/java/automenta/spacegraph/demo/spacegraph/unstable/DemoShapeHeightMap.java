///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package automenta.spacegraph.demo.spacegraph.unstable;
//
//import automenta.spacegraph.Surface;
//import automenta.spacegraph.video.SGWindow;
//import automenta.spacegraph.math.linalg.Vec3f;
//import automenta.spacegraph.shape.HeightMap;
//import javax.media.opengl.GL2;
//
///**
// *
// * @author seh
// */
//public class DemoShapeHeightMap extends Surface {
////    private final HeightMap h;
//
//    public DemoShapeHeightMap() {
////        h = add(new HeightMap() {
////
////            @Override public float getMaxX() { return 4.0f;            }
////            @Override public float getMaxY() { return 4.0f;            }
////            @Override public float getMinX() { return -4.0f;            }
////            @Override public float getMinY() { return -4.0f;            }
////
////            @Override
////            public float getStepSize() {
////                return 0.1f;
////            }
////
////            @Override
////            public float getValue(float x, float y) {
////                return (float)(Math.cos(x+ getT()) + Math.sin(y + getT()))*0.5f;
////                //return ((int)(x + y) % 4)/4.0f;
////            }
////
////
////            @Override
////            public void getVertexColor(float x, float y, float[] color) {
////                color[0] = (float)Math.random();
////                color[1] = 1f;
////                color[2] = 1f;
////                color[3] = 1f;
////            }
////        });
//    }
//
//    @Override
//    protected void updateSpace(GL2 gl) {
//        super.updateSpace(gl);
//
////        h.getCenter().set(0, 0, -2);
////        h.getRotation().add(new Vec3f((float)(166.5f * getDT()), 0.1f, 0));
//    }
//
//    public static void main(String[] args) {
//        new SGWindow("HeightMap", new DemoShapeHeightMap());
//    }
//
//}
