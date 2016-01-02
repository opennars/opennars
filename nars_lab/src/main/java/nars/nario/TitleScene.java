package nars.nario;

import nars.nario.level.BgLevelGenerator;
import nars.nario.level.LevelGenerator;
import nars.nario.sprites.Mario;

import java.awt.*;

public class TitleScene extends Scene
{
    private final MarioComponent component;
    private int tick;
    private final BgRenderer bgLayer0;
    private final BgRenderer bgLayer1;
    
    public TitleScene(MarioComponent component, GraphicsConfiguration gc)
    {
        this.component = component;
        bgLayer0 = new BgRenderer(BgLevelGenerator.createLevel(2048, 15, false, LevelGenerator.TYPE_OVERGROUND), gc, 320, 240, 1);        
        bgLayer1 = new BgRenderer(BgLevelGenerator.createLevel(2048, 15, true, LevelGenerator.TYPE_OVERGROUND), gc, 320, 240, 2);
    }

    @Override
    public void init()
    {
        Art.startMusic(4);
    }

    @Override
    public void render(Graphics g, float alpha)
    {
        bgLayer0.setCam(tick+160, 0);
        bgLayer1.setCam(tick+160, 0);
        bgLayer1.render(g, tick, alpha);
        bgLayer0.render(g, tick, alpha);
//        g.setColor(Color.decode("#8080a0"));
//        g.fillRect(0, 0, 320, 240);
        int yo = -18+Math.abs((int)(Math.sin((tick+alpha)/6.0)*8));
        g.drawImage(Art.logo, 0, yo, null);
        g.drawImage(Art.titleScreen, 0, 140, null);
    }

    private void drawString(Graphics g, String text, int x, int y, int c)
    {
        char[] ch = text.toCharArray();
        for (int i = 0; i < ch.length; i++)
        {
            g.drawImage(Art.font[ch[i] - 32][c], x + i * 8, y, null);
        }
    }


    private boolean wasDown = true;
    @Override
    public void tick()
    {
        tick++;
        if (!wasDown && keys[Mario.KEY_JUMP])
        {
            component.startGame();
        }
        if (keys[Mario.KEY_JUMP])
        {
            wasDown = false;
        }
    }

    @Override
    public float getX(float alpha)
    {
        return 0;
    }

    @Override
    public float getY(float alpha)
    {
        return 0;
    }

}
