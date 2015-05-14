///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package automenta.spacegraph.demo.physics;
//
//import com.bulletphysics.collision.shapes.BoxShape;
//import javax.vecmath.Quat4f;
//import com.bulletphysics.BulletStats;
//import com.bulletphysics.collision.dispatch.CollisionObject;
//import com.bulletphysics.collision.dispatch.CollisionWorld;
//import com.bulletphysics.dynamics.RigidBody;
//import com.bulletphysics.dynamics.constraintsolver.Point2PointConstraint;
//import com.bulletphysics.linearmath.Transform;
//import javax.vecmath.Vector3f;
//import automenta.spacegraph.physics.PhysicsController;
//import automenta.spacegraph.physics.PhysicsPanel;
//import automenta.spacegraph.swing.SwingWindow;
//import com.bulletphysics.demos.opengl.GLDebugDrawer;
//import com.bulletphysics.dynamics.DynamicsWorld;
//import com.bulletphysics.collision.broadphase.DbvtBroadphase;
//import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
//import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
//import automenta.spacegraph.physics.PhysicsApp;
//import com.bulletphysics.collision.broadphase.BroadphaseInterface;
//import com.bulletphysics.collision.dispatch.CollisionDispatcher;
//import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
//import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
//import static com.bulletphysics.demos.opengl.IGL.*;
//
///**
// *
// * @author seh
// */
//abstract public class DefaultPhysicsApp extends PhysicsApp {
//
//    // keep the collision shapes, for deletion/cleanup
//    //protected ObjectArrayList<CollisionShape> collisionShapes = new ObjectArrayList<CollisionShape>();
//    protected BroadphaseInterface broadphase;
//    protected CollisionDispatcher dispatcher;
//    protected ConstraintSolver solver;
//    protected DefaultCollisionConfiguration collisionConfiguration;
//    public final static Vector3f zero3 = new Vector3f(0, 0, 0);
//    private static float mousePickClamping = 3f;
//
//    private float minCameraDistance = 1.0f;
//    private float maxCameraDistance = 50.0f;
//
//    private boolean rotating = false;
//    private boolean zooming = false;
//    int lastMouseX, lastMouseY;
//
//    public DefaultPhysicsApp() {
//        super();
//    }
//
//    public DynamicsWorld getSpace() {
//        return dynamicsWorld;
//    }
//
//    @Override
//    public void clientMoveAndDisplay() {
//        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//
//        // simple dynamics world doesn't handle fixed-time-stepping
//        float ms = getDeltaTimeMicroseconds();
//
//        double s = ms / 1000000f;
//
//        // step the simulation
//        if (dynamicsWorld != null) {
//            dynamicsWorld.stepSimulation((float)s);
//            // optional but useful: debug drawing
//            dynamicsWorld.debugDrawWorld();
//        }
//
//        renderme(s);
//
//        //glFlush();
//        //glutSwapBuffers();
//    }
//
////    @Override
////    public void displayCallback() {
////        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
////
////        renderme();
////
////        // optional but useful: debug drawing to detect problems
////        if (dynamicsWorld != null) {
////            dynamicsWorld.debugDrawWorld();
////        }
////
////        //glFlush();
////        //glutSwapBuffers();
////    }
//
//
//    public void initPhysics() {
//        setCameraDistance(50f);
//
//        // collision configuration contains default setup for memory, collision setup
//        collisionConfiguration = new DefaultCollisionConfiguration();
//
//        // use the default collision dispatcher. For parallel processing you can use a diffent dispatcher (see Extras/BulletMultiThreaded)
//        dispatcher = new CollisionDispatcher(collisionConfiguration);
//
//        broadphase = new DbvtBroadphase();
//
//        // the default constraint solver. For parallel processing you can use a different solver (see Extras/BulletMultiThreaded)
//        SequentialImpulseConstraintSolver sol = new SequentialImpulseConstraintSolver();
//        solver = sol;
//
//        // TODO: needed for SimpleDynamicsWorld
//        //sol.setSolverMode(sol.getSolverMode() & ~SolverMode.SOLVER_CACHE_FRIENDLY.getMask());
//
//        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
//
//        getSpace().setGravity(zero3);
//
//        initApp();
//
//        clientResetScene();
//    }
//
//    abstract protected void initApp();
//
//    public void mouseFunc(int button, int state, int x, int y) {
//        //printf("button %i, state %i, x=%i,y=%i\n",button,state,x,y);
//        //button 0, state 0 means left mouse down
//
//        Vector3f rayTo = new Vector3f(getRayTo(x, y));
//
//
//        switch (button) {
//            case 2: {
//                rotating = (state == 0);
//                break;
//            }
//            case 1: {
//                zooming = (state == 0);
//                break;
//            }
////            case 1: {
////                if (state == 0) {
////                    // apply an impulse
////                    if (dynamicsWorld != null) {
////                        CollisionWorld.ClosestRayResultCallback rayCallback = new CollisionWorld.ClosestRayResultCallback(cameraPosition, rayTo);
////                        dynamicsWorld.rayTest(cameraPosition, rayTo, rayCallback);
////                        if (rayCallback.hasHit()) {
////                            RigidBody body = RigidBody.upcast(rayCallback.collisionObject);
////                            if (body != null) {
////                                body.setActivationState(CollisionObject.ACTIVE_TAG);
////                                Vector3f impulse = new Vector3f(rayTo);
////                                impulse.normalize();
////                                float impulseStrength = 10f;
////                                impulse.scale(impulseStrength);
////                                Vector3f relPos = new Vector3f();
////                                relPos.sub(rayCallback.hitPointWorld, body.getCenterOfMassPosition(new Vector3f()));
////                                body.applyImpulse(impulse, relPos);
////                            }
////                        }
////                    }
////                } else {
////                }
////                break;
////            }
//            case 0: {
//                if (state == 0) {
//                    // add a point to point constraint for picking
//                    if (dynamicsWorld != null) {
//                        CollisionWorld.ClosestRayResultCallback rayCallback = new CollisionWorld.ClosestRayResultCallback(cameraPosition, rayTo);
//                        dynamicsWorld.rayTest(cameraPosition, rayTo, rayCallback);
//                        if (rayCallback.hasHit()) {
//                            RigidBody body = RigidBody.upcast(rayCallback.collisionObject);
//                            if (body != null) {
//                                // other exclusions?
//                                if (!(body.isStaticObject() || body.isKinematicObject())) {
//                                    pickedBody = body;
//                                    pickedBody.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
//
//                                    Vector3f pickPos = new Vector3f(rayCallback.hitPointWorld);
//
//                                    Transform tmpTrans = body.getCenterOfMassTransform(new Transform());
//                                    tmpTrans.inverse();
//                                    Vector3f localPivot = new Vector3f(pickPos);
//                                    tmpTrans.transform(localPivot);
//
//                                    Point2PointConstraint p2p = new Point2PointConstraint(body, localPivot);
//                                    p2p.setting.impulseClamp = mousePickClamping;
//
//                                    dynamicsWorld.addConstraint(p2p);
//                                    pickConstraint = p2p;
//                                    // save mouse position for dragging
//                                    BulletStats.gOldPickingPos.set(rayTo);
//                                    Vector3f eyePos = new Vector3f(cameraPosition);
//                                    Vector3f tmp = new Vector3f();
//                                    tmp.sub(pickPos, eyePos);
//                                    BulletStats.gOldPickingDist = tmp.length();
//                                    // very weak constraint for picking
//                                    p2p.setting.tau = 0.1f;
//                                }
//                            }
//                        }
//                    }
//
//                } else {
//
//                    if (pickConstraint != null && dynamicsWorld != null) {
//                        dynamicsWorld.removeConstraint(pickConstraint);
//                        // delete m_pickConstraint;
//                        //printf("removed constraint %i",gPickingConstraintId);
//                        pickConstraint = null;
//                        pickedBody.forceActivationState(CollisionObject.ACTIVE_TAG);
//                        pickedBody.setDeactivationTime(0f);
//                        pickedBody = null;
//                    }
//                }
//                break;
//            }
//            default: {
//            }
//        }
//    }
//
//    public void mouseMotionFunc(int x, int y) {
//        if (pickConstraint != null) {
//            // move the constraint pivot
//            Point2PointConstraint p2p = (Point2PointConstraint) pickConstraint;
//            if (p2p != null) {
//                // keep it at the same picking distance
//
//                Vector3f newRayTo = new Vector3f(getRayTo(x, y));
//                Vector3f eyePos = new Vector3f(cameraPosition);
//                Vector3f dir = new Vector3f();
//                dir.sub(newRayTo, eyePos);
//                dir.normalize();
//                dir.scale(BulletStats.gOldPickingDist);
//
//                Vector3f newPos = new Vector3f();
//                newPos.add(eyePos, dir);
//                p2p.setPivotB(newPos);
//            }
//        }
//        if (rotating) {
//            nextAzi+= (x - lastMouseX);
//            nextEle+= (y - lastMouseY);
//            //nextEle = Math.min(180, Math.max(-180, nextEle));
//
//            lastMouseX = x;
//            lastMouseY = y;
//        } else if (zooming) {
//            nextCameraDistance -= (y - lastMouseY);
//            nextCameraDistance = Math.min(maxCameraDistance, Math.max(minCameraDistance, nextCameraDistance));
//        } else {
//            lastMouseX = x;
//            lastMouseY = y;
//         }
//    }
//
//    public static void start(final PhysicsApp app) {
//        PhysicsPanel surface = new PhysicsPanel();
//        app.init(surface);
//        app.getDynamicsWorld().setDebugDrawer(new GLDebugDrawer(surface));
//        new PhysicsController(surface, app);
//        new SwingWindow(surface, 800, 600, true);
//    }
//
//    public void shootBox(Vector3f destination) {
//        if (dynamicsWorld != null) {
//            float mass = 10f;
//            Transform startTransform = new Transform();
//            startTransform.setIdentity();
//            Vector3f camPos = new Vector3f(getCameraPosition());
//            startTransform.origin.set(camPos);
//
//            if (shootBoxShape == null) {
//                //#define TEST_UNIFORM_SCALING_SHAPE 1
//                //#ifdef TEST_UNIFORM_SCALING_SHAPE
//                //btConvexShape* childShape = new btBoxShape(btVector3(1.f,1.f,1.f));
//                //m_shootBoxShape = new btUniformScalingShape(childShape,0.5f);
//                //#else
//                shootBoxShape = new BoxShape(new Vector3f(1f, 1f, 1f));
//                //#endif//
//            }
//
//            RigidBody body = this.newRigidBody(mass, startTransform, shootBoxShape, new Vector3f(1f, 0.2f, 0.2f));
//            dynamicsWorld.addRigidBody(body);
//
//            Vector3f linVel = new Vector3f(destination.x - camPos.x, destination.y - camPos.y, destination.z - camPos.z);
//            linVel.normalize();
//            linVel.scale(ShootBoxInitialSpeed);
//
//            Transform worldTrans = body.getWorldTransform(new Transform());
//            worldTrans.origin.set(camPos);
//            worldTrans.setRotation(new Quat4f(0f, 0f, 0f, 1f));
//            body.setWorldTransform(worldTrans);
//
//            body.setLinearVelocity(linVel);
//            body.setAngularVelocity(new Vector3f(0f, 0f, 0f));
//
//            body.setCcdMotionThreshold(1f);
//            body.setCcdSweptSphereRadius(0.2f);
//        }
//    }
//
//}
