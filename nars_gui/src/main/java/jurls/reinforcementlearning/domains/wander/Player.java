package jurls.reinforcementlearning.domains.wander;

import static jurls.reinforcementlearning.domains.wander.Obstacle.d;
import jurls.reinforcementlearning.domains.wander.brain.MyPerception;
import jurls.reinforcementlearning.domains.wander.brain.actions.MoveForward;
import jurls.reinforcementlearning.domains.wander.brain.actions.Nop;
import jurls.reinforcementlearning.domains.wander.brain.actions.TurnLeft;
import jurls.reinforcementlearning.domains.wander.brain.actions.TurnRight;
import jurls.reinforcementlearning.domains.wander.brain.Action;
import jurls.reinforcementlearning.domains.wander.brain.actions.MoveBackward;

public class Player {

    public static final double TURNING_ANGLE = MyPerception.RADAR_R/4f;
    public static final double STEP_SIZE = MyPerception.RADAR_D/2d;
    private static final int MOVE_FORWARD = 0;
    private static final int MOVE_BACKWARD = 1;
    private static final int TURN_LEFT = 2;
    private static final int TURN_RIGHT = 3;
    private static final int NOP = 4;
    public final MyPerception perception;

    public double x;
    public double y;
    public double r;
    public double angle;
    double vx, vy;
    private World world;
    public final Action[] actions;
    private int currentAction;
    private double yOld;
    private double xOld;

    public Player(World world) {
        this.world = world;
        r = 8;
        actions = new Action[5];
        actions[MOVE_FORWARD] = new MoveForward(this);
        actions[MOVE_BACKWARD] = new MoveBackward(this);
        actions[TURN_LEFT] = new TurnLeft(this);
        actions[TURN_RIGHT] = new TurnRight(this);
        actions[NOP] = new Nop();
        perception = new MyPerception(this);
//        perception.setAddRandomInput(true);
//        brain = new CuriousBrain(perception, actionArray, new int[]{10}, new int[]{200, 100, 100});
//        brain.setAlpha(0.9);
//        brain.setGamma(0.9);
//        brain.setLambda(0.5);
//        brain.setRandActions(5);
//        ErrorBackpropagationNN predictionNN = brain.getCuriosity().getNn();
//        predictionNN.setAlpha(0.5);
//        predictionNN.setMomentum(0.2);
//        //CuriousPlayerPerception.setRMin(0.005);
    }

//    public void count() {
//        brain.count();
//    }
    
    public void act(int action) {
        currentAction = action;
        actions[action].execute();        
    }

    public void turn(double delta) {
        angle += delta;
    }

    public void moveForward(double step) {
        vx += Math.cos(angle) * step * 0.1;
        vy += Math.sin(angle) * step * 0.1;
        if (world.isCollision()) {
            x = xOld;
            y = yOld;
            vx = 0;
            vy = 0;
        }
        else {
            xOld = x;
            yOld = y;           
        }
    }
    
    public void update() {
        x += vx;
        y += vy;
        vx *= 0.9;
        vy *= 0.9;
    }

    public void randomizePosition() {
        while (world.isCollision()) {
            x = xOld = d(-World.SIZE, World.SIZE);
            y = yOld = d(-World.SIZE, World.SIZE);
        }
    }

    public World getWorld() {
        return world;
    }

    public double speed() {
        return Math.sqrt((vx*vx) + (vy*vy));
        //return (currentAction == MOVE_FORWARD) && !world.isCollision();
    }

    public boolean collides() {
        return (currentAction == MOVE_FORWARD) && world.isCollision();
    }
//
//    public MyPerception getPerception() {
//        return perception;
//    }
//
//    public Brain getBrain() {
//        return brain;
//    }
//
//    public double getNovelty() {
//        return perception.getNovelty();
//    }
//
//    public double getReward() {
//        return perception.getReward();
//    }
}
