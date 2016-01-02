package nars.nario.sprites;

import nars.nario.Art;
import nars.nario.LevelScene;


public class BulletBill extends Sprite
{
    private final int width = 4;
    int height = 24;

    private final LevelScene world;
    public int facing;

    public boolean avoidCliffs = false;
    public int anim;

    public boolean dead = false;
    private int deadTime = 0;


    public BulletBill(LevelScene world, float x, float y, int dir)
    {
        sheet = Art.enemies;

        this.x = x;
        this.y = y;
        this.world = world;
        xPicO = 8;
        yPicO = 31;

        height = 12;
        facing = 0;
        wPic = 16;
        yPic = 5;

        xPic = 0;
        ya = -5;
        facing = dir;
    }

    @Override
    public void collideCheck()
    {
        if (dead) return;

        float xMarioD = world.mario.x - x;
        float yMarioD = world.mario.y - y;
        float w = 16;
        if (xMarioD > -16 && xMarioD < 16)
        {
            if (yMarioD > -height && yMarioD < world.mario.height)
            {
                if (world.mario.ya > 0 && yMarioD <= 0 && (!world.mario.onGround || !world.mario.wasOnGround))
                {
                    world.mario.stomp(this);
                    dead = true;

                    xa = 0;
                    ya = 1;
                    deadTime = 100;
                }
                else
                {
                    world.mario.getHurt();
                }
            }
        }
    }

    @Override
    public void move()
    {
        if (deadTime > 0)
        {
            deadTime--;

            if (deadTime == 0)
            {
                deadTime = 1;
                for (int i = 0; i < 8; i++)
                {
                    world.addSprite(new Sparkle((int) (x + Math.random() * 16 - 8) + 4, (int) (y - Math.random() * 8) + 4, (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
                }
                spriteContext.removeSprite(this);
            }

            x += xa;
            y += ya;
            ya *= 0.95;
            ya += 1;

            return;
        }

        float sideWaysSpeed = 4.0f;

        xa = facing * sideWaysSpeed;
        xFlipPic = facing == -1;
        move(xa, 0);
    }

    private boolean move(float xa, float ya)
    {
        x += xa;
        return true;
    }
    
    @Override
    public boolean fireballCollideCheck(Fireball fireball)
    {
        if (deadTime != 0) return false;

        float xD = fireball.x - x;
        float yD = fireball.y - y;

        if (xD > -16 && xD < 16)
        {
            if (yD > -height && yD < fireball.height)
            {
                return true;
            }
        }
        return false;
    }      

    @Override
    public boolean shellCollideCheck(Shell shell)
    {
        if (deadTime != 0) return false;

        float xD = shell.x - x;
        float yD = shell.y - y;

        if (xD > -16 && xD < 16)
        {
            if (yD > -height && yD < shell.height)
            {
                //world.sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1);

                dead = true;

                xa = 0;
                ya = 1;
                deadTime = 100;

                return true;
            }
        }
        return false;
    }      
}