///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package automenta.spacegraph.demo.physics;
//
//import automenta.spacegraph.physics.BodyControl;
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
//public abstract class DemoBlock extends DefaultPhysicsApp {
//
//    @Override
//    protected void initApp() {
//
//        CollisionShape colShape = new BoxShape(new Vector3f(10, 10, 1));
//
//        // Create Dynamic Objects
//        Transform startTransform = new Transform();
//        startTransform.setIdentity();
//
//        float mass = 1000f;
//
//        startTransform.origin.set(
//                0,
//                10f + 0,
//                0);
//
//
//        RigidBody body = newRigidBody(mass, startTransform, colShape, getBlockControl());
//
//        getSpace().addRigidBody(body);
//
//    }
//
//    abstract public BodyControl getBlockControl();
//
//
//}
