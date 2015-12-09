package nars.nario.sprites;

import nars.nario.Art;
import nars.nario.LevelScene;

import java.awt.*;


public class Enemy extends Sprite
{
    public static final int ENEMY_RED_KOOPA = 0;
    public static final int ENEMY_GREEN_KOOPA = 1;
    public static final int ENEMY_GOOMBA = 2;
    public static final int ENEMY_SPIKY = 3;
    public static final int ENEMY_FLOWER = 4;

    private static float GROUND_INERTIA = 0.89f;
    private static float AIR_INERTIA = 0.89f;

    private float runTime;
    private boolean onGround = false;
    private boolean mayJump = false;
    private int jumpTime = 0;
    private float xJumpSpeed;
    private float yJumpSpeed;

    int width = 4;
    int height = 24;

    private LevelScene world;
    public int facing;
    public int deadTime = 0;
    public boolean flyDeath = false;

    public boolean avoidCliffs = true;
    private int type;

    public boolean winged = true;
    private int wingTime = 0;
    
    public boolean noFireballDeath;

    public Enemy(LevelScene world, int x, int y, int dir, int type, boolean winged)
    {
        this.type = type;
        sheet = Art.enemies;
        this.winged = winged;

        this.x = x;
        this.y = y;
        this.world = world;
        xPicO = 8;
        yPicO = 31;

        avoidCliffs = type == Enemy.ENEMY_RED_KOOPA;
        
        noFireballDeath = type == Enemy.ENEMY_SPIKY;

        yPic = type;
        if (yPic > 1) height = 12;
        facing = dir;
        if (facing == 0) facing = 1;
        this.wPic = 16;
    }

    public void collideCheck()
    {
        if (deadTime != 0)
        {
            return;
        }

        float xMarioD = world.mario.x - x;
        float yMarioD = world.mario.y - y;
        float w = 16;
        if (xMarioD > -width*2-4 && xMarioD < width*2+4)
        {
            if (yMarioD > -height && yMarioD < world.mario.height)
            {
                if (type != Enemy.ENEMY_SPIKY && world.mario.ya > 0 && yMarioD <= 0 && (!world.mario.onGround || !world.mario.wasOnGround))
                {
                    world.mario.stomp(this);
                    if (winged)
                    {
                        winged = false;
                        ya = 0;
                    }
                    else
                    {
                        this.yPicO = 31 - (32 - 8);
                        hPic = 8;
                        if (spriteTemplate != null) spriteTemplate.isDead = true;
                        deadTime = 10;
                        winged = false;

                        if (type == Enemy.ENEMY_RED_KOOPA)
                        {
                            spriteContext.addSprite(new Shell(world, x, y, 0));
                        }
                        else if (type == Enemy.ENEMY_GREEN_KOOPA)
                        {
                            spriteContext.addSprite(new Shell(world, x, y, 1));
                        }
                    }
                }
                else
                {
                    world.mario.getHurt();
                }
            }
        }
    }

    public void move()
    {
        wingTime++;
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

            if (flyDeath)
            {
                x += xa;
                y += ya;
                ya *= 0.95;
                ya += 1;
            }
            return;
        }


        float sideWaysSpeed = 1.75f;
        //        float sideWaysSpeed = onGround ? 2.5f : 1.2f;

        if (xa > 2)
        {
            facing = 1;
        }
        if (xa < -2)
        {
            facing = -1;
        }

        xa = facing * sideWaysSpeed;

        mayJump = (onGround);

        xFlipPic = facing == -1;

        runTime += (Math.abs(xa)) + 5;

        int runFrame = ((int) (runTime / 20)) % 2;

        if (!onGround)
        {
            runFrame = 1;
        }


        if (!move(xa, 0)) facing = -facing;
        onGround = false;
        move(0, ya);

        ya *= winged ? 0.95f : 0.85f;
        xa *= onGround ? GROUND_INERTIA : AIR_INERTIA;

        if (!onGround)
        {
            ya += winged ? 0.6f : 2;
        }
        else if (winged)
        {
            ya = -10;
        }

        if (winged) runFrame = wingTime / 4 % 2;

