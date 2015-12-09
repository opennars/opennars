package jurls.reinforcementlearning.domains.grid;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


public class Grid2DRelative implements World {

    private final int width, height;

    private final double REWARD_MAGNITUDE;
    private final double JUMP_FRACTION;
    private final double ENERGY_COST_FACTOR;
    private final double MATCH_REWARD_FACTOR;

    private double[] action;

    private final int totalTime;
    private int time;

    private final double noise;
    private double focusPositionX;
    private double focusPositionY;

    private int nextFocusPositionX;
    private int nextFocusPositionY;
    
    private double positionX;
    private double positionY;
    private Image2DPanel image;
    private final double POSITION_VELOCITY;
    private double[] sensor;
    int displayPeriod = 64;

    public class Image2DPanel extends JPanel {
        private final BufferedImage bi;
        private final Graphics2D g2;

        public Image2DPanel() {
            super(new BorderLayout());

            bi
                    = new BufferedImage(width, height,
                            BufferedImage.TYPE_INT_ARGB);

            g2 = bi.createGraphics();
        }

        public void updateImage() {
            g2.clearRect(0, 0, width, height);
            
            int px = 0, py = 0;
            for (double aSensor : sensor) {
                g2.setPaint(Color.getHSBColor(0.5f, 0.5f, (float) aSensor));
                g2.fillRect(px, py, 1, 1);
                px++;
                if (px == width) {
                    px = 0;
                    py++;
                }
            }
            //g2.setPaint(Color.RED);
            //g2.fillRect(focusPositionX-1, focusPositionY-1, 2, 2);

            g2.setPaint(new Color(0, 0, 1.0f, 0.85f));
            g2.fillRect((int)Math.round(positionX), (int)Math.round(positionY), 1, 1);
        }
        @Override
        public void paint(Graphics g) {
            g.drawImage(bi, 0, 0, getWidth(), getHeight(), null);
            /*
                        dx, dy, dx+cw, dy+ch,
                        sx, sy, sx+cw, sy+ch,
                        null);*/
            
            validate();
            repaint();
        }        

        

    }

    public Grid2DRelative(int width, int height, int totalTime, double noise, double focusVelocity) {
        time = 1;
        this.width = width;
        this.height = height;
        ENERGY_COST_FACTOR = 0.01;
        MATCH_REWARD_FACTOR = 1.0;
        REWARD_MAGNITUDE = 1;
        JUMP_FRACTION = 0.002;
        POSITION_VELOCITY = 0.1;
        this.noise = noise;

        if (Simulation.DISPLAY) {
            image = new Image2DPanel();
            //AgentPanel.window(image, true);
        }
        
        this.totalTime = totalTime;
        randomFocus();
    }

    protected void randomFocus() {
        nextFocusPositionX = (int) (Math.random() * width);
        nextFocusPositionY = (int) (Math.random() * height);
    }

    @Override
    public String getName() {
        return getClass().toString();
    }

    @Override
    public int getNumSensors() {
        return height * width;
    }

    @Override
    public int getNumActions() {
        return 4;
    }

    @Override
    public boolean isActive() {
        return time < totalTime;
    }

    double[] action2 = null;

    @Override
    public double step(double[] action, double[] sensor) {

        
        time++;

        this.sensor = sensor;
        this.action = action;
        
        double speed = 0.001;
        focusPositionX = speed * (nextFocusPositionX) + (1.0 - speed) * focusPositionX;
        focusPositionY = speed * (nextFocusPositionY) + (1.0 - speed) * focusPositionY;

        //# At random intervals, jump to a random position in the world
        if (Math.random() < JUMP_FRACTION) {
            randomFocus();
        }

        /*        
         # Assign basic_feature_input elements as binary. 
         # Represent the presence or absence of the current position in the bin.
         */
        //blur the action
        /*if (action2 == null) action2 = new double[action.length];
         for (int i = 0; i < action2.length; i++) {
         action2[i] = action[i];
         if (i > 0) action2[i] += 0.5 * action[i-1];
         if (i < action2.length-1) action2[i] += 0.5 * action[i+1];
         } */
        /*if (action[0] > 0.5) {
         //nothing
         }*/
        if ((action[0] > 0.5) && !(action[1] > 0.5)) {
            positionX-=POSITION_VELOCITY * action[0];
        }
        if ((action[1] > 0.5) && !(action[0] > 0.5)) {
            positionX+=POSITION_VELOCITY * action[1];
        }
        if (positionX < 0) {
            positionX = 0;
        }
        if (positionX >= width) {
            positionX = width - 1;
        }
        if ((action[2] > 0.5) && !(action[3] > 0.5)) {
            positionY-=POSITION_VELOCITY * action[2];
        }
        if ((action[3] > 0.5) && !(action[2] > 0.5)) {
            positionY+=POSITION_VELOCITY * action[3];
        }
        if (positionY < 0) {
            positionY = 0;
        }
        if (positionY >= height) {
            positionY = height - 1;
        }

        double match = 0;
        double energyCost = 0;

        double dx = (positionX - focusPositionX);
        double dy = (positionY - focusPositionY);
        double dist = Math.sqrt(dx * dx + dy * dy);
        match = 1.0 / (1.0 + dist);

        double reward = REWARD_MAGNITUDE * ((MATCH_REWARD_FACTOR * match) - (energyCost * ENERGY_COST_FACTOR)) - 0.5;

        int ix = (int)Math.round(positionX);
        int iy = (int)Math.round(positionY);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double exp = 1.0; //sharpen

                double fdx = x - focusPositionX;
                double fdy = y - focusPositionY;
                double fdist = Math.sqrt(fdx * fdx + fdy * fdy);

                int i = x + y * width;
                double v = Math.pow(1.0 / (1.0 + fdist/2.0), exp) + (Math.random() * noise);
                if (v < 0.0) {
                    v = 0;
                }

                
                /*if ((ix==x) && (iy==y))
                    v += 0.5;*/
                
                if (v > 1.0) v = 1.0;
                sensor[i] = v;

                
            }

        }

        if (Simulation.DISPLAY) {
            if (time % displayPeriod == 0)
                image.updateImage();
        }
        
        return reward;
    }

    public static void main(String[] args) throws Exception {
        //Class<? extends Agent> a = BeccaAgent.class;
        Class<? extends Agent> a = RandomAgent.class;
        //Class<? extends Agent> a = QLAgent.class;

        new Simulation(a, new Grid2DRelative(10, 10, 11990000, 0.02, 0.01));

    }
}
