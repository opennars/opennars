//package automenta.spacegraph.physics;
//
//import automenta.spacegraph.physics.BodyControl.AbstractBodyControl;
//import java.util.WeakHashMap;
//import com.bulletphysics.BulletGlobals;
//import com.bulletphysics.BulletStats;
//import com.bulletphysics.collision.dispatch.CollisionObject;
//import com.bulletphysics.collision.dispatch.CollisionWorld;
//import com.bulletphysics.collision.shapes.BoxShape;
//import com.bulletphysics.collision.shapes.CollisionShape;
//import com.bulletphysics.demos.opengl.FastFormat;
//import com.bulletphysics.demos.opengl.IGL;
//import com.bulletphysics.dynamics.DynamicsWorld;
//import com.bulletphysics.dynamics.RigidBody;
//import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
//import com.bulletphysics.dynamics.constraintsolver.Point2PointConstraint;
//import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
//import com.bulletphysics.linearmath.CProfileIterator;
//import com.bulletphysics.linearmath.CProfileManager;
//import com.bulletphysics.linearmath.Clock;
//import com.bulletphysics.linearmath.DebugDrawModes;
//import com.bulletphysics.linearmath.DefaultMotionState;
//import com.bulletphysics.linearmath.QuaternionUtil;
//import com.bulletphysics.linearmath.Transform;
//import com.bulletphysics.linearmath.VectorUtil;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.vecmath.Color3f;
//import javax.vecmath.Matrix3f;
//import javax.vecmath.Quat4f;
//import javax.vecmath.Vector3f;
//import static com.bulletphysics.demos.opengl.IGL.*;
//
///**
// *
// * @author seh
// */
//public abstract class PhysicsApp {
//
//    //protected final BulletStack stack = BulletStack.get();
//    private static final float STEPSIZE = 5;
//    public static int numObjects = 0;
//    public static final int maxNumObjects = 16384;
//    public static Transform[] startTransforms = new Transform[maxNumObjects];
//    public static CollisionShape[] gShapePtr = new CollisionShape[maxNumObjects]; //1 rigidbody has 1 shape (no re-use of shapes)
//    public static RigidBody pickedBody = null; // for deactivation state
//
//    static {
//        for (int i = 0; i < startTransforms.length; i++) {
//            startTransforms[i] = new Transform();
//        }
//    }
//    // TODO: class CProfileIterator* m_profileIterator;
//    // JAVA NOTE: added
//    protected IGL gl;
//    protected Clock clock = new Clock();
//    // this is the most important class
//    protected DynamicsWorld dynamicsWorld = null;
//    // constraint for mouse picking
//    protected TypedConstraint pickConstraint = null;
//    protected CollisionShape shootBoxShape = null;
//    protected int debugMode = 0;
//
//    protected float cameraDistance = 15f;
//    protected float ele = 20f;
//    protected float azi = 180f;
//    protected float nextCameraDistance = cameraDistance;
//    protected float nextEle = ele;
//    protected float nextAzi = azi;
//
//    protected final Vector3f cameraPosition = new Vector3f(0f, 0f, 0f);
//    protected final Vector3f cameraTargetPosition = new Vector3f(0f, 0f, 0f); // look at
//    protected float scaleBottom = 0.5f;
//    protected float scaleFactor = 2f;
//    protected final Vector3f cameraUp = new Vector3f(0f, 1f, 0f);
//    protected int forwardAxis = 2;
//    protected int glutScreenWidth = 0;
//    protected int glutScreenHeight = 0;
//    protected float ShootBoxInitialSpeed = 40f;
//    protected boolean stepping = true;
//    protected boolean singleStep = false;
//    protected boolean idle = false;
//    protected int lastKey;
//    private CProfileIterator profileIterator;
//    private double zNear = 1.0;
//    private double zFar = 10000.0;
//    private float rotationMomentum = 0.99f;
//    private float cameraMomentum = 0.99f;
//
//    private WeakHashMap<RigidBody, BodyControl> bodyControl = new WeakHashMap();
//
//    public PhysicsApp() {
//        super();
//
//        //BulletStats.setProfileEnabled(true);
//        profileIterator = CProfileManager.getIterator();
//    }
//
//    public void init(IGL gl) {
//        this.gl = gl;
//        try {
//            initPhysics();
//        } catch (Exception ex) {
//            Logger.getLogger(PhysicsApp.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    protected abstract void initPhysics() throws Exception;
//
//    public void destroy() {
//        // TODO: CProfileManager::Release_Iterator(m_profileIterator);
//        //if (m_shootBoxShape)
//        //	delete m_shootBoxShape;
//    }
//
//    public void myinit() {
//        float[] light_ambient = new float[]{0.2f, 0.2f, 0.2f, 1.0f};
//        float[] light_diffuse = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
//        float[] light_specular = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
//        /* light_position is NOT default value */
//        float[] light_position0 = new float[]{1.0f, 10.0f, 1.0f, 0.0f};
//        float[] light_position1 = new float[]{-1.0f, -10.0f, -1.0f, 0.0f};
//
//        gl.glLight(GL_LIGHT0, GL_AMBIENT, light_ambient);
//        gl.glLight(GL_LIGHT0, GL_DIFFUSE, light_diffuse);
//        gl.glLight(GL_LIGHT0, GL_SPECULAR, light_specular);
//        gl.glLight(GL_LIGHT0, GL_POSITION, light_position0);
//
//        gl.glLight(GL_LIGHT1, GL_AMBIENT, light_ambient);
//        gl.glLight(GL_LIGHT1, GL_DIFFUSE, light_diffuse);
//        gl.glLight(GL_LIGHT1, GL_SPECULAR, light_specular);
//        gl.glLight(GL_LIGHT1, GL_POSITION, light_position1);
//
//        gl.glEnable(GL_LIGHTING);
//        gl.glEnable(GL_LIGHT0);
//        gl.glEnable(GL_LIGHT1);
//
//        gl.glShadeModel(GL_SMOOTH);
//        gl.glEnable(GL_DEPTH_TEST);
//        gl.glDepthFunc(GL_LESS);
//
//        gl.glClearColor(0.7f, 0.7f, 0.7f, 0f);
//
//        //glEnable(GL_CULL_FACE);
//        //glCullFace(GL_BACK);
//    }
//
//    public void setCameraDistance(float dist) {
//        cameraDistance = dist;
//    }
//
//    public float getCameraDistance() {
//        return cameraDistance;
//    }
//
//    public void toggleIdle() {
//        if (idle) {
//            idle = false;
//        } else {
//            idle = true;
//        }
//    }
//
//    public float lerp(float current, float next, float momentum) {
//        return current * momentum + (1.0f - momentum) * next;
//    }
//
//    public void updateCamera() {
//        gl.glMatrixMode(GL_PROJECTION);
//        gl.glLoadIdentity();
//
//        ele = lerp(ele, nextEle, rotationMomentum);
//        azi = lerp(azi, nextAzi, rotationMomentum);
//        cameraDistance = lerp(cameraDistance, nextCameraDistance, cameraMomentum);
//
//        float rele = ele * 0.01745329251994329547f; // rads per deg
//        float razi = azi * 0.01745329251994329547f; // rads per deg
//
//        Quat4f rot = new Quat4f();
//        QuaternionUtil.setRotation(rot, cameraUp, razi);
//
//        Vector3f eyePos = new Vector3f();
//        eyePos.set(0f, 0f, 0f);
//        VectorUtil.setCoord(eyePos, forwardAxis, -cameraDistance);
//
//        Vector3f forward = new Vector3f();
//        forward.set(eyePos.x, eyePos.y, eyePos.z);
//        if (forward.lengthSquared() < BulletGlobals.FLT_EPSILON) {
//            forward.set(1f, 0f, 0f);
//        }
//        Vector3f right = new Vector3f();
//        right.cross(cameraUp, forward);
//        Quat4f roll = new Quat4f();
//        QuaternionUtil.setRotation(roll, right, -rele);
//
//        Matrix3f tmpMat1 = new Matrix3f();
//        Matrix3f tmpMat2 = new Matrix3f();
//        tmpMat1.set(rot);
//        tmpMat2.set(roll);
//        tmpMat1.mul(tmpMat2);
//        tmpMat1.transform(eyePos);
//
//        cameraPosition.set(eyePos);
//
//        if (glutScreenWidth > glutScreenHeight) {
//            float aspect = glutScreenWidth / (float) glutScreenHeight;
//            gl.glFrustum(-aspect, aspect, -1.0, 1.0, zNear, zFar);
//        } else {
//            float aspect = glutScreenHeight / (float) glutScreenWidth;
//            gl.glFrustum(-1.0, 1.0, -aspect, aspect, zNear, zFar);
//        }
//
//        gl.glMatrixMode(GL_MODELVIEW);
//        gl.glLoadIdentity();
//        gl.gluLookAt(cameraPosition.x, cameraPosition.y, cameraPosition.z,
//                cameraTargetPosition.x, cameraTargetPosition.y, cameraTargetPosition.z,
//                cameraUp.x, cameraUp.y, cameraUp.z);
//    }
//
//    public void stepLeft() {
//        azi -= STEPSIZE;
//        if (azi < 0) {
//            azi += 360;
//        }
//        updateCamera();
//    }
//
//    public void stepRight() {
//        azi += STEPSIZE;
//        if (azi >= 360) {
//            azi -= 360;
//        }
//        updateCamera();
//    }
//
//    public void stepFront() {
//        ele += STEPSIZE;
//        if (ele >= 360) {
//            ele -= 360;
//        }
//        updateCamera();
//    }
//
//    public void stepBack() {
//        ele -= STEPSIZE;
//        if (ele < 0) {
//            ele += 360;
//        }
//        updateCamera();
//    }
//
//    public void zoomIn() {
//        cameraDistance -= 0.4f;
//        updateCamera();
//        if (cameraDistance < 0.1f) {
//            cameraDistance = 0.1f;
//        }
//    }
//
//    public void zoomOut() {
//        cameraDistance += 0.4f;
//        updateCamera();
//    }
//
//    public void reshape(int w, int h) {
//        glutScreenWidth = w;
//        glutScreenHeight = h;
//
//        gl.glViewport(0, 0, w, h);
//        updateCamera();
//    }
//
//    abstract public void mouseMotionFunc(int x, int y);
//    abstract public void mouseFunc(int button, int state, int x, int y);
//
////    public void keyboardCallback(char key, int x, int y, int modifiers) {
////        lastKey = 0;
////
////        if (key >= 0x31 && key < 0x37) {
////            int child = key - 0x31;
////            profileIterator.enterChild(child);
////        }
////        if (key == 0x30) {
////            profileIterator.enterParent();
////        }
////
////        switch (key) {
////            case 'l':
////                stepLeft();
////                break;
////            case 'r':
////                stepRight();
////                break;
////            case 'f':
////                stepFront();
////                break;
////            case 'b':
////                stepBack();
////                break;
////            case 'z':
////                zoomIn();
////                break;
////            case 'x':
////                zoomOut();
////                break;
////            case 'i':
////                toggleIdle();
////                break;
////            case 'h':
////                if ((debugMode & DebugDrawModes.NO_HELP_TEXT) != 0) {
////                    debugMode = debugMode & (~DebugDrawModes.NO_HELP_TEXT);
////                } else {
////                    debugMode |= DebugDrawModes.NO_HELP_TEXT;
////                }
////                break;
////
////            case 'w':
////                if ((debugMode & DebugDrawModes.DRAW_WIREFRAME) != 0) {
////                    debugMode = debugMode & (~DebugDrawModes.DRAW_WIREFRAME);
////                } else {
////                    debugMode |= DebugDrawModes.DRAW_WIREFRAME;
////                }
////                break;
////
////            case 'p':
////                if ((debugMode & DebugDrawModes.PROFILE_TIMINGS) != 0) {
////                    debugMode = debugMode & (~DebugDrawModes.PROFILE_TIMINGS);
////                } else {
////                    debugMode |= DebugDrawModes.PROFILE_TIMINGS;
////                }
////                break;
////
////            case 'm':
////                if ((debugMode & DebugDrawModes.ENABLE_SAT_COMPARISON) != 0) {
////                    debugMode = debugMode & (~DebugDrawModes.ENABLE_SAT_COMPARISON);
////                } else {
////                    debugMode |= DebugDrawModes.ENABLE_SAT_COMPARISON;
////                }
////                break;
////
////            case 'n':
////                if ((debugMode & DebugDrawModes.DISABLE_BULLET_LCP) != 0) {
////                    debugMode = debugMode & (~DebugDrawModes.DISABLE_BULLET_LCP);
////                } else {
////                    debugMode |= DebugDrawModes.DISABLE_BULLET_LCP;
////                }
////                break;
////
////            case 't':
////                if ((debugMode & DebugDrawModes.DRAW_TEXT) != 0) {
////                    debugMode = debugMode & (~DebugDrawModes.DRAW_TEXT);
////                } else {
////                    debugMode |= DebugDrawModes.DRAW_TEXT;
////                }
////                break;
////            case 'y':
////                if ((debugMode & DebugDrawModes.DRAW_FEATURES_TEXT) != 0) {
////                    debugMode = debugMode & (~DebugDrawModes.DRAW_FEATURES_TEXT);
////                } else {
////                    debugMode |= DebugDrawModes.DRAW_FEATURES_TEXT;
////                }
////                break;
////            case 'a':
////                if ((debugMode & DebugDrawModes.DRAW_AABB) != 0) {
////                    debugMode = debugMode & (~DebugDrawModes.DRAW_AABB);
////                } else {
////                    debugMode |= DebugDrawModes.DRAW_AABB;
////                }
////                break;
////            case 'c':
////                if ((debugMode & DebugDrawModes.DRAW_CONTACT_POINTS) != 0) {
////                    debugMode = debugMode & (~DebugDrawModes.DRAW_CONTACT_POINTS);
////                } else {
////                    debugMode |= DebugDrawModes.DRAW_CONTACT_POINTS;
////                }
////                break;
////
////            case 'd':
////                if ((debugMode & DebugDrawModes.NO_DEACTIVATION) != 0) {
////                    debugMode = debugMode & (~DebugDrawModes.NO_DEACTIVATION);
////                } else {
////                    debugMode |= DebugDrawModes.NO_DEACTIVATION;
////                }
////                if ((debugMode & DebugDrawModes.NO_DEACTIVATION) != 0) {
////                    BulletGlobals.setDeactivationDisabled(true);
////                } else {
////                    BulletGlobals.setDeactivationDisabled(false);
////                }
////                break;
////
////            case 'o': {
////                stepping = !stepping;
////                break;
////            }
////            case 's':
////                clientMoveAndDisplay();
////                break;
////            //    case ' ' : newRandom(); break;
////            case ' ':
////                clientResetScene();
////                break;
////            case '1': {
////                if ((debugMode & DebugDrawModes.ENABLE_CCD) != 0) {
////                    debugMode = debugMode & (~DebugDrawModes.ENABLE_CCD);
////                } else {
////                    debugMode |= DebugDrawModes.ENABLE_CCD;
////                }
////                break;
////            }
////
////            case '.': {
////                shootBox(getCameraTargetPosition());
////                break;
////            }
////
////            case '+': {
////                ShootBoxInitialSpeed += 10f;
////                break;
////            }
////            case '-': {
////                ShootBoxInitialSpeed -= 10f;
////                break;
////            }
////
////            default:
////                // std::cout << "unused key : " << key << std::endl;
////                break;
////        }
////
////        if (getDynamicsWorld() != null && getDynamicsWorld().getDebugDrawer() != null) {
////            getDynamicsWorld().getDebugDrawer().setDebugMode(debugMode);
////        }
////
////        //LWJGL.postRedisplay();
////    }
//    public int getDebugMode() {
//        return debugMode;
//    }
//
//    public void setDebugMode(int mode) {
//        debugMode = mode;
//        if (getDynamicsWorld() != null && getDynamicsWorld().getDebugDrawer() != null) {
//            getDynamicsWorld().getDebugDrawer().setDebugMode(mode);
//        }
//    }
//
//    public void specialKeyboardUp(int key, int x, int y, int modifiers) {
//        //LWJGL.postRedisplay();
//    }
//
////	public void specialKeyboard(int key, int x, int y, int modifiers) {
////		switch (key) {
////			case Keyboard.KEY_F1: {
////				break;
////			}
////			case Keyboard.KEY_F2: {
////				break;
////			}
////			case Keyboard.KEY_END: {
////				int numObj = getDynamicsWorld().getNumCollisionObjects();
////				if (numObj != 0) {
////					CollisionObject obj = getDynamicsWorld().getCollisionObjectArray().getQuick(numObj - 1);
////
////					getDynamicsWorld().removeCollisionObject(obj);
////					RigidBody body = RigidBody.upcast(obj);
////					if (body != null && body.getMotionState() != null) {
////						//delete body->getMotionState();
////					}
////					//delete obj;
////				}
////				break;
////			}
////			case Keyboard.KEY_LEFT:
////				stepLeft();
////				break;
////			case Keyboard.KEY_RIGHT:
////				stepRight();
////				break;
////			case Keyboard.KEY_UP:
////				stepFront();
////				break;
////			case Keyboard.KEY_DOWN:
////				stepBack();
////				break;
////			case Keyboard.KEY_PRIOR /* TODO: check PAGE_UP */:
////				zoomIn();
////				break;
////			case Keyboard.KEY_NEXT /* TODO: checkPAGE_DOWN */:
////				zoomOut();
////				break;
////			case Keyboard.KEY_HOME:
////				toggleIdle();
////				break;
////			default:
////				// std::cout << "unused (special) key : " << key << std::endl;
////				break;
////		}
////
////		//LWJGL.postRedisplay();
////	}
//    public void moveAndDisplay() {
//        if (!idle) {
//            clientMoveAndDisplay();
//        }
//    }
//
//    public Vector3f getRayTo(int x, int y) {
//        float top = 1f;
//        float bottom = -1f;
//        float nearPlane = 1f;
//        float tanFov = (top - bottom) * 0.5f / nearPlane;
//        float fov = 2f * (float) Math.atan(tanFov);
//
//        Vector3f rayFrom = new Vector3f(getCameraPosition());
//        Vector3f rayForward = new Vector3f();
//        rayForward.sub(getCameraTargetPosition(), getCameraPosition());
//        rayForward.normalize();
//        float farPlane = 10000f;
//        rayForward.scale(farPlane);
//
//        Vector3f rightOffset = new Vector3f();
//        Vector3f vertical = new Vector3f(cameraUp);
//
//        Vector3f hor = new Vector3f();
//        // TODO: check: hor = rayForward.cross(vertical);
//        hor.cross(rayForward, vertical);
//        hor.normalize();
//        // TODO: check: vertical = hor.cross(rayForward);
//        vertical.cross(hor, rayForward);
//        vertical.normalize();
//
//        float tanfov = (float) Math.tan(0.5f * fov);
//
//        float aspect = glutScreenHeight / (float) glutScreenWidth;
//
//        hor.scale(2f * farPlane * tanfov);
//        vertical.scale(2f * farPlane * tanfov);
//
//        if (aspect < 1f) {
//            hor.scale(1f / aspect);
//        } else {
//            vertical.scale(aspect);
//        }
//
//        Vector3f rayToCenter = new Vector3f();
//        rayToCenter.add(rayFrom, rayForward);
//        Vector3f dHor = new Vector3f(hor);
//        dHor.scale(1f / (float) glutScreenWidth);
//        Vector3f dVert = new Vector3f(vertical);
//        dVert.scale(1.f / (float) glutScreenHeight);
//
//        Vector3f tmp1 = new Vector3f();
//        Vector3f tmp2 = new Vector3f();
//        tmp1.scale(0.5f, hor);
//        tmp2.scale(0.5f, vertical);
//
//        Vector3f rayTo = new Vector3f();
//        rayTo.sub(rayToCenter, tmp1);
//        rayTo.add(tmp2);
//
//        tmp1.scale(x, dHor);
//        tmp2.scale(y, dVert);
//
//        rayTo.add(tmp1);
//        rayTo.sub(tmp2);
//        return rayTo;
//    }
//
//    public boolean removeBody(RigidBody b) {
//        dynamicsWorld.removeRigidBody(b);
//        setControl(b, null);
//        return false;
//    }
//
//    public RigidBody newRigidBody(float mass, Transform startTransform, CollisionShape shape) {
//        return newRigidBody(mass, startTransform, shape, (BodyControl)null);
//    }
//    public RigidBody newRigidBody(float mass, Transform startTransform, CollisionShape shape, Vector3f color) {
//        return newRigidBody(mass, startTransform, shape, new AbstractBodyControl(color));
//    }
//
//    /** creates a new rigid body and registers its controller, but does not add it to the world */
//    public RigidBody newRigidBody(float mass, Transform startTransform, CollisionShape shape, BodyControl control) {
//        // rigidbody is dynamic if and only if mass is non zero, otherwise static
//        boolean isDynamic = (mass != 0f);
//
//        Vector3f localInertia = new Vector3f(0f, 0f, 0f);
//        if (isDynamic) {
//            shape.calculateLocalInertia(mass, localInertia);
//        }
//
//        // using motionstate is recommended, it provides interpolation capabilities, and only synchronizes 'active' objects
//
//        //#define USE_MOTIONSTATE 1
//        //#ifdef USE_MOTIONSTATE
//        DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
//
//        RigidBodyConstructionInfo cInfo = new RigidBodyConstructionInfo(mass, myMotionState, shape, localInertia);
//
//        RigidBody body = new RigidBody(cInfo);
//        //#else
//        //btRigidBody* body = new btRigidBody(mass,0,shape,localInertia);
//        //body->setWorldTransform(startTransform);
//        //#endif//
//
//        setControl(body, control);
//
//        return body;
//    }
//
//    protected void setControl(RigidBody body, BodyControl control) {
//        if (control != null) {
//            bodyControl.put(body, control);
//        } else {
//            bodyControl.remove(body);
//        }
//    }
//
//    // See http://www.lighthouse3d.com/opengl/glut/index.php?bmpfontortho
//    public void setOrthographicProjection() {
//        // switch to projection mode
//        gl.glMatrixMode(GL_PROJECTION);
//
//        // save previous matrix which contains the
//        //settings for the perspective projection
//        gl.glPushMatrix();
//        // reset matrix
//        gl.glLoadIdentity();
//        // set a 2D orthographic projection
//        gl.gluOrtho2D(0f, glutScreenWidth, 0f, glutScreenHeight);
//        gl.glMatrixMode(GL_MODELVIEW);
//        gl.glLoadIdentity();
//
//        // invert the y axis, down is positive
//        gl.glScalef(1f, -1f, 1f);
//        // mover the origin from the bottom left corner
//        // to the upper left corner
//        gl.glTranslatef(0f, -glutScreenHeight, 0f);
//    }
//
//    public void resetPerspectiveProjection() {
//        gl.glMatrixMode(GL_PROJECTION);
//        gl.glPopMatrix();
//        gl.glMatrixMode(GL_MODELVIEW);
//        updateCamera();
//    }
//
//    private void displayProfileString(float xOffset, float yStart, CharSequence message) {
//        drawString(message, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
//    }
//    private static double time_since_reset = 0f;
//
//    private final Transform m = new Transform();
//    protected Color3f TEXT_COLOR = new Color3f(0f, 0f, 0f);
//    private StringBuilder buf = new StringBuilder();
//
//    public void renderme(double dt) {
//        updateCamera();
//
//        if (dynamicsWorld != null) {
//            for (int i = 0; i < dynamicsWorld.getNumCollisionObjects(); i++) {
//                CollisionObject colObj = dynamicsWorld.getCollisionObjectArray().getQuick(i);
//                RigidBody body = RigidBody.upcast(colObj);
//
//                if (body != null && body.getMotionState() != null) {
//                    DefaultMotionState myMotionState = (DefaultMotionState) body.getMotionState();
//                    m.set(myMotionState.graphicsWorldTrans);
//                } else {
//                    colObj.getWorldTransform(m);
//                }
//
//
//                BodyControl control = getControl(body);
//                if (control!=null)
//                    control.update(body, dt);
//
//                GLObjectDrawer.drawOpenGL(gl, m, colObj.getCollisionShape(), getDebugMode(), control);
//
//            }
//
//            float xOffset = 10f;
//            float yStart = 20f;
//            float yIncr = 20f;
//
//            gl.glDisable(GL_LIGHTING);
//            gl.glColor3f(0f, 0f, 0f);
//
//            if ((debugMode & DebugDrawModes.NO_HELP_TEXT) == 0) {
//                setOrthographicProjection();
//
//                yStart = showProfileInfo(xOffset, yStart, yIncr);
//
////					#ifdef USE_QUICKPROF
////					if ( getDebugMode() & btIDebugDraw::DBG_ProfileTimings)
////					{
////						static int counter = 0;
////						counter++;
////						std::map<std::string, hidden::ProfileBlock*>::iterator iter;
////						for (iter = btProfiler::mProfileBlocks.begin(); iter != btProfiler::mProfileBlocks.end(); ++iter)
////						{
////							char blockTime[128];
////							sprintf(blockTime, "%s: %lf",&((*iter).first[0]),btProfiler::getBlockTime((*iter).first, btProfiler::BLOCK_CYCLE_SECONDS));//BLOCK_TOTAL_PERCENT));
////							glRasterPos3f(xOffset,yStart,0);
////							BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),blockTime);
////							yStart += yIncr;
////
////						}
////					}
////					#endif //USE_QUICKPROF
//
//
////                String s = "mouse to interact";
////                drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
////
////                // JAVA NOTE: added
////                s = "LMB=drag, RMB=shoot box, MIDDLE=apply impulse";
////                drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
////
////                s = "space to reset";
////                drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
////
////                s = "cursor keys and z,x to navigate";
////                drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
////
////                s = "i to toggle simulation, s single step";
////                drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
////
////                s = "q to quit";
////                drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
////
////                s = ". to shoot box or trimesh (MovingConcaveDemo)";
////                drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
////
////                // not yet hooked up again after refactoring...
////
////                s = "d to toggle deactivation";
////                drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
////
////                s = "g to toggle mesh animation (ConcaveDemo)";
////                drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
////
////                // JAVA NOTE: added
////                s = "e to spawn new body (GenericJointDemo)";
////                drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
////
////                s = "h to toggle help text";
////                drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
////
////                //buf = "p to toggle profiling (+results to file)";
////                //drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
//
//                //bool useBulletLCP = !(getDebugMode() & btIDebugDraw::DBG_DisableBulletLCP);
//                //bool useCCD = (getDebugMode() & btIDebugDraw::DBG_EnableCCD);
//                //glRasterPos3f(xOffset,yStart,0);
//                //sprintf(buf,"1 CCD mode (adhoc) = %i",useCCD);
//                //BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
//                //yStart += yIncr;
//
////                //glRasterPos3f(xOffset, yStart, 0);
////                //buf = String.format(%10.2f", ShootBoxInitialSpeed);
////                buf.setLength(0);
////                buf.append("+- shooting speed = ");
////                FastFormat.append(buf, ShootBoxInitialSpeed);
////                drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
////
////                //#ifdef SHOW_NUM_DEEP_PENETRATIONS
////                buf.setLength(0);
////                buf.append("gNumDeepPenetrationChecks = ");
////                FastFormat.append(buf, BulletStats.gNumDeepPenetrationChecks);
////                drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
////
////                buf.setLength(0);
////                buf.append("gNumGjkChecks = ");
////                FastFormat.append(buf, BulletStats.gNumGjkChecks);
////                drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
////
////                buf.setLength(0);
////                buf.append("gNumSplitImpulseRecoveries = ");
////                FastFormat.append(buf, BulletStats.gNumSplitImpulseRecoveries);
////                drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
//
//                //buf = String.format("gNumAlignedAllocs = %d", BulletGlobals.gNumAlignedAllocs);
//                // TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
//                //yStart += yIncr;
//
//                //buf = String.format("gNumAlignedFree= %d", BulletGlobals.gNumAlignedFree);
//                // TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
//                //yStart += yIncr;
//
//                //buf = String.format("# alloc-free = %d", BulletGlobals.gNumAlignedAllocs - BulletGlobals.gNumAlignedFree);
//                // TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
//                //yStart += yIncr;
//
//                //enable BT_DEBUG_MEMORY_ALLOCATIONS define in Bullet/src/LinearMath/btAlignedAllocator.h for memory leak detection
//                //#ifdef BT_DEBUG_MEMORY_ALLOCATIONS
//                //glRasterPos3f(xOffset,yStart,0);
//                //sprintf(buf,"gTotalBytesAlignedAllocs = %d",gTotalBytesAlignedAllocs);
//                //BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
//                //yStart += yIncr;
//                //#endif //BT_DEBUG_MEMORY_ALLOCATIONS
//
////                if (getDynamicsWorld() != null) {
////                    buf.setLength(0);
////                    buf.append("# objects = ");
////                    FastFormat.append(buf, getDynamicsWorld().getNumCollisionObjects());
////                    drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                    yStart += yIncr;
////
////                    buf.setLength(0);
////                    buf.append("# pairs = ");
////                    FastFormat.append(buf, getDynamicsWorld().getBroadphase().getOverlappingPairCache().getNumOverlappingPairs());
////                    drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                    yStart += yIncr;
////
////                }
//                //#endif //SHOW_NUM_DEEP_PENETRATIONS
//
////                // JAVA NOTE: added
////                int free = (int) Runtime.getRuntime().freeMemory();
////                int total = (int) Runtime.getRuntime().totalMemory();
////                buf.setLength(0);
////                buf.append("heap = ");
////                FastFormat.append(buf, (float) (total - free) / (1024 * 1024));
////                buf.append(" / ");
////                FastFormat.append(buf, (float) (total) / (1024 * 1024));
////                buf.append(" MB");
////                drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
////                yStart += yIncr;
//
//                resetPerspectiveProjection();
//            }
//
//            gl.glEnable(GL_LIGHTING);
//        }
//
//        updateCamera();
//    }
//
//    public BodyControl getControl(final RigidBody b) {
//        return bodyControl.get(b);
//    }
//
//    public void clientResetScene() {
//        //#ifdef SHOW_NUM_DEEP_PENETRATIONS
//        BulletStats.gNumDeepPenetrationChecks = 0;
//        BulletStats.gNumGjkChecks = 0;
//        //#endif //SHOW_NUM_DEEP_PENETRATIONS
//
//        int numObjects = 0;
//        if (dynamicsWorld != null) {
//            dynamicsWorld.stepSimulation(1f / 60f, 0);
//            numObjects = dynamicsWorld.getNumCollisionObjects();
//        }
//
//        for (int i = 0; i < numObjects; i++) {
//            CollisionObject colObj = dynamicsWorld.getCollisionObjectArray().getQuick(i);
//            RigidBody body = RigidBody.upcast(colObj);
//            if (body != null) {
//                if (body.getMotionState() != null) {
//                    DefaultMotionState myMotionState = (DefaultMotionState) body.getMotionState();
//                    myMotionState.graphicsWorldTrans.set(myMotionState.startWorldTrans);
//                    colObj.setWorldTransform(myMotionState.graphicsWorldTrans);
//                    colObj.setInterpolationWorldTransform(myMotionState.startWorldTrans);
//                    colObj.activate();
//                }
//                // removed cached contact points
//                dynamicsWorld.getBroadphase().getOverlappingPairCache().cleanProxyFromPairs(colObj.getBroadphaseHandle(), getDynamicsWorld().getDispatcher());
//
//                body = RigidBody.upcast(colObj);
//                if (body != null && !body.isStaticObject()) {
//                    RigidBody.upcast(colObj).setLinearVelocity(new Vector3f(0f, 0f, 0f));
//                    RigidBody.upcast(colObj).setAngularVelocity(new Vector3f(0f, 0f, 0f));
//                }
//            }
//
//            /*
//            //quickly search some issue at a certain simulation frame, pressing space to reset
//            int fixed=18;
//            for (int i=0;i<fixed;i++)
//            {
//            getDynamicsWorld()->stepSimulation(1./60.f,1);
//            }
//             */
//        }
//    }
//
//    public DynamicsWorld getDynamicsWorld() {
//        return dynamicsWorld;
//    }
//
//    public void setCameraUp(Vector3f camUp) {
//        cameraUp.set(camUp);
//    }
//
//    public void setCameraForwardAxis(int axis) {
//        forwardAxis = axis;
//    }
//
//    public Vector3f getCameraPosition() {
//        return cameraPosition;
//    }
//
//    public Vector3f getCameraTargetPosition() {
//        return cameraTargetPosition;
//    }
//
//    public float getDeltaTimeMicroseconds() {
//        //#ifdef USE_BT_CLOCK
//        float dt = clock.getTimeMicroseconds();
//        clock.reset();
//        return dt;
//        //#else
//        //return btScalar(16666.);
//        //#endif
//    }
//
//    public abstract void clientMoveAndDisplay();
//
//    public boolean isIdle() {
//        return idle;
//    }
//
//    public void setIdle(boolean idle) {
//        this.idle = idle;
//    }
//
//    public void drawString(CharSequence s, int x, int y, Color3f color) {
//        gl.drawString(s, x, y, color.x, color.y, color.z);
//    }
//
//    protected float showProfileInfo(float xOffset, float yStart, float yIncr) {
//        if (!idle) {
//            time_since_reset = CProfileManager.getTimeSinceReset();
//        }
//
//        {
//            // recompute profiling data, and store profile strings
//
//            double totalTime = 0;
//
//            int frames_since_reset = CProfileManager.getFrameCountSinceReset();
//
//            profileIterator.first();
//
//            double parent_time = profileIterator.isRoot() ? time_since_reset : profileIterator.getCurrentParentTotalTime();
//
//            {
//                buf.setLength(0);
//                buf.append("--- Profiling: ");
//                buf.append(profileIterator.getCurrentParentName());
//                buf.append(" (total running time: ");
//                FastFormat.append(buf, (float) parent_time, 3);
//                buf.append(" ms) ---");
//                displayProfileString(xOffset, yStart, buf);
//                yStart += yIncr;
//                String s = "press number (1,2...) to display child timings, or 0 to go up to parent";
//                displayProfileString(xOffset, yStart, s);
//                yStart += yIncr;
//            }
//
//            double accumulated_time = 0.f;
//
//            for (int i = 0; !profileIterator.isDone(); profileIterator.next()) {
//                double current_total_time = profileIterator.getCurrentTotalTime();
//                accumulated_time += current_total_time;
//                double fraction = parent_time > BulletGlobals.FLT_EPSILON ? (current_total_time / parent_time) * 100 : 0f;
//
//                buf.setLength(0);
//                FastFormat.append(buf, ++i);
//                buf.append(" -- ");
//                buf.append(profileIterator.getCurrentName());
//                buf.append(" (");
//                FastFormat.append(buf, (float) fraction, 2);
//                buf.append(" %) :: ");
//                FastFormat.append(buf, (float) (current_total_time / (double) frames_since_reset), 3);
//                buf.append(" ms / frame (");
//                FastFormat.append(buf, profileIterator.getCurrentTotalCalls());
//                buf.append(" calls)");
//
//                displayProfileString(xOffset, yStart, buf);
//                yStart += yIncr;
//                totalTime += current_total_time;
//            }
//
//            buf.setLength(0);
//            buf.append("Unaccounted (");
//            FastFormat.append(buf, (float) (parent_time > BulletGlobals.FLT_EPSILON ? ((parent_time - accumulated_time) / parent_time) * 100 : 0.f), 3);
//            buf.append(" %) :: ");
//            FastFormat.append(buf, (float) (parent_time - accumulated_time), 3);
//            buf.append(" ms");
//
//            displayProfileString(xOffset, yStart, buf);
//            yStart += yIncr;
//
//            String s = "-------------------------------------------------";
//            displayProfileString(xOffset, yStart, s);
//            yStart += yIncr;
//
//        }
//
//        return yStart;
//    }
//
//}
