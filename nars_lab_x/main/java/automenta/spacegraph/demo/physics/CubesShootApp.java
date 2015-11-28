///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package automenta.spacegraph.demo.physics;
//
//import com.bulletphysics.collision.shapes.BoxShape;
//import com.bulletphysics.collision.shapes.CollisionShape;
//import com.bulletphysics.dynamics.RigidBody;
//import com.bulletphysics.linearmath.Transform;
//import javax.vecmath.Vector3f;
//
///**
// *
// * @author seh
// */
//public class CubesShootApp extends DefaultPhysicsApp {
//
//    // create 125 (5x5x5) dynamic object
//    private static final int ARRAY_SIZE_X = 5;
//    private static final int ARRAY_SIZE_Y = 5;
//    private static final int ARRAY_SIZE_Z = 5;
//    // maximum number of objects (and allow user to shoot additional boxes)
//    private static final int MAX_PROXIES = (ARRAY_SIZE_X * ARRAY_SIZE_Y * ARRAY_SIZE_Z + 1024);
//    private static final int START_POS_X = -5;
//    private static final int START_POS_Y = -5;
//    private static final int START_POS_Z = -3;
//
//    public CubesShootApp() {
//        super();
//    }
//
//    public Vector3f getDefaultGravity() {
//        return new Vector3f(0f, -10f, 0f);
//    }
//
//    public void addGround() {
//        CollisionShape groundShape = new BoxShape(new Vector3f(50f, 25f, 50f));
//        //collisionShapes.add(groundShape);
//
//        Transform groundTransform = new Transform();
//        groundTransform.setIdentity();
//        groundTransform.origin.set(0, -56, 0);
//
//        Vector3f c = new Vector3f(0.4f, 1.0f, 0.4f);
//        getSpace().addRigidBody(newRigidBody(0f, groundTransform, groundShape, c));
//    }
//
//    public static class ZeroGravityCubes extends CubesShootApp {
//
//        @Override
//        public Vector3f getDefaultGravity() {
//            return new Vector3f(0f, 0f, 0f);
//        }
//
//        public void addGround() { /* no ground */ }
//    }
//
//    @Override
//    protected void initApp() {
//        getSpace().setGravity(getDefaultGravity());
//
//        addGround();
//
//        float start_x = START_POS_X - ARRAY_SIZE_X / 2;
//        float start_y = START_POS_Y;
//        float start_z = START_POS_Z - ARRAY_SIZE_Z / 2;
//
//        for (int k = 0; k < ARRAY_SIZE_Y; k++) {
//            for (int i = 0; i < ARRAY_SIZE_X; i++) {
//                for (int j = 0; j < ARRAY_SIZE_Z; j++) {
//
//                    CollisionShape colShape = new BoxShape(new Vector3f(1, 1, 1));
//                    //CollisionShape colShape = new SphereShape(1f);
//                    //collisionShapes.add(colShape);
//
//                    // Create Dynamic Objects
//                    Transform startTransform = new Transform();
//                    startTransform.setIdentity();
//
//                    float mass = 1f;
//
//                    startTransform.origin.set(
//                            2f * i + start_x,
//                            10f + 2f * k + start_y,
//                            2f * j + start_z);
//
//                    Vector3f c = new Vector3f((float)k / (float)ARRAY_SIZE_Y, 0.2f, 1.0f);
//
//                    RigidBody body = newRigidBody(mass, startTransform, colShape, c);
//
//                    getSpace().addRigidBody(body);
//                }
//            }
//        }
//    }
//
//    public static void main(String[] args) {
//        start(new CubesShootApp());
//    }
//
//}
