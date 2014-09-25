package nars.narclear;

import nars.narclear.jbox2d.PhysicsCamera;
import nars.narclear.jbox2d.TestbedSettings;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.joints.PrismaticJoint;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

/**
 *
 * @author me
 */
public class RobotArm extends PhysicsModel {

    private RevoluteJoint shoulderJoint;
    private PrismaticJoint m_joint2;
    private RevoluteJoint fingerLeftJoint;
    private RevoluteJoint fingerRightJoint;
    private RevoluteJoint elbowJoint;

    @Override
    public void initTest(boolean argDeserialized) {

        for (int i = 0; i < 10; i++) {
            getCamera().zoomToPoint(new Vec2(0, 0), PhysicsCamera.ZoomType.ZOOM_IN);
        }

        Body ground = null;
        {
            BodyDef bd = new BodyDef();
            ground = getWorld().createBody(bd);

            EdgeShape shape = new EdgeShape();
            shape.set(new Vec2(-40.0f, 0.0f), new Vec2(40.0f, 0.0f));
            ground.createFixture(shape, 0.0f);
        }

        {
            Body prevBody = ground;

            // Define upper arm.
            {
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(0.5f, 4.0f);

                BodyDef bd = new BodyDef();
                bd.type = BodyType.DYNAMIC;
                bd.position.set(0.0f, 5.0f);
                Body body = getWorld().createBody(bd);
                body.createFixture(shape, 2.0f);

                RevoluteJointDef rjd = new RevoluteJointDef();
                rjd.initialize(prevBody, body, new Vec2(0.0f, 2.0f));
                rjd.motorSpeed = 0; //1.0f * MathUtils.PI;
                rjd.maxMotorTorque = 10000.0f;
                rjd.enableMotor = true;
                rjd.lowerAngle = -MathUtils.PI / 2f * 1.5f;
                rjd.upperAngle = MathUtils.PI / 2f * 1.5f;
                rjd.enableLimit = true;
                shoulderJoint = (RevoluteJoint) getWorld().createJoint(rjd);

                prevBody = body;
            }

            Body lowerArm;
            // Define lower arm.            
            {
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(0.5f, 2.0f);

                BodyDef bd = new BodyDef();
                bd.type = BodyType.DYNAMIC;
                bd.position.set(0.0f, 11.0f);
                lowerArm = getWorld().createBody(bd);
                lowerArm.createFixture(shape, 1.0f);

                RevoluteJointDef rjd = new RevoluteJointDef();
                rjd.initialize(prevBody, lowerArm, new Vec2(0.0f, 9.0f));
                rjd.enableMotor = true;
                rjd.lowerAngle = -MathUtils.PI / 2f * 1.5f;
                rjd.upperAngle = MathUtils.PI / 2f * 1.5f;
                rjd.enableLimit = true;
                elbowJoint = (RevoluteJoint) getWorld().createJoint(rjd);

                prevBody = lowerArm;
            }
            
            //Finger Right
            {
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(0.1f, 0.75f);

                BodyDef bd = new BodyDef();
                bd.type = BodyType.DYNAMIC;
                bd.position.set(0.5f, 13.5f);
                Body body = getWorld().createBody(bd);
                body.createFixture(shape, 0.25f);

                RevoluteJointDef rjd = new RevoluteJointDef();
                rjd.initialize(lowerArm, body, new Vec2(0.5f, 13.0f));
                rjd.enableMotor = true;
                rjd.upperAngle = MathUtils.PI / 8f * 1.5f;
                rjd.lowerAngle = -MathUtils.PI / 4f * 1.5f;
                rjd.enableLimit = true;
                fingerRightJoint = (RevoluteJoint) getWorld().createJoint(rjd);

                prevBody = body;
            }
            //Finger Left
            {
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(0.1f, 0.75f);

                BodyDef bd = new BodyDef();
                bd.type = BodyType.DYNAMIC;
                bd.position.set(-0.5f, 13.5f);
                Body body = getWorld().createBody(bd);
                body.createFixture(shape, 0.25f);

                RevoluteJointDef rjd = new RevoluteJointDef();
                rjd.initialize(lowerArm, body, new Vec2(-0.5f, 13.0f));
                rjd.enableMotor = true;
                rjd.upperAngle = MathUtils.PI / 4f * 1.5f;
                rjd.lowerAngle = -MathUtils.PI / 8f * 1.5f;
                rjd.enableLimit = true;
                fingerLeftJoint = (RevoluteJoint) getWorld().createJoint(rjd);

            }
            

//      // Define piston
//      {
//        PolygonShape shape = new PolygonShape();
//        shape.setAsBox(1.5f, 1.5f);
//
//        BodyDef bd = new BodyDef();
//        bd.type = BodyType.DYNAMIC;
//        bd.fixedRotation = true;
//        bd.position.set(0.0f, 17.0f);
//        Body body = getWorld().createBody(bd);
//        body.createFixture(shape, 2.0f);
//
//        RevoluteJointDef rjd = new RevoluteJointDef();
//        rjd.initialize(prevBody, body, new Vec2(0.0f, 17.0f));
//        getWorld().createJoint(rjd);
//
//        PrismaticJointDef pjd = new PrismaticJointDef();
//        pjd.initialize(ground, body, new Vec2(0.0f, 17.0f), new Vec2(0.0f, 1.0f));
//
//        pjd.maxMotorForce = 1000.0f;
//        pjd.enableMotor = false;
//
//        m_joint2 = (PrismaticJoint) getWorld().createJoint(pjd);
//      }
//      // Create a payload
//      {
//        PolygonShape shape = new PolygonShape();
//        shape.setAsBox(1.5f, 1.5f);
//
//        BodyDef bd = new BodyDef();
//        bd.type = BodyType.DYNAMIC;
//        bd.position.set(0.0f, 23.0f);
//        Body body = getWorld().createBody(bd);
//        body.createFixture(shape, 2.0f);
//      }
        }
    }