        xPic = runFrame;
    }

    private boolean move(float xa, float ya)
    {
        while (xa > 8)
        {
            if (!move(8, 0)) return false;
            xa -= 8;
        }
        while (xa < -8)
        {
            if (!move(-8, 0)) return false;
            xa += 8;
        }
        while (ya > 8)
        {
            if (!move(0, 8)) return false;
            ya -= 8;
        }
        while (ya < -8)
        {
            if (!move(0, -8)) return false;
            ya += 8;
        }

        boolean collide = false;
        if (ya > 0)
        {
            if (isBlocking(x + xa - width, y + ya, xa, 0) || isBlocking(x + xa + width, y + ya, xa, 0) || isBlocking(x + xa - width, y + ya + 1, xa, ya) || isBlocking(x + xa + width, y + ya + 1, xa, ya)) collide = true;
        }
        if (ya < 0)
            if (isBlocking(x + xa, y + ya - height, xa, ya) || collide || isBlocking(x + xa - width, y + ya - height, xa, ya) || isBlocking(x + xa + width, y + ya - height, xa, ya))
                collide = true;
        if (xa > 0)
        {
            if (isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
            if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya)) collide = true;
            if (isBlocking(x + xa + width, y + ya, xa, ya)) collide = true;

            if (avoidCliffs && onGround && !world.level.isBlocking((int) ((x + xa + width) / 16), (int) ((y) / 16 + 1), xa, 1)) collide = true;
        }
        if (xa < 0)
        {
            if (isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
            if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya)) collide = true;
            if (isBlocking(x + xa - width, y + ya, xa, ya)) collide = true;

            if (avoidCliffs && onGround && !world.level.isBlocking((int) ((x + xa - width) / 16), (int) ((y) / 16 + 1), xa, 1)) collide = true;
        }

        if (collide)
        {
            if (xa < 0)
            {
                x = (int) ((x - width) / 16) * 16 + width;
                this.xa = 0;
            }
            if (xa > 0)
            {
                x = (int) ((x + width) / 16 + 1) * 16 - width - 1;
                this.xa = 0;
            }
            if (ya < 0)
            {
                y = (int) ((y - height) / 16) * 16 + height;
                jumpTime = 0;
                this.ya = 0;
            }
            if (ya > 0)
            {
                y = (int) (y / 16 + 1) * 16 - 1;
                onGround = true;
            }
            return false;
        }
        else
        {
            x += xa;
            y += ya;
            return true;
        }
    }

    private boolean isBlocking(float _x, float _y, float xa, float ya)
    {
        int x = (int) (_x / 16);
        int y = (int) (_y / 16);
        if (x == (int) (this.x / 16) && y == (int) (this.y / 16)) return false;

        boolean blocking = world.level.isBlocking(x, y, xa, ya);

        byte block = world.level.getBlock(x, y);

        return blocking;
    }

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

                xa = shell.facing * 2;
                ya = -5;
                flyDeath = true;
                if (spriteTemplate != null) spriteTemplate.isDead = true;
                deadTime = 100;
                winged = false;
                hPic = -hPic;
                yPicO = -yPicO + 16;
                return true;
            }
        }
        return false;
    }

    public boolean fireballCollideCheck(Fireball fireball)
    {
        if (deadTime != 0) return false;

        float xD = fireball.x - x;
        float yD = fireball.y - y;

        if (xD > -16 && xD < 16)
        {
            if (yD > -height && yD < fireball.height)
            {
                if (noFireballDeath) return true;
                
                //world.sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1);

                xa = fireball.facing * 2;
                ya = -5;
                flyDeath = true;
                if (spriteTemplate != null) spriteTemplate.isDead = true;
                deadTime = 100;
                winged = false;
                hPic = -hPic;
                yPicO = -yPicO + 16;
                return true;
            }
        }
        return false;
    }

    public void bumpCheck(int xTile, int yTile)
    {
        if (deadTime != 0) return;

        if (x + width > xTile * 16 && x - width < xTile * 16 + 16 && yTile == (int) ((y - 1) / 16))
        {
            //world.sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1);

            xa = -world.mario.facing * 2;
            ya = -5;
            flyDeath = true;
            if (spriteTemplate != null) spriteTemplate.isDead = true;
            deadTime = 100;
            winged = false;
            hPic = -hPic;
            yPicO = -yPicO + 16;
        }
    }

    public void render(Graphics og, float alpha)
    {
        if (winged)
        {
            int xPixel = (int) (xOld + (x - xOld) * alpha) - xPicO;
            int yPixel = (int) (yOld + (y - yOld) * alpha) - yPicO;

            if (type == Enemy.ENEMY_GREEN_KOOPA || type == Enemy.ENEMY_RED_KOOPA)
            {
            }
            else
            {
                xFlipPic = !xFlipPic;
                og.drawImage(sheet[wingTime / 4 % 2][4], xPixel + (xFlipPic ? wPic : 0) + (xFlipPic ? 10 : -10), yPixel + (yFlipPic ? hPic : 0) - 8, xFlipPic ? -wPic : wPic, yFlipPic ? -hPic : hPic, null);
                xFlipPic = !xFlipPic;
            }
        }

        super.render(og, alpha);

        if (winged)
        {
            int xPixel = (int) (xOld + (x - xOld) * alpha) - xPicO;
            int yPixel = (int) (yOld + (y - yOld) * alpha) - yPicO;

            if (type == Enemy.ENEMY_GREEN_KOOPA || type == Enemy.ENEMY_RED_KOOPA)
            {
                og.drawImage(sheet[wingTime / 4 % 2][4], xPixel + (xFlipPic ? wPic : 0) + (xFlipPic ? 10 : -10), yPixel + (yFlipPic ? hPic : 0) - 10, xFlipPic ? -wPic : wPic, yFlipPic ? -hPic : hPic, null);
            }
            else
            {
                og.drawImage(sheet[wingTime / 4 % 2][4], xPixel + (xFlipPic ? wPic : 0) + (xFlipPic ? 10 : -10), yPixel + (yFlipPic ? hPic : 0) - 8, xFlipPic ? -wPic : wPic, yFlipPic ? -hPic : hPic, null);
            }
        }
    }
    
    public String toString() {
        switch (type) {
            case 0:
                return "ENEMY_RED_KOOPA";
            case 1:
                return "ENEMY_GREEN_KOOPA";
            case 2:
                return "ENEMY_GOOMBA";
            case 3: 
                return "ENEMY_SPIKY";
            case 4:
                return "ENEMY_FLOWER";
        }
        return super.toString();    
    }
}