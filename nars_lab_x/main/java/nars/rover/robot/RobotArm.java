package nars.rover.robot;

/**
 *
 * @author me
 */
abstract public class RobotArm extends Robotic {

    public RobotArm(String id) {
        super(id);
    }
//
//    protected RevoluteJoint shoulderJoint;
//    //protected PrismaticJoint m_joint2;
//    protected RevoluteJoint fingerLeftJoint;
//    protected RevoluteJoint fingerRightJoint;
//    protected RevoluteJoint elbowJoint;
//    protected Body lowerArm;
//    protected Body upperArm;
//
//    @Override
//    public void initTest(boolean argDeserialized) {
//
//        for (int i = 0; i < 10; i++) {
//            getCamera().zoomToPoint(new Vec2(0, 0), PhysicsCamera.ZoomType.ZOOM_IN);
//        }
//
//        Body ground = null;
//        {
//            BodyDef bd = new BodyDef();
//            ground = getWorld().createBody(bd);
//
//            EdgeShape shape = new EdgeShape();
//            shape.set(new Vec2(-40.0f, 0.0f), new Vec2(40.0f, 0.0f));
//            ground.createFixture(shape, 0.0f);
//        }
//
//        {
//            Body prevBody = ground;
//
//            // Define upper arm.
//            {
//                PolygonShape shape = new PolygonShape();
//                shape.setAsBox(0.5f, 4.0f);
//
//                BodyDef bd = new BodyDef();
//                bd.type = BodyType.DYNAMIC;
//                bd.position.set(0.0f, 5.0f);
//                upperArm = getWorld().createBody(bd);
//                upperArm.createFixture(shape, 2.0f);
//
//                RevoluteJointDef rjd = new RevoluteJointDef();
//                rjd.initialize(prevBody, upperArm, new Vec2(0.0f, 2.0f));
//                rjd.motorSpeed = 0; //1.0f * MathUtils.PI;
//                rjd.maxMotorTorque = 10000.0f;
//                rjd.enableMotor = true;
//                rjd.lowerAngle = -MathUtils.PI / 2f * 1.5f;
//                rjd.upperAngle = MathUtils.PI / 2f * 1.5f;
//                rjd.enableLimit = true;
//                shoulderJoint = (RevoluteJoint) getWorld().createJoint(rjd);
//
//                prevBody = upperArm;
//            }
//
//
//            // Define lower arm.
//            {
//                PolygonShape shape = new PolygonShape();
//                shape.setAsBox(0.5f, 2.0f);
//
//                BodyDef bd = new BodyDef();
//                bd.type = BodyType.DYNAMIC;
//                bd.position.set(0.0f, 11.0f);
//                lowerArm = getWorld().createBody(bd);
//                lowerArm.createFixture(shape, 1.0f);
//
//                RevoluteJointDef rjd = new RevoluteJointDef();
//                rjd.initialize(prevBody, lowerArm, new Vec2(0.0f, 9.0f));
//                rjd.enableMotor = true;
//                rjd.lowerAngle = -MathUtils.PI / 2f * 1.5f;
//                rjd.upperAngle = MathUtils.PI / 2f * 1.5f;
//                rjd.enableLimit = true;
//                elbowJoint = (RevoluteJoint) getWorld().createJoint(rjd);
//
//            }
//
//            //Finger Right
//            {
//                PolygonShape shape = new PolygonShape();
//                shape.setAsBox(0.1f, 0.75f);
//
//                BodyDef bd = new BodyDef();
//                bd.type = BodyType.DYNAMIC;
//                bd.position.set(0.5f, 13.5f);
//                Body body = getWorld().createBody(bd);
//                body.createFixture(shape, 0.25f);
//
//                RevoluteJointDef rjd = new RevoluteJointDef();
//                rjd.initialize(lowerArm, body, new Vec2(0.5f, 13.0f));
//                rjd.enableMotor = true;
//                rjd.upperAngle = MathUtils.PI / 8f * 1.5f;
//                rjd.lowerAngle = -MathUtils.PI / 4f * 1.5f;
//                rjd.enableLimit = true;
//                fingerRightJoint = (RevoluteJoint) getWorld().createJoint(rjd);
//
//                prevBody = body;
//            }
//            //Finger Left
//            {
//                PolygonShape shape = new PolygonShape();
//                shape.setAsBox(0.1f, 0.75f);
//
//                BodyDef bd = new BodyDef();
//                bd.type = BodyType.DYNAMIC;
//                bd.position.set(-0.5f, 13.5f);
//                Body body = getWorld().createBody(bd);
//                body.createFixture(shape, 0.25f);
//
//                RevoluteJointDef rjd = new RevoluteJointDef();
//                rjd.initialize(lowerArm, body, new Vec2(-0.5f, 13.0f));
//                rjd.enableMotor = true;
//                rjd.upperAngle = MathUtils.PI / 4f * 1.5f;
//                rjd.lowerAngle = -MathUtils.PI / 8f * 1.5f;
//                rjd.enableLimit = true;
//                fingerLeftJoint = (RevoluteJoint) getWorld().createJoint(rjd);
//
//            }
//
//
////      // Define piston
////      {
////        PolygonShape shape = new PolygonShape();
////        shape.setAsBox(1.5f, 1.5f);
////
////        BodyDef bd = new BodyDef();
////        bd.type = BodyType.DYNAMIC;
////        bd.fixedRotation = true;
////        bd.position.set(0.0f, 17.0f);
////        Body body = getWorld().createBody(bd);
////        body.createFixture(shape, 2.0f);
////
////        RevoluteJointDef rjd = new RevoluteJointDef();
////        rjd.initialize(prevBody, body, new Vec2(0.0f, 17.0f));
////        getWorld().createJoint(rjd);
////
////        PrismaticJointDef pjd = new PrismaticJointDef();
////        pjd.initialize(ground, body, new Vec2(0.0f, 17.0f), new Vec2(0.0f, 1.0f));
////
////        pjd.maxMotorForce = 1000.0f;
////        pjd.enableMotor = false;
////
////        m_joint2 = (PrismaticJoint) getWorld().createJoint(pjd);
////      }
////      // Create a payload
////      {
////        PolygonShape shape = new PolygonShape();
////        shape.setAsBox(1.5f, 1.5f);
////
////        BodyDef bd = new BodyDef();
////        bd.type = BodyType.DYNAMIC;
////        bd.position.set(0.0f, 23.0f);
////        Body body = getWorld().createBody(bd);
////        body.createFixture(shape, 2.0f);
////      }
//        }
//    }
//
//    RayCastClosestCallback ccallback = new RayCastClosestCallback();
//    Vec2 pooledHead = new Vec2();
//    Vec2 point1 = new Vec2();
//    Vec2 point2 = new Vec2();
//    Vec2 d = new Vec2();
//    Color3f laserColor = new Color3f(0.85f, 0, 0);
//
//    @Override
//    public void step(float timeStep, TestbedSettings settings, TestbedPanel panel) {
//        super.step(timeStep, settings, panel);
//
//
//        int pixels = 5;
//        float angle = 0.6f;
//
//        float focusAngle = -angle/2f;
//        float aStep = focusAngle/pixels;
//        float L = 11.0f;
//        float a = (float) (lowerArm.getAngle() + Math.PI/2f - focusAngle/2f);
//        boolean[] hit = new boolean[pixels];
//
//        for (int i = 0; i < pixels; i++) {
//            point1 = lowerArm.getWorldPoint(new Vec2(0,2));
//
//            d.set(L * MathUtils.cos(a), L * MathUtils.sin(a));
//            point2.set(point1);
//            point2.addLocal(d);
//
//
//            ccallback.init();
//            getWorld().raycast(ccallback, point1, point2);
//
//            if (ccallback.m_hit) {
//              draw().drawPoint(ccallback.m_point, 5.0f, new Color3f(0.4f, 0.9f, 0.4f));
//              draw().drawSegment(point1, ccallback.m_point, new Color3f(0.8f, 0.8f, 0.8f));
//              pooledHead.set(ccallback.m_normal);
//              pooledHead.mulLocal(.5f).addLocal(ccallback.m_point);
//              draw().drawSegment(ccallback.m_point, pooledHead, new Color3f(0.9f, 0.9f, 0.4f));
//              hit[i]= true;
//            } else {
//              draw().drawSegment(point1, point2, laserColor);
//            }
//            a+= aStep;
//        }
//        sight(hit);
//    }
//
//    public void sight(boolean[] hit) {
//
//    }
//
//    @Override
//    public void keyPressed(char argKeyChar, int argKeyCode) {
//
////        switch (argKeyChar) {
////            case 'f':
////                m_joint2.enableMotor(!m_joint2.isMotorEnabled());
////                getModel().getKeys()['f'] = false;
////                break;
////            case 'm':
////                shoulderJoint.enableMotor(!shoulderJoint.isMotorEnabled());
////                getModel().getKeys()['m'] = false;
////                break;
////        }
//    }
//
//    public void set(float shoulderAngle, float elbowAngle, float fingerAngle) {
//        set(shoulderAngle, elbowAngle, fingerAngle, -fingerAngle);
//    }
//    public void set(float shoulderAngle, float elbowAngle, float fingerLeftAngle, float fingerRightAngle) {
//        float angleTolerance = 0.05f;
//        shoulderJoint.setLimits(shoulderAngle-angleTolerance, shoulderAngle+angleTolerance);
//        elbowJoint.setLimits(elbowAngle-angleTolerance, elbowAngle+angleTolerance);
//        fingerLeftJoint.setLimits(fingerLeftAngle-angleTolerance, fingerLeftAngle+angleTolerance);
//        fingerRightJoint.setLimits(fingerRightAngle-angleTolerance, fingerRightAngle+angleTolerance);
//
//    }
//
//    @Override
//    public String getTestName() {
//        return "Robot Arm";
//    }
//
//    public static void main(String[] args) {
//        RobotArm r = new RobotArm();
//        //new PhysicsRun(null,null,r).start(30);
//
//    }
//

}
