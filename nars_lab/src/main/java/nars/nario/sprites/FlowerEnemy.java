package nars.nario.sprites;

import nars.nario.LevelScene;

public class FlowerEnemy extends Enemy
{
    private int tick;
    private int yStart;
    private int jumpTime = 0;
    private LevelScene world;
    
    public FlowerEnemy(LevelScene world, int x, int y)
    {
        super(world, x, y, 1, ENEMY_SPIKY, false);
        
        noFireballDeath = false;
        this.world = world;
        this.xPic = 0;
        this.yPic = 6;
        this.yPicO = 24;
        this.height = 12;
        this.width = 2;
        
        yStart = y;
        ya = -8;
        
        this.y-=1;
        
        this.layer = 0;
        
        for (int i=0; i<4; i++)
        {
            move();
        }
    }

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

        tick++;
        
        if (y>=yStart)
        {
            y = yStart;

            int xd = (int)(Math.abs(world.mario.x-x));
            jumpTime++;
            ya = jumpTime > 40 && xd > 24 ? -8 : 0;
        }
        else
        {
            jumpTime = 0;
        }
        
        y+=ya;
        ya*=0.9;
        ya+=0.1f;
        
        xPic = ((tick/2)&1)*2+((tick/6)&1);
    }

/*    public void render(Graphics og, float alpha)
    {
        if (!visible) return;
        
        int xPixel = (int)(xOld+(x-xOld)*alpha)-xPicO;
        int yPixel = (int)(yOld+(y-yOld)*alpha)-yPicO;

        int a = ((tick/3)&1)*2;
//        a += ((tick/8)&1);
        og.drawImage(sheet[a*2+0][6], xPixel-8, yPixel+8, 16, 32, null);
        og.drawImage(sheet[a*2+1][6], xPixel+8, yPixel+8, 16, 32, null);
    }*/
}