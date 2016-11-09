package nars.lab.grid2d.particle;

public class Particle {

    public float xPos, yPos, xVel, yVel;
    public int size;
    public float pxVel, pyVel;
    public int age = 0;
    public int[] gradient;
    public int rgba;

	//seems to be {screen_foreground, particle trail, particle trail, ... , particle center }
    public final static int[] FIRE_GRAD = {0x00000000, 0xaf9f1604, 0xffdf3509, 0xffef6a10, 0xfffc9b11, 0xffffaa22, 0xffffbb33, 0xffffdd66, 0xffffffaa, 0xffffffff};
    public final static int[] SMOKE_GRAD = {0x00000000, 0x8f666666, 0x8f888888, 0x8f888888, 0x8f666666};
    public final static int[] WATER_GRAD = {0x00000000, 0xffffffff, 0xffffffff, 0xffffffff, 0x8f1144dd};
    public final static int[] SAND_GRAD = {0x00000000, 0x8f666666, 0x8f888888, 0x8f888888, 0xfffc9b11};
    public final static int[] EXP_GRAD = {0x00000000, 0xff0000ff, 0xffdf3509, 0xffef6a10, 0xfffc9b11, 0xffffaa22, 0xffffbb33, 0xffffdd66, 0xffffffaa, 0xffffffff};

	//0xff0000ff
    /*I believe initializing the gradient within the particle class will more easily allow us to use
     * more than one color of particle
     */
    public Particle(float xPos, float yPos, float xVel, float yVel) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.xVel = xVel;
        this.yVel = yVel;
    }

    public Particle(int[] gradient, int size, float xPos, float yPos, float xVel, float yVel) {
        this.size = size;
        this.gradient = gradient;
        this.xPos = xPos;
        this.yPos = yPos;
        this.xVel = xVel;
        this.yVel = yVel;
    }

    public Particle() {
        this.xPos = 0;
        this.yPos = 0;
        this.xVel = 0;
        this.yVel = 0;
    }

    public void setParticle(float xPos, float yPos, float xVel, float yVel) {

        this.xPos = xPos;
        this.yPos = yPos;
        this.xVel = xVel;
        this.yVel = yVel;

    }








    public int[] getGradient() {
        return gradient;
    }








}