    @Override
    public void step(TestbedSettings settings) {
        super.step(settings);

    //addTextLine("Keys: (f) toggle friction, (m) toggle motor");
        //float torque = m_joint1.getMotorTorque(1);
        //Formatter f = new Formatter();
        //addTextLine(f.format("Friction: %b, Motor Force = %5.0f, ", m_joint2.isMotorEnabled(), torque).toString());
        //f.close();
    }

    @Override
    public void keyPressed(char argKeyChar, int argKeyCode) {

        switch (argKeyChar) {
            case 'f':
                m_joint2.enableMotor(!m_joint2.isMotorEnabled());
                getModel().getKeys()['f'] = false;
                break;
            case 'm':
                shoulderJoint.enableMotor(!shoulderJoint.isMotorEnabled());
                getModel().getKeys()['m'] = false;
                break;
        }
    }
    
    public void set(float shoulderAngle, float elbowAngle, float fingerAngle) {
        set(shoulderAngle, elbowAngle, fingerAngle, -fingerAngle);
    }
    public void set(float shoulderAngle, float elbowAngle, float fingerLeftAngle, float fingerRightAngle) {
        float angleTolerance = 0.05f;
        shoulderJoint.setLimits(shoulderAngle-angleTolerance, shoulderAngle+angleTolerance);
        elbowJoint.setLimits(elbowAngle-angleTolerance, elbowAngle+angleTolerance);
        fingerLeftJoint.setLimits(fingerLeftAngle-angleTolerance, fingerLeftAngle+angleTolerance);
        fingerRightJoint.setLimits(fingerRightAngle-angleTolerance, fingerRightAngle+angleTolerance);
        
    }

    @Override
    public String getTestName() {
        return "Robot Arm";
    }

    public static void main(String[] args) {
        RobotArm r = new RobotArm();
        new PhysicsRun(r).start(30);
        
    }
}
